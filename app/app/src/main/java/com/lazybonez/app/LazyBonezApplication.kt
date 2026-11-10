package com.lazybonez.app

import android.app.Application
import io.opentelemetry.android.OpenTelemetryRum
import io.opentelemetry.android.agent.OpenTelemetryRumInitializer

class LazyBonezApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        rum = OpenTelemetryRumInitializer.initialize(
            context = this,
            configuration = {},
        )
    }

    companion object {
        @JvmStatic
        lateinit var rum: OpenTelemetryRum
    }
}
