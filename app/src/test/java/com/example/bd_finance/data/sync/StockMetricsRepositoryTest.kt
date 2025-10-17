package com.example.bd_finance.data.sync

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class StockMetricsRepositoryTest {

    private lateinit var context: Context
    private lateinit var database: StockMetricsDatabase
    private lateinit var repository: StockMetricsRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, StockMetricsDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = RoomStockMetricsRepository(database.stockMetricsDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `save and retrieve normalized metrics`() = runTest {
        val metrics = sampleMetrics("AAPL", lastUpdated = 1_000L)

        repository.save(metrics)

        val latest = repository.observe("AAPL").first()
        assertEquals(metrics, latest)
    }

    @Test
    fun `latest snapshot returns map of stored metrics`() = runTest {
        val aapl = sampleMetrics("AAPL", lastUpdated = 2_000L)
        val msft = sampleMetrics("MSFT", lastUpdated = 3_000L)

        repository.save(aapl)
        repository.save(msft)

        val snapshot = repository.snapshot().first()
        assertEquals(2, snapshot.size)
        assertEquals(aapl, snapshot["AAPL"])
        assertEquals(msft, snapshot["MSFT"])
    }

    private fun sampleMetrics(ticker: String, lastUpdated: Long): NormalizedStockMetrics {
        val sectorSnapshot = SectorMedianSnapshot(
            sectorName = "Technology",
            metrics = SectorMedianMetrics(
                priceToEarnings = 22.5,
                priceToBook = 4.7,
                returnOnEquity = 0.18,
                returnOnAssets = 0.09,
                operatingMargin = 0.24,
                netMargin = 0.19,
                debtToEquity = 0.42
            ),
            source = "test",
            fallbackUsed = false
        )
        val historical = HistoricalFundamentalSnapshot(
            metrics = mapOf(
                FundamentalMetric.PE_RATIO to HistoricalMetricWindow(
                    fiveYear = HistoricalWindow(
                        points = listOf(
                            HistoricalDataPoint(2019, 24.0),
                            HistoricalDataPoint(2020, 26.0),
                            HistoricalDataPoint(2021, 28.0),
                            HistoricalDataPoint(2022, 27.0),
                            HistoricalDataPoint(2023, 25.0)
                        ),
                        median = 26.0,
                        average = 26.0
                    ),
                    tenYear = null
                )
            )
        )
        return NormalizedStockMetrics(
            ticker = ticker,
            price = 180.0,
            forwardPe = 25.0,
            trailingPe = 28.4,
            pegRatio = 1.6,
            priceToBook = 4.8,
            dividendRate = 0.92,
            epsForward = 6.12,
            bookValue = 31.4,
            beta = 1.08,
            sector = "Technology",
            sectorSnapshot = sectorSnapshot,
            historicalFundamentals = historical,
            refreshMetadata = RefreshMetadata(
                lastUpdatedMillis = lastUpdated,
                retries = 2,
                usedFallback = false
            )
        )
    }
}

