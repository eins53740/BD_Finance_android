package com.example.bd_finance.data.sync

import android.content.Context
import android.content.SharedPreferences

interface TickerProvider {
    fun tickers(): List<String>
    fun addTicker(ticker: String)
}

class DefaultTickerProvider(
    private val prefs: SharedPreferences
) : TickerProvider {

    constructor(context: Context) : this(
        context.getSharedPreferences("bd_finance_tickers", Context.MODE_PRIVATE)
    )

    override fun tickers(): List<String> {
        val stored = prefs.getStringSet(KEY_TICKERS, null)
        return stored?.sorted() ?: DEFAULT_TICKERS
    }

    override fun addTicker(ticker: String) {
        val normalized = ticker.trim().uppercase()
        if (normalized.isBlank()) return
        val current = prefs.getStringSet(KEY_TICKERS, null)?.toMutableSet() ?: DEFAULT_TICKERS.toMutableSet()
        current.add(normalized)
        prefs.edit().putStringSet(KEY_TICKERS, current).apply()
    }

    companion object {
        private const val KEY_TICKERS = "watched_tickers"
        private val DEFAULT_TICKERS = listOf("AAPL", "MSFT", "GOOGL")
    }
}
