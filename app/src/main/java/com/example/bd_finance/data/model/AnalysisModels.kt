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
    val llmOpinionHtml: String?,
    val fundamentalInsights: FundamentalInsights? = null,
    val intrinsicValuations: List<IntrinsicValuation> = emptyList()
)

data class FundamentalInsights(
    val valuationScores: List<FundamentalMetricScore>,
    val profitabilityScores: List<FundamentalMetricScore>,
    val stabilityScores: List<FundamentalMetricScore>,
    val growth: GrowthInsights,
    val historicalDeltas: List<HistoricalDelta>,
    val metadata: FundamentalDataMetadata
)

data class FundamentalMetricScore(
    val id: String,
    val label: String,
    val score: Int?,
    val status: MetricStrength,
    val currentValue: String,
    val sectorBenchmark: String?,
    val decadeAverage: String?,
    val note: String? = null
)

enum class MetricStrength {
    STRONG,
    NEUTRAL,
    WEAK,
    UNKNOWN
}

data class GrowthInsights(
    val revenue: GrowthMetric?,
    val earningsPerShare: GrowthMetric?,
    val freeCashFlow: GrowthMetric?,
    val commentary: String?
)

data class GrowthMetric(
    val label: String,
    val fiveYearCagr: Double?,
    val tenYearCagr: Double?,
    val trend: GrowthTrend
)

enum class GrowthTrend {
    ACCELERATING,
    DECELERATING,
    MIXED,
    UNKNOWN
}

data class HistoricalDelta(
    val label: String,
    val currentValue: String,
    val referenceValue: String?,
    val deltaPercent: Double?
)

data class FundamentalDataMetadata(
    val lastUpdatedMillis: Long,
    val usedFallback: Boolean,
    val source: String?
)

data class IntrinsicValuation(
    val model: IntrinsicModel,
    val intrinsicValue: Double?,
    val priceRatio: Double?,
    val band: ValuationBand,
    val assumptions: String,
    val status: IntrinsicValuationStatus
)

enum class IntrinsicModel {
    DISCOUNTED_CASH_FLOW,
    BEN_GRAHAM,
    DIVIDEND_DISCOUNT
}

enum class IntrinsicValuationStatus {
    AVAILABLE,
    INSUFFICIENT_DATA,
    INVALID
}

enum class ValuationBand {
    CHEAP,
    FAIR,
    EXPENSIVE,
    UNKNOWN
}

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
    val dividendRate: Double?,
    val epsForward: Double?,
    val bookValue: Double?,
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
