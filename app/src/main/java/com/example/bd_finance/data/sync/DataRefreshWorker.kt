package com.example.bd_finance.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DataRefreshWorker(
    appContext: Context,
    params: WorkerParameters,
    private val aggregator: StockMetricsAggregator,
    private val repository: StockMetricsRepository,
    private val tickerProvider: TickerProvider
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val tickers = tickerProvider.tickers()
        tickers.forEach { ticker ->
            val metrics = aggregator.aggregate(ticker) ?: return@forEach
            repository.save(metrics)
        }
        Result.success()
    }
}
