package com.example.bd_finance.data.watchlist

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchlistDao {
    @Query("SELECT * FROM watchlist ORDER BY addedDate DESC")
    fun getAllFlow(): Flow<List<WatchlistItem>>

    @Query("SELECT * FROM watchlist ORDER BY addedDate DESC")
    suspend fun getAll(): List<WatchlistItem>

    @Query("SELECT * FROM watchlist WHERE ticker = :ticker")
    suspend fun getByTicker(ticker: String): WatchlistItem?

    @Query("SELECT EXISTS(SELECT 1 FROM watchlist WHERE ticker = :ticker)")
    suspend fun exists(ticker: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: WatchlistItem)

    @Update
    suspend fun update(item: WatchlistItem)

    @Delete
    suspend fun delete(item: WatchlistItem)

    @Query("DELETE FROM watchlist WHERE ticker = :ticker")
    suspend fun deleteByTicker(ticker: String)

    @Query("DELETE FROM watchlist")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM watchlist")
    suspend fun getCount(): Int
}
