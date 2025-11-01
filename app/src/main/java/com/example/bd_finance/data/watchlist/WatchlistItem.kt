package com.example.bd_finance.data.watchlist

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.bd_finance.data.model.StockVerdict
import java.time.Instant

@Entity(tableName = "watchlist")
data class WatchlistItem(
    @PrimaryKey
    val ticker: String,
    val companyName: String?,
    val addedDate: Instant,
    val lastPrice: Double?,
    val lastPriceChange: Double?,
    val lastRecommendation: StockVerdict?,
    val lastUpdated: Instant?
)
