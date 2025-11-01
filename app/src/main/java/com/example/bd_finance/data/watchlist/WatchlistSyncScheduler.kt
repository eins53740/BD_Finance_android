package com.example.bd_finance.data.watchlist

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

object WatchlistSyncScheduler {

    fun schedule(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<WatchlistSyncWorker>(
            repeatInterval = 6, // Sync every 6 hours
            repeatIntervalTimeUnit = TimeUnit.HOURS,
            flexTimeInterval = 1, // Allow 1 hour of flexibility
            flexTimeIntervalUnit = TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WatchlistSyncWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context)
            .cancelUniqueWork(WatchlistSyncWorker.WORK_NAME)
    }
}
