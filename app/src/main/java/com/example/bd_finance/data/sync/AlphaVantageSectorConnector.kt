package com.example.bd_finance.data.sync

import com.example.bd_finance.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import java.util.Locale

class AlphaVantageSectorConnector(
    private val client: OkHttpClient,
    private val apiKey: String = BuildConfig.ALPHA_VANTAGE_API_KEY,
    private val baseUrl: HttpUrl = DEFAULT_BASE_URL
) : StockMetricsConnector {

    override suspend fun fetch(ticker: String): ConnectorStockMetrics? {
        if (apiKey.isBlank()) return null
        val normalized = ticker.uppercase(Locale.US)
        return withContext(Dispatchers.IO) {
            val url = baseUrl.newBuilder()
                .addPathSegment("query")
                .addQueryParameter("function", "OVERVIEW")
                .addQueryParameter("symbol", normalized)
                .addQueryParameter("apikey", apiKey)
                .build()
            val request = Request.Builder().url(url).get().build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    if (response.code == 429) throw IOException("Alpha Vantage rate limited")
                    throw IOException("Alpha Vantage overview failed: ${response.code}")
                }
                val body = response.body?.string() ?: return@use null
                val json = JSONObject(body)
                if (!json.has("Symbol")) return@use null
                val sector = json.optString("Sector").takeIf { it.isNotBlank() }
                val snapshot = SectorMedianSnapshot(
                    sectorName = sector,
                    metrics = SectorMedianMetrics(
                        priceToEarnings = json.optDoubleOrNull("PERatio"),
                        priceToBook = json.optDoubleOrNull("PriceToBookRatio"),
                        returnOnEquity = json.optDoubleOrNull("ReturnOnEquityTTM"),
                        returnOnAssets = json.optDoubleOrNull("ReturnOnAssetsTTM"),
                        operatingMargin = json.optDoubleOrNull("OperatingMarginTTM"),
                        netMargin = json.optDoubleOrNull("ProfitMargin"),
                        debtToEquity = json.optDoubleOrNull("DebtEquityRatio")
                    ),
                    source = "Alpha Vantage overview",
                    fallbackUsed = true
                )
                val metadata = RefreshMetadata(
                    lastUpdatedMillis = System.currentTimeMillis(),
                    retries = 0,
                    usedFallback = true
                )
                return@use ConnectorStockMetrics(
                    ticker = normalized,
                    price = null,
                    forwardPe = null,
                    trailingPe = null,
                    pegRatio = null,
                    priceToBook = null,
                    dividendRate = null,
                    epsForward = null,
                    bookValue = null,
                    beta = null,
                    returnOnEquity = json.optDoubleOrNull("ReturnOnEquityTTM"),
                    returnOnAssets = json.optDoubleOrNull("ReturnOnAssetsTTM"),
                    operatingMargin = json.optDoubleOrNull("OperatingMarginTTM"),
                    netMargin = json.optDoubleOrNull("ProfitMargin"),
                    debtToEquity = json.optDoubleOrNull("DebtEquityRatio"),
                    sector = sector,
                    sectorSnapshot = snapshot,
                    historicalFundamentals = null,
                    refreshMetadata = metadata
                )
            }
        }
    }

    companion object {
        private val DEFAULT_BASE_URL: HttpUrl = HttpUrl.Builder()
            .scheme("https")
            .host("www.alphavantage.co")
            .build()
    }
}

private fun JSONObject.optDoubleOrNull(key: String): Double? =
    optString(key).takeIf { it.isNotBlank() && it != "None" }?.toDoubleOrNull()
