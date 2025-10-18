package com.example.bd_finance.data.fundamentals

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class FundamentalConfigOverridesTest {

    @Before
    fun reset() {
        FundamentalConfigRegistry.replace(FundamentalScoringConfig())
    }

    @Test
    fun `applyJson updates registry`() {
        val payload = """
            {
              "sectorWeight": 0.3,
              "historyWeight": 0.7,
              "zScoreClamp": 2.5,
              "marginStdDevReference": 0.03,
              "betaNeutralValue": 2.5,
              "pegTarget": 0.9,
              "dcfDiscountFloor": 0.09,
              "dcfTerminalGrowth": 0.02,
              "dividendDiscountFloor": 0.09,
              "dividendGrowthCap": 0.05
            }
        """.trimIndent()

        FundamentalConfigOverrides.applyJson(payload)

        val updated = FundamentalConfigRegistry.current()
        assertEquals(0.3, updated.sectorWeight, 1e-6)
        assertEquals(0.7, updated.historyWeight, 1e-6)
        assertEquals(2.5, updated.zScoreClamp, 1e-6)
        assertEquals(0.03, updated.marginStdDevReference, 1e-6)
        assertEquals(2.5, updated.betaNeutralValue, 1e-6)
        assertEquals(0.9, updated.pegTarget, 1e-6)
        assertEquals(0.09, updated.dcfDiscountFloor, 1e-6)
        assertEquals(0.02, updated.dcfTerminalGrowth, 1e-6)
        assertEquals(0.09, updated.dividendDiscountFloor, 1e-6)
        assertEquals(0.05, updated.dividendGrowthCap, 1e-6)
        assertEquals(1.0, updated.sectorWeight + updated.historyWeight, 1e-6)
    }
}
