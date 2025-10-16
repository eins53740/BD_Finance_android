package com.example.bd_finance.data

import android.util.Log
import com.example.bd_finance.BuildConfig
import com.example.bd_finance.data.analysis.EvaluationThresholds
import com.example.bd_finance.data.analysis.MermaidDefinitionBuilder
import com.example.bd_finance.data.analysis.StockEvaluator
import com.example.bd_finance.data.llm.LargeLanguageModelClient
import com.example.bd_finance.data.model.DecisionStatus
import com.example.bd_finance.data.model.PeerComparison
import com.example.bd_finance.data.model.StockAnalysis
import com.example.bd_finance.data.model.StockQuote
import com.example.bd_finance.data.model.StockVerdict
import com.example.bd_finance.data.network.YahooFinanceClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.JavaNetCookieJar
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.net.CookieManager
import java.net.CookiePolicy
import java.util.Locale

class StockAnalysisRepository(
    private val financeClient: YahooFinanceClient,
    private val evaluator: StockEvaluator,
    private val mermaidBuilder: MermaidDefinitionBuilder,
    private val llmClient: LargeLanguageModelClient?,
    private val cacheTtlMillis: Long = BuildConfig.CACHE_TTL_MS
) {

    private val cache = mutableMapOf<String, CachedAnalysis>()
    private val mutex = Mutex()

    suspend fun analyze(ticker: String): StockAnalysis {
        val normalized = ticker.trim().uppercase(Locale.US)
        if (normalized.isBlank()) throw IllegalArgumentException("Ticker required")

        val cached: StockAnalysis? = mutex.withLock {
            val entry = cache[normalized]
            if (entry != null && !entry.isExpired(cacheTtlMillis)) {
                return@withLock entry.analysis
            }
            null
        }
        if (cached != null) return cached

        val result = fetchAndEvaluate(normalized)

        mutex.withLock {
            cache[normalized] = CachedAnalysis(System.currentTimeMillis(), result)
        }
        return result
    }

    private suspend fun fetchAndEvaluate(ticker: String): StockAnalysis = coroutineScope {
        val quote = financeClient.fetchQuote(ticker)
        val historyDeferred = async { financeClient.fetchHistoricalPrices(ticker) }
        val peersDeferred = async { financeClient.fetchPeerSymbols(ticker) }

        val history = runCatching { historyDeferred.await() }
            .onFailure { Log.w(TAG, "History fetch failed: ${it.message}") }
            .getOrDefault(emptyList())

        val peerSymbols = runCatching { peersDeferred.await() }
            .onFailure { Log.w(TAG, "Peer lookup failed: ${it.message}") }
            .getOrDefault(emptyList())
            .take(3)

        val peerComparisons = buildPeerComparisons(peerSymbols.map { it.symbol }, quote)

        val evaluationResult = evaluator.evaluate(
            quote = quote,
            history = history,
            peerComparisons = peerComparisons
        )

        val analysis = evaluationResult.analysis.copy(
            mermaidDefinition = mermaidBuilder.build(
                decisions = evaluationResult.analysis.decisionTrail,
                verdict = evaluationResult.verdict
            )
        )

        val llmOpinion = runCatching {
            llmClient?.synthesizeSecondOpinion(analysis)
        }.onFailure {
            Log.w(TAG, "LLM synthesis failed: ${it.message}")
        }.getOrNull()

        return@coroutineScope analysis.copy(
            llmOpinionHtml = llmOpinion
        )
    }

    private suspend fun buildPeerComparisons(
        symbols: List<String>,
        baseQuote: StockQuote
    ): List<PeerComparison> = withContext(Dispatchers.IO) {
        symbols.mapNotNull { symbol ->
            runCatching { financeClient.fetchQuote(symbol) }
                .onFailure { Log.w(TAG, "Peer quote load failed for $symbol: ${it.message}") }
                .getOrNull()
        }.map { quote ->
            val verdict = classifyPeer(quote)
            val delta = if (quote.changePercent != null && baseQuote.changePercent != null) {
                quote.changePercent - baseQuote.changePercent
            } else null
            PeerComparison(
                ticker = quote.ticker,
                name = quote.companyName,
                verdict = verdict,
                performanceDelta = delta,
                price = quote.price
            )
        }
    }

    private fun classifyPeer(quote: StockQuote): StockVerdict {
        val pe = quote.forwardPe ?: quote.trailingPe
        val beta = quote.beta
        val checks = listOfNotNull(
            pe?.let {
                when {
                    it <= evaluationThresholds.pePass -> DecisionStatus.PASS
                    it <= evaluationThresholds.peClose -> DecisionStatus.CLOSE_FAIL
                    else -> DecisionStatus.FAIL
                }
            },
            beta?.let {
                when {
                    it <= evaluationThresholds.betaPass -> DecisionStatus.PASS
                    it <= evaluationThresholds.betaClose -> DecisionStatus.CLOSE_FAIL
                    else -> DecisionStatus.FAIL
                }
            },
            quote.changePercent?.let {
                when {
                    it >= evaluationThresholds.momentumPass * 100 -> DecisionStatus.PASS
                    it >= evaluationThresholds.momentumClose * 100 -> DecisionStatus.CLOSE_FAIL
                    else -> DecisionStatus.FAIL
                }
            }
        )
        val failCount = checks.count { it == DecisionStatus.FAIL }
        val closeCount = checks.count { it == DecisionStatus.CLOSE_FAIL }
        return when {
            failCount >= 2 -> StockVerdict.DO_NOT_BUY
            failCount == 1 -> StockVerdict.BUY_WITH_CAUTION
            closeCount >= 2 -> StockVerdict.BUY_WITH_CAUTION
            else -> StockVerdict.BUY
        }
    }

    fun clearCache() {
        cache.clear()
    }

    private data class CachedAnalysis(
        val timestamp: Long,
        val analysis: StockAnalysis
    ) {
        fun isExpired(ttlMillis: Long): Boolean =
            System.currentTimeMillis() - timestamp > ttlMillis

    }

    companion object {
        private const val TAG = "StockAnalysisRepo"
        private val evaluationThresholds = EvaluationThresholds()

        fun default(): StockAnalysisRepository {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }
            val cookieManager = CookieManager().apply {
                setCookiePolicy(CookiePolicy.ACCEPT_ALL)
            }
            val client = OkHttpClient.Builder()
                .cookieJar(JavaNetCookieJar(cookieManager))
                .addInterceptor(logging)
                .build()

            val financeClient = YahooFinanceClient(client)
            val evaluator = StockEvaluator(evaluationThresholds)
            val mermaidBuilder = MermaidDefinitionBuilder()
            val llmClient = LargeLanguageModelClient(
                httpClient = client,
                groqApiKey = BuildConfig.GROQ_API_KEY,
                geminiApiKey = BuildConfig.GEMINI_API_KEY
            )
            return StockAnalysisRepository(
                financeClient = financeClient,
                evaluator = evaluator,
                mermaidBuilder = mermaidBuilder,
                llmClient = llmClient
            )
        }
    }
}
