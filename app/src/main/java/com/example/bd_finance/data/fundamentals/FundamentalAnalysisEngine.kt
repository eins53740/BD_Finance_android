package com.example.bd_finance.data.fundamentals

import com.example.bd_finance.data.model.FundamentalDataMetadata
import com.example.bd_finance.data.model.FundamentalInsights
import com.example.bd_finance.data.model.FundamentalMetricScore
import com.example.bd_finance.data.model.GrowthInsights
import com.example.bd_finance.data.model.GrowthMetric
import com.example.bd_finance.data.model.GrowthTrend
import com.example.bd_finance.data.model.HistoricalDelta
import com.example.bd_finance.data.model.IntrinsicModel
import com.example.bd_finance.data.model.IntrinsicValuation
import com.example.bd_finance.data.model.IntrinsicValuationStatus
import com.example.bd_finance.data.model.MetricStrength
import com.example.bd_finance.data.model.StockQuote
import com.example.bd_finance.data.model.ValuationBand
import com.example.bd_finance.data.sync.FundamentalMetric
import com.example.bd_finance.data.sync.HistoricalFundamentalSnapshot
import com.example.bd_finance.data.sync.HistoricalMetricWindow
import com.example.bd_finance.data.sync.HistoricalWindow
import com.example.bd_finance.data.sync.NormalizedStockMetrics
import com.example.bd_finance.data.sync.SectorMedianMetrics
import com.example.bd_finance.data.sync.StockMetricsAggregator
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.StateFlow

class FundamentalAnalysisEngine(
    private val aggregator: StockMetricsAggregator,
    private val configFlow: StateFlow<FundamentalScoringConfig> = FundamentalConfigRegistry.state()
) {

    suspend fun analyze(ticker: String, quote: StockQuote): FundamentalAnalysisResult? {
        val normalized = runCatching { aggregator.aggregate(ticker) }.getOrNull() ?: return null
        val config = configFlow.value
        val insights = ScoreBuilder(quote, normalized, config).build()
        val valuations = IntrinsicValuationBuilder(quote, normalized, config).build()
        if (insights == null && valuations.isEmpty()) return null
        return FundamentalAnalysisResult(insights, valuations)
    }
}

data class FundamentalAnalysisResult(
    val insights: FundamentalInsights?,
    val valuations: List<IntrinsicValuation>
)

private class ScoreBuilder(
    private val quote: StockQuote,
    private val metrics: NormalizedStockMetrics,
    private val config: FundamentalScoringConfig
) {

    private val sector: SectorMedianMetrics? = metrics.sectorSnapshot?.metrics
    private val history: HistoricalFundamentalSnapshot? = metrics.historicalFundamentals

    fun build(): FundamentalInsights? {
        val valuation = valuationScores()
        val profitability = profitabilityScores()
        val stability = stabilityScores()
        val growth = growthInsights()
        val deltas = historicalDeltas()
        if (valuation.isEmpty() && profitability.isEmpty() && stability.isEmpty() && deltas.isEmpty() && growth == null) {
            return null
        }
        val metadata = FundamentalDataMetadata(
            lastUpdatedMillis = metrics.refreshMetadata.lastUpdatedMillis,
            usedFallback = metrics.refreshMetadata.usedFallback || (metrics.sectorSnapshot?.fallbackUsed == true),
            source = metrics.sectorSnapshot?.source
        )
        return FundamentalInsights(
            valuationScores = valuation,
            profitabilityScores = profitability,
            stabilityScores = stability,
            growth = growth ?: GrowthInsights(null, null, null, null),
            historicalDeltas = deltas,
            metadata = metadata
        )
    }

    private fun valuationScores(): List<FundamentalMetricScore> = buildList {
        addIfNotNull(ratioScore("forward_pe", "Forward P/E", quote.forwardPe, sector?.priceToEarnings, historyMedian(FundamentalMetric.PE_RATIO), preferLower = true))
        addIfNotNull(ratioScore("price_to_book", "Price-to-Book", quote.priceToBook, sector?.priceToBook, historyMedian(FundamentalMetric.PRICE_TO_BOOK), preferLower = true))
        addIfNotNull(
            ratioScore(
                id = "peg",
                label = "PEG Ratio",
                current = quote.pegRatio,
                sectorBenchmark = config.pegTarget,
                historicalBenchmark = config.pegTarget,
                preferLower = true,
                fallbackLabel = "Target ${formatRatio(config.pegTarget, true)}"
            )
        )
    }

    private fun profitabilityScores(): List<FundamentalMetricScore> = buildList {
        addIfNotNull(ratioScore("roe", "Return on Equity", metrics.returnOnEquity, sector?.returnOnEquity, historyMedian(FundamentalMetric.RETURN_ON_EQUITY), preferLower = false))
        addIfNotNull(ratioScore("roa", "Return on Assets", metrics.returnOnAssets, sector?.returnOnAssets, null, preferLower = false))
        addIfNotNull(ratioScore("operating_margin", "Operating Margin", metrics.operatingMargin, sector?.operatingMargin, historyMedian(FundamentalMetric.OPERATING_MARGIN), preferLower = false))
        addIfNotNull(ratioScore("net_margin", "Net Margin", metrics.netMargin, sector?.netMargin, historyMedian(FundamentalMetric.NET_MARGIN), preferLower = false))
    }

    private fun stabilityScores(): List<FundamentalMetricScore> = buildList {
        addIfNotNull(ratioScore("debt_to_equity", "Debt-to-Equity", metrics.debtToEquity, sector?.debtToEquity, historyMedian(FundamentalMetric.DEBT_TO_EQUITY), preferLower = true))
        addIfNotNull(marginConsistencyScore(historyWindow(FundamentalMetric.NET_MARGIN)))
        addIfNotNull(betaScore(quote.beta))
    }

    private fun historicalDeltas(): List<HistoricalDelta> = buildList {
        addDelta("Forward P/E", quote.forwardPe, historyMedian(FundamentalMetric.PE_RATIO), preferLower = true)
        addDelta("Price-to-Book", quote.priceToBook, historyMedian(FundamentalMetric.PRICE_TO_BOOK), preferLower = true)
        addDelta("ROE", metrics.returnOnEquity, historyMedian(FundamentalMetric.RETURN_ON_EQUITY), preferLower = false)
        addDelta("Net Margin", metrics.netMargin, historyMedian(FundamentalMetric.NET_MARGIN), preferLower = false)
    }

    private fun growthInsights(): GrowthInsights? {
        val revenue = growthMetric("Revenue", historyWindow(FundamentalMetric.REVENUE))
        val eps = growthMetric("EPS", historyWindow(FundamentalMetric.EARNINGS_PER_SHARE))
        val fcf = growthMetric("Free Cash Flow", historyWindow(FundamentalMetric.FREE_CASH_FLOW))
        if (revenue == null && eps == null && fcf == null) return null
        val commentary = buildString {
            revenue?.let { append("Revenue CAGR 5Y ${formatPercent(it.fiveYearCagr)} | 10Y ${formatPercent(it.tenYearCagr)}. ") }
            eps?.let { append("EPS CAGR 5Y ${formatPercent(it.fiveYearCagr)}. ") }
            fcf?.let { append("FCF CAGR 5Y ${formatPercent(it.fiveYearCagr)}.") }
        }.trim().ifEmpty { null }
        return GrowthInsights(revenue, eps, fcf, commentary)
    }

    private fun ratioScore(
        id: String,
        label: String,
        current: Double?,
        sectorBenchmark: Double?,
        historicalBenchmark: Double?,
        preferLower: Boolean,
        fallbackLabel: String? = null
    ): FundamentalMetricScore? {
        val currentValue = current ?: return null
        val weights = mutableListOf<Pair<Double, Double>>()
        sectorBenchmark?.let { weights += config.sectorWeight to it }
        historicalBenchmark?.let { weights += config.historyWeight to it }
        if (weights.isEmpty() && fallbackLabel != null) {
            weights += 1.0 to (sectorBenchmark ?: historicalBenchmark ?: config.pegTarget)
        }
        if (weights.isEmpty()) return null
        val scoreValue = weightedSignal(currentValue, weights, preferLower)
        val score = scoreValue?.let(::scoreFromSignal)
        val strength = score.toStrength()
        return FundamentalMetricScore(
            id = id,
            label = label,
            score = score,
            status = strength,
            currentValue = formatRatio(currentValue, preferLower),
            sectorBenchmark = sectorBenchmark?.let { formatRatio(it, preferLower) },
            decadeAverage = historicalBenchmark?.let { formatRatio(it, preferLower) },
            note = fallbackLabel
        )
    }

    private fun marginConsistencyScore(window: HistoricalMetricWindow?): FundamentalMetricScore? {
        val points = window?.fiveYear?.points?.mapNotNull { it.value } ?: return null
        if (points.size < 3) return null
        val mean = points.average()
        val variance = points.map { (it - mean).pow(2) }.average()
        val stdDev = kotlin.math.sqrt(variance)
        val score = ((1.0 - min(stdDev / config.marginStdDevReference, 1.0)) * 100).roundToInt()
        return FundamentalMetricScore(
            id = "margin_consistency",
            label = "Margin Consistency",
            score = score,
            status = score.toStrength(),
            currentValue = String.format(Locale.US, "%.1fpp std dev (5Y)", stdDev * 100),
            sectorBenchmark = null,
            decadeAverage = null,
            note = "Lower variation across 5Y margins is better"
        )
    }

    private fun betaScore(beta: Double?): FundamentalMetricScore? {
        beta ?: return null
        val capped = beta.coerceIn(0.0, config.betaNeutralValue)
        val score = ((config.betaNeutralValue - capped) / config.betaNeutralValue * 100).roundToInt()
        return FundamentalMetricScore(
            id = "beta",
            label = "Beta",
            score = score,
            status = score.toStrength(),
            currentValue = String.format(Locale.US, "%.2f", beta),
            sectorBenchmark = "<= 1.0 preferred",
            decadeAverage = null,
            note = "Lower beta implies more price stability"
        )
    }

    private fun weightedSignal(current: Double, benchmarks: List<Pair<Double, Double>>, preferLower: Boolean): Double? {
        val values = benchmarks.mapNotNull { (weight, benchmark) ->
            signal(current, benchmark, preferLower)?.let { weight to it }
        }
        if (values.isEmpty()) return null
        val weightSum = values.sumOf { it.first }
        val aggregate = values.sumOf { it.first * it.second } / weightSum
        return aggregate.coerceIn(-config.zScoreClamp, config.zScoreClamp)
    }

    private fun signal(current: Double, benchmark: Double, preferLower: Boolean): Double? {
        if (benchmark == 0.0) return null
        return if (preferLower) {
            (benchmark - current) / abs(benchmark)
        } else {
            (current - benchmark) / abs(benchmark)
        }
    }

    private fun scoreFromSignal(signal: Double): Int =
        (((signal + config.zScoreClamp) / (2 * config.zScoreClamp)).coerceIn(0.0, 1.0) * 100).roundToInt()

    private fun growthMetric(label: String, window: HistoricalMetricWindow?): GrowthMetric? {
        val five = cagr(window?.fiveYear)
        val ten = cagr(window?.tenYear)
        if (five == null && ten == null) return null
        val trend = when {
            five != null && ten != null && five > ten + 0.01 -> GrowthTrend.ACCELERATING
            five != null && ten != null && five + 0.01 < ten -> GrowthTrend.DECELERATING
            five != null && ten != null -> GrowthTrend.MIXED
            else -> GrowthTrend.UNKNOWN
        }
        return GrowthMetric(label, five, ten, trend)
    }

    private fun MutableList<HistoricalDelta>.addDelta(label: String, current: Double?, reference: Double?, preferLower: Boolean) {
        val currentValue = current ?: return
        val referenceValue = reference ?: return
        val delta = if (referenceValue == 0.0) null else ((currentValue - referenceValue) / abs(referenceValue)) * 100.0
        add(
            HistoricalDelta(
                label = "$label vs history",
                currentValue = formatRatio(currentValue, preferLower),
                referenceValue = formatRatio(referenceValue, preferLower),
                deltaPercent = delta
            )
        )
    }

    private fun formatRatio(value: Double, preferLower: Boolean): String =
        if (preferLower) String.format(Locale.US, "%.2fx", value) else formatPercent(value)

    private fun formatPercent(value: Double?): String =
        value?.let { String.format(Locale.US, "%.2f%%", it * 100) } ?: "-"

    private fun historyMedian(metric: FundamentalMetric): Double? =
        history?.metrics?.get(metric)?.tenYear?.median
            ?: history?.metrics?.get(metric)?.fiveYear?.median

    private fun historyWindow(metric: FundamentalMetric): HistoricalMetricWindow? =
        history?.metrics?.get(metric)

    private fun cagr(window: HistoricalWindow?): Double? {
        val points = window?.points?.filter { it.value != null }?.sortedBy { it.year } ?: return null
        if (points.size < 2) return null
        val start = points.first().value ?: return null
        val end = points.last().value ?: return null
        if (start <= 0.0 || end <= 0.0) return null
        val years = max(points.last().year - points.first().year, points.size - 1)
        if (years <= 0) return null
        return (end / start).pow(1.0 / years) - 1.0
    }

    private fun <T> MutableList<T>.addIfNotNull(item: T?) {
        if (item != null) add(item)
    }
}

private class IntrinsicValuationBuilder(
    private val quote: StockQuote,
    private val metrics: NormalizedStockMetrics,
    private val config: FundamentalScoringConfig
) {

    fun build(): List<IntrinsicValuation> {
        val items = mutableListOf<IntrinsicValuation>()
        dcfValuation()?.let(items::add)
        benGrahamValuation()?.let(items::add)
        dividendDiscountValuation()?.let(items::add)
        return items
    }

    private fun dcfValuation(): IntrinsicValuation? {
        val window = metrics.historicalFundamentals
            ?.metrics
            ?.get(FundamentalMetric.FREE_CASH_FLOW)
            ?.fiveYear ?: return insufficient(IntrinsicModel.DISCOUNTED_CASH_FLOW, "Missing free cash flow history")
        val fcfCagr = cagr(window) ?: 0.05
        val latest = window.points.lastOrNull()?.value ?: return insufficient(
            IntrinsicModel.DISCOUNTED_CASH_FLOW,
            "Missing latest free cash flow"
        )
        val marketCap = quote.marketCap ?: return insufficient(
            IntrinsicModel.DISCOUNTED_CASH_FLOW,
            "Missing market capitalization"
        )
        val price = quote.price ?: return insufficient(
            IntrinsicModel.DISCOUNTED_CASH_FLOW,
            "Missing price"
        )
        val shares = marketCap / price
        if (shares <= 0) return insufficient(IntrinsicModel.DISCOUNTED_CASH_FLOW, "Invalid share count")
        val fcfPerShare = latest / shares
        val growthRate = fcfCagr.coerceIn(0.0, 0.15)
        val discountRate = max(config.dcfDiscountFloor, growthRate + 0.05)
        val terminalGrowth = config.dcfTerminalGrowth
        var projected = fcfPerShare
        var presentValue = 0.0
        for (year in 1..5) {
            projected *= (1 + growthRate)
            presentValue += projected / (1 + discountRate).pow(year)
        }
        val terminalValue = projected * (1 + terminalGrowth) / (discountRate - terminalGrowth)
        presentValue += terminalValue / (1 + discountRate).pow(5)
        return valuation(
            model = IntrinsicModel.DISCOUNTED_CASH_FLOW,
            intrinsic = presentValue,
            price = price,
            assumptions = "FCF CAGR ${(growthRate * 100).roundToInt()}%, discount ${formatPercent(discountRate)}, terminal ${formatPercent(terminalGrowth)}"
        )
    }

    private fun benGrahamValuation(): IntrinsicValuation? {
        val window = metrics.historicalFundamentals
            ?.metrics
            ?.get(FundamentalMetric.EARNINGS_PER_SHARE)
            ?.fiveYear ?: return insufficient(IntrinsicModel.BEN_GRAHAM, "Missing EPS history")
        val epsCagr = cagr(window)?.coerceIn(0.0, 0.2) ?: return insufficient(
            IntrinsicModel.BEN_GRAHAM,
            "Missing EPS growth"
        )
        val eps = window.points.lastOrNull()?.value ?: metrics.epsForward ?: return insufficient(
            IntrinsicModel.BEN_GRAHAM,
            "Missing EPS value"
        )
        val growthRate = (epsCagr * 100).coerceAtLeast(0.0)
        val intrinsic = eps * (8.5 + 2 * growthRate)
        val price = quote.price ?: return insufficient(
            IntrinsicModel.BEN_GRAHAM,
            "Missing price"
        )
        return valuation(
            model = IntrinsicModel.BEN_GRAHAM,
            intrinsic = intrinsic,
            price = price,
            assumptions = "EPS ${String.format(Locale.US, "%.2f", eps)}; growth ${(growthRate).roundToInt()}%"
        )
    }

    private fun dividendDiscountValuation(): IntrinsicValuation? {
        val dividend = quote.dividendRate ?: quote.trailingAnnualDividendRate ?: return insufficient(
            IntrinsicModel.DIVIDEND_DISCOUNT,
            "Missing dividend data"
        )
        val price = quote.price ?: return insufficient(
            IntrinsicModel.DIVIDEND_DISCOUNT,
            "Missing price"
        )
        val growth = (quote.dividendYield ?: 0.0).coerceIn(0.0, config.dividendGrowthCap)
        val discountRate = max(config.dividendDiscountFloor, growth + 0.05)
        if (discountRate <= growth) {
            return IntrinsicValuation(
                model = IntrinsicModel.DIVIDEND_DISCOUNT,
                intrinsicValue = null,
                priceRatio = null,
                band = ValuationBand.UNKNOWN,
                assumptions = "Growth ${formatPercent(growth)}, discount ${formatPercent(discountRate)}",
                status = IntrinsicValuationStatus.INVALID
            )
        }
        val intrinsic = dividend * (1 + growth) / (discountRate - growth)
        return valuation(
            model = IntrinsicModel.DIVIDEND_DISCOUNT,
            intrinsic = intrinsic,
            price = price,
            assumptions = "Growth ${formatPercent(growth)}, discount ${formatPercent(discountRate)}"
        )
    }

    private fun valuation(
        model: IntrinsicModel,
        intrinsic: Double,
        price: Double,
        assumptions: String
    ): IntrinsicValuation {
        val ratio = intrinsic / price
        val band = when {
            ratio.isNaN() || ratio.isInfinite() -> ValuationBand.UNKNOWN
            ratio >= 1.15 -> ValuationBand.CHEAP
            ratio in 0.85..1.15 -> ValuationBand.FAIR
            ratio > 0 -> ValuationBand.EXPENSIVE
            else -> ValuationBand.UNKNOWN
        }
        return IntrinsicValuation(
            model = model,
            intrinsicValue = intrinsic,
            priceRatio = ratio,
            band = band,
            assumptions = assumptions,
            status = IntrinsicValuationStatus.AVAILABLE
        )
    }

    private fun insufficient(model: IntrinsicModel, reason: String): IntrinsicValuation? =
        IntrinsicValuation(
            model = model,
            intrinsicValue = null,
            priceRatio = null,
            band = ValuationBand.UNKNOWN,
            assumptions = reason,
            status = IntrinsicValuationStatus.INSUFFICIENT_DATA
        )

    private fun cagr(window: HistoricalWindow): Double? {
        val points = window.points.filter { it.value != null }.sortedBy { it.year }
        if (points.size < 2) return null
        val start = points.first().value ?: return null
        val end = points.last().value ?: return null
        if (start <= 0.0 || end <= 0.0) return null
        val years = max(points.last().year - points.first().year, points.size - 1)
        if (years <= 0) return null
        return (end / start).pow(1.0 / years) - 1.0
    }

    private fun formatPercent(value: Double): String = String.format(Locale.US, "%.2f%%", value * 100)
}

private fun Int?.toStrength(): MetricStrength = when {
    this == null -> MetricStrength.UNKNOWN
    this >= 70 -> MetricStrength.STRONG
    this >= 40 -> MetricStrength.NEUTRAL
    else -> MetricStrength.WEAK
}
