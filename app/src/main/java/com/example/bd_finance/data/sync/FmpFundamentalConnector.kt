package com.example.bd_finance.data.sync

import com.example.bd_finance.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.Locale
import kotlin.collections.buildMap

class FmpFundamentalConnector(
    private val client: OkHttpClient,
    private val apiKey: String = BuildConfig.FMP_API_KEY,
    private val maxAttempts: Int = 3,
    private val initialDelayMillis: Long = 300L,
    private val baseUrl: HttpUrl = DEFAULT_BASE_URL
) : StockMetricsConnector {

    override suspend fun fetch(ticker: String): ConnectorStockMetrics? {
        if (apiKey.isBlank()) return null
        val normalized = ticker.uppercase(Locale.US)
        return withContext(Dispatchers.IO) {
            var aggregatedRetries = 0
            val profile = fetchWithRetry { loadProfile(normalized) }.also {
                aggregatedRetries += it.second
            }.first ?: return@withContext null

            val sectorName = profile.optString("sector").takeIf { it.isNotBlank() }
            val returnOnEquity = profile.optDoubleOrNull("returnOnEquityTTM")
                ?: profile.optDoubleOrNull("returnOnEquity")
            val returnOnAssets = profile.optDoubleOrNull("returnOnAssetsTTM")
                ?: profile.optDoubleOrNull("returnOnAssets")
            val operatingMargin = profile.optDoubleOrNull("operatingMarginTTM")
                ?: profile.optDoubleOrNull("operatingMargin")
            val netMargin = profile.optDoubleOrNull("profitMargin")
                ?: profile.optDoubleOrNull("netMargin")
            val debtToEquity = profile.optDoubleOrNull("debtToEquityTTM")
                ?: profile.optDoubleOrNull("debtEquityRatio")

            val sectorSnapshot = sectorName?.let {
                fetchWithRetry { loadSectorSnapshot(it) }.also { tuple ->
                    aggregatedRetries += tuple.second
                }.first
            }

            val incomeStatements = fetchWithRetry { loadIncomeStatement(normalized) }.also {
                aggregatedRetries += it.second
            }.first
            val cashFlows = fetchWithRetry { loadCashFlow(normalized) }.also {
                aggregatedRetries += it.second
            }.first

            val historical = fetchWithRetry {
                loadHistoricalFundamentals(normalized, incomeStatements, cashFlows)
            }.also {
                aggregatedRetries += it.second
            }.first

            val metadata = RefreshMetadata(
                lastUpdatedMillis = System.currentTimeMillis(),
                retries = aggregatedRetries,
                usedFallback = false
            )

            ConnectorStockMetrics(
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
                returnOnEquity = returnOnEquity,
                returnOnAssets = returnOnAssets,
                operatingMargin = operatingMargin,
                netMargin = netMargin,
                debtToEquity = debtToEquity,
                sector = sectorName,
                sectorSnapshot = sectorSnapshot,
                historicalFundamentals = historical,
                refreshMetadata = metadata
            )
        }
    }

    private suspend fun loadProfile(ticker: String): JSONObject? {
        val url = baseUrl.newBuilder()
            .addPathSegments("api/v3/profile/$ticker")
            .addQueryParameter("apikey", apiKey)
            .build()
        val response = execute(url) ?: return null
        val array = JSONArray(response)
        return if (array.length() > 0) array.getJSONObject(0) else null
    }

    private suspend fun loadSectorSnapshot(sector: String): SectorMedianSnapshot? {
        val url = baseUrl.newBuilder()
            .addPathSegments("api/v4/advanced_sector_statistics")
            .addQueryParameter("sector", sector)
            .addQueryParameter("apikey", apiKey)
            .build()
        val response = execute(url) ?: return null
        val array = JSONArray(response)
        if (array.length() == 0) return null
        val payload = array.getJSONObject(0)
        val metrics = SectorMedianMetrics(
            priceToEarnings = payload.peekDouble(
                "peRatioMedian", "peRatio", "priceEarningsRatioMedian"
            ),
            priceToBook = payload.peekDouble(
                "pbRatioMedian", "priceToBookRatioMedian", "priceToBook"
            ),
            returnOnEquity = payload.peekDouble(
                "roeMedian", "returnOnEquityMedian"
            ),
            returnOnAssets = payload.peekDouble(
                "roaMedian", "returnOnAssetsMedian"
            ),
            operatingMargin = payload.peekDouble(
                "operatingMarginMedian", "operatingProfitMarginMedian"
            ),
            netMargin = payload.peekDouble(
                "netMarginMedian", "netProfitMarginMedian"
            ),
            debtToEquity = payload.peekDouble(
                "debtToEquityMedian", "debtEquityRatioMedian"
            )
        )
        return SectorMedianSnapshot(
            sectorName = payload.optString("sector").takeIf { it.isNotBlank() } ?: sector,
            metrics = metrics,
            source = "FMP advanced_sector_statistics",
            fallbackUsed = false
        )
    }

    private suspend fun loadHistoricalFundamentals(
        ticker: String,
        incomeStatements: Map<Int, IncomeStatementEntry>?,
        cashFlows: Map<Int, Double?>?
    ): HistoricalFundamentalSnapshot? {
        val url = baseUrl.newBuilder()
            .addPathSegments("api/v3/ratios/$ticker")
            .addQueryParameter("period", "annual")
            .addQueryParameter("limit", "10")
            .addQueryParameter("apikey", apiKey)
            .build()
        val response = execute(url) ?: return null
        val array = JSONArray(response)
        if (array.length() == 0) return null

        val series = FundamentalMetric.values().associateWith { mutableListOf<HistoricalDataPoint>() }

        for (index in 0 until array.length()) {
            val obj = array.optJSONObject(index) ?: continue
            val year = obj.parseYear() ?: continue
            FundamentalMetric.values().forEach { metric ->
                if (metric in setOf(
                        FundamentalMetric.REVENUE,
                        FundamentalMetric.EARNINGS_PER_SHARE,
                        FundamentalMetric.FREE_CASH_FLOW
                    )
                ) {
                    return@forEach
                }
                val value = obj.lookupMetric(metric)
                val list = series[metric] ?: return@forEach
                list.add(HistoricalDataPoint(year = year, value = value))
            }
        }

        incomeStatements?.forEach { (year, entry) ->
            entry.revenue?.let {
                series[FundamentalMetric.REVENUE]?.add(HistoricalDataPoint(year, it))
            }
            entry.eps?.let {
                series[FundamentalMetric.EARNINGS_PER_SHARE]?.add(HistoricalDataPoint(year, it))
            }
        }

        cashFlows?.forEach { (year, value) ->
            series[FundamentalMetric.FREE_CASH_FLOW]?.add(HistoricalDataPoint(year, value))
        }

        val windows = buildMap<FundamentalMetric, HistoricalMetricWindow> {
            series.forEach { (metric, points) ->
                if (points.isEmpty()) return@forEach
                val sorted = points.sortedBy { it.year }
                val five = sorted.buildWindow(limit = 5)
                val ten = sorted.buildWindow(limit = 10)
                if (five != null || ten != null) {
                    put(metric, HistoricalMetricWindow(fiveYear = five, tenYear = ten))
                }
            }
        }
        if (windows.isEmpty()) return null
        return HistoricalFundamentalSnapshot(windows)
    }

    private fun List<HistoricalDataPoint>.buildWindow(limit: Int): HistoricalWindow? {
        if (isEmpty()) return null
        val slice = takeLast(limit)
        val numeric = slice.mapNotNull { it.value }
        if (slice.isEmpty()) return null
        val median = numeric.medianOrNull()
        val average = numeric.averageOrNull()
        return HistoricalWindow(points = slice, median = median, average = average)
    }

    private suspend fun execute(url: HttpUrl): String? {
        val request = Request.Builder().url(url).get().build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                if (response.code == 429) throw IOException("Rate limited by FMP")
                throw IOException("Unexpected HTTP ${response.code}")
            }
            return response.body?.string()
        }
    }

    private suspend fun <T> fetchWithRetry(block: suspend () -> T?): Pair<T?, Int> {
        var delayMillis = initialDelayMillis
        var retries = 0
        repeat(maxAttempts) { attempt ->
            try {
                return Pair(block(), retries)
            } catch (io: IOException) {
                if (attempt == maxAttempts - 1) throw io
                retries++
                delay(delayMillis)
                delayMillis = (delayMillis * 2).coerceAtMost(5_000L)
            }
        }
        return Pair(null, retries)
    }

    private fun JSONObject.peekDouble(vararg keys: String): Double? =
        keys.firstNotNullOfOrNull { key -> optDoubleOrNull(key) }

    private fun JSONObject.optDoubleOrNull(key: String): Double? =
        if (has(key) && !isNull(key)) {
            val value = optDouble(key)
            if (value.isFinite() && !value.isNaN()) value else null
        } else null

    private fun JSONObject.parseYear(): Int? {
        val directYear = optString("calendarYear").takeIf { it.isNotBlank() }?.toIntOrNull()
        if (directYear != null) return directYear
        val date = optString("date").takeIf { it.isNotBlank() } ?: return null
        return date.take(4).toIntOrNull()
    }

    private fun JSONObject.lookupMetric(metric: FundamentalMetric): Double? =
        when (metric) {
            FundamentalMetric.PE_RATIO ->
                peekDouble("peRatio", "priceEarningsRatio")
            FundamentalMetric.PRICE_TO_BOOK ->
                peekDouble("pbRatio", "priceToBookRatio")
            FundamentalMetric.RETURN_ON_EQUITY ->
                peekDouble("returnOnEquity", "roe")
            FundamentalMetric.OPERATING_MARGIN ->
                peekDouble("operatingProfitMargin", "operatingMargin")
            FundamentalMetric.NET_MARGIN ->
                peekDouble("netProfitMargin", "netMargin")
            FundamentalMetric.DEBT_TO_EQUITY ->
                peekDouble("debtEquityRatio", "longTermDebtToEquity")
            FundamentalMetric.REVENUE,
            FundamentalMetric.EARNINGS_PER_SHARE,
            FundamentalMetric.FREE_CASH_FLOW ->
                null
        }

    private fun Double.isFinite(): Boolean = !isInfinite() && !isNaN()

    private fun List<Double>.medianOrNull(): Double? {
        if (isEmpty()) return null
        val sorted = sorted()
        val middle = sorted.size / 2
        return if (sorted.size % 2 == 0) {
            (sorted[middle - 1] + sorted[middle]) / 2.0
        } else {
            sorted[middle]
        }
    }

    private fun List<Double>.averageOrNull(): Double? =
        if (isEmpty()) null else this.sum() / size

    private suspend fun loadIncomeStatement(ticker: String): Map<Int, IncomeStatementEntry>? {
        val url = baseUrl.newBuilder()
            .addPathSegments("api/v3/income-statement/$ticker")
            .addQueryParameter("period", "annual")
            .addQueryParameter("limit", "10")
            .addQueryParameter("apikey", apiKey)
            .build()
        val response = execute(url) ?: return null
        val array = JSONArray(response)
        if (array.length() == 0) return emptyMap()
        val entries = mutableMapOf<Int, IncomeStatementEntry>()
        for (index in 0 until array.length()) {
            val obj = array.optJSONObject(index) ?: continue
            val year = obj.parseYear() ?: continue
            val revenue = obj.optDoubleOrNull("revenue") ?: obj.optDoubleOrNull("revenueUSD")
            val eps = obj.peekDouble("eps", "epsDiluted", "epsdiluted")
            if (revenue != null || eps != null) {
                entries[year] = IncomeStatementEntry(revenue, eps)
            }
        }
        return entries
    }

    private suspend fun loadCashFlow(ticker: String): Map<Int, Double?>? {
        val url = baseUrl.newBuilder()
            .addPathSegments("api/v3/cash-flow-statement/$ticker")
            .addQueryParameter("period", "annual")
            .addQueryParameter("limit", "10")
            .addQueryParameter("apikey", apiKey)
            .build()
        val response = execute(url) ?: return null
        val array = JSONArray(response)
        if (array.length() == 0) return emptyMap()
        val entries = mutableMapOf<Int, Double?>()
        for (index in 0 until array.length()) {
            val obj = array.optJSONObject(index) ?: continue
            val year = obj.parseYear() ?: continue
            val freeCashFlow = obj.optDoubleOrNull("freeCashFlow")
            entries[year] = freeCashFlow
        }
        return entries
    }

    private data class IncomeStatementEntry(
        val revenue: Double?,
        val eps: Double?
    )

    companion object {
        private val DEFAULT_BASE_URL: HttpUrl = HttpUrl.Builder()
            .scheme("https")
            .host("financialmodelingprep.com")
            .build()
    }
}
