package com.example.bd_finance.data.portfolio

import kotlinx.coroutines.flow.Flow

data class PortfolioSummary(
    val totalValue: Double,
    val totalCostBasis: Double,
    val totalGainLoss: Double,
    val totalGainLossPercent: Double,
    val uniqueStocks: Int
)

class PortfolioRepository(
    private val dao: PortfolioDao
) {
    fun getAllFlow(): Flow<List<PortfolioHolding>> = dao.getAllFlow()

    suspend fun getAll(): List<PortfolioHolding> = dao.getAll()

    suspend fun getById(id: String): PortfolioHolding? = dao.getById(id)

    suspend fun getByTicker(ticker: String): List<PortfolioHolding> = dao.getByTicker(ticker)

    suspend fun add(holding: PortfolioHolding) = dao.insert(holding)

    suspend fun update(holding: PortfolioHolding) = dao.update(holding)

    suspend fun delete(id: String) = dao.deleteById(id)

    suspend fun clear() = dao.deleteAll()

    suspend fun getSummary(): PortfolioSummary {
        val holdings = getAll()
        val totalCostBasis = holdings.sumOf { it.quantity * it.purchasePrice }
        val totalValue = holdings.sumOf { (it.lastPrice ?: it.purchasePrice) * it.quantity }
        val totalGainLoss = totalValue - totalCostBasis
        val totalGainLossPercent = if (totalCostBasis > 0) {
            (totalGainLoss / totalCostBasis) * 100.0
        } else 0.0
        val uniqueStocks = holdings.map { it.ticker }.distinct().size

        return PortfolioSummary(
            totalValue = totalValue,
            totalCostBasis = totalCostBasis,
            totalGainLoss = totalGainLoss,
            totalGainLossPercent = totalGainLossPercent,
            uniqueStocks = uniqueStocks
        )
    }
}
