package com.example.bd_finance.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.bd_finance.data.portfolio.PortfolioDao
import com.example.bd_finance.data.portfolio.PortfolioHolding
import com.example.bd_finance.data.sync.StockMetricsDao
import com.example.bd_finance.data.sync.StockMetricsEntity
import com.example.bd_finance.data.watchlist.WatchlistDao
import com.example.bd_finance.data.watchlist.WatchlistItem

@Database(
    entities = [
        StockMetricsEntity::class,
        WatchlistItem::class,
        PortfolioHolding::class
    ],
    version = 4,
    exportSchema = false
)
@TypeConverters(RoomConverters::class)
abstract class BDFinanceDatabase : RoomDatabase() {
    abstract fun stockMetricsDao(): StockMetricsDao
    abstract fun watchlistDao(): WatchlistDao
    abstract fun portfolioDao(): PortfolioDao
}
