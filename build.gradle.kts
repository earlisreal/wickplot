plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.mavenPublish)
    alias(libs.plugins.dokka)
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

mavenPublishing {
    // Maven Central via the Sonatype Central Portal. This configures the upload; CI's tag-triggered
    // job runs the publishAndReleaseToMavenCentral task so the deployment releases without a manual
    // Portal click. Coordinates/secrets: GROUP/POM_ARTIFACT_ID/VERSION_NAME in gradle.properties,
    // mavenCentral*/signingInMemoryKey* in ~/.gradle/gradle.properties or CI env.
    publishToMavenCentral()
    signAllPublications()

    // Coordinates come from gradle.properties (GROUP / POM_ARTIFACT_ID / VERSION_NAME) — the plugin
    // reads and finalizes them itself; an explicit coordinates() call here would collide.

    pom {
        name.set("wickplot")
        description.set(
            "Candlestick / trading charts for Compose Multiplatform, drawn natively on a Compose " +
                "Canvas — candles, volume, indicator line overlays, trade entry/exit markers, " +
                "crosshair, and pan/zoom. No WebView, no JS bridge."
        )
        inceptionYear.set("2026")
        url.set("https://github.com/earlisreal/wickplot")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("earlisreal")
                name.set("Earl Savadera")
                url.set("https://github.com/earlisreal")
            }
        }
        scm {
            url.set("https://github.com/earlisreal/wickplot")
            connection.set("scm:git:git://github.com/earlisreal/wickplot.git")
            developerConnection.set("scm:git:ssh://git@github.com/earlisreal/wickplot.git")
        }
    }
}
