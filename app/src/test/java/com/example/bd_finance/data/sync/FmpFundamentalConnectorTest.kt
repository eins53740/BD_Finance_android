
package com.example.bd_finance.data.sync

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class FmpFundamentalConnectorTest {

    private lateinit var server: MockWebServer
    private lateinit var client: OkHttpClient

    @Before
    fun setUp() {
        server = MockWebServer().apply { start() }
        client = OkHttpClient()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `fetch builds sector snapshot and historical fundamentals`() = runTest {
        server.enqueue(jsonResponse(profilePayload()))
        server.enqueue(jsonResponse(sectorPayload()))
        server.enqueue(jsonResponse(ratiosPayload()))

        val baseUrl = server.url("/")
        val connector = FmpFundamentalConnector(
            client = client,
            apiKey = "test",
            baseUrl = baseUrl
        )

        val result = connector.fetch("AAPL")

        assertEquals(3, server.requestCount)
        assertNotNull(result)
        requireNotNull(result)

        assertEquals("AAPL", result.ticker)
        assertEquals("Technology", result.sector)

        val snapshot = result.sectorSnapshot
        assertNotNull(snapshot)
        requireNotNull(snapshot)
        assertEquals("Technology", snapshot.sectorName)
        assertFalse(snapshot.fallbackUsed)
        assertEquals(25.0, snapshot.metrics.priceToEarnings!!, 0.0)
        assertEquals(5.0, snapshot.metrics.priceToBook!!, 0.0)

        val fundamentals = result.historicalFundamentals
        assertNotNull(fundamentals)
        requireNotNull(fundamentals)
        val peWindow = fundamentals.metrics[FundamentalMetric.PE_RATIO]
        assertNotNull(peWindow)
        val fiveYear = peWindow?.fiveYear
        assertNotNull(fiveYear)
        requireNotNull(fiveYear)
        assertEquals(5, fiveYear.points.size)
        assertEquals(21.0, fiveYear.median!!, 0.0)

        val metadata = result.refreshMetadata
        assertFalse(metadata.usedFallback)
        assertEquals(0, metadata.retries)
    }

    private fun jsonResponse(body: String): MockResponse =
        MockResponse()
            .setHeader("Content-Type", "application/json")
            .setBody(body)

    private fun profilePayload(): String =
        """[
            {"symbol":"AAPL","sector":"Technology"}
        ]""".trimIndent()

    private fun sectorPayload(): String =
        """[
            {
                "sector":"Technology",
                "peRatioMedian":25.0,
                "pbRatioMedian":5.0,
                "roeMedian":0.25,
                "roaMedian":0.12,
                "operatingMarginMedian":0.30,
                "netMarginMedian":0.21,
                "debtToEquityMedian":0.45
            }
        ]""".trimIndent()

    private fun ratiosPayload(): String =
        """[
            {"calendarYear":"2023","peRatio":23.0,"pbRatio":5.0,"returnOnEquity":0.21,"operatingProfitMargin":0.29,"netProfitMargin":0.21},
            {"calendarYear":"2022","peRatio":22.0,"pbRatio":4.9,"returnOnEquity":0.20,"operatingProfitMargin":0.28,"netProfitMargin":0.20},
            {"calendarYear":"2021","peRatio":21.0,"pbRatio":4.8,"returnOnEquity":0.19,"operatingProfitMargin":0.27,"netProfitMargin":0.19},
            {"calendarYear":"2020","peRatio":20.0,"pbRatio":4.7,"returnOnEquity":0.18,"operatingProfitMargin":0.26,"netProfitMargin":0.18},
            {"calendarYear":"2019","peRatio":19.0,"pbRatio":4.6,"returnOnEquity":0.17,"operatingProfitMargin":0.25,"netProfitMargin":0.17}
        ]""".trimIndent()
}
