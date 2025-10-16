package com.example.bd_finance.data.llm

import com.example.bd_finance.data.model.StockAnalysis
import com.example.bd_finance.data.model.StockVerdict
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class LargeLanguageModelClient(
    private val httpClient: OkHttpClient,
    private val groqApiKey: String,
    private val geminiApiKey: String
) {

    suspend fun synthesizeSecondOpinion(analysis: StockAnalysis): String? {
        val prompt = PromptBuilder.buildPrompt(analysis)
        val groqResult = runCatching { callGroq(prompt) }.getOrNull()
        if (!groqResult.isNullOrBlank()) {
            return MarkdownRenderer.toHtml(groqResult.trim())
        }
        val geminiResult = runCatching { callGemini(prompt) }.getOrNull()
        if (!geminiResult.isNullOrBlank()) {
            return MarkdownRenderer.toHtml(geminiResult.trim())
        }
        return OpinionTemplate.renderFallback(analysis)
    }

    private suspend fun callGroq(prompt: String): String? {
        if (groqApiKey.isBlank()) return null
        return withContext(Dispatchers.IO) {
            val url = "https://api.groq.com/openai/v1/chat/completions"
            val body = JSONObject().apply {
                put("model", "llama-3.1-8b-instant")
                put("temperature", 0.25)
                put("max_tokens", 600)
                put(
                    "messages",
                    JSONArray().apply {
                        put(
                            JSONObject().apply {
                                put("role", "system")
                                put(
                                    "content",
                                    "You are an equity research assistant writing concise investment commentary for retail investors."
                                )
                            }
                        )
                        put(
                            JSONObject().apply {
                                put("role", "user")
                                put("content", prompt)
                            }
                        )
                    }
                )
            }
            val request = Request.Builder()
                .url(url)
                .header("Authorization", "Bearer $groqApiKey")
                .header("Content-Type", "application/json")
                .post(body.toString().toRequestBody(JSON_MEDIA_TYPE))
                .build()
            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IOException("Groq request failed: ${response.code}")
                }
                val payload = response.body?.string() ?: return@use null
                val root = JSONObject(payload)
                val choices = root.optJSONArray("choices") ?: return@use null
                if (choices.length() == 0) return@use null
                val message = choices.optJSONObject(0)?.optJSONObject("message")
                message?.optString("content")
            }
        }
    }

    private suspend fun callGemini(prompt: String): String? {
        if (geminiApiKey.isBlank()) return null
        return withContext(Dispatchers.IO) {
            val url =
                "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-lite:generateContent?key=$geminiApiKey"
            val body = JSONObject().apply {
                put(
                    "contents",
                    JSONArray().apply {
                        put(
                            JSONObject().apply {
                                put(
                                    "parts",
                                    JSONArray().apply {
                                        put(JSONObject().apply { put("text", prompt) })
                                    }
                                )
                            }
                        )
                    }
                )
                put(
                    "generationConfig",
                    JSONObject().apply {
                        put("temperature", 0.3)
                        put("maxOutputTokens", 600)
                    }
                )
            }
            val request = Request.Builder()
                .url(url)
                .header("Content-Type", "application/json")
                .post(body.toString().toRequestBody(JSON_MEDIA_TYPE))
                .build()
            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IOException("Gemini request failed: ${response.code}")
                }
                val payload = response.body?.string() ?: return@use null
                val root = JSONObject(payload)
                val candidates = root.optJSONArray("candidates") ?: return@use null
                if (candidates.length() == 0) return@use null
                val content =
                    candidates.optJSONObject(0)?.optJSONObject("content")?.optJSONArray("parts")
                val text = content?.optJSONObject(0)?.optString("text")
                text
            }
        }
    }

    companion object {
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }
}

private object PromptBuilder {
    fun buildPrompt(analysis: StockAnalysis): String {
        val summary = analysis.summary
        val metrics = analysis.metrics.joinToString(separator = "\n") {
            "- ${it.name}: ${it.value}"
        }
        val decisions = analysis.decisionTrail.joinToString(separator = "\n") {
            val statusLabel = when (it.status) {
                com.example.bd_finance.data.model.DecisionStatus.PASS -> "Pass"
                com.example.bd_finance.data.model.DecisionStatus.CLOSE_FAIL -> "Close Fail"
                com.example.bd_finance.data.model.DecisionStatus.FAIL -> "Fail"
            }
            "- ${it.title} (${statusLabel}): observed ${it.observed}, threshold ${it.threshold}"
        }
        val risk = analysis.riskInsights.joinToString(separator = "\n") {
            "- ${it.title}: ${it.summary} (${it.scoreLabel})"
        }
        val momentum = analysis.momentumInsights.joinToString(separator = "\n") {
            "- ${it.periodLabel}: ${String.format("%.2f%%", it.percentChange)}"
        }
        val verdictGuidance = when (analysis.summary.verdict) {
            StockVerdict.BUY -> "Provide a confident but risk-aware buy viewpoint."
            StockVerdict.BUY_WITH_CAUTION -> "Highlight the upside but emphasize the caution areas before buying."
            StockVerdict.DO_NOT_BUY -> "Explain why the equity is not attractive and possible catalysts to monitor."
        }
        return """
            You are reviewing a stock evaluation summary.
            
            Ticker: ${summary.ticker}
            Company: ${summary.companyName ?: "Unknown"}
            Verdict: ${summary.verdict.headline}
            
            Core Metrics:
            $metrics
            
            Decision Trail:
            $decisions
            
            Risk Insights:
            $risk
            
            Momentum:
            $momentum
            
            Task:
            Produce a Markdown briefing that starts with a single line formatted exactly as `Rating: X/10 — <summary>` (where X is an integer from 1 to 10 and 10 means “buy now!”). After the rating line, include:
            1. An opening verdict paragraph elaborating on the current stance.
            2. A short bullet list of the 2–3 strongest supports.
            3. A short bullet list of the key risks or watch items.
            4. A closing recommendation aligned with the verdict that references the rating.
            
            Weigh quantitative metrics and the decision trail when choosing the rating. Tone: analytical, investor-facing, objective.
            $verdictGuidance
        """.trimIndent()
    }
}

private object MarkdownRenderer {
    fun toHtml(markdown: String): String {
        val lines = markdown.lines()
        val builder = StringBuilder()
        builder.append("<div class=\"llm-opinion\">")
        var inList = false
        lines.forEach { rawLine ->
            val line = rawLine.trim()
            when {
                line.startsWith("### ") -> {
                    if (inList) {
                        builder.append("</ul>")
                        inList = false
                    }
                    builder.append("<h3>${line.removePrefix("### ").escape()}</h3>")
                }
                line.startsWith("## ") -> {
                    if (inList) {
                        builder.append("</ul>")
                        inList = false
                    }
                    builder.append("<h2>${line.removePrefix("## ").escape()}</h2>")
                }
                line.startsWith("- ") -> {
                    if (!inList) {
                        builder.append("<ul>")
                        inList = true
                    }
                    builder.append("<li>${line.removePrefix("- ").escape()}</li>")
                }
                line.isBlank() -> {
                    if (inList) {
                        builder.append("</ul>")
                        inList = false
                    }
                }
                else -> {
                    if (inList) {
                        builder.append("</ul>")
                        inList = false
                    }
                    builder.append("<p>${line.escape()}</p>")
                }
            }
        }
        if (inList) {
            builder.append("</ul>")
        }
        builder.append("</div>")
        return builder.toString()
    }

    private fun String.escape(): String =
        replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
}

private object OpinionTemplate {
    fun renderFallback(analysis: StockAnalysis): String? {
        val summary = analysis.summary
        val (rating, ratingBlurb) = when (summary.verdict) {
            StockVerdict.BUY -> 9 to "Strong fundamental support and attractive upside."
            StockVerdict.BUY_WITH_CAUTION -> 6 to "Upside exists but positioning should remain cautious."
            StockVerdict.DO_NOT_BUY -> 3 to "Too many red flags to justify a purchase right now."
        }
        val positive = analysis.decisionTrail
            .filter { it.status == com.example.bd_finance.data.model.DecisionStatus.PASS }
            .take(3)
            .joinToString(separator = "") {
                "<li>${it.title}: ${it.observed}</li>"
            }
        val risks = analysis.decisionTrail
            .filter { it.status != com.example.bd_finance.data.model.DecisionStatus.PASS }
            .take(3)
            .joinToString(separator = "") {
                "<li>${it.title}: ${it.observed} (needs attention)</li>"
            }
        val builder = StringBuilder()
        builder.append("<div class=\"llm-opinion\">")
        builder.append("<p><strong>Rating:</strong> $rating/10 — $ratingBlurb</p>")
        val verdictNarrative = when (summary.verdict) {
            StockVerdict.BUY -> "Conditions align with a bullish stance."
            StockVerdict.BUY_WITH_CAUTION -> "Upside exists but investors should size positions carefully."
            StockVerdict.DO_NOT_BUY -> "Current metrics do not justify a new position."
        }
        builder.append("<p><strong>${summary.verdict.headline}:</strong> $verdictNarrative</p>")
        if (positive.isNotBlank()) {
            builder.append("<h3>Supports</h3><ul>$positive</ul>")
        }
        if (risks.isNotBlank()) {
            builder.append("<h3>Risks</h3><ul>$risks</ul>")
        }
        builder.append("</div>")
        return builder.toString()
    }
}
