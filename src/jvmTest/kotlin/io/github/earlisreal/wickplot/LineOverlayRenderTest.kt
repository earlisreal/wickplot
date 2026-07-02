package io.github.earlisreal.wickplot

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeCanvas
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.font.createFontFamilyResolver
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ImageInfo
import org.jetbrains.skia.Surface
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Offscreen pixel-level checks that [drawCandlestickChart] actually paints each [LineOverlay]. Flat
 * bars keep the viewport's price range predictable so the horizontal overlay lines land inside the
 * plot; the assertions scan the raw bitmap for the overlays' exact colours (which appear nowhere in
 * [ChartColors.Dark] or the test data).
 */
class LineOverlayRenderTest {

    private val magenta = Color(0xFFFF00FF)
    private val cyan = Color(0xFF00FFFF)

    @Test
    fun `draws every overlay in its own colour`() {
        val pixels = renderPixels(
            overlays = listOf(
                LineOverlay(points = horizontalLine(100.8), color = magenta, strokeWidth = 4f),
                LineOverlay(points = horizontalLine(99.2), color = cyan, strokeWidth = 4f),
            ),
        )
        assertTrue(pixels.contains(0xFFFF00FF.toInt()), "magenta overlay should be painted")
        assertTrue(pixels.contains(0xFF00FFFF.toInt()), "cyan overlay should be painted")
    }

    @Test
    fun `overlay without an explicit colour uses the theme overlay colour`() {
        val pixels = renderPixels(
            overlays = listOf(LineOverlay(points = horizontalLine(100.8), strokeWidth = 4f)),
        )
        val themeOverlay = 0xFF3FB950.toInt() // ChartColors.Dark.overlay
        assertTrue(pixels.contains(themeOverlay), "default-coloured overlay should use ChartColors.overlay")
    }

    // ── Fixtures ──────────────────────────────────────────────────────────────

    /** Identical bars so the fitted price range is exactly [99, 101] every run. */
    private fun flatBars(n: Int = 60): List<OhlcvCandle> {
        val day = LocalDate(2025, 6, 2)
        return (0 until n).map { i ->
            val t = LocalTime(9 + (30 + i) / 60, (30 + i) % 60)
            OhlcvCandle(LocalDateTime(day, t), open = 100.0, high = 101.0, low = 99.0, close = 100.5, volume = 50_000)
        }
    }

    private fun horizontalLine(value: Double, n: Int = 60): List<LinePoint> =
        (0 until n).map { LinePoint(barIndex = it, value = value) }

    /** Renders the chart offscreen and returns every pixel as ARGB ints. */
    private fun renderPixels(overlays: List<LineOverlay>, width: Int = 500, height: Int = 300): IntArray {
        val bars = flatBars()
        val viewport = ChartViewport.fit(bars, BarWindow.initial(bars.size))
        val surface = Surface.makeRasterN32Premul(width, height)
        val density = Density(1f)
        val textMeasurer = TextMeasurer(createFontFamilyResolver(), density, LayoutDirection.Ltr)
        CanvasDrawScope().draw(
            density = density,
            layoutDirection = LayoutDirection.Ltr,
            canvas = surface.canvas.asComposeCanvas(),
            size = Size(width.toFloat(), height.toFloat()),
        ) {
            drawCandlestickChart(
                bars = bars,
                markers = emptyList(),
                viewport = viewport,
                colors = ChartColors.Dark,
                textMeasurer = textMeasurer,
                title = "TEST",
                overlays = overlays,
            )
        }
        val bitmap = Bitmap().apply { allocPixels(ImageInfo.makeN32Premul(width, height)) }
        surface.readPixels(bitmap, 0, 0)
        val pixels = IntArray(width * height)
        var i = 0
        for (y in 0 until height) for (x in 0 until width) pixels[i++] = bitmap.getColor(x, y)
        return pixels
    }
}
