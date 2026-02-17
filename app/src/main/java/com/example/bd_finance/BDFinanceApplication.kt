package com.example.bd_finance

import android.app.Application
import androidx.work.Configuration
import com.example.bd_finance.data.network.YahooFinanceClient
import com.example.bd_finance.data.sync.StockMetricsSyncModule
import com.example.bd_finance.data.sync.StockMetricsSyncScheduler
import com.example.bd_finance.data.sync.StockMetricsWorkerFactory
import okhttp3.JavaNetCookieJar
import okhttp3.OkHttpClient
import java.net.CookieManager
import java.net.CookiePolicy

class BDFinanceApplication : Application(), Configuration.Provider {

    private val okHttpClient by lazy {
        val cookieManager = CookieManager().apply {
            setCookiePolicy(CookiePolicy.ACCEPT_ALL)
        }
        OkHttpClient.Builder()
            .cookieJar(JavaNetCookieJar(cookieManager))
            .build()
    }
    private val yahooClient by lazy { YahooFinanceClient(okHttpClient) }
    private val database by lazy { StockMetricsSyncModule.provideDatabase(this) }
    private val repository by lazy { StockMetricsSyncModule.provideRepository(database) }
    private val aggregator by lazy { StockMetricsSyncModule.provideAggregator(okHttpClient, yahooClient) }
    private val tickerProvider by lazy { StockMetricsSyncModule.provideTickerProvider(this) }
    private val workerFactory by lazy { StockMetricsWorkerFactory(aggregator, repository, tickerProvider) }

    override fun onCreate() {
        super.onCreate()
        StockMetricsSyncScheduler.schedule(this)
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
