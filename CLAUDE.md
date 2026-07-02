# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

wickplot is a Compose Multiplatform library: candlestick/trading charts drawn natively on a Compose
`Canvas` (no WebView, no JS bridge). Published to Maven Central as `io.github.earlisreal:wickplot`.
Targets: `jvm` (desktop), `iosArm64`, `iosSimulatorArm64`, `wasmJs`. Single-module Gradle project —
the library is the repo root.

## Commands

```bash
./gradlew jvmTest                 # unit tests (commonTest) + the Skia screenshot harness (jvmTest)
./gradlew build                   # full build, all targets (wasm browser tests need a browser)
./gradlew compileKotlinIosArm64 compileKotlinIosSimulatorArm64 compileKotlinWasmJs  # per-target compile checks (iOS needs macOS)
./gradlew publishToMavenLocal     # dry-run publish into ~/.m2 (signed only if a key is configured)
./gradlew dokkaGenerate           # API docs -> build/dokka/html/
```

Run a single test class: `./gradlew jvmTest --tests "io.github.earlisreal.wickplot.CandlestickChartMathTest"`.
The screenshot harness (`CanvasChartSampleRenderTest`) writes sample PNGs to `build/samples/` — use
them to eyeball rendering changes without a headed app.

## Architecture

Package `io.github.earlisreal.wickplot`, sources in `src/{commonMain,commonTest,jvmTest}`:

- `Candle.kt` — the public data surface: `Candle` interface (timestamp/OHLC/volume; consumers
  implement it on their own bar type), `OhlcvCandle` convenience impl, `PriceMarker` (trade
  diamond), `LinePoint` (overlay polyline point).
- `CandlestickChartMath.kt` — pure pan/zoom/scale math: `BarWindow` (visible index range; clamped,
  cursor-anchored zoom) and `ChartViewport` (price/volume scales, index<->pixel, price<->pixel),
  `niceAxisStep`. Unit-tested — keep it side-effect free.
- `ChartInitialView.kt` — computes the initial `BarWindow` framing a trade (`TradeFramingMode`:
  CALENDAR or INTRADAY). Unit-tested.
- `CandlestickChartRenderer.kt` — `DrawScope.drawCandlestickChart(...)`: the single drawing source
  of truth (grid, volume band, candles, VWAP, markers, crosshair + OHLCV legend, axes). All visual
  changes go here, so the composable and the screenshot harness render identically.
- `CandlestickCanvasChart.kt` — the composable: window/crosshair state + input handling via
  `awaitPointerEventScope` (drag pan, scroll zoom, hover). Note: `onPointerEvent` is not public in
  commonMain — use `awaitPointerEventScope` + `awaitPointerEvent()`.
- `CanvasChartSampleRenderTest.kt` (jvmTest) — offscreen Skia render harness producing PNGs.

## Conventions

- **No new runtime dependencies.** commonMain depends on Compose runtime/foundation/ui and
  kotlinx-datetime only; build-time plugins are fine.
- The published API surface is the types above — changes to them break consumers; prefer additive
  changes with default parameters.
- Keep `kotlin` / `composeMultiplatform` / `kotlinx-datetime` in `gradle/libs.versions.toml` in
  sync with the primary consumer (the eJournal desktop app) when bumping.
- `docs/` is local-only (gitignored). README images are GitHub-hosted user-attachments links
  (upload the `build/samples/*.png` harness output to a GitHub issue and embed the resulting URL) —
  no binaries are committed to the repo.

## Publishing

- vanniktech maven-publish plugin. Coordinates come from `gradle.properties`
  (`GROUP` / `POM_ARTIFACT_ID` / `VERSION_NAME`) — do NOT add an explicit `coordinates()` call;
  it collides with the finalized properties.
- Secrets (`mavenCentralUsername/Password`, `signingInMemoryKey/Password`) live in
  `~/.gradle/gradle.properties` locally and in GitHub Actions secrets on CI — never in the repo.
  Signing tasks are skipped automatically when no key is configured.
- Release flow: set `VERSION_NAME=X.Y.Z` (drop `-SNAPSHOT`) in `gradle.properties`, commit, tag
  `vX.Y.Z`, push the tag — CI runs `publishAndReleaseToMavenCentral`. Then bump to the next
  `-SNAPSHOT`.
- Dokka is the v2 Gradle plugin: the docs task is `dokkaGenerate` (v1's `dokkaHtml` does not exist).
