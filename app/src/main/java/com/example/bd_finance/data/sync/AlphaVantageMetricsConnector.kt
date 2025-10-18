package com.example.bd_finance.data.sync

import com.example.bd_finance.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class AlphaVantageMetricsConnector(
    private val client: OkHttpClient,
    private val apiKey: String = BuildConfig.ALPHA_VANTAGE_API_KEY
) : StockMetricsConnector {

    override suspend fun fetch(ticker: String): ConnectorStockMetrics? {
        if (apiKey.isBlank()) return null
        val url = HttpUrl.Builder()
            .scheme("https")
            .host("www.alphavantage.co")
            .addPathSegment("query")
            .addQueryParameter("function", "OVERVIEW")
            .addQueryParameter("symbol", ticker)
            .addQueryParameter("apikey", apiKey)
            .build()
        val request = Request.Builder()
            .url(url)
            .get()
            .build()
        return withContext(Dispatchers.IO) {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@use null
                val body = response.body?.string() ?: return@use null
                val json = JSONObject(body)
                if (!json.has("Symbol")) return@use null
                ConnectorStockMetrics(
                    ticker = ticker,
                    price = null,
                    forwardPe = json.optDoubleOrNull("ForwardPE"),
                    trailingPe = json.optDoubleOrNull("TrailingPE"),
                    pegRatio = json.optDoubleOrNull("PEGRatio"),
                    priceToBook = json.optDoubleOrNull("PriceToBookRatio"),
                    dividendRate = json.optDoubleOrNull("DividendPerShare"),
                    epsForward = json.optDoubleOrNull("EPS"),
                    bookValue = json.optDoubleOrNull("BookValue"),
                    beta = json.optDoubleOrNull("Beta"),
                    returnOnEquity = json.optDoubleOrNull("ReturnOnEquityTTM"),
                    returnOnAssets = json.optDoubleOrNull("ReturnOnAssetsTTM"),
                    operatingMargin = json.optDoubleOrNull("OperatingMarginTTM"),
                    netMargin = json.optDoubleOrNull("ProfitMargin"),
                    debtToEquity = json.optDoubleOrNull("DebtEquityRatio"),
                    sector = json.optString("Sector").takeIf { it.isNotBlank() }
                )
            }
        }
    }
}

private fun JSONObject.optDoubleOrNull(key: String): Double? =
    optString(key).takeIf { it.isNotBlank() && it != "None" }?.toDoubleOrNull()
