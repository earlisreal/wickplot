package io.github.earlisreal.wickplot

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.plus
import java.util.Random

/**
 * Deterministic sample OHLCV data shared by the screenshot harness ([CanvasChartSampleRenderTest])
 * and the interactive manual-verification window ([main]). Fixed seeds keep both stable across runs.
 */

/** Deterministic random-walk daily OHLCV. */
internal fun sampleBars(count: Int = 160): List<OhlcvCandle> {
    val rnd = Random(42)
    val bars = ArrayList<OhlcvCandle>(count)
    var price = 100.0
    var date = LocalDate(2025, 6, 2)
    repeat(count) {
        val open = price
        val close = (open + (rnd.nextDouble() - 0.47) * 3.0).coerceAtLeast(5.0)
        val high = maxOf(open, close) + rnd.nextDouble() * 1.8
        val low = (minOf(open, close) - rnd.nextDouble() * 1.8).coerceAtLeast(1.0)
        val volume = (600_000 + rnd.nextInt(2_200_000)).toLong()
        bars.add(OhlcvCandle(LocalDateTime(date, LocalTime(0, 0)), open, high, low, close, volume))
        price = close
        date = date.plus(DatePeriod(days = 1))
    }
    return bars
}

/** Deterministic one-minute bars starting 09:30, for the intraday sample. */
internal fun minuteSampleBars(count: Int = 180): List<OhlcvCandle> {
    val rnd = Random(7)
    val bars = ArrayList<OhlcvCandle>(count)
    var price = 50.0
    val day = LocalDate(2025, 6, 2)
    repeat(count) { i ->
        val open = price
        val close = (open + (rnd.nextDouble() - 0.48) * 0.6).coerceAtLeast(1.0)
        val high = maxOf(open, close) + rnd.nextDouble() * 0.35
        val low = (minOf(open, close) - rnd.nextDouble() * 0.35).coerceAtLeast(0.5)
        val volume = (40_000 + rnd.nextInt(180_000)).toLong()
        val t = LocalTime(9 + (30 + i) / 60, (30 + i) % 60)
        bars.add(OhlcvCandle(LocalDateTime(day, t), open, high, low, close, volume))
        price = close
    }
    return bars
}

/** Cumulative typical-price VWAP, one point per bar. */
internal fun vwapLineFor(bars: List<OhlcvCandle>): List<LinePoint> {
    var tpv = 0.0
    var vol = 0.0
    return bars.mapIndexed { i, b ->
        val typical = (b.high + b.low + b.close) / 3.0
        tpv += typical * b.volume
        vol += b.volume
        LinePoint(barIndex = i, value = if (vol > 0) tpv / vol else b.close)
    }
}
