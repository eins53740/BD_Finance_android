package com.example.bd_finance.data.sync

import android.content.Context
import androidx.room.Room
import com.example.bd_finance.BuildConfig
import com.example.bd_finance.data.network.YahooFinanceClient
import okhttp3.OkHttpClient
import kotlin.collections.buildList

object StockMetricsSyncModule {

    fun provideDatabase(context: Context): StockMetricsDatabase =
        Room.databaseBuilder(context, StockMetricsDatabase::class.java, "stock_metrics.db")
            .addMigrations(*StockMetricsMigrations.ALL)
            .build()

    fun provideRepository(database: StockMetricsDatabase): StockMetricsRepository =
        RoomStockMetricsRepository(database.stockMetricsDao())

    fun provideAggregator(okHttpClient: OkHttpClient, yahooClient: YahooFinanceClient): StockMetricsAggregator {
        val connectors = buildList {
            add(YahooFinanceMetricsConnector(yahooClient))
            fmpConnector(okHttpClient)?.let { add(it) }
            alphaVantageMetricsConnector(okHttpClient)?.let { add(it) }
            alphaVantageSectorConnector(okHttpClient)?.let { add(it) }
        }
        return DefaultStockMetricsAggregator(connectors)
    }

    fun provideTickerProvider(): TickerProvider = DefaultTickerProvider()

    fun provideWorkerFactory(
        context: Context,
        okHttpClient: OkHttpClient,
        yahooClient: YahooFinanceClient
    ): StockMetricsWorkerFactory {
        val database = provideDatabase(context)
        val repository = provideRepository(database)
        val aggregator = provideAggregator(okHttpClient, yahooClient)
        val tickerProvider = provideTickerProvider()
        return StockMetricsWorkerFactory(aggregator, repository, tickerProvider)
    }

    private fun alphaVantageMetricsConnector(client: OkHttpClient): StockMetricsConnector? {
        return if (BuildConfig.ALPHA_VANTAGE_API_KEY.isBlank()) {
            null
        } else {
            AlphaVantageMetricsConnector(client, BuildConfig.ALPHA_VANTAGE_API_KEY)
        }
    }

    private fun alphaVantageSectorConnector(client: OkHttpClient): StockMetricsConnector? =
        if (BuildConfig.ALPHA_VANTAGE_API_KEY.isBlank()) {
            null
        } else {
            AlphaVantageSectorConnector(client, BuildConfig.ALPHA_VANTAGE_API_KEY)
        }

    private fun fmpConnector(client: OkHttpClient): StockMetricsConnector? =
        if (BuildConfig.FMP_API_KEY.isBlank()) {
            null
        } else {
            FmpFundamentalConnector(client, BuildConfig.FMP_API_KEY)
        }
}
