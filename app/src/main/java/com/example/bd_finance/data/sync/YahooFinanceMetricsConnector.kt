package com.example.bd_finance.data.sync

import com.example.bd_finance.data.model.StockQuote
import com.example.bd_finance.data.network.YahooFinanceClient

class YahooFinanceMetricsConnector(
    private val client: YahooFinanceClient
) : StockMetricsConnector {
    override suspend fun fetch(ticker: String): ConnectorStockMetrics? {
        val quote = runCatching { client.fetchQuote(ticker) }.getOrNull() ?: return null
        return quote.toConnectorMetrics()
    }
}

internal fun StockQuote.toConnectorMetrics(): ConnectorStockMetrics =
    ConnectorStockMetrics(
        ticker = ticker,
        price = price,
        forwardPe = forwardPe,
        trailingPe = trailingPe,
        pegRatio = pegRatio,
        priceToBook = priceToBook,
        dividendRate = dividendRate ?: trailingAnnualDividendRate,
        epsForward = epsForward,
        bookValue = bookValue,
        beta = beta,
        sector = sector
    )
