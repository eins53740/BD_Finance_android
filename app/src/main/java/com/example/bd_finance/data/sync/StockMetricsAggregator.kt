package com.example.bd_finance.data.sync

interface StockMetricsConnector {
    suspend fun fetch(ticker: String): ConnectorStockMetrics?
}

interface StockMetricsAggregator {
    suspend fun aggregate(ticker: String): NormalizedStockMetrics?
}

class DefaultStockMetricsAggregator(
    private val connectors: List<StockMetricsConnector>
) : StockMetricsAggregator {
    override suspend fun aggregate(ticker: String): NormalizedStockMetrics? {
        val metrics = connectors.mapNotNull { connector -> connector.fetch(ticker) }
        if (metrics.isEmpty()) return null
        return mergeMetrics(metrics)
    }
}

