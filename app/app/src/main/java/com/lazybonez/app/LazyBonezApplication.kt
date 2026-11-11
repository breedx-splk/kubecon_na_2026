package com.lazybonez.app

import android.app.Application
import io.opentelemetry.android.OpenTelemetryRum
import io.opentelemetry.android.agent.OpenTelemetryRumInitializer
import io.opentelemetry.api.common.AttributeKey.stringKey
import io.opentelemetry.api.common.Attributes

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
                globalAttributes {
                    Attributes.of(stringKey("otel.demo"), "don't be lazy")
                }
            },
        )
    }

    companion object {
        @JvmStatic
        lateinit var rum: OpenTelemetryRum
    }
}
