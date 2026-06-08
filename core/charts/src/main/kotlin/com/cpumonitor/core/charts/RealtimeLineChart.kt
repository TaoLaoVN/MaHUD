package com.cpumonitor.core.charts

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cpumonitor.core.designsystem.model.ChartSeriesUiState
import com.cpumonitor.core.designsystem.theme.MonitorDimens
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.core.cartesian.AutoScrollCondition
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries

/**
 * Realtime rolling line chart for monitoring metrics.
 *
 * Consumes immutable [ChartSeriesUiState] and animates as new samples arrive.
 *
 * @param series immutable chart data from ViewModel StateFlow.
 * @param lineColor stroke color; falls back to [defaultLineColor] or theme primary.
 * @param showAxis when false, hides axes for compact dashboard tiles.
 */
@Composable
fun RealtimeLineChart(
    series: ChartSeriesUiState,
    modifier: Modifier = Modifier,
    lineColor: Color? = series.lineColor,
    defaultLineColor: Color = MaterialTheme.colorScheme.primary,
    showAxis: Boolean = false,
    chartHeight: Dp = MonitorDimens.chartHeight,
) {
    val modelProducer = remember(series.id) { CartesianChartModelProducer() }

    LaunchedEffect(series.values) {
        if (series.values.isEmpty()) return@LaunchedEffect
        modelProducer.runTransaction {
            lineSeries { series(series.values) }
        }
    }

    RealtimeChartHost(
        modelProducer = modelProducer,
        modifier = modifier,
        showAxis = showAxis,
        chartHeight = chartHeight,
        lineColors = listOf(lineColor ?: defaultLineColor),
    )
}

/**
 * Multi-series realtime line chart for comparing metrics (e.g. big/little cores).
 */
@Composable
fun RealtimeMultiLineChart(
    seriesList: List<ChartSeriesUiState>,
    modifier: Modifier = Modifier,
    showAxis: Boolean = false,
    chartHeight: Dp = MonitorDimens.chartHeightLarge,
) {
    val modelProducer = remember(seriesList.map { it.id }) {
        CartesianChartModelProducer()
    }

    LaunchedEffect(seriesList.map { it.values }) {
        val nonEmpty = seriesList.filter { it.values.isNotEmpty() }
        if (nonEmpty.isEmpty()) return@LaunchedEffect
        modelProducer.runTransaction {
            lineSeries {
                nonEmpty.forEach { chartSeries ->
                    series(chartSeries.values)
                }
            }
        }
    }

    RealtimeChartHost(
        modelProducer = modelProducer,
        modifier = modifier,
        showAxis = showAxis,
        chartHeight = chartHeight,
        lineColors = seriesList.mapIndexed { index, series ->
            series.lineColor ?: MonitorChartDefaults.fallbackLineColor(index)
        },
    )
}

@Composable
private fun RealtimeChartHost(
    modelProducer: CartesianChartModelProducer,
    modifier: Modifier,
    showAxis: Boolean,
    chartHeight: Dp,
    lineColors: List<Color> = emptyList(),
) {
    val scrollState = rememberVicoScrollState(
        scrollEnabled = true,
        autoScrollCondition = AutoScrollCondition.OnModelGrowth,
    )

    val resolvedColors = lineColors.ifEmpty {
        listOf(MaterialTheme.colorScheme.primary)
    }

    val lineLayer = rememberLineCartesianLayer(
        lineProvider = LineCartesianLayer.LineProvider.series(
            resolvedColors.map { color ->
                LineCartesianLayer.rememberLine(
                    fill = LineCartesianLayer.LineFill.single(fill(color)),
                )
            },
        ),
    )

    CartesianChartHost(
        chart = rememberCartesianChart(
            lineLayer,
            startAxis = if (showAxis) VerticalAxis.rememberStart() else null,
            bottomAxis = if (showAxis) HorizontalAxis.rememberBottom() else null,
        ),
        modelProducer = modelProducer,
        modifier = modifier
            .fillMaxWidth()
            .height(chartHeight),
        scrollState = scrollState,
    )
}
