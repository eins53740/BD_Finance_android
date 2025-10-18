package com.example.bd_finance.data.sync

data class NormalizedStockMetrics(
    val ticker: String,
    val price: Double?,
    val forwardPe: Double?,
    val trailingPe: Double?,
    val pegRatio: Double?,
    val priceToBook: Double?,
    val dividendRate: Double?,
    val epsForward: Double?,
    val bookValue: Double?,
    val beta: Double?,
    val returnOnEquity: Double? = null,
    val returnOnAssets: Double? = null,
    val operatingMargin: Double? = null,
    val netMargin: Double? = null,
    val debtToEquity: Double? = null,
    val sector: String?,
    val sectorSnapshot: SectorMedianSnapshot?,
    val historicalFundamentals: HistoricalFundamentalSnapshot?,
    val refreshMetadata: RefreshMetadata = RefreshMetadata()
)

data class ConnectorStockMetrics(
    val ticker: String,
    val price: Double?,
    val forwardPe: Double?,
    val trailingPe: Double?,
    val pegRatio: Double?,
    val priceToBook: Double?,
    val dividendRate: Double?,
    val epsForward: Double?,
    val bookValue: Double?,
    val beta: Double?,
    val returnOnEquity: Double? = null,
    val returnOnAssets: Double? = null,
    val operatingMargin: Double? = null,
    val netMargin: Double? = null,
    val debtToEquity: Double? = null,
    val sector: String? = null,
    val sectorSnapshot: SectorMedianSnapshot? = null,
    val historicalFundamentals: HistoricalFundamentalSnapshot? = null,
    val refreshMetadata: RefreshMetadata = RefreshMetadata()
)

internal fun mergeMetrics(metrics: List<ConnectorStockMetrics>): NormalizedStockMetrics? {
    if (metrics.isEmpty()) return null
    val ticker = metrics.first().ticker
    fun select(selector: (ConnectorStockMetrics) -> Double?): Double? =
        metrics.firstNotNullOfOrNull(selector)

    fun selectString(selector: (ConnectorStockMetrics) -> String?): String? =
        metrics.firstNotNullOfOrNull { connector ->
            selector(connector)?.takeIf { it.isNotBlank() }
        }

    val metadata = RefreshMetadata.combine(metrics.map { it.refreshMetadata })
    val sectorSnapshot = metrics.mapNotNull { it.sectorSnapshot }
        .sortedBy { snapshot -> snapshot.fallbackUsed }
        .firstOrNull()
    val fundamentals = metrics.firstNotNullOfOrNull { it.historicalFundamentals }

    return NormalizedStockMetrics(
        ticker = ticker,
        price = select { it.price },
        forwardPe = select { it.forwardPe },
        trailingPe = select { it.trailingPe },
        pegRatio = select { it.pegRatio },
        priceToBook = select { it.priceToBook },
        dividendRate = select { it.dividendRate },
        epsForward = select { it.epsForward },
        bookValue = select { it.bookValue },
        beta = select { it.beta },
        returnOnEquity = select { it.returnOnEquity },
        returnOnAssets = select { it.returnOnAssets },
        operatingMargin = select { it.operatingMargin },
        netMargin = select { it.netMargin },
        debtToEquity = select { it.debtToEquity },
        sector = selectString { it.sector },
        sectorSnapshot = sectorSnapshot,
        historicalFundamentals = fundamentals,
        refreshMetadata = metadata
    )
}

data class RefreshMetadata(
    val lastUpdatedMillis: Long = System.currentTimeMillis(),
    val retries: Int = 0,
    val usedFallback: Boolean = false
) {
    companion object {
        fun combine(items: List<RefreshMetadata>): RefreshMetadata {
            if (items.isEmpty()) return RefreshMetadata()
            val lastUpdated = items.maxOf { it.lastUpdatedMillis }
            val retriesSum = items.sumOf { it.retries }
            val fallback = items.any { it.usedFallback }
            return RefreshMetadata(
                lastUpdatedMillis = lastUpdated,
                retries = retriesSum,
                usedFallback = fallback
            )
        }
    }
}

data class SectorMedianSnapshot(
    val sectorName: String?,
    val metrics: SectorMedianMetrics,
    val source: String,
    val fallbackUsed: Boolean
)

data class SectorMedianMetrics(
    val priceToEarnings: Double?,
    val priceToBook: Double?,
    val returnOnEquity: Double?,
    val returnOnAssets: Double?,
    val operatingMargin: Double?,
    val netMargin: Double?,
    val debtToEquity: Double?
)

data class HistoricalFundamentalSnapshot(
    val metrics: Map<FundamentalMetric, HistoricalMetricWindow>
)

enum class FundamentalMetric {
    PE_RATIO,
    PRICE_TO_BOOK,
    RETURN_ON_EQUITY,
    OPERATING_MARGIN,
    NET_MARGIN,
    DEBT_TO_EQUITY,
    REVENUE,
    EARNINGS_PER_SHARE,
    FREE_CASH_FLOW
}

data class HistoricalMetricWindow(
    val fiveYear: HistoricalWindow?,
    val tenYear: HistoricalWindow?
)

data class HistoricalWindow(
    val points: List<HistoricalDataPoint>,
    val median: Double?,
    val average: Double?
)

data class HistoricalDataPoint(
    val year: Int,
    val value: Double?
)

