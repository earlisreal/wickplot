package io.github.earlisreal.wickplot

import androidx.compose.ui.graphics.Color
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LineOverlayTest {

    private val vwap = LineOverlay(
        points = listOf(LinePoint(0, 10.0), LinePoint(1, 11.0)),
        label = "VWAP",
    )
    private val unlabeled = LineOverlay(
        points = listOf(LinePoint(0, 5.0), LinePoint(1, 6.0)),
        color = Color.Magenta,
    )

    @Test
    fun `legend lists labeled overlays with their value at the bar`() {
        val entries = overlayLegendEntries(listOf(vwap, unlabeled), barIndex = 1)
        assertEquals(listOf(vwap to 11.0), entries)
    }

    @Test
    fun `legend skips overlays without a point at the bar`() {
        val entries = overlayLegendEntries(listOf(vwap), barIndex = 7)
        assertTrue(entries.isEmpty())
    }

    @Test
    fun `legend skips unlabeled overlays`() {
        val entries = overlayLegendEntries(listOf(unlabeled), barIndex = 0)
        assertTrue(entries.isEmpty())
    }
}
