plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

// wickplot: candlestick / trading charts for Compose Multiplatform, drawn natively on a Compose
// Canvas. Depends only on Compose (runtime/foundation/ui) and kotlinx-datetime — no webview, no
// JS bridge, no app-specific code.
kotlin {
    jvm()
    // No iosX64: Compose Multiplatform 1.11 no longer publishes ios_x64 (Intel simulator) variants.
    iosArm64()
    iosSimulatorArm64()
    wasmJs { browser() }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.ui)
            implementation(libs.kotlinx.datetime)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmTest.dependencies {
            // Skia (skiko) + desktop Compose graphics for the offscreen PNG screenshot harness.
            implementation(compose.desktop.currentOs)
        }
    }
}
