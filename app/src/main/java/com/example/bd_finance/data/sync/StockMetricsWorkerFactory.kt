package com.example.bd_finance.data.sync

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters

class StockMetricsWorkerFactory(
    private val aggregator: StockMetricsAggregator,
    private val repository: StockMetricsRepository,
    private val tickerProvider: TickerProvider
) : WorkerFactory() {
    override fun createWorker(appContext: Context, workerClassName: String, workerParameters: WorkerParameters): ListenableWorker? {
        return if (workerClassName == DataRefreshWorker::class.qualifiedName) {
            DataRefreshWorker(appContext, workerParameters, aggregator, repository, tickerProvider)
        } else {
            null
        }
    }
}
