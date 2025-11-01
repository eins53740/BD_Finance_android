package com.example.bd_finance.data.watchlist

import kotlinx.coroutines.flow.Flow

class WatchlistRepository(
    private val dao: WatchlistDao
) {
    fun getAllFlow(): Flow<List<WatchlistItem>> = dao.getAllFlow()

    suspend fun getAll(): List<WatchlistItem> = dao.getAll()

    suspend fun getByTicker(ticker: String): WatchlistItem? = dao.getByTicker(ticker)

    suspend fun exists(ticker: String): Boolean = dao.exists(ticker)

    suspend fun add(item: WatchlistItem) = dao.insert(item)

    suspend fun remove(ticker: String) = dao.deleteByTicker(ticker)

    suspend fun update(item: WatchlistItem) = dao.update(item)

    suspend fun getCount(): Int = dao.getCount()

    suspend fun clear() = dao.deleteAll()
}
