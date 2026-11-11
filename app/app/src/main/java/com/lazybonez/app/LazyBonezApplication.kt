package com.lazybonez.app

import android.app.Application
import io.opentelemetry.android.OpenTelemetryRum
import io.opentelemetry.android.agent.OpenTelemetryRumInitializer
import kotlin.time.Duration.Companion.seconds

class LazyBonezApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        rum = OpenTelemetryRumInitializer.initialize(
            context = this,
            configuration = {
                httpExport {
                    baseUrl = "http://10.0.2.2:4318"
                }
                diskBuffering {
                    enabled(false) // For demo purposes, not recommended for production.
                }
                disableMetrics()
            },
        )
    }

    companion object {
        @JvmStatic
        lateinit var rum: OpenTelemetryRum
    }
}
