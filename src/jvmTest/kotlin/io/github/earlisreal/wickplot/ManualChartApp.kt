package io.github.earlisreal.wickplot

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.singleWindowApplication

/**
 * Interactive manual-verification window: `./gradlew runSample`. Opens the real
 * [CandlestickCanvasChart] filled with deterministic sample data (500 daily bars, trade markers,
 * an SMA-20 overlay) so gestures can be checked by hand — drag to pan (slowly too: sub-bar
 * movement must accumulate, not stall), scroll to zoom around the cursor, hover for the
 * crosshair + OHLCV legend. Lives in jvmTest so it never ships in the library artifact.
 */
fun main() = singleWindowApplication(
    title = "wickplot — manual verification (drag to pan, scroll to zoom, hover for crosshair)",
    state = WindowState(size = DpSize(1100.dp, 620.dp)),
) {
    val bars = remember { sampleBars(count = 500) }
    val markers = remember(bars) {
        listOf(
            PriceMarker(barIndex = 412, price = bars[412].low, isBuy = true),
            PriceMarker(barIndex = 452, price = bars[452].high, isBuy = false),
        )
    }
    val sma20 = remember(bars) {
        LineOverlay(
            points = (19 until bars.size).map { i ->
                LinePoint(barIndex = i, value = (i - 19..i).sumOf { bars[it].close } / 20.0)
            },
            label = "SMA 20",
        )
    }
    CandlestickCanvasChart(
        bars = bars,
        markers = markers,
        title = "ACME · D",
        modifier = Modifier.fillMaxSize(),
        overlays = listOf(sma20),
    )
}
