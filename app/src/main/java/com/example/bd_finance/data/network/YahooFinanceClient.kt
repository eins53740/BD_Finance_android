package com.example.bd_finance.data.network

import com.example.bd_finance.data.model.HistoricalPrice
import com.example.bd_finance.data.model.PeerSymbol
import com.example.bd_finance.data.model.StockQuote
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import kotlin.math.abs

class YahooFinanceClient(
    private val client: OkHttpClient
) {
    @Volatile
    private var crumb: String? = null
    private val crumbMutex = Mutex()

    private fun execute(url: String) = client.newCall(buildRequest(url)).execute()

    private fun buildRequest(url: String): Request =
        Request.Builder()
            .url(url)
            .get()
            .apply {
                DEFAULT_HEADERS.forEach { (key, value) ->
                    header(key, value)
                }
            }
            .build()

    private suspend fun ensureCrumb(): String {
        val existing = crumb
        if (existing != null) return existing
        return crumbMutex.withLock {
            crumb ?: fetchCrumb().also { crumb = it }
        }
    }

    private fun invalidateCrumb() {
        crumb = null
    }

    private fun fetchCrumb(): String {
        execute("https://fc.yahoo.com").use { /* warm up cookie jar */ }
        execute("https://query1.finance.yahoo.com/v1/test/getcrumb").use { response ->
            if (!response.isSuccessful) {
                throw IOException("Failed to fetch crumb: ${response.code}")
            }
            val value = response.body?.string()?.trim()
            if (value.isNullOrEmpty()) {
                throw IOException("Empty crumb response")
            }
            return value
        }
    }

    private suspend fun <T> runWithCrumb(
        baseUrl: String,
        parser: (JSONObject) -> T
    ): T {
        var retryCount = 0
        while (true) {
            val crumbValue = ensureCrumb()
            val url = appendCrumb(baseUrl, crumbValue)
            var retry = false
            execute(url).use { response ->
                if (response.code == 401 || response.code == 403) {
                    invalidateCrumb()
                    if (retryCount++ < 2) {
                        retry = true
                        return@use
                    }
                    throw IOException("Failed to load data: ${response.code}")
                }
                if (!response.isSuccessful) {
                    throw IOException("Failed to load data: ${response.code}")
                }
                val payload = response.body?.string() ?: throw IOException("Empty response")
                val json = JSONObject(payload)
                return parser(json)
            }
            if (retry) continue
        }
    }

    suspend fun fetchQuote(ticker: String): StockQuote = withContext(Dispatchers.IO) {
        val normalized = ticker.uppercase()
        val baseUrl = "https://query1.finance.yahoo.com/v7/finance/quote?symbols=$normalized"
        runWithCrumb(baseUrl) { root ->
            val results = root.optJSONObject("quoteResponse")
                ?.optJSONArray("result")
                ?: throw IOException("Malformed quote payload")
            if (results.length() == 0) {
                throw IOException("No quote data for $normalized")
            }
            val quote = results.getJSONObject(0)
            StockQuote(
                ticker = normalized,
                companyName = quote.optString("longName").takeIf { it.isNotBlank() }
                    ?: quote.optString("shortName"),
                currency = quote.optString("currency"),
                price = quote.optDoubleOrNull("regularMarketPrice"),
                previousClose = quote.optDoubleOrNull("regularMarketPreviousClose"),
                changePercent = quote.optDoubleOrNull("regularMarketChangePercent"),
                marketCap = quote.optLongOrNull("marketCap")?.toDouble(),
                beta = quote.optDoubleOrNull("beta"),
                forwardPe = quote.optDoubleOrNull("forwardPE"),
                trailingPe = quote.optDoubleOrNull("trailingPE"),
                pegRatio = quote.optDoubleOrNull("pegRatio"),
                priceToBook = quote.optDoubleOrNull("priceToBook"),
                fiftyTwoWeekHigh = quote.optDoubleOrNull("fiftyTwoWeekHigh"),
                fiftyTwoWeekLow = quote.optDoubleOrNull("fiftyTwoWeekLow"),
                dividendYield = quote.optDoubleOrNull("trailingAnnualDividendYield")
                    ?: quote.optDoubleOrNull("dividendYield"),
                payoutRatio = quote.optDoubleOrNull("payoutRatio"),
                trailingAnnualDividendRate = quote.optDoubleOrNull("trailingAnnualDividendRate"),
                sector = quote.optString("sector"),
                industry = quote.optString("industry")
            )
        }
    }

    suspend fun fetchHistoricalPrices(
        ticker: String,
        range: String = "1y",
        interval: String = "1d"
    ): List<HistoricalPrice> = withContext(Dispatchers.IO) {
        val normalized = ticker.uppercase()
        val baseUrl =
            "https://query1.finance.yahoo.com/v8/finance/chart/$normalized?range=$range&interval=$interval"
        runWithCrumb(baseUrl) { root ->
            val result = root.optJSONObject("chart")
                ?.optJSONArray("result")
                ?.optJSONObject(0)
                ?: throw IOException("Malformed history payload")
            val timestamps = result.optJSONArray("timestamp") ?: JSONArray()
            val prices = result.optJSONObject("indicators")
                ?.optJSONArray("quote")
                ?.optJSONObject(0)
                ?.optJSONArray("close") ?: JSONArray()
            buildList {
                for (index in 0 until minOf(timestamps.length(), prices.length())) {
                    val ts = timestamps.optLongOrNull(index) ?: continue
                    val close = prices.optDoubleOrNull(index) ?: continue
                    if (!close.isFinite() || close <= 0.0) continue
                    add(HistoricalPrice(ts, close))
                }
            }
        }
    }

    suspend fun fetchPeerSymbols(ticker: String): List<PeerSymbol> = withContext(Dispatchers.IO) {
        val normalized = ticker.uppercase()
        val baseUrl =
            "https://query2.finance.yahoo.com/v6/finance/recommendationsbysymbol/$normalized"
        runWithCrumb(baseUrl) { root ->
            val result = root.optJSONObject("finance")
                ?.optJSONArray("result")
                ?.optJSONObject(0)
                ?.optJSONArray("recommendedSymbols") ?: JSONArray()
            buildList {
                for (index in 0 until result.length()) {
                    val entry = result.optJSONObject(index) ?: continue
                    val symbol = entry.optString("symbol")
                    if (symbol.isNullOrBlank()) continue
                    add(
                        PeerSymbol(
                            symbol = symbol.uppercase(),
                            score = entry.optDoubleOrNull("score") ?: 0.0
                        )
                    )
                }
            }.sortedByDescending { abs(it.score) }
        }
    }
}

private val DEFAULT_HEADERS = listOf(
    "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36",
    "Accept" to "*/*",
    "Accept-Language" to "en-US,en;q=0.9",
    "Connection" to "keep-alive",
    "Referer" to "https://finance.yahoo.com/"
)

private fun appendCrumb(url: String, crumb: String): String {
    val separator = if (url.contains("?")) "&" else "?"
    val encoded = URLEncoder.encode(crumb, StandardCharsets.UTF_8)
    return "$url$separator" + "crumb=$encoded"
}

private fun JSONObject.optDoubleOrNull(name: String): Double? =
    if (has(name) && !isNull(name)) optDouble(name) else null

private fun JSONObject.optLongOrNull(name: String): Long? =
    if (has(name) && !isNull(name)) optLong(name) else null

private fun JSONArray.optDoubleOrNull(index: Int): Double? =
    if (!isNull(index)) optDouble(index) else null

private fun JSONArray.optLongOrNull(index: Int): Long? =
    if (!isNull(index)) optLong(index) else null

private fun Double.isFinite(): Boolean = !isNaN() && !isInfinite()
