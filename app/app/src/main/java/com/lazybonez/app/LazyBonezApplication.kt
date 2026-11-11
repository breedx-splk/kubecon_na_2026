package com.lazybonez.app

import android.app.Application
import io.opentelemetry.android.OpenTelemetryRum
import io.opentelemetry.android.agent.OpenTelemetryRumInitializer
import io.opentelemetry.api.common.Attributes
import java.util.concurrent.atomic.AtomicLong

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
                instrumentations {
                    activity {
                        screenNameExtractor { activity ->
                            when (activity) {
                                is SplashActivity -> "Splash"
                                is LaunchActivity -> "Home"
                                is AboutActivity -> "About"
                                is PlotifyActivity -> "Plotify"
                                else -> activity.javaClass.simpleName
                            }
                        }
                    }
                }
                disableMetrics()
                globalAttributesSupplier {
                    {
                        Attributes.builder()
                            .put("otel.demo", "don't be lazy")
                            .put("lazybonez.round.number", roundNumber.get())
                            .build()
                    }
                }
            },
        )
    }

    companion object {
        private val roundNumber = AtomicLong(0)

        fun nextRound(): Long = roundNumber.incrementAndGet()

        @JvmStatic
        lateinit var rum: OpenTelemetryRum
    }
}
