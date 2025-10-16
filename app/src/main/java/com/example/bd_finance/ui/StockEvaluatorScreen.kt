package com.example.bd_finance.ui

import android.annotation.SuppressLint
import android.graphics.Color
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bd_finance.data.StockAnalysisRepository
import com.example.bd_finance.data.analysis.toDisplayString
import com.example.bd_finance.data.model.DecisionStatus
import com.example.bd_finance.data.model.DividendInsight
import com.example.bd_finance.data.model.MomentumInsight
import com.example.bd_finance.data.model.MetricEntry
import com.example.bd_finance.data.model.PeerComparison
import com.example.bd_finance.data.model.RiskInsight
import com.example.bd_finance.data.model.StockAnalysis
import com.example.bd_finance.data.model.StockVerdict
import com.example.bd_finance.data.model.VerdictColor
import com.example.bd_finance.data.model.formatPercent
import com.example.bd_finance.R
import java.text.NumberFormat
import java.util.Locale

@Composable
fun StockEvaluatorRoot() {
    val repository = remember { StockAnalysisRepository.default() }
    val viewModel: StockEvaluatorViewModel = viewModel(
        factory = StockEvaluatorViewModelFactory(repository)
    )
    val state by viewModel.uiState.collectAsState()
    StockEvaluatorScreen(
        state = state,
        onAnalyze = viewModel::analyzeTicker,
        onRetry = viewModel::retry
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockEvaluatorScreen(
    state: StockEvaluatorUiState,
    onAnalyze: (String) -> Unit,
    onRetry: () -> Unit
) {
    var tickerInput by rememberSaveable { mutableStateOf("") }
    val isLoading = state is StockEvaluatorUiState.Loading

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.app_name),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                actions = {
                    if (state is StockEvaluatorUiState.Error) {
                        IconButton(onClick = onRetry) {
                            Icon(
                                imageVector = Icons.Default.Autorenew,
                                contentDescription = "Retry"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TickerInputCard(
                ticker = tickerInput,
                onTickerChange = { tickerInput = it.uppercase() },
                onAnalyze = { onAnalyze(tickerInput) },
                isLoading = isLoading
            )

            when (state) {
                is StockEvaluatorUiState.Idle -> EmptyState()
                is StockEvaluatorUiState.Loading -> LoadingState(state.ticker)
                is StockEvaluatorUiState.Error -> ErrorState(state.ticker, state.message, onRetry)
                is StockEvaluatorUiState.Success -> {
                    AnalysisContent(
                        analysis = state.analysis,
                        onRefresh = { onAnalyze(state.analysis.summary.ticker) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TickerInputCard(
    ticker: String,
    onTickerChange: (String) -> Unit,
    onAnalyze: () -> Unit,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Ticker Intake",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            OutlinedTextField(
                value = ticker,
                onValueChange = onTickerChange,
                label = { Text(text = "Stock Ticker") },
                placeholder = { Text(text = "Enter stock ticker (e.g., AAPL)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = onAnalyze,
                enabled = ticker.isNotBlank() && !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = if (isLoading) "Analyzing…" else "Analyze")
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = "Run Your First Analysis",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Supply a stock ticker to review fundamentals, risk, a flowchart, and an AI perspective.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun LoadingState(ticker: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator()
            Column {
                Text(
                    text = "Analyzing $ticker…",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Fetching market data, running thresholds, and requesting the AI commentary.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun ErrorState(
    ticker: String,
    message: String,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.ErrorOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Analysis unavailable",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            val detail = if (ticker.isNotBlank()) {
                "We could not finish the evaluation for $ticker. $message"
            } else {
                message
            }
            Text(
                text = detail,
                style = MaterialTheme.typography.bodyMedium
            )
            OutlinedButton(onClick = onRetry) {
                Icon(imageVector = Icons.Default.Autorenew, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Retry")
            }
        }
    }
}

@Composable
private fun AnalysisContent(
    analysis: StockAnalysis,
    onRefresh: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SummaryCard(analysis, onRefresh)
        MetricsSection(analysis.metrics)
        DecisionTrailSection(analysis)
        RiskSection(analysis.riskInsights)
        MomentumSection(analysis.momentumInsights)
        PeerSection(analysis.peerComparisons, analysis.summary.currency)
        DividendSection(analysis.dividendInsight)
        MermaidSection(analysis.mermaidDefinition)
        LlmOpinionSection(analysis.llmOpinionHtml)
    }
}

@Composable
private fun SummaryCard(
    analysis: StockAnalysis,
    onRefresh: () -> Unit
) {
    val summary = analysis.summary
    val verdictColors = verdictColors(summary.verdict)
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = verdictColors.cardColor)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = summary.companyName ?: summary.ticker,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = summary.ticker,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                AssistChip(
                    onClick = {},
                    label = {
                        Text(
                            text = summary.verdict.headline,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = when (summary.verdict) {
                                StockVerdict.BUY -> Icons.Default.CheckCircle
                                StockVerdict.BUY_WITH_CAUTION -> Icons.Default.Warning
                                StockVerdict.DO_NOT_BUY -> Icons.Default.ErrorOutline
                            },
                            contentDescription = null
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = verdictColors.badgeColor,
                        labelColor = verdictColors.textColor,
                        leadingIconContentColor = verdictColors.textColor
                    ),
                    enabled = false
                )
            }
            val priceText = summary.price?.let { currencyFormatter(summary.currency).format(it) } ?: "—"
            val changeText = summary.changePercent.formatPercent()
            Text(
                text = "$priceText ($changeText)",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = summary.verdictNarrative,
                style = MaterialTheme.typography.bodyMedium
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Updated ${summary.updatedAt.toDisplayString()}",
                    style = MaterialTheme.typography.bodySmall
                )
                TextButton(onClick = onRefresh) {
                    Text("Refresh")
                }
            }
        }
    }
}

@Composable
private fun MetricsSection(metrics: List<MetricEntry>) {
    if (metrics.isEmpty()) return
    Text(
        text = "Key Metrics",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold
    )
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        metrics.forEach { metric ->
            MetricCard(metric)
        }
    }
}

@Composable
private fun MetricCard(metric: MetricEntry) {
    val noteColor = when (metric.status) {
        DecisionStatus.PASS -> MaterialTheme.colorScheme.tertiaryContainer
        DecisionStatus.CLOSE_FAIL -> MaterialTheme.colorScheme.secondaryContainer
        DecisionStatus.FAIL -> MaterialTheme.colorScheme.errorContainer
        null -> MaterialTheme.colorScheme.surfaceVariant
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = noteColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = metric.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = metric.value,
                style = MaterialTheme.typography.bodyMedium
            )
            metric.description?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun DecisionTrailSection(analysis: StockAnalysis) {
    Text(
        text = "Decision Trail",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold
    )
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        analysis.decisionTrail.forEach { decision ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = when (decision.status) {
                        DecisionStatus.PASS -> MaterialTheme.colorScheme.tertiaryContainer
                        DecisionStatus.CLOSE_FAIL -> MaterialTheme.colorScheme.secondaryContainer
                        DecisionStatus.FAIL -> MaterialTheme.colorScheme.errorContainer
                    }
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = decision.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = decision.observed,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Target: ${decision.threshold}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    decision.note?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RiskSection(insights: List<RiskInsight>) {
    if (insights.isEmpty()) return
    Text(
        text = "Risk Insights",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold
    )
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        insights.forEach { insight ->
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = insight.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = insight.summary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    RiskScoreBadge(insight.scoreLabel, insight.score)
                }
            }
        }
    }
}

@Composable
private fun RiskScoreBadge(
    label: String,
    score: Int
) {
    val gradient = when {
        score >= 80 -> Brush.horizontalGradient(
            listOf(
                MaterialTheme.colorScheme.tertiary,
                MaterialTheme.colorScheme.tertiaryContainer
            )
        )
        score >= 60 -> Brush.horizontalGradient(
            listOf(
                MaterialTheme.colorScheme.secondary,
                MaterialTheme.colorScheme.secondaryContainer
            )
        )
        else -> Brush.horizontalGradient(
            listOf(
                MaterialTheme.colorScheme.error,
                MaterialTheme.colorScheme.errorContainer
            )
        )
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(brush = gradient, shape = RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "$score / 100",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun MomentumSection(insights: List<MomentumInsight>) {
    if (insights.isEmpty()) return
    Text(
        text = "Momentum",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        insights.forEach { insight ->
            val background = if (insight.isPositive) {
                MaterialTheme.colorScheme.tertiaryContainer
            } else {
                MaterialTheme.colorScheme.errorContainer
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(background, RoundedCornerShape(12.dp))
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = insight.periodLabel,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = String.format("%.2f%%", insight.percentChange),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun PeerSection(peers: List<PeerComparison>, currency: String?) {
    if (peers.isEmpty()) return
    Text(
        text = "Peer Comparison",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold
    )
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        peers.forEach { peer ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = peer.ticker,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = peer.name ?: "",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        AssistChip(
                            onClick = {},
                            label = { Text(peer.verdict.headline) },
                            enabled = false
                        )
                    }
                    peer.price?.let {
                        Text(
                            text = currencyFormatter(currency).format(it),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    peer.performanceDelta?.let {
                        Text(
                            text = "Δ vs primary: ${String.format("%.2f%%", it)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DividendSection(dividendInsight: DividendInsight?) {
    dividendInsight ?: return
    Text(
        text = "Dividend Outlook",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold
    )
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Yield: ${dividendInsight.yield?.let { String.format("%.2f%%", it) } ?: "—"}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Payout ratio: ${dividendInsight.payoutRatio?.let { String.format("%.0f%%", it * 100) } ?: "—"}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = dividendInsight.consistencyNarrative,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun MermaidSection(definition: String) {
    if (definition.isBlank()) return
    Text(
        text = "Decision Flow",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold
    )
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            MermaidDiagram(definition)
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun MermaidDiagram(definition: String) {
    val html = remember(definition) { mermaidHtml(definition) }
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp),
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                setBackgroundColor(Color.TRANSPARENT)
                webChromeClient = WebChromeClient()
                webViewClient = WebViewClient()
            }
        },
        update = { webView ->
            webView.loadDataWithBaseURL(
                "https://mermaid.ink/",
                html,
                "text/html",
                "UTF-8",
                null
            )
        }
    )
}

private fun mermaidHtml(definition: String): String {
    val escaped = definition
        .replace("\\", "\\\\")
        .replace("`", "\\`")
        .replace("\$", "\\$")
    return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8"/>
            <style>
                :root { color-scheme: light dark; }
                body { margin: 0; background: transparent; font-family: sans-serif; }
                .mermaid { width: 100%; height: 100%; }
                @media (prefers-color-scheme: dark) {
                    body { color: #e4e2ff; }
                }
            </style>
            <script src="https://cdn.jsdelivr.net/npm/mermaid@10/dist/mermaid.min.js"></script>
            <script>
                document.addEventListener("DOMContentLoaded", function() {
                    mermaid.initialize({ startOnLoad: true, securityLevel: "loose", theme: "neutral" });
                });
            </script>
        </head>
        <body>
            <div class="mermaid">$escaped</div>
        </body>
        </html>
    """.trimIndent()
}

@Composable
private fun LlmOpinionSection(html: String?) {
    Text(
        text = "Second Opinion",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold
    )
    if (!html.isNullOrBlank()) {
        HtmlBlock(html)
    } else {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Text(
                text = stringResource(id = R.string.llm_missing),
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
    SignatureFooter()
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun HtmlBlock(content: String) {
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp),
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = false
                setBackgroundColor(Color.TRANSPARENT)
            }
        },
        update = { webView ->
            webView.loadDataWithBaseURL(
                null,
                """
                    <html>
                    <head>
                        <meta charset="utf-8"/>
                        <style>
                            :root { color-scheme: light dark; }
                            body {
                                margin: 0;
                                padding: 0.5rem;
                                background: transparent;
                                font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
                                color: #1d1b20;
                            }
                            @media (prefers-color-scheme: dark) {
                                body { color: #e6e1e5; }
                                a { color: #aad1ff; }
                            }
                            h2, h3 { margin-top: 0.75rem; margin-bottom: 0.25rem; }
                            ul { padding-left: 1.2rem; margin-top: 0.25rem; }
                            li { margin-bottom: 0.25rem; }
                        </style>
                    </head>
                    <body>${content}</body>
                    </html>
                """.trimIndent(),
                "text/html",
                "UTF-8",
                null
            )
        }
    )
}

private data class VerdictColors(
    val cardColor: androidx.compose.ui.graphics.Color,
    val badgeColor: androidx.compose.ui.graphics.Color,
    val textColor: androidx.compose.ui.graphics.Color
)

@Composable
private fun verdictColors(verdict: StockVerdict): VerdictColors {
    return when (verdict.displayColor) {
        VerdictColor.Success -> VerdictColors(
            cardColor = MaterialTheme.colorScheme.tertiaryContainer,
            badgeColor = MaterialTheme.colorScheme.tertiary,
            textColor = MaterialTheme.colorScheme.onTertiary
        )
        VerdictColor.Warning -> VerdictColors(
            cardColor = MaterialTheme.colorScheme.secondaryContainer,
            badgeColor = MaterialTheme.colorScheme.secondary,
            textColor = MaterialTheme.colorScheme.onSecondary
        )
        VerdictColor.Danger -> VerdictColors(
            cardColor = MaterialTheme.colorScheme.errorContainer,
            badgeColor = MaterialTheme.colorScheme.error,
            textColor = MaterialTheme.colorScheme.onError
        )
    }
}

private fun currencyFormatter(currency: String? = "USD"): NumberFormat =
    NumberFormat.getCurrencyInstance(Locale.US).apply {
        currency?.let {
            try {
                this.currency = java.util.Currency.getInstance(it)
            } catch (_: IllegalArgumentException) {
            }
        }
    }


@Composable
private fun SignatureFooter() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        androidx.compose.foundation.Image(
            painter = androidx.compose.ui.res.painterResource(id = R.drawable.bd_finance_footer),
            contentDescription = "BD Finance footer",
            modifier = Modifier.height(24.dp)
        )
        Text(
            text = "BD Finance 2025\u2122 — from BD, to my friends",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

