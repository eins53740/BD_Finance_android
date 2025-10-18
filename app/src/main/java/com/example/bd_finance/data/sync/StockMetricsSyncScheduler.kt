package com.example.bd_finance.data.sync

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object StockMetricsSyncScheduler {
    private const val UNIQUE_WORK_NAME = "stock_metrics_refresh"

    fun schedule(context: Context, repeatIntervalHours: Long = 12L) {
        val request = PeriodicWorkRequestBuilder<DataRefreshWorker>(repeatIntervalHours, TimeUnit.HOURS)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}
