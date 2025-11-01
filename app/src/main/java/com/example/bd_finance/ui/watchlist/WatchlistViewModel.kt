package com.example.bd_finance.ui.watchlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.bd_finance.data.StockAnalysisRepository
import com.example.bd_finance.data.model.StockVerdict
import com.example.bd_finance.data.watchlist.WatchlistItem
import com.example.bd_finance.data.watchlist.WatchlistRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant

sealed interface WatchlistUiState {
    data object Loading : WatchlistUiState
    data class Success(val items: List<WatchlistItemUi>) : WatchlistUiState
    data class Error(val message: String) : WatchlistUiState
}

data class WatchlistItemUi(
    val ticker: String,
    val companyName: String?,
    val addedDate: Instant,
    val lastPrice: Double?,
    val lastPriceChange: Double?,
    val lastRecommendation: StockVerdict?,
    val lastUpdated: Instant?
)

class WatchlistViewModel(
    private val repository: WatchlistRepository,
    private val analysisRepository: StockAnalysisRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<WatchlistUiState>(WatchlistUiState.Loading)
    val uiState: StateFlow<WatchlistUiState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAllFlow().collect { items ->
                _uiState.value = WatchlistUiState.Success(items.map { it.toUi() })
            }
        }
    }

    fun removeItem(ticker: String) {
        viewModelScope.launch {
            repository.remove(ticker)
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val items = repository.getAll()
                items.forEach { item ->
                    try {
                        val analysis = analysisRepository.analyze(item.ticker)
                        repository.update(
                            item.copy(
                                lastPrice = analysis.summary.price,
                                lastPriceChange = analysis.summary.changePercent,
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

    private fun WatchlistItem.toUi() = WatchlistItemUi(
        ticker = ticker,
        companyName = companyName,
        addedDate = addedDate,
        lastPrice = lastPrice,
        lastPriceChange = lastPriceChange,
        lastRecommendation = lastRecommendation,
        lastUpdated = lastUpdated
    )
}

class WatchlistViewModelFactory(
    private val repository: WatchlistRepository,
    private val analysisRepository: StockAnalysisRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WatchlistViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WatchlistViewModel(repository, analysisRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
