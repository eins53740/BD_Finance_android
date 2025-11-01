package com.example.bd_finance.data.sync

import android.content.Context
import androidx.room.Room
import com.example.bd_finance.BuildConfig
import com.example.bd_finance.data.BDFinanceDatabase
import com.example.bd_finance.data.DatabaseMigrations
import com.example.bd_finance.data.network.YahooFinanceClient
import com.example.bd_finance.data.portfolio.PortfolioRepository
import com.example.bd_finance.data.watchlist.WatchlistRepository
import okhttp3.OkHttpClient
import kotlin.collections.buildList

object StockMetricsSyncModule {

    fun provideDatabase(context: Context): BDFinanceDatabase =
        Room.databaseBuilder(context, BDFinanceDatabase::class.java, "bd_finance.db")
            .addMigrations(*StockMetricsMigrations.ALL, *DatabaseMigrations.ALL)
            .fallbackToDestructiveMigration() // For development only
            .build()

    fun provideRepository(database: BDFinanceDatabase): StockMetricsRepository =
        RoomStockMetricsRepository(database.stockMetricsDao())

    fun provideWatchlistRepository(database: BDFinanceDatabase): WatchlistRepository =
        WatchlistRepository(database.watchlistDao())

    fun providePortfolioRepository(database: BDFinanceDatabase): PortfolioRepository =
        PortfolioRepository(database.portfolioDao())

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
