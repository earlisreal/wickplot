# wickplot

Candlestick / trading charts for **Compose Multiplatform**, drawn directly on a native Compose
`Canvas` — no WebView, no JS bridge, no platform chart library underneath.

> A *wick* is the thin line above or below a candle's body, marking the bar's high and low.

![wickplot dark sample](https://github.com/user-attachments/assets/215f977d-c31b-400d-94b9-965d645ed24a)

## Features

- Candlesticks + volume band from any `List<Candle>` — implement the small `Candle` interface on
  your own bar type (zero-copy), or use the bundled `OhlcvCandle`
- VWAP overlay (`vwap: List<LinePoint>`)
- Exact-price trade entry/exit markers (`PriceMarker` diamonds)
- Crosshair with OHLCV legend, price axis, adaptive time axis (intraday `HH:MM` / daily `MM-DD`)
- Cursor-anchored scroll zoom and drag pan, clamped to the data
- `ChartColors.Dark` / `ChartColors.Light` presets, fully customizable
- Trade-centric initial framing via `ChartInitialView` (calendar or intraday modes)
- Pure, unit-tested pan/zoom/viewport math; the renderer is exercised by a Skia screenshot harness

Targets: **JVM/Desktop, iOS (arm64, simulatorArm64), wasmJs**. License: Apache-2.0.

## Install

```kotlin
commonMain.dependencies {
    implementation("io.github.earlisreal:wickplot:0.1.0")
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
    vwap = emptyList(),          // optional overlay, List<LinePoint>
    intraday = false,            // switches the time-axis format
)
```

Drag to pan, scroll to zoom (anchored at the cursor), hover for a crosshair with the bar's OHLCV.

Light theme:

![wickplot light sample](https://github.com/user-attachments/assets/ea669e22-db90-4942-a836-e728928595db)

## Why not Vico?

Vico is a great general-purpose Compose chart engine and has a candlestick layer, but no built-in
trading overlays. wickplot is purpose-built for trade analysis: volume, VWAP, and exact-price trade
markers out of the box, with indicator panes on the roadmap.

## Roadmap

- Moving-average overlays (SMA / EMA / WMA / Bollinger)
- RSI & MACD sub-panes (stacked panes sharing the X axis)
- Android target

## License

[Apache License 2.0](LICENSE)
