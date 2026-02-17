package com.example.bd_finance.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.bd_finance.data.StockAnalysisRepository
import com.example.bd_finance.data.model.StockAnalysis
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.net.UnknownHostException

sealed interface StockEvaluatorUiState {
    data object Idle : StockEvaluatorUiState
    data class Loading(val ticker: String) : StockEvaluatorUiState
    data class Success(val analysis: StockAnalysis) : StockEvaluatorUiState
    data class Error(val ticker: String, val message: String) : StockEvaluatorUiState
}

class StockEvaluatorViewModel(
    private val repository: StockAnalysisRepository
) : ViewModel() {

    private val _uiState: MutableStateFlow<StockEvaluatorUiState> =
        MutableStateFlow(StockEvaluatorUiState.Idle)
    val uiState: StateFlow<StockEvaluatorUiState> = _uiState.asStateFlow()

    fun analyzeTicker(ticker: String) {
        val normalized = ticker.trim()
        if (normalized.isEmpty()) {
            _uiState.value = StockEvaluatorUiState.Error("", "Ticker is required")
            return
        }
        _uiState.value = StockEvaluatorUiState.Loading(normalized.uppercase())
        viewModelScope.launch {
            _uiState.value = try {
                val result = repository.analyze(normalized)
                StockEvaluatorUiState.Success(result)
            } catch (ex: Exception) {
                val message = when (ex) {
                    is UnknownHostException -> "No internet connection. Check your network and try again."
                    is java.net.SocketTimeoutException -> "Request timed out. Try again in a moment."
                    is java.io.IOException -> "Network error: ${ex.message}"
                    else -> ex.message ?: "Unexpected error"
                }
                StockEvaluatorUiState.Error(
                    ticker = normalized.uppercase(),
                    message = message
                )
            }
        }
    }

    fun retry() {
        val current = _uiState.value
        if (current is StockEvaluatorUiState.Error) {
            analyzeTicker(current.ticker)
        }
    }
}

class StockEvaluatorViewModelFactory(
    private val repository: StockAnalysisRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StockEvaluatorViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StockEvaluatorViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
