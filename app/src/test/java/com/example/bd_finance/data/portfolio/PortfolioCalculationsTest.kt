package com.example.bd_finance.data.portfolio

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

class PortfolioCalculationsTest {

    @Test
    fun `calculate gain for profitable holding`() {
        // Arrange: Buy 10 shares at $100, now worth $150
        val holding = PortfolioHolding(
            ticker = "AAPL",
            companyName = "Apple Inc.",
            quantity = 10.0,
            purchasePrice = 100.0,
            purchaseDate = Instant.now(),
            notes = null,
            lastPrice = 150.0,
            lastRecommendation = null,
            lastUpdated = Instant.now()
        )

        // Act
        val costBasis = holding.quantity * holding.purchasePrice
        val currentValue = (holding.lastPrice ?: holding.purchasePrice) * holding.quantity
        val gainLoss = currentValue - costBasis
        val gainLossPercent = (gainLoss / costBasis) * 100.0

        // Assert
        assertEquals(1000.0, costBasis, 0.01)
        assertEquals(1500.0, currentValue, 0.01)
        assertEquals(500.0, gainLoss, 0.01)
        assertEquals(50.0, gainLossPercent, 0.01)
    }

    @Test
    fun `calculate loss for losing holding`() {
        // Arrange: Buy 5 shares at $200, now worth $180
        val holding = PortfolioHolding(
            ticker = "MSFT",
            companyName = "Microsoft",
            quantity = 5.0,
            purchasePrice = 200.0,
            purchaseDate = Instant.now(),
            notes = null,
            lastPrice = 180.0,
            lastRecommendation = null,
            lastUpdated = Instant.now()
        )

        // Act
        val costBasis = holding.quantity * holding.purchasePrice
        val currentValue = (holding.lastPrice ?: holding.purchasePrice) * holding.quantity
        val gainLoss = currentValue - costBasis
        val gainLossPercent = (gainLoss / costBasis) * 100.0

        // Assert
        assertEquals(1000.0, costBasis, 0.01)
        assertEquals(900.0, currentValue, 0.01)
        assertEquals(-100.0, gainLoss, 0.01)
        assertEquals(-10.0, gainLossPercent, 0.01)
    }

    @Test
    fun `portfolio summary with multiple holdings`() {
        // Arrange
        val holdings = listOf(
            PortfolioHolding(
                ticker = "AAPL",
                companyName = "Apple",
                quantity = 10.0,
                purchasePrice = 100.0,
                purchaseDate = Instant.now(),
                notes = null,
                lastPrice = 150.0,
                lastRecommendation = null,
                lastUpdated = null
            ),
            PortfolioHolding(
                ticker = "MSFT",
                companyName = "Microsoft",
                quantity = 5.0,
                purchasePrice = 200.0,
                purchaseDate = Instant.now(),
                notes = null,
                lastPrice = 180.0,
                lastRecommendation = null,
                lastUpdated = null
            )
        )

        // Act
        val totalCostBasis = holdings.sumOf { it.quantity * it.purchasePrice }
        val totalValue = holdings.sumOf { (it.lastPrice ?: it.purchasePrice) * it.quantity }
        val totalGainLoss = totalValue - totalCostBasis
        val totalGainLossPercent = (totalGainLoss / totalCostBasis) * 100.0
        val uniqueStocks = holdings.map { it.ticker }.distinct().size

        // Assert
        assertEquals(2000.0, totalCostBasis, 0.01)
        assertEquals(2400.0, totalValue, 0.01)
        assertEquals(400.0, totalGainLoss, 0.01)
        assertEquals(20.0, totalGainLossPercent, 0.01)
        assertEquals(2, uniqueStocks)
    }

    @Test
    fun `portfolio with no price updates uses purchase price`() {
        // Arrange: New holding with no lastPrice
        val holding = PortfolioHolding(
            ticker = "GOOGL",
            companyName = "Alphabet",
            quantity = 3.0,
            purchasePrice = 120.0,
            purchaseDate = Instant.now(),
            notes = null,
            lastPrice = null,
            lastRecommendation = null,
            lastUpdated = null
        )

        // Act
        val costBasis = holding.quantity * holding.purchasePrice
        val currentValue = (holding.lastPrice ?: holding.purchasePrice) * holding.quantity
        val gainLoss = currentValue - costBasis

        // Assert
        assertEquals(360.0, costBasis, 0.01)
        assertEquals(360.0, currentValue, 0.01)
        assertEquals(0.0, gainLoss, 0.01)
    }

    @Test
    fun `fractional shares calculate correctly`() {
        // Arrange: Buy 2.5 shares at $100.50
        val holding = PortfolioHolding(
            ticker = "TSLA",
            companyName = "Tesla",
            quantity = 2.5,
            purchasePrice = 100.50,
            purchaseDate = Instant.now(),
            notes = null,
            lastPrice = 120.75,
            lastRecommendation = null,
            lastUpdated = null
        )

        // Act
        val costBasis = holding.quantity * holding.purchasePrice
        val currentValue = (holding.lastPrice ?: holding.purchasePrice) * holding.quantity
        val gainLoss = currentValue - costBasis
        val gainLossPercent = (gainLoss / costBasis) * 100.0

        // Assert
        assertEquals(251.25, costBasis, 0.01)
        assertEquals(301.875, currentValue, 0.01)
        assertEquals(50.625, gainLoss, 0.01)
        assertEquals(20.15, gainLossPercent, 0.01)
    }

    @Test
    fun `empty portfolio has zero values`() {
        // Arrange
        val holdings = emptyList<PortfolioHolding>()

        // Act
        val totalCostBasis = holdings.sumOf { it.quantity * it.purchasePrice }
        val totalValue = holdings.sumOf { (it.lastPrice ?: it.purchasePrice) * it.quantity }
        val totalGainLoss = totalValue - totalCostBasis
        val uniqueStocks = holdings.map { it.ticker }.distinct().size

        // Assert
        assertEquals(0.0, totalCostBasis, 0.01)
        assertEquals(0.0, totalValue, 0.01)
        assertEquals(0.0, totalGainLoss, 0.01)
        assertEquals(0, uniqueStocks)
    }
}
