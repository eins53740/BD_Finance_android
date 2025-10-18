package com.example.bd_finance.data.sync

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StockMetricsAggregatorTest {

    private lateinit var aggregator: StockMetricsAggregator

    @Before
    fun setUp() {
        val primaryConnector = FakeConnector(
            mapOf(
                "AAPL" to ConnectorStockMetrics(
                    ticker = "AAPL",
                    price = 180.0,
                    forwardPe = 25.0,
                    trailingPe = null,
                    pegRatio = null,
                    priceToBook = null,
                    dividendRate = null,
                    epsForward = null,
                    bookValue = null,
                    beta = null
                )
            )
        )
        val secondaryConnector = FakeConnector(
            mapOf(
                "AAPL" to ConnectorStockMetrics(
                    ticker = "AAPL",
                    price = null,
                    forwardPe = null,
                    trailingPe = 28.4,
                    pegRatio = 1.6,
                    priceToBook = 4.8,
                    dividendRate = 0.92,
                    epsForward = 6.12,
                    bookValue = 31.4,
                    beta = 1.08
                )
            )
        )
        aggregator = DefaultStockMetricsAggregator(listOf(primaryConnector, secondaryConnector))
    }

    @Test
    fun `merge metrics pulls missing values from secondary connector`() = runTest {
        val result = aggregator.aggregate("AAPL")

        assertNotNull(result)
        requireNotNull(result)
        assertEquals("AAPL", result.ticker)
        assertEquals(180.0, result.price!!, 0.0)
        assertEquals(25.0, result.forwardPe!!, 0.0)
        assertEquals(28.4, result.trailingPe!!, 0.0)
        assertEquals(1.6, result.pegRatio!!, 0.0)
        assertEquals(4.8, result.priceToBook!!, 0.0)
        assertEquals(0.92, result.dividendRate!!, 0.0)
        assertEquals(6.12, result.epsForward!!, 0.0)
        assertEquals(31.4, result.bookValue!!, 0.0)
        assertEquals(1.08, result.beta!!, 0.0)
    }

    @Test
    fun `missing ticker returns null`() = runTest {
        val result = aggregator.aggregate("MSFT")
        assertEquals(null, result)
    }

    private class FakeConnector(
        private val data: Map<String, ConnectorStockMetrics>
    ) : StockMetricsConnector {
        override suspend fun fetch(ticker: String): ConnectorStockMetrics? = data[ticker]
    }
}
