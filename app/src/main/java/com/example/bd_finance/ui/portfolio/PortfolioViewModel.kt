package com.example.bd_finance.ui.portfolio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.bd_finance.data.StockAnalysisRepository
import com.example.bd_finance.data.model.StockVerdict
import com.example.bd_finance.data.portfolio.PortfolioHolding
import com.example.bd_finance.data.portfolio.PortfolioRepository
import com.example.bd_finance.data.portfolio.PortfolioSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant

sealed interface PortfolioUiState {
    data object Loading : PortfolioUiState
    data class Success(
        val summary: PortfolioSummary,
        val holdings: List<PortfolioHoldingUi>
    ) : PortfolioUiState
    data class Error(val message: String) : PortfolioUiState
}

data class PortfolioHoldingUi(
    val id: String,
    val ticker: String,
    val companyName: String?,
    val quantity: Double,
    val purchasePrice: Double,
    val purchaseDate: Instant,
    val currentPrice: Double,
    val currentValue: Double,
    val costBasis: Double,
    val gainLoss: Double,
    val gainLossPercent: Double,
    val lastRecommendation: StockVerdict?,
    val currency: String
)

class PortfolioViewModel(
    private val repository: PortfolioRepository,
    private val analysisRepository: StockAnalysisRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<PortfolioUiState>(PortfolioUiState.Loading)
    val uiState: StateFlow<PortfolioUiState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAllFlow().collect { holdings ->
                refreshState(holdings)
            }
        }
    }

    private suspend fun refreshState(holdings: List<PortfolioHolding>) {
        val summary = repository.getSummary()
        val holdingsUi = holdings.map { holding ->
            val currentPrice = holding.lastPrice ?: holding.purchasePrice
            val currentValue = currentPrice * holding.quantity
            val costBasis = holding.purchasePrice * holding.quantity
            val gainLoss = currentValue - costBasis
            val gainLossPercent = if (costBasis > 0) (gainLoss / costBasis) * 100.0 else 0.0

            PortfolioHoldingUi(
                id = holding.id,
                ticker = holding.ticker,
                companyName = holding.companyName,
                quantity = holding.quantity,
                purchasePrice = holding.purchasePrice,
                purchaseDate = holding.purchaseDate,
                currentPrice = currentPrice,
                currentValue = currentValue,
                costBasis = costBasis,
                gainLoss = gainLoss,
                gainLossPercent = gainLossPercent,
                lastRecommendation = holding.lastRecommendation,
                currency = holding.currency
            )
        }
        _uiState.value = PortfolioUiState.Success(summary, holdingsUi)
    }

    fun addHolding(holding: PortfolioHolding) {
        viewModelScope.launch {
            repository.add(holding)
        }
    }

    fun deleteHolding(id: String) {
        viewModelScope.launch {
            repository.delete(id)
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val holdings = repository.getAll()
                holdings.forEach { holding ->
                    try {
                        val analysis = analysisRepository.analyze(holding.ticker)
                        repository.update(
                            holding.copy(
                                companyName = analysis.summary.companyName,
                                lastPrice = analysis.summary.price,
                                lastRecommendation = analysis.summary.verdict,
                                lastUpdated = Instant.now()
                            )
                        )
                    } catch (e: Exception) {
                        // Skip failed updates
                    }
                }
            } finally {
                _isRefreshing.value = false
            }
        }
    }
}

class PortfolioViewModelFactory(
    private val repository: PortfolioRepository,
    private val analysisRepository: StockAnalysisRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PortfolioViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PortfolioViewModel(repository, analysisRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
