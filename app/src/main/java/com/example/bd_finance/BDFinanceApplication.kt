package com.example.bd_finance

import android.app.Application
import androidx.work.Configuration
import com.example.bd_finance.data.CombinedWorkerFactory
import com.example.bd_finance.data.StockAnalysisRepository
import com.example.bd_finance.data.network.YahooFinanceClient
import com.example.bd_finance.data.sync.StockMetricsSyncModule
import com.example.bd_finance.data.sync.StockMetricsSyncScheduler
import com.example.bd_finance.data.sync.StockMetricsWorkerFactory
import com.example.bd_finance.data.watchlist.WatchlistSyncScheduler
import com.example.bd_finance.data.watchlist.WatchlistWorkerFactory
import okhttp3.OkHttpClient

class BDFinanceApplication : Application(), Configuration.Provider {

    private val okHttpClient by lazy { OkHttpClient.Builder().build() }
    private val yahooClient by lazy { YahooFinanceClient(okHttpClient) }
    private val database by lazy { StockMetricsSyncModule.provideDatabase(this) }
    private val stockMetricsRepository by lazy { StockMetricsSyncModule.provideRepository(database) }
    private val aggregator by lazy { StockMetricsSyncModule.provideAggregator(okHttpClient, yahooClient) }
    private val tickerProvider by lazy { StockMetricsSyncModule.provideTickerProvider() }

    // Watchlist repositories
    private val watchlistRepository by lazy { StockMetricsSyncModule.provideWatchlistRepository(database) }
    private val analysisRepository by lazy { StockAnalysisRepository.default() }

    // Worker factories
    private val stockMetricsWorkerFactory by lazy {
        StockMetricsWorkerFactory(aggregator, stockMetricsRepository, tickerProvider)
    }
    private val watchlistWorkerFactory by lazy {
        WatchlistWorkerFactory(watchlistRepository, analysisRepository)
    }
    private val combinedWorkerFactory by lazy {
        CombinedWorkerFactory(listOf(stockMetricsWorkerFactory, watchlistWorkerFactory))
    }

    override fun onCreate() {
        super.onCreate()
        StockMetricsSyncScheduler.schedule(this)
        WatchlistSyncScheduler.schedule(this)
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(combinedWorkerFactory)
            .build()
}
