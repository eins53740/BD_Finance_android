package com.example.bd_finance.data.sync

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.testing.WorkManagerTestInitHelper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class DataRefreshWorkerTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        WorkManagerTestInitHelper.initializeTestWorkManager(context)
    }

    @Test
    fun `worker triggers aggregator for provided tickers`() = runTest {
        val tickers = listOf("AAPL", "MSFT", "AMZN")
        val aggregator = object : StockMetricsAggregator {
            override suspend fun aggregate(ticker: String): NormalizedStockMetrics? {
                return NormalizedStockMetrics(
                    ticker = ticker,
                    price = 100.0,
                    forwardPe = 20.0,
                    trailingPe = 22.0,
                    pegRatio = 1.5,
                    priceToBook = 4.0,
                    dividendRate = 1.0,
                    epsForward = 5.0,
                    bookValue = 20.0,
                    beta = 1.0,
                    sector = null,
                    sectorSnapshot = null,
                    historicalFundamentals = null,
                    refreshMetadata = RefreshMetadata(lastUpdatedMillis = 0L, retries = 0, usedFallback = false)
                )
            }
        }
        val repository = InMemoryRepository()
        val tickerProvider = object : TickerProvider {
            override fun tickers(): List<String> = tickers
        }

        val worker = TestListenableWorkerBuilder<DataRefreshWorker>(context)
            .setWorkerFactory(TestWorkerFactory(aggregator, repository, tickerProvider))
            .build()

        val result = worker.startWork().get()
        assertEquals(ListenableWorker.Result.success()::class, result::class)
        val snapshot = repository.snapshotFlow.value
        assertEquals(tickers.size, snapshot.size)
        tickers.forEach { assertEquals(it, snapshot[it]?.ticker) }
    }

    private class InMemoryRepository : StockMetricsRepository {
        val snapshotFlow = MutableStateFlow<Map<String, NormalizedStockMetrics>>(emptyMap())
        override suspend fun save(metrics: NormalizedStockMetrics) {
            snapshotFlow.value = snapshotFlow.value + (metrics.ticker to metrics)
        }
        override suspend fun get(ticker: String): NormalizedStockMetrics? = snapshotFlow.value[ticker]
        override fun observe(ticker: String): Flow<NormalizedStockMetrics?> = snapshotFlow.map { it[ticker] }
        override fun snapshot(): Flow<Map<String, NormalizedStockMetrics>> = snapshotFlow
    }

    private class TestWorkerFactory(
        private val aggregator: StockMetricsAggregator,
        private val repository: StockMetricsRepository,
        private val tickerProvider: TickerProvider
    ) : androidx.work.WorkerFactory() {
        override fun createWorker(appContext: Context, workerClassName: String, params: WorkerParameters): ListenableWorker? {
            return DataRefreshWorker(appContext, params, aggregator, repository, tickerProvider)
        }
    }
}


