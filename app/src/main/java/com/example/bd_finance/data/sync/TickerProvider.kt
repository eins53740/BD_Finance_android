package com.example.bd_finance.data.sync

interface TickerProvider {
    fun tickers(): List<String>
}

class DefaultTickerProvider : TickerProvider {
    override fun tickers(): List<String> = listOf("AAPL", "MSFT", "GOOGL")
}
