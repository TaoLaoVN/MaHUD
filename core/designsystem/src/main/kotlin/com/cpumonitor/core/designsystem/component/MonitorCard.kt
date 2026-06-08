package com.cpumonitor.core.designsystem.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import com.cpumonitor.core.designsystem.theme.MonitorCardShape
import com.cpumonitor.core.designsystem.theme.MonitorDimens
import com.cpumonitor.core.designsystem.theme.isAmoledTheme

/**
 * Default styling tokens for monitoring cards.
 */
object MonitorCardDefaults {
    val shape: Shape = MonitorCardShape
    val contentPadding: Dp = MonitorDimens.cardPadding

    @Composable
    fun cardColors(
        containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    ) = CardDefaults.cardColors(
        containerColor = containerColor,
        contentColor = contentColor,
    )

    @Composable
    fun elevatedCardColors() = cardColors(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
    )

    @Composable
    fun amoledAwareCardColors() = if (isAmoledTheme()) {
        cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
        )
    } else {
        elevatedCardColors()
    }
}

/**
 * Base card container for all monitoring widgets.
 *
 * Provides consistent padding, shape, and AMOLED-aware surface colors.
 */
@Composable
fun MonitorCard(
    modifier: Modifier = Modifier,
    containerColor: Color = MonitorCardDefaults.amoledAwareCardColors().containerColor,
    contentColor: Color = MonitorCardDefaults.amoledAwareCardColors().contentColor,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MonitorCardDefaults.shape,
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = MonitorDimens.cardElevation),
    ) {
        Column(
            modifier = Modifier.padding(MonitorCardDefaults.contentPadding),
            content = content,
        )
    }
}

/**
 * Card header row with title and optional trailing content (e.g. live badge).
 */
@Composable
fun MonitorCardHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    trailingContent: @Composable (() -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        trailingContent?.invoke()
    }
}

/**
 * Large metric readout used inside monitoring cards.
 */
@Composable
fun MonitorMetricValue(
    value: String,
    unit: String,
    modifier: Modifier = Modifier,
    label: String? = null,
    valueColor: Color = MaterialTheme.colorScheme.primary,
    compact: Boolean = false,
) {
    val valueStyle = if (compact) {
        MaterialTheme.typography.titleLarge
    } else {
        MaterialTheme.typography.headlineSmall
    }.copy(fontFamily = FontFamily.Monospace)

    Column(modifier = modifier) {
        if (label != null) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Row(verticalAlignment = Alignment.Bottom) {
            AnimatedContent(
                targetState = value,
                transitionSpec = {
                    (fadeIn() + slideInVertically { it / 2 }) togetherWith
                        (fadeOut() + slideOutVertically { -it / 2 })
                },
                label = "metric_value",
            ) { animatedValue ->
                Text(
                    text = animatedValue,
                    style = valueStyle,
                    color = valueColor,
                )
            }
            if (unit.isNotEmpty()) {
                Text(
                    text = unit,
                    modifier = Modifier.padding(
                        start = MonitorDimens.spacingXs,
                        bottom = MonitorDimens.spacingXs,
                    ),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
