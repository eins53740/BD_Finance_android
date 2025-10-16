package com.example.bd_finance.data.analysis

import com.example.bd_finance.data.model.DecisionCheck
import com.example.bd_finance.data.model.DecisionStatus
import com.example.bd_finance.data.model.DividendInsight
import com.example.bd_finance.data.model.HistoricalPrice
import com.example.bd_finance.data.model.MomentumInsight
import com.example.bd_finance.data.model.MetricEntry
import com.example.bd_finance.data.model.PeerComparison
import com.example.bd_finance.data.model.RiskInsight
import com.example.bd_finance.data.model.StockAnalysis
import com.example.bd_finance.data.model.StockQuote
import com.example.bd_finance.data.model.StockSummary
import com.example.bd_finance.data.model.StockVerdict
import com.example.bd_finance.data.model.VerdictColor
import com.example.bd_finance.data.model.formatPercent
import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt

class StockEvaluator(
    private val thresholds: EvaluationThresholds = EvaluationThresholds()
) {

    fun evaluate(
        quote: StockQuote,
        history: List<HistoricalPrice>,
        peerComparisons: List<PeerComparison>
    ): EvaluationResult {
        val price = quote.price
        val decisions = mutableListOf<DecisionCheck>()

        decisions += assessPe(quote)
        decisions += assessPeg(quote)
        decisions += assessPriceDistanceFromHigh(quote)
        decisions += assessMomentum(history, periods = 63, id = "momentum3m", label = "3M momentum")
        decisions += assessMomentum(history, periods = 126, id = "momentum6m", label = "6M momentum")
        decisions += assessBeta(quote)
        decisions += assessMarketCap(quote)
        decisions += assessDividendCoverage(quote)

        val failCount = decisions.count { it.status == DecisionStatus.FAIL }
        val closeCount = decisions.count { it.status == DecisionStatus.CLOSE_FAIL }
        val verdict = when {
            failCount >= 2 -> StockVerdict.DO_NOT_BUY
            failCount == 1 && closeCount >= 1 -> StockVerdict.DO_NOT_BUY
            failCount == 1 -> StockVerdict.BUY_WITH_CAUTION
            closeCount >= thresholds.closeFailTolerance -> StockVerdict.BUY_WITH_CAUTION
            else -> StockVerdict.BUY
        }

        val formattedPrice = price?.let { currencyFormatter(quote.currency).format(it) } ?: "—"
        val change = quote.changePercent.formatPercent()
        val verdictNarrative = buildVerdictNarrative(verdict, formattedPrice, change, decisions)

        val summary = StockSummary(
            ticker = quote.ticker,
            companyName = quote.companyName,
            verdict = verdict,
            verdictNarrative = verdictNarrative,
            price = price,
            previousClose = quote.previousClose,
            changePercent = quote.changePercent,
            currency = quote.currency,
            marketCap = quote.marketCap,
            beta = quote.beta,
            updatedAt = Instant.now()
        )

        val metrics = buildMetrics(quote, history, decisions)
        val momentum = buildMomentumInsights(history)
        val risk = buildRiskInsights(quote, history, decisions)
        val dividend = buildDividendInsight(quote)

        return EvaluationResult(
            analysis = StockAnalysis(
                summary = summary,
                metrics = metrics,
                decisionTrail = decisions,
                riskInsights = risk,
                momentumInsights = momentum,
                peerComparisons = peerComparisons,
                dividendInsight = dividend,
                mermaidDefinition = "", // placeholder, will be populated by repository
                llmOpinionHtml = null
            ),
            verdict = verdict
        )
    }

    private fun buildVerdictNarrative(
        verdict: StockVerdict,
        priceText: String,
        changePercent: String,
        decisions: List<DecisionCheck>
    ): String {
        val nonPass = decisions.filter { it.status != DecisionStatus.PASS }.map { it.title }
        val cautionSummary = when {
            nonPass.isEmpty() -> "No material caution flags triggered."
            nonPass.size == 1 -> "${nonPass.first()} needs attention."
            else -> "${nonPass.first()} and ${nonPass[1]} need attention."
        }
        return when (verdict) {
            StockVerdict.BUY ->
                "Screens favor a buy at $priceText with $changePercent today. $cautionSummary"
            StockVerdict.BUY_WITH_CAUTION ->
                "Buy case is mixed at $priceText with $changePercent today. $cautionSummary"
            StockVerdict.DO_NOT_BUY ->
                "Signals lean defensive at $priceText with $changePercent today. $cautionSummary"
        }
    }

    private fun assessPe(quote: StockQuote): DecisionCheck {
        val value = quote.forwardPe ?: quote.trailingPe
        val observed = value?.let { "%.1fx".format(it) } ?: "data unavailable"
        val status = when {
            value == null -> DecisionStatus.CLOSE_FAIL
            value <= thresholds.pePass -> DecisionStatus.PASS
            value <= thresholds.peClose -> DecisionStatus.CLOSE_FAIL
            else -> DecisionStatus.FAIL
        }
        return DecisionCheck(
            id = "valuation",
            title = "Valuation (P/E)",
            observed = observed,
            threshold = "≤ %.1fx".format(thresholds.pePass),
            status = status,
            note = if (value == null) {
                "Forward P/E unavailable; review valuation manually."
            } else null
        )
    }

    private fun assessPeg(quote: StockQuote): DecisionCheck {
        val value = quote.pegRatio
        val observed = value?.let { "%.2f".format(it) } ?: "data unavailable"
        val status = when {
            value == null -> DecisionStatus.CLOSE_FAIL
            value <= thresholds.pegPass -> DecisionStatus.PASS
            value <= thresholds.pegClose -> DecisionStatus.CLOSE_FAIL
            else -> DecisionStatus.FAIL
        }
        return DecisionCheck(
            id = "growthEfficiency",
            title = "Growth Efficiency (PEG)",
            observed = observed,
            threshold = "≤ %.1f".format(thresholds.pegPass),
            status = status,
            note = if (value == null) "PEG ratio missing; growth alignment uncertain." else null
        )
    }

    private fun assessPriceDistanceFromHigh(quote: StockQuote): DecisionCheck {
        val price = quote.price
        val high = quote.fiftyTwoWeekHigh
        val ratio = if (price != null && high != null && high > 0) price / high else null
        val observed = ratio?.let { "%.1f%% of 52w high".format(it * 100) } ?: "data unavailable"
        val status = when {
            ratio == null -> DecisionStatus.CLOSE_FAIL
            ratio <= thresholds.priceVsHighPass -> DecisionStatus.PASS
            ratio <= thresholds.priceVsHighClose -> DecisionStatus.CLOSE_FAIL
            else -> DecisionStatus.FAIL
        }
        val threshold = "≤ %.0f%% of 52w high".format(thresholds.priceVsHighPass * 100)
        return DecisionCheck(
            id = "valueDiscipline",
            title = "Value Discipline",
            observed = observed,
            threshold = threshold,
            status = status,
            note = null
        )
    }

    private fun assessMomentum(
        history: List<HistoricalPrice>,
        periods: Int,
        id: String,
        label: String
    ): DecisionCheck {
        val change = history.percentChange(periods)
        val observed = change.formatPercent()
        val status = when {
            change == null -> DecisionStatus.CLOSE_FAIL
            change >= thresholds.momentumPass -> DecisionStatus.PASS
            change >= thresholds.momentumClose -> DecisionStatus.CLOSE_FAIL
            else -> DecisionStatus.FAIL
        }
        val threshold = "≥ %.0f%%".format(thresholds.momentumPass * 100)
        val note = if (change == null) "Insufficient price history." else null
        return DecisionCheck(
            id = id,
            title = label,
            observed = observed,
            threshold = threshold,
            status = status,
            note = note
        )
    }

    private fun assessBeta(quote: StockQuote): DecisionCheck {
        val value = quote.beta
        val observed = value?.let { "%.2f".format(it) } ?: "data unavailable"
        val status = when {
            value == null -> DecisionStatus.CLOSE_FAIL
            value <= thresholds.betaPass -> DecisionStatus.PASS
            value <= thresholds.betaClose -> DecisionStatus.CLOSE_FAIL
            else -> DecisionStatus.FAIL
        }
        val threshold = "≤ %.2f".format(thresholds.betaPass)
        return DecisionCheck(
            id = "volatility",
            title = "Volatility (Beta)",
            observed = observed,
            threshold = threshold,
            status = status,
            note = null
        )
    }

    private fun assessMarketCap(quote: StockQuote): DecisionCheck {
        val value = quote.marketCap
        val observed = value?.let { humanReadableMoney(it, quote.currency) } ?: "data unavailable"
        val status = when {
            value == null -> DecisionStatus.CLOSE_FAIL
            value >= thresholds.marketCapPass -> DecisionStatus.PASS
            value >= thresholds.marketCapClose -> DecisionStatus.CLOSE_FAIL
            else -> DecisionStatus.FAIL
        }
        val threshold = "≥ %s".format(humanReadableMoney(thresholds.marketCapPass, quote.currency))
        return DecisionCheck(
            id = "scale",
            title = "Market Scale",
            observed = observed,
            threshold = threshold,
            status = status,
            note = null
        )
    }

    private fun assessDividendCoverage(quote: StockQuote): DecisionCheck {
        val yield = quote.dividendYield?.times(100)
        val payout = quote.payoutRatio
        val status = when {
            yield == null || yield.isNaN() -> DecisionStatus.CLOSE_FAIL
            payout == null || payout.isNaN() -> DecisionStatus.CLOSE_FAIL
            yield >= thresholds.dividendYieldPass && payout <= thresholds.payoutRatioPass -> DecisionStatus.PASS
            yield >= thresholds.dividendYieldClose && payout <= thresholds.payoutRatioClose -> DecisionStatus.CLOSE_FAIL
            yield <= 0.0 -> DecisionStatus.FAIL
            payout > 1.0 -> DecisionStatus.FAIL
            else -> DecisionStatus.CLOSE_FAIL
        }
        val observedYield = yield?.let { "%.2f%% yield".format(it) } ?: "yield unavailable"
        val observedPayout = payout?.let { "%.0f%% payout".format(it * 100) } ?: "payout unknown"
        val threshold =
            "Yield ≥ %.1f%% with payout ≤ %.0f%%".format(
                thresholds.dividendYieldPass,
                thresholds.payoutRatioPass * 100
            )
        val note = if (yield == null || payout == null) {
            "Dividend details incomplete."
        } else null
        return DecisionCheck(
            id = "dividend",
            title = "Dividend Sustainability",
            observed = "$observedYield, $observedPayout",
            threshold = threshold,
            status = status,
            note = note
        )
    }

    private fun buildMetrics(
        quote: StockQuote,
        history: List<HistoricalPrice>,
        decisions: List<DecisionCheck>
    ): List<MetricEntry> {
        val oneYearChange = history.percentChange(252)
        val decisionMap = decisions.associateBy { it.id }
        return listOf(
            MetricEntry(
                name = "Forward P/E",
                value = quote.forwardPe?.let { "%.1f×".format(it) } ?: "—",
                status = decisionMap["valuation"]?.status,
                description = null
            ),
            MetricEntry(
                name = "PEG Ratio",
                value = quote.pegRatio?.let { "%.2f".format(it) } ?: "—",
                status = decisionMap["growthEfficiency"]?.status,
                description = null
            ),
            MetricEntry(
                name = "52w Range Position",
                value = ratioToRange(quote).let { "%.0f%%".format(it * 100) },
                status = decisionMap["valueDiscipline"]?.status,
                description = "0% near the low, 100% near the high."
            ),
            MetricEntry(
                name = "1Y Momentum",
                value = oneYearChange.formatPercent(),
                status = null,
                description = null
            ),
            MetricEntry(
                name = "Beta",
                value = quote.beta?.let { "%.2f".format(it) } ?: "—",
                status = decisionMap["volatility"]?.status,
                description = null
            ),
            MetricEntry(
                name = "Dividend Yield",
                value = quote.dividendYield?.times(100)?.let { "%.2f%%".format(it) } ?: "—",
                status = decisionMap["dividend"]?.status,
                description = null
            ),
            MetricEntry(
                name = "Market Cap",
                value = quote.marketCap?.let { humanReadableMoney(it, quote.currency) } ?: "—",
                status = decisionMap["scale"]?.status,
                description = null
            )
        )
    }

    private fun buildMomentumInsights(history: List<HistoricalPrice>): List<MomentumInsight> {
        val periods = listOf(
            "1M" to 21,
            "3M" to 63,
            "6M" to 126,
            "1Y" to 252
        )
        return periods.mapNotNull { (label, period) ->
            val change = history.percentChange(period) ?: return@mapNotNull null
            MomentumInsight(
                periodLabel = label,
                percentChange = change,
                isPositive = change >= 0
            )
        }
    }

    private fun buildRiskInsights(
        quote: StockQuote,
        history: List<HistoricalPrice>,
        decisions: List<DecisionCheck>
    ): List<RiskInsight> {
        val volatilityScore = when (quote.beta) {
            null -> 60
            in Double.NEGATIVE_INFINITY..thresholds.betaPass -> 85
            in thresholds.betaPass..thresholds.betaClose -> 60
            else -> 35
        }
        val valuationCheck = decisions.first { it.id == "valuation" }
        val valuationScore = when (valuationCheck.status) {
            DecisionStatus.PASS -> 90
            DecisionStatus.CLOSE_FAIL -> 65
            DecisionStatus.FAIL -> 30
        }
        val priceRatio = ratioToRange(quote)
        val proximityScore = when {
            priceRatio < 0.3 -> 80
            priceRatio < 0.7 -> 65
            else -> 40
        }
        val momentum = history.percentChange(126)
        val momentumScore = when {
            momentum == null -> 55
            momentum >= 20 -> 85
            momentum >= 0 -> 65
            else -> 35
        }
        return listOf(
            RiskInsight(
                title = "Volatility",
                summary = quote.beta?.let { "Beta at %.2f".format(it) } ?: "Beta unavailable",
                scoreLabel = ratingForScore(volatilityScore),
                score = volatilityScore
            ),
            RiskInsight(
                title = "Valuation",
                summary = "P/E check ${valuationCheck.status.name.lowercase()}",
                scoreLabel = ratingForScore(valuationScore),
                score = valuationScore
            ),
            RiskInsight(
                title = "Price Position",
                summary = "Trading at %.0f%% of 52w range".format(priceRatio * 100),
                scoreLabel = ratingForScore(proximityScore),
                score = proximityScore
            ),
            RiskInsight(
                title = "Momentum",
                summary = "6M change ${momentum.formatPercent()}",
                scoreLabel = ratingForScore(momentumScore),
                score = momentumScore
            )
        )
    }

    private fun ratingForScore(score: Int): String = when {
        score >= 80 -> "Favorable"
        score >= 60 -> "Balanced"
        score >= 40 -> "Caution"
        else -> "Elevated"
    }

    private fun buildDividendInsight(quote: StockQuote): DividendInsight? {
        val yield = quote.dividendYield?.takeIf { it.isFinite() }?.times(100)
        val payout = quote.payoutRatio
        if (yield == null && payout == null) return null
        val narrative = when {
            yield == null || yield <= 0 -> "Dividend coverage uncertain; yield unavailable."
            payout != null && payout in 0.0..0.75 -> "Coverage looks sustainable relative to earnings."
            payout != null && payout > 1.0 -> "Payout exceeds earnings, raising sustainability concerns."
            else -> "Review payout stability in upcoming reports."
        }
        return DividendInsight(
            yield = yield,
            payoutRatio = payout,
            nextPaymentDate = null,
            consistencyNarrative = narrative
        )
    }

    private fun ratioToRange(quote: StockQuote): Double {
        val price = quote.price ?: return 0.5
        val low = quote.fiftyTwoWeekLow ?: return 0.5
        val high = quote.fiftyTwoWeekHigh ?: return 0.5
        if (high <= low) return 0.5
        val ratio = (price - low) / (high - low)
        return ratio.coerceIn(0.0, 1.0)
    }

    private fun currencyFormatter(currency: String?): NumberFormat =
        NumberFormat.getCurrencyInstance(Locale.US).apply {
            currency?.let {
                try {
                    this.currency = java.util.Currency.getInstance(it)
                } catch (_: IllegalArgumentException) {
                }
            }
        }

    private fun humanReadableMoney(value: Double, currency: String?): String {
        val units = listOf(
            1_000_000_000_000.0 to "T",
            1_000_000_000.0 to "B",
            1_000_000.0 to "M"
        )
        for ((threshold, suffix) in units) {
            if (value >= threshold) {
                return "%.2f%s".format(value / threshold, suffix)
            }
        }
        val formatter = currencyFormatter(currency)
        return formatter.format(value)
    }

    private fun List<HistoricalPrice>.percentChange(periods: Int): Double? {
        if (isEmpty()) return null
        val sorted = sortedBy { it.epochSeconds }
        if (sorted.size == 1) return 0.0
        val baselineIndex = (sorted.size - periods - 1).coerceAtLeast(0)
        val latest = sorted.last().close
        val baseline = sorted[baselineIndex].close
        if (baseline <= 0) return null
        return ((latest - baseline) / baseline) * 100.0
    }
}

data class EvaluationResult(
    val analysis: StockAnalysis,
    val verdict: StockVerdict
)

data class EvaluationThresholds(
    val pePass: Double = 25.0,
    val peClose: Double = 32.0,
    val pegPass: Double = 1.5,
    val pegClose: Double = 1.8,
    val priceVsHighPass: Double = 0.90,
    val priceVsHighClose: Double = 0.97,
    val betaPass: Double = 1.4,
    val betaClose: Double = 1.75,
    val marketCapPass: Double = 2_000_000_000.0,
    val marketCapClose: Double = 1_000_000_000.0,
    val dividendYieldPass: Double = 1.5,
    val dividendYieldClose: Double = 0.5,
    val payoutRatioPass: Double = 0.75,
    val payoutRatioClose: Double = 0.95,
    val momentumPass: Double = 0.02,
    val momentumClose: Double = -0.03,
    val closeFailTolerance: Int = 2
)

class MermaidDefinitionBuilder {

    fun build(decisions: List<DecisionCheck>, verdict: StockVerdict): String {
        val builder = StringBuilder()
        builder.appendLine("flowchart TD")
        builder.appendLine("    start([[Start]])")

        if (decisions.isEmpty()) {
            builder.appendLine("    start --> verdict[[${verdict.headline}]]")
        }

        decisions.forEachIndexed { index, decision ->
            val nodeId = nodeId(index)
            builder.appendLine("    $nodeId{${decision.title.escapeMermaid()}}")
            val description =
                "${decision.observed.escapeMermaid()}\\nTarget: ${decision.threshold.escapeMermaid()}"
            val statusEdge = when (decision.status) {
                DecisionStatus.PASS -> "PASS"
                DecisionStatus.CLOSE_FAIL -> "WARN"
                DecisionStatus.FAIL -> "FAIL"
            }
            if (index == 0) {
                builder.appendLine("    start -->|$statusEdge| $nodeId")
            } else {
                val previousId = nodeId(index - 1)
                builder.appendLine("    $previousId -->|$statusEdge| $nodeId")
            }
            builder.appendLine("    ${nodeId}:::${decision.status.name.lowercase()}")
        }
        if (decisions.isNotEmpty()) {
            builder.appendLine("    ${nodeId(decisions.lastIndex)} --> verdict[[${verdict.headline}]]")
        }
        builder.appendLine("    verdict:::${verdict.displayColor.name.lowercase()}")

        builder.appendLine("    classDef pass fill:#0F9D58,stroke:#0F9D58,color:#ffffff")
        builder.appendLine("    classDef close_fail fill:#F4B400,stroke:#F4B400,color:#202124")
        builder.appendLine("    classDef fail fill:#DB4437,stroke:#DB4437,color:#ffffff")
        builder.appendLine("    classDef success fill:#0F9D58,stroke:#0F9D58,color:#ffffff")
        builder.appendLine("    classDef warning fill:#F4B400,stroke:#F4B400,color:#202124")
        builder.appendLine("    classDef danger fill:#DB4437,stroke:#DB4437,color:#ffffff")
        builder.appendLine("    linkStyle default stroke-width:2px")

        return builder.toString()
    }

    private fun nodeId(index: Int): String = "n$index"

    private fun String.escapeMermaid(): String =
        replace("\"", "\\\"").replace("[\\[\\]]".toRegex(), "")
}

fun Instant.toDisplayString(): String =
    DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm z")
        .withLocale(Locale.getDefault())
        .withZone(ZoneId.systemDefault())
        .format(this)
