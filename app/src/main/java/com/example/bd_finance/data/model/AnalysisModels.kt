package com.example.bd_finance.data.model

import java.time.Instant
import kotlin.math.abs

enum class StockVerdict(val headline: String) {
    BUY("Buy"),
    BUY_WITH_CAUTION("Buy with Caution"),
    DO_NOT_BUY("Do Not Buy");

    val displayColor: VerdictColor
        get() = when (this) {
            BUY -> VerdictColor.Success
            BUY_WITH_CAUTION -> VerdictColor.Warning
            DO_NOT_BUY -> VerdictColor.Danger
        }
}

enum class VerdictColor {
    Success,
    Warning,
    Danger
}

enum class DecisionStatus {
    PASS,
    CLOSE_FAIL,
    FAIL;

    fun toVerdictColor(): VerdictColor = when (this) {
        PASS -> VerdictColor.Success
        CLOSE_FAIL -> VerdictColor.Warning
        FAIL -> VerdictColor.Danger
    }
}

data class StockSummary(
    val ticker: String,
    val companyName: String?,
    val verdict: StockVerdict,
    val verdictNarrative: String,
    val price: Double?,
    val previousClose: Double?,
    val changePercent: Double?,
    val currency: String?,
    val marketCap: Double?,
    val beta: Double?,
    val updatedAt: Instant
)

data class MetricEntry(
    val name: String,
    val value: String,
    val status: DecisionStatus?,
    val description: String?
)

data class DecisionCheck(
    val id: String,
    val title: String,
    val observed: String,
    val threshold: String,
    val status: DecisionStatus,
    val note: String?
) {
    val anchor: String = id.lowercase()
}

data class RiskInsight(
    val title: String,
    val summary: String,
    val scoreLabel: String,
    val score: Int
)

data class MomentumInsight(
    val periodLabel: String,
    val percentChange: Double,
    val isPositive: Boolean
)

data class PeerComparison(
    val ticker: String,
    val name: String?,
    val verdict: StockVerdict,
    val performanceDelta: Double?,
    val price: Double?
)

data class DividendInsight(
    val yield: Double?,
    val payoutRatio: Double?,
    val nextPaymentDate: String?,
    val consistencyNarrative: String
)

data class StockAnalysis(
    val summary: StockSummary,
    val metrics: List<MetricEntry>,
    val decisionTrail: List<DecisionCheck>,
    val riskInsights: List<RiskInsight>,
    val momentumInsights: List<MomentumInsight>,
    val peerComparisons: List<PeerComparison>,
    val dividendInsight: DividendInsight?,
    val mermaidDefinition: String,
    val llmOpinionHtml: String?
)

data class StockQuote(
    val ticker: String,
    val companyName: String?,
    val currency: String?,
    val price: Double?,
    val previousClose: Double?,
    val changePercent: Double?,
    val marketCap: Double?,
    val beta: Double?,
    val forwardPe: Double?,
    val trailingPe: Double?,
    val pegRatio: Double?,
    val priceToBook: Double?,
    val fiftyTwoWeekHigh: Double?,
    val fiftyTwoWeekLow: Double?,
    val dividendYield: Double?,
    val payoutRatio: Double?,
    val trailingAnnualDividendRate: Double?,
    val sector: String?,
    val industry: String?
)

data class HistoricalPrice(
    val epochSeconds: Long,
    val close: Double
)

data class PeerSymbol(
    val symbol: String,
    val score: Double
)

fun Double?.formatPercent(): String =
    if (this == null || this.isNaN() || this.isInfinite()) {
        "—"
    } else {
        val sign = if (this >= 0) "+" else "−"
        val magnitude = abs(this)
        "%s%.2f%%".format(sign, magnitude)
    }
