package com.example.bd_finance.data.portfolio

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PortfolioDao {
    @Query("SELECT * FROM portfolio ORDER BY purchaseDate DESC")
    fun getAllFlow(): Flow<List<PortfolioHolding>>

    @Query("SELECT * FROM portfolio ORDER BY purchaseDate DESC")
    suspend fun getAll(): List<PortfolioHolding>

    @Query("SELECT * FROM portfolio WHERE id = :id")
    suspend fun getById(id: String): PortfolioHolding?

    @Query("SELECT * FROM portfolio WHERE ticker = :ticker")
    suspend fun getByTicker(ticker: String): List<PortfolioHolding>

    @Query("SELECT SUM(quantity * purchasePrice) FROM portfolio")
    suspend fun getTotalCostBasis(): Double?

    @Query("SELECT COUNT(DISTINCT ticker) FROM portfolio")
    suspend fun getUniqueTickerCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(holding: PortfolioHolding)

    @Update
    suspend fun update(holding: PortfolioHolding)

    @Delete
    suspend fun delete(holding: PortfolioHolding)

    @Query("DELETE FROM portfolio WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM portfolio")
    suspend fun deleteAll()
}
