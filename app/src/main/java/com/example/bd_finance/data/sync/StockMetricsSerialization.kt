package com.example.bd_finance.data.sync

import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.util.Base64
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import kotlin.collections.buildList

internal fun SectorMedianSnapshot?.toCompressedJson(): String? {
    val snapshot = this ?: return null
    val metricsJson = JSONObject().apply {
        putDoubleIfFinite("priceToEarnings", snapshot.metrics.priceToEarnings)
        putDoubleIfFinite("priceToBook", snapshot.metrics.priceToBook)
        putDoubleIfFinite("returnOnEquity", snapshot.metrics.returnOnEquity)
        putDoubleIfFinite("returnOnAssets", snapshot.metrics.returnOnAssets)
        putDoubleIfFinite("operatingMargin", snapshot.metrics.operatingMargin)
        putDoubleIfFinite("netMargin", snapshot.metrics.netMargin)
        putDoubleIfFinite("debtToEquity", snapshot.metrics.debtToEquity)
    }
    val root = JSONObject().apply {
        putOpt("sectorName", snapshot.sectorName)
        put("source", snapshot.source)
        put("fallbackUsed", snapshot.fallbackUsed)
        put("metrics", metricsJson)
    }
    return compressJson(root)
}

internal fun String?.toSectorMedianSnapshot(): SectorMedianSnapshot? {
    val encoded = this ?: return null
    return runCatching {
        val json = decodeJson(encoded)
        val metricsJson = json.optJSONObject("metrics") ?: JSONObject()
        SectorMedianSnapshot(
            sectorName = json.optStringOrNull("sectorName"),
            source = json.optStringOrNull("source") ?: "unknown",
            fallbackUsed = json.optBoolean("fallbackUsed", false),
            metrics = SectorMedianMetrics(
                priceToEarnings = metricsJson.optDoubleOrNull("priceToEarnings"),
                priceToBook = metricsJson.optDoubleOrNull("priceToBook"),
                returnOnEquity = metricsJson.optDoubleOrNull("returnOnEquity"),
                returnOnAssets = metricsJson.optDoubleOrNull("returnOnAssets"),
                operatingMargin = metricsJson.optDoubleOrNull("operatingMargin"),
                netMargin = metricsJson.optDoubleOrNull("netMargin"),
                debtToEquity = metricsJson.optDoubleOrNull("debtToEquity")
            )
        )
    }.getOrNull()
}

internal fun HistoricalFundamentalSnapshot?.toCompressedJson(): String? {
    val snapshot = this ?: return null
    val metricsJson = JSONObject()
    snapshot.metrics.forEach { (metric, window) ->
        metricsJson.put(metric.name, window.toJson())
    }
    val root = JSONObject().apply { put("metrics", metricsJson) }
    return compressJson(root)
}

internal fun String?.toHistoricalFundamentalSnapshot(): HistoricalFundamentalSnapshot? {
    val encoded = this ?: return null
    return runCatching {
        val json = decodeJson(encoded)
        val metricsJson = json.optJSONObject("metrics") ?: JSONObject()
        val map = mutableMapOf<FundamentalMetric, HistoricalMetricWindow>()
        FundamentalMetric.values().forEach { metric ->
            metricsJson.optJSONObject(metric.name)?.let { metricJson ->
                map[metric] = metricJson.toWindow()
            }
        }
        HistoricalFundamentalSnapshot(map)
    }.getOrNull()
}

private fun HistoricalMetricWindow.toJson(): JSONObject =
    JSONObject().apply {
        fiveYear?.let { put("fiveYear", it.toJson()) }
        tenYear?.let { put("tenYear", it.toJson()) }
    }

private fun JSONObject.toWindow(): HistoricalMetricWindow =
    HistoricalMetricWindow(
        fiveYear = optJSONObject("fiveYear")?.toHorizon(),
        tenYear = optJSONObject("tenYear")?.toHorizon()
    )

private fun HistoricalWindow.toJson(): JSONObject =
    JSONObject().apply {
        putDoubleIfFinite("median", median)
        putDoubleIfFinite("average", average)
        val pointsArray = JSONArray()
        points.forEach { point ->
            pointsArray.put(
                JSONObject().apply {
                    put("year", point.year)
                    putDoubleIfFinite("value", point.value)
                }
            )
        }
        put("points", pointsArray)
    }

private fun JSONObject.toHorizon(): HistoricalWindow {
    val pointsArray = optJSONArray("points") ?: JSONArray()
    val points = buildList {
        for (index in 0 until pointsArray.length()) {
            val entry = pointsArray.optJSONObject(index) ?: continue
            val year = entry.optInt("year", Int.MIN_VALUE)
            if (year == Int.MIN_VALUE) continue
            add(
                HistoricalDataPoint(
                    year = year,
                    value = entry.optDoubleOrNull("value")
                )
            )
        }
    }
    val medianValue = optDoubleOrNull("median")
    val averageValue = optDoubleOrNull("average")
    return HistoricalWindow(points = points, median = medianValue, average = averageValue)
}

private fun JSONObject.putDoubleIfFinite(key: String, value: Double?) {
    val numeric = value
    if (numeric != null && numeric.isFinite()) {
        put(key, numeric)
    }
}

private fun Double.isFinite(): Boolean = !isNaN() && !isInfinite()

private fun JSONObject.optStringOrNull(key: String): String? =
    optString(key).takeIf { it.isNotBlank() }

private fun JSONObject.optDoubleOrNull(key: String): Double? =
    if (has(key) && !isNull(key)) {
        val value = optDouble(key)
        if (!value.isNaN() && !value.isInfinite()) value else null
    } else {
        null
    }

private fun compressJson(json: JSONObject): String {
    val raw = json.toString()
    val output = ByteArrayOutputStream()
    GZIPOutputStream(output).use { stream ->
        stream.write(raw.toByteArray(StandardCharsets.UTF_8))
    }
    return Base64.getEncoder().encodeToString(output.toByteArray())
}

private fun decodeJson(encoded: String): JSONObject {
    return try {
        val bytes = Base64.getDecoder().decode(encoded)
        val input = GZIPInputStream(ByteArrayInputStream(bytes))
        val raw = input.bufferedReader(StandardCharsets.UTF_8).use { it.readText() }
        JSONObject(raw)
    } catch (_: Exception) {
        JSONObject(encoded)
    }
}
