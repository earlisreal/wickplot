package io.github.earlisreal.wickplot

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.asComposeCanvas
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.font.createFontFamilyResolver
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Surface
import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Not a unit test in the strict sense — it's the screenshot harness for the Canvas chart. It renders
 * the real [drawCandlestickChart] output (dark + light) to PNGs under build/samples/ so the sample
 * can be eyeballed without launching a headed run. Kept as a @Test so it runs via
 * `./gradlew jvmTest --tests "*CanvasChartSampleRenderTest"`.
 */
class CanvasChartSampleRenderTest {

    @Test
    fun `render dark and light sample charts to PNG`() {
        val bars = sampleBars()
        val window = BarWindow.initial(bars.size, maxBars = 120)
        val viewport = ChartViewport.fit(bars, window)
        // A couple of trade fills inside the visible window (entry long, exit).
        val markers = listOf(
            PriceMarker(barIndex = 72, price = bars[72].low, isBuy = true),
            PriceMarker(barIndex = 112, price = bars[112].high, isBuy = false),
        )
        // Free-floating crosshair: an arbitrary cursor pixel inside the plot (scaled DrawScope space).
        val crosshair = Offset(1500f, 500f)

        val outDir = File("build/samples").apply { mkdirs() }
        val dark = renderPng(1100, 620, scale = 2f) {
            drawCandlestickChart(bars, markers, viewport, ChartColors.Dark, it, "ACME · D", crosshair)
        }
        val light = renderPng(1100, 620, scale = 2f) {
            drawCandlestickChart(bars, markers, viewport, ChartColors.Light, it, "ACME · D", crosshair)
        }
        File(outDir, "sample-dark.png").writeBytes(dark)
        File(outDir, "sample-light.png").writeBytes(light)

        // Intraday variant exercising a labeled line overlay (VWAP) + HH:MM time axis.
        val minuteBars = minuteSampleBars()
        val vwap = LineOverlay(points = vwapLineFor(minuteBars), label = "VWAP")
        val ivWindow = BarWindow.initial(minuteBars.size, maxBars = 120)
        val ivViewport = ChartViewport.fit(minuteBars, ivWindow)
        val ivMarkers = listOf(
            PriceMarker(barIndex = 38, price = minuteBars[38].low, isBuy = true),
            PriceMarker(barIndex = 92, price = minuteBars[92].high, isBuy = false),
        )
        val intraday = renderPng(1100, 620, scale = 2f) {
            drawCandlestickChart(minuteBars, ivMarkers, ivViewport, ChartColors.Dark, it, "ACME · 1m", Offset(1200f, 600f), listOf(vwap), true)
        }
        File(outDir, "sample-intraday-vwap.png").writeBytes(intraday)

        assertTrue(dark.size > 1000, "dark PNG should have real content")
        assertTrue(light.size > 1000, "light PNG should have real content")
        assertTrue(intraday.size > 1000, "intraday PNG should have real content")
    }

    @Test
    fun `render social banner hero`() {
        // Chunkier candles than the default sample (maxBars 120) so the daily uptrend reads
        // clearly in the narrower chart panel of the GitHub social banner's split layout.
        val bars = sampleBars()
        val window = BarWindow.initial(bars.size, maxBars = 80)
        val viewport = ChartViewport.fit(bars, window)
        val vwap = LineOverlay(points = vwapLineFor(bars), label = "VWAP")
        // Marker indices are relative to the window start so they stay on-screen regardless of
        // how many bars are visible.
        val buyIndex = window.startIndex + 20
        val sellIndex = window.startIndex + 60
        val markers = listOf(
            PriceMarker(barIndex = buyIndex, price = bars[buyIndex].low, isBuy = true),
            PriceMarker(barIndex = sellIndex, price = bars[sellIndex].high, isBuy = false),
        )
        val crosshair = Offset(1100f, 480f)

        val outDir = File("build/samples").apply { mkdirs() }
        val hero = renderPng(740, 640, scale = 2f) {
            drawCandlestickChart(
                bars, markers, viewport, ChartColors.Light, it, "ACME · D", crosshair, listOf(vwap),
            )
        }
        File(outDir, "banner-hero-light.png").writeBytes(hero)

        assertTrue(hero.size > 1000, "banner hero PNG should have real content")
    }

    private fun renderPng(width: Int, height: Int, scale: Float, block: DrawScope.(TextMeasurer) -> Unit): ByteArray {
        val pxW = (width * scale).toInt()
        val pxH = (height * scale).toInt()
        val surface = Surface.makeRasterN32Premul(pxW, pxH)
        val density = Density(scale)
        val textMeasurer = TextMeasurer(createFontFamilyResolver(), density, LayoutDirection.Ltr)
        CanvasDrawScope().draw(
            density = density,
            layoutDirection = LayoutDirection.Ltr,
            canvas = surface.canvas.asComposeCanvas(),
            size = Size(pxW.toFloat(), pxH.toFloat()),
        ) {
            block(textMeasurer)
        }
        val data = surface.makeImageSnapshot().encodeToData(EncodedImageFormat.PNG)
            ?: error("PNG encode failed")
        return data.bytes
    }
}
