package com.example.bd_finance.data.fundamentals

import com.example.bd_finance.data.model.IntrinsicModel
import com.example.bd_finance.data.model.IntrinsicValuationStatus
import com.example.bd_finance.data.model.StockQuote
import com.example.bd_finance.data.sync.FundamentalMetric
import com.example.bd_finance.data.sync.HistoricalDataPoint
import com.example.bd_finance.data.sync.HistoricalFundamentalSnapshot
import com.example.bd_finance.data.sync.HistoricalMetricWindow
import com.example.bd_finance.data.sync.HistoricalWindow
import com.example.bd_finance.data.sync.NormalizedStockMetrics
import com.example.bd_finance.data.sync.RefreshMetadata
import com.example.bd_finance.data.sync.SectorMedianMetrics
import com.example.bd_finance.data.sync.SectorMedianSnapshot
import com.example.bd_finance.data.sync.StockMetricsAggregator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FundamentalAnalysisEngineTest {

    @Test
    fun `analyze returns insights and intrinsic valuations`() = runTest {
        val normalized = sampleMetrics()
        val engine = FundamentalAnalysisEngine(FakeAggregator(normalized))
        val quote = sampleQuote()

        val result = engine.analyze("AAPL", quote)

        assertNotNull(result)
        val insights = result?.insights
        assertNotNull("Expected valuation scores", insights?.valuationScores?.takeIf { it.isNotEmpty() })
        assertTrue("Expected profitability scores", insights?.profitabilityScores?.isNotEmpty() == true)
        val valuations = result?.valuations.orEmpty()
        assertTrue(valuations.any { it.model == IntrinsicModel.DISCOUNTED_CASH_FLOW && it.status == IntrinsicValuationStatus.AVAILABLE })
        assertTrue(valuations.any { it.model == IntrinsicModel.BEN_GRAHAM })
        assertTrue(valuations.any { it.model == IntrinsicModel.DIVIDEND_DISCOUNT })
    }

    private fun sampleQuote(): StockQuote = StockQuote(
        ticker = "AAPL",
        companyName = "Apple Inc.",
        currency = "USD",
        price = 180.0,
        previousClose = 178.0,
        changePercent = 0.012,
        marketCap = 2.8E12,
        beta = 1.05,
        forwardPe = 22.0,
        trailingPe = 24.0,
        pegRatio = 1.4,
        priceToBook = 7.2,
        dividendRate = 0.92,
        epsForward = 6.5,
        bookValue = 25.0,
        fiftyTwoWeekHigh = 190.0,
        fiftyTwoWeekLow = 140.0,
        dividendYield = 0.015,
        payoutRatio = 0.22,
        trailingAnnualDividendRate = 0.92,
        sector = "Technology",
        industry = "Consumer Electronics"
    )

    private fun sampleMetrics(): NormalizedStockMetrics {
        val peWindow = HistoricalMetricWindow(
            fiveYear = HistoricalWindow(
                points = listOf(
                    HistoricalDataPoint(2019, 25.0),
                    HistoricalDataPoint(2020, 23.0),
                    HistoricalDataPoint(2021, 22.0),
                    HistoricalDataPoint(2022, 21.5),
                    HistoricalDataPoint(2023, 20.5)
                ),
                median = 22.0,
                average = 22.4
            ),
            tenYear = HistoricalWindow(
                points = listOf(
                    HistoricalDataPoint(2014, 30.0),
                    HistoricalDataPoint(2015, 28.0),
                    HistoricalDataPoint(2016, 27.0),
                    HistoricalDataPoint(2017, 26.0),
                    HistoricalDataPoint(2018, 25.0),
                    HistoricalDataPoint(2019, 25.0),
                    HistoricalDataPoint(2020, 23.0),
                    HistoricalDataPoint(2021, 22.0),
                    HistoricalDataPoint(2022, 21.5),
                    HistoricalDataPoint(2023, 20.5)
                ),
                median = 25.0,
                average = 24.8
            )
        )
        val pbWindow = HistoricalMetricWindow(
            fiveYear = HistoricalWindow(
                points = listOf(
                    HistoricalDataPoint(2019, 9.0),
                    HistoricalDataPoint(2020, 8.4),
                    HistoricalDataPoint(2021, 7.9),
                    HistoricalDataPoint(2022, 7.8),
                    HistoricalDataPoint(2023, 7.5)
                ),
                median = 7.9,
                average = 8.1
            ),
            tenYear = HistoricalWindow(
                points = listOf(
                    HistoricalDataPoint(2014, 11.0),
                    HistoricalDataPoint(2015, 10.5),
                    HistoricalDataPoint(2016, 10.0),
                    HistoricalDataPoint(2017, 9.5),
                    HistoricalDataPoint(2018, 9.2),
                    HistoricalDataPoint(2019, 9.0),
                    HistoricalDataPoint(2020, 8.4),
                    HistoricalDataPoint(2021, 7.9),
                    HistoricalDataPoint(2022, 7.8),
                    HistoricalDataPoint(2023, 7.5)
                ),
                median = 9.2,
                average = 9.4
            )
        )
        val roeWindow = HistoricalMetricWindow(
            fiveYear = HistoricalWindow(
                points = listOf(
                    HistoricalDataPoint(2019, 0.42),
                    HistoricalDataPoint(2020, 0.38),
                    HistoricalDataPoint(2021, 0.46),
                    HistoricalDataPoint(2022, 0.41),
                    HistoricalDataPoint(2023, 0.43)
                ),
                median = 0.42,
                average = 0.42
            ),
            tenYear = null
        )
        val netMarginWindow = HistoricalMetricWindow(
            fiveYear = HistoricalWindow(
                points = listOf(
                    HistoricalDataPoint(2019, 0.22),
                    HistoricalDataPoint(2020, 0.21),
                    HistoricalDataPoint(2021, 0.23),
                    HistoricalDataPoint(2022, 0.22),
                    HistoricalDataPoint(2023, 0.23)
                ),
                median = 0.22,
                average = 0.22
            ),
            tenYear = null
        )
        val revenueWindow = HistoricalMetricWindow(
            fiveYear = HistoricalWindow(
                points = listOf(
                    HistoricalDataPoint(2019, 2.6E11),
                    HistoricalDataPoint(2020, 2.74E11),
                    HistoricalDataPoint(2021, 2.94E11),
                    HistoricalDataPoint(2022, 3.12E11),
                    HistoricalDataPoint(2023, 3.33E11)
                ),
                median = 2.94E11,
                average = 3.03E11
            ),
            tenYear = HistoricalWindow(
                points = listOf(
                    HistoricalDataPoint(2014, 1.8E11),
                    HistoricalDataPoint(2015, 1.9E11),
                    HistoricalDataPoint(2016, 2.0E11),
                    HistoricalDataPoint(2017, 2.2E11),
                    HistoricalDataPoint(2018, 2.4E11),
                    HistoricalDataPoint(2019, 2.6E11),
                    HistoricalDataPoint(2020, 2.74E11),
                    HistoricalDataPoint(2021, 2.94E11),
                    HistoricalDataPoint(2022, 3.12E11),
                    HistoricalDataPoint(2023, 3.33E11)
                ),
                median = 2.4E11,
                average = 2.53E11
            )
        )
        val epsWindow = HistoricalMetricWindow(
            fiveYear = HistoricalWindow(
                points = listOf(
                    HistoricalDataPoint(2019, 3.0),
                    HistoricalDataPoint(2020, 3.3),
                    HistoricalDataPoint(2021, 4.0),
                    HistoricalDataPoint(2022, 4.5),
                    HistoricalDataPoint(2023, 4.8)
                ),
                median = 4.0,
                average = 3.92
            ),
            tenYear = null
        )
        val fcfWindow = HistoricalMetricWindow(
            fiveYear = HistoricalWindow(
                points = listOf(
                    HistoricalDataPoint(2019, 7.2E10),
                    HistoricalDataPoint(2020, 8.0E10),
                    HistoricalDataPoint(2021, 9.0E10),
                    HistoricalDataPoint(2022, 9.5E10),
                    HistoricalDataPoint(2023, 1.05E11)
                ),
                median = 9.0E10,
                average = 8.96E10
            ),
            tenYear = null
        )

        val snapshot = HistoricalFundamentalSnapshot(
            metrics = mapOf(
                FundamentalMetric.PE_RATIO to peWindow,
                FundamentalMetric.PRICE_TO_BOOK to pbWindow,
                FundamentalMetric.RETURN_ON_EQUITY to roeWindow,
                FundamentalMetric.NET_MARGIN to netMarginWindow,
                FundamentalMetric.REVENUE to revenueWindow,
                FundamentalMetric.EARNINGS_PER_SHARE to epsWindow,
                FundamentalMetric.FREE_CASH_FLOW to fcfWindow
            )
        )

        return NormalizedStockMetrics(
            ticker = "AAPL",
            price = 180.0,
            forwardPe = 22.0,
            trailingPe = 24.0,
            pegRatio = 1.3,
            priceToBook = 7.2,
            dividendRate = 0.92,
            epsForward = 6.5,
            bookValue = 25.0,
            beta = 1.05,
            returnOnEquity = 0.43,
            returnOnAssets = 0.17,
            operatingMargin = 0.27,
            netMargin = 0.23,
            debtToEquity = 1.5,
            sector = "Technology",
            sectorSnapshot = SectorMedianSnapshot(
                sectorName = "Technology",
                metrics = SectorMedianMetrics(
                    priceToEarnings = 25.5,
                    priceToBook = 8.4,
                    returnOnEquity = 0.32,
                    returnOnAssets = 0.11,
                    operatingMargin = 0.21,
                    netMargin = 0.18,
                    debtToEquity = 1.9
                ),
                source = "FMP advanced_sector_statistics",
                fallbackUsed = false
            ),
            historicalFundamentals = snapshot,
            refreshMetadata = RefreshMetadata(
                lastUpdatedMillis = 1_706_995_200_000,
                retries = 1,
                usedFallback = false
            )
        )
    }

    private class FakeAggregator(
        private val metrics: NormalizedStockMetrics
    ) : StockMetricsAggregator {
        override suspend fun aggregate(ticker: String): NormalizedStockMetrics? = metrics
    }
}
