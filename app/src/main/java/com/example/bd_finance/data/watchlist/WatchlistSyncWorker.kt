package com.example.bd_finance.data.watchlist

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.bd_finance.data.StockAnalysisRepository
import java.time.Instant

class WatchlistSyncWorker(
    context: Context,
    params: WorkerParameters,
    private val repository: WatchlistRepository,
    private val analysisRepository: StockAnalysisRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            Log.d(TAG, "Starting watchlist sync")
            val items = repository.getAll()

            if (items.isEmpty()) {
                Log.d(TAG, "No watchlist items to sync")
                return Result.success()
            }

            var successCount = 0
            var failureCount = 0

            items.forEach { item ->
                try {
                    val analysis = analysisRepository.analyze(item.ticker)
                    repository.update(
                        item.copy(
                            companyName = analysis.summary.companyName ?: item.companyName,
                            lastPrice = analysis.summary.price,
                            lastPriceChange = analysis.summary.changePercent,
                            lastRecommendation = analysis.summary.verdict,
                            lastUpdated = Instant.now()
                        )
                    )
                    successCount++
                    Log.d(TAG, "Updated ${item.ticker}")
                } catch (e: Exception) {
                    failureCount++
                    Log.w(TAG, "Failed to update ${item.ticker}: ${e.message}")
                }
            }

            Log.d(TAG, "Watchlist sync completed: $successCount succeeded, $failureCount failed")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Watchlist sync failed", e)
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "WatchlistSyncWorker"
        const val WORK_NAME = "watchlist_sync"
    }
}
