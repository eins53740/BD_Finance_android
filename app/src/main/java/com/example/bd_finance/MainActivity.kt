package com.example.bd_finance

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.bd_finance.ui.BDFinanceApp
import com.example.bd_finance.ui.theme.BD_FinanceTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BD_FinanceTheme {
                BDFinanceApp()
            }
        }
    }
}
