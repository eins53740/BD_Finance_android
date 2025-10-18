
package com.example.bd_finance.data.sync

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class AlphaVantageSectorConnectorTest {

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
    fun `fetch emits fallback snapshot metadata`() = runTest {
        server.enqueue(
            MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody(overviewPayload())
        )

        val connector = AlphaVantageSectorConnector(
            client = client,
            apiKey = "demo",
            baseUrl = server.url("/")
        )

        val result = connector.fetch("MSFT")

        assertEquals(1, server.requestCount)
        assertNotNull(result)
        requireNotNull(result)

        val snapshot = result.sectorSnapshot
        assertNotNull(snapshot)
        requireNotNull(snapshot)
        assertTrue(snapshot.fallbackUsed)
        assertEquals("Technology", snapshot.sectorName)
        assertEquals(18.5, snapshot.metrics.priceToEarnings!!, 0.0)
        assertTrue(result.refreshMetadata.usedFallback)
    }

    private fun overviewPayload(): String =
        """{
            "Symbol":"MSFT",
            "Sector":"Technology",
            "PERatio":"18.5",
            "PriceToBookRatio":"12.3",
            "ReturnOnEquityTTM":"0.35",
            "ReturnOnAssetsTTM":"0.16",
            "OperatingMarginTTM":"0.42",
            "ProfitMargin":"0.33",
            "DebtEquityRatio":"0.45"
        }""".trimIndent()
}
