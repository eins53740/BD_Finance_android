package com.example.bd_finance.data.portfolio

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.bd_finance.data.model.StockVerdict
import java.time.Instant
import java.util.UUID

@Entity(tableName = "portfolio")
data class PortfolioHolding(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val ticker: String,
    val companyName: String?,
    val quantity: Double,
    val purchasePrice: Double,
    val purchaseDate: Instant,
    val notes: String?,
    val lastPrice: Double?,
    val lastRecommendation: StockVerdict?,
    val lastUpdated: Instant?,
    val currency: String = "USD"
)
