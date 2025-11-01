package com.example.bd_finance.data.watchlist

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.example.bd_finance.data.StockAnalysisRepository

class WatchlistWorkerFactory(
    private val repository: WatchlistRepository,
    private val analysisRepository: StockAnalysisRepository
) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return when (workerClassName) {
            WatchlistSyncWorker::class.java.name -> {
                WatchlistSyncWorker(
                    appContext,
                    workerParameters,
                    repository,
                    analysisRepository
                )
            }
            else -> null
        }
    }
}
