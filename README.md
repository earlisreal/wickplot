# wickplot

[![Maven Central](https://img.shields.io/maven-central/v/io.github.earlisreal/wickplot)](https://central.sonatype.com/artifact/io.github.earlisreal/wickplot)

Candlestick / trading charts for **Compose Multiplatform**, drawn directly on a native Compose
`Canvas` — no WebView, no JS bridge, no platform chart library underneath.

> A *wick* is the thin line above or below a candle's body, marking the bar's high and low.

![wickplot dark sample](https://github.com/user-attachments/assets/215f977d-c31b-400d-94b9-965d645ed24a)

## Features

- Candlesticks + volume band from any `List<Candle>` — implement the small `Candle` interface on
  your own bar type (zero-copy), or use the bundled `OhlcvCandle`
- Line overlays for host-computed indicators — VWAP, moving averages, band edges
  (`overlays: List<LineOverlay>`; per-line colour, stroke width, and optional legend label)
- Exact-price trade entry/exit markers (`PriceMarker` diamonds)
- Crosshair with OHLCV legend, price axis, adaptive time axis (intraday `HH:MM` / daily `MM-DD`)
- Cursor-anchored scroll zoom and drag pan, clamped to the data
- `ChartColors.Dark` / `ChartColors.Light` presets, fully customizable
- Trade-centric initial framing via `ChartInitialView` (calendar or intraday modes)
- Pure, unit-tested pan/zoom/viewport math; the renderer is exercised by a Skia screenshot harness

wickplot renders what you give it — the Lightweight-Charts philosophy. Indicator *values* (VWAP,
moving averages, …) are computed by your app and passed in as plain `LineOverlay`s; the library
never computes market math.

Targets: **JVM/Desktop, iOS (arm64, simulatorArm64), wasmJs**. License: Apache-2.0.

## Install

```kotlin
commonMain.dependencies {
    implementation("io.github.earlisreal:wickplot:0.1.1")
}
```

## Quick start

```kotlin
val bars: List<Candle> = listOf(
    OhlcvCandle(LocalDateTime(2026, 7, 1, 9, 30), open = 100.0, high = 103.5, low = 99.2, close = 102.8, volume = 120_000),
    // ...
)

CandlestickCanvasChart(
    bars = bars,
    markers = listOf(PriceMarker(barIndex = 12, price = 101.25, isBuy = true)),
    title = "ACME · D",
    colors = ChartColors.Dark,   // or ChartColors.Light
    overlays = emptyList(),      // indicator lines — see "Drawing indicators" below
    intraday = false,            // switches the time-axis format
)
```

Drag to pan, scroll to zoom (anchored at the cursor), hover for a crosshair with the bar's OHLCV.

Light theme:

![wickplot light sample](https://github.com/user-attachments/assets/ea669e22-db90-4942-a836-e728928595db)

## Drawing indicators

Your app computes the values, wickplot draws the lines: anything of shape
`List<Candle> → List<LinePoint>` plugs straight into `overlays`.

```kotlin
// A simple moving average — a few lines of your code.
fun sma(bars: List<Candle>, period: Int): List<LinePoint> =
    ((period - 1) until bars.size).map { i ->
        LinePoint(barIndex = i, value = ((i - period + 1)..i).sumOf { bars[it].close } / period)
    }

// Session VWAP: cumulative typical price × volume, reset at each new trading day.
fun sessionVwap(bars: List<Candle>): List<LinePoint> {
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

CandlestickCanvasChart(
    bars = bars,
    markers = emptyList(),
    title = "ACME · 1m",
    overlays = listOf(
        LineOverlay(points = sessionVwap(bars), label = "VWAP"),          // no color → ChartColors.overlay
        LineOverlay(points = sma(bars, 20), label = "SMA 20", color = Color(0xFF64B5F6)),
        LineOverlay(points = sma(bars, 50), color = Color(0xFFFFB74D), strokeWidth = 2f),
    ),
    intraday = true,
)
```

Overlays with a `label` show their value at the hovered bar in the crosshair legend, in the
overlay's colour; unlabeled overlays are drawn but stay out of the legend. Multi-line indicators
(Bollinger, Keltner) are just several overlays — one per band edge.

## Why not Vico?

Vico is a great general-purpose Compose chart engine and has a candlestick layer, but no built-in
trading overlays. wickplot is purpose-built for trade analysis: volume, indicator line overlays,
and exact-price trade markers out of the box, with indicator panes on the roadmap.

## Roadmap

- Stacked sub-panes sharing the X axis (for host-computed RSI / MACD / volume studies)
- Android target

## License

[Apache License 2.0](LICENSE)
