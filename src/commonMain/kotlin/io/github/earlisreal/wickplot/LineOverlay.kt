package io.github.earlisreal.wickplot

import androidx.compose.ui.graphics.Color

/**
 * One polyline drawn over the candles — the chart's only indicator primitive. The host computes the
 * values (VWAP, moving average, band edge, …) and the chart just draws them; wickplot never knows
 * what the line means.
 *
 * @property points bar-index-anchored values, in ascending bar order.
 * @property label when set, the crosshair legend shows "label value" for the hovered bar.
 * @property color line (and legend) colour; null uses [ChartColors.overlay].
 * @property strokeWidth line width in px, in the same DrawScope units as the renderer's other strokes.
 */
data class LineOverlay(
    val points: List<LinePoint>,
    val label: String? = null,
    val color: Color? = null,
    val strokeWidth: Float = 1.5f,
)

/** The legend lines for [barIndex]: each labeled overlay paired with its value at that bar. */
internal fun overlayLegendEntries(overlays: List<LineOverlay>, barIndex: Int): List<Pair<LineOverlay, Double>> =
    overlays.mapNotNull { overlay ->
        if (overlay.label == null) null
        else overlay.points.firstOrNull { it.barIndex == barIndex }?.let { overlay to it.value }
    }
