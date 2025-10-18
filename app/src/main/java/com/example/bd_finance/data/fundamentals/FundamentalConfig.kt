package com.example.bd_finance.data.fundamentals

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject
import kotlin.math.max

data class FundamentalScoringConfig(
    val sectorWeight: Double = 0.6,
    val historyWeight: Double = 0.4,
    val zScoreClamp: Double = 3.0,
    val marginStdDevReference: Double = 0.05,
    val betaNeutralValue: Double = 3.0,
    val pegTarget: Double = 1.0,
    val dcfDiscountFloor: Double = 0.08,
    val dcfTerminalGrowth: Double = 0.025,
    val dividendDiscountFloor: Double = 0.08,
    val dividendGrowthCap: Double = 0.06
) {
    fun normalized(): FundamentalScoringConfig {
        val clamp = max(0.1, zScoreClamp)
        val marginReference = max(0.0001, marginStdDevReference)
        val betaCap = max(1.0, betaNeutralValue)
        val sector = max(0.0, sectorWeight)
        val history = max(0.0, historyWeight)
        val weightSum = if (sector + history == 0.0) 1.0 else (sector + history)
        return copy(
            sectorWeight = sector / weightSum,
            historyWeight = history / weightSum,
            zScoreClamp = clamp,
            marginStdDevReference = marginReference,
            betaNeutralValue = betaCap
        )
    }
}

object FundamentalConfigRegistry {
    private val state = MutableStateFlow(FundamentalScoringConfig().normalized())

    fun state(): StateFlow<FundamentalScoringConfig> = state.asStateFlow()
    fun current(): FundamentalScoringConfig = state.value

    fun replace(config: FundamentalScoringConfig) {
        state.value = config.normalized()
    }

    fun update(block: (FundamentalScoringConfig) -> FundamentalScoringConfig) {
        replace(block(state.value))
    }
}

object FundamentalConfigOverrides {

    fun applyJson(json: String) {
        runCatching {
            val root = JSONObject(json)
            val base = FundamentalConfigRegistry.current()
            val updated = FundamentalScoringConfig(
                sectorWeight = root.optDoubleOrDefault("sectorWeight", base.sectorWeight),
                historyWeight = root.optDoubleOrDefault("historyWeight", base.historyWeight),
                zScoreClamp = root.optDoubleOrDefault("zScoreClamp", base.zScoreClamp),
                marginStdDevReference = root.optDoubleOrDefault("marginStdDevReference", base.marginStdDevReference),
                betaNeutralValue = root.optDoubleOrDefault("betaNeutralValue", base.betaNeutralValue),
                pegTarget = root.optDoubleOrDefault("pegTarget", base.pegTarget),
                dcfDiscountFloor = root.optDoubleOrDefault("dcfDiscountFloor", base.dcfDiscountFloor),
                dcfTerminalGrowth = root.optDoubleOrDefault("dcfTerminalGrowth", base.dcfTerminalGrowth),
                dividendDiscountFloor = root.optDoubleOrDefault("dividendDiscountFloor", base.dividendDiscountFloor),
                dividendGrowthCap = root.optDoubleOrDefault("dividendGrowthCap", base.dividendGrowthCap)
            )
            FundamentalConfigRegistry.replace(updated)
        }.getOrElse {
            // Silently ignore malformed overrides; callers can log if needed.
        }
    }

    private fun JSONObject.optDoubleOrDefault(name: String, default: Double): Double =
        if (has(name) && !isNull(name)) optDouble(name, default) else default
}
