package com.example.bd_finance

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import com.example.bd_finance.ui.StockEvaluatorRoot
import com.example.bd_finance.ui.theme.BD_FinanceTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BD_FinanceTheme {
                Surface {
                    StockEvaluatorRoot()
                }
            }
        }
    }
}
