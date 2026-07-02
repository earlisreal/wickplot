package io.github.earlisreal.wickplot

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import kotlinx.datetime.LocalDate

/**
 * Compile guard for the README's "Drawing indicators" sample — keep this file and the README
 * snippet in sync so the documented code can't rot when the API changes. Never runs; compiling is
 * the assertion.
 */

private fun sma(bars: List<Candle>, period: Int): List<LinePoint> =
    ((period - 1) until bars.size).map { i ->
        LinePoint(barIndex = i, value = ((i - period + 1)..i).sumOf { bars[it].close } / period)
    }

private fun sessionVwap(bars: List<Candle>): List<LinePoint> {
    var tpv = 0.0
    var vol = 0.0
    var day: LocalDate? = null
    return bars.mapIndexedNotNull { i, b ->
        if (b.timestamp.date != day) { tpv = 0.0; vol = 0.0; day = b.timestamp.date }
        tpv += (b.high + b.low + b.close) / 3.0 * b.volume
        vol += b.volume
        if (vol > 0.0) LinePoint(barIndex = i, value = tpv / vol) else null
    }
}

@Suppress("unused")
@Composable
private fun readmeIndicatorsSample(bars: List<Candle>) {
    CandlestickCanvasChart(
        bars = bars,
        markers = emptyList(),
        title = "ACME · 1m",
        overlays = listOf(
            LineOverlay(points = sessionVwap(bars), label = "VWAP"),
            LineOverlay(points = sma(bars, 20), label = "SMA 20", color = Color(0xFF64B5F6)),
            LineOverlay(points = sma(bars, 50), color = Color(0xFFFFB74D), strokeWidth = 2f),
        ),
        intraday = true,
    )
}
