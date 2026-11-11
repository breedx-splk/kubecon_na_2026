package com.lazybonez.app

import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.StatusCode
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.context.Context

/** Tracks a spin through stopping, plot generation, and result submission. Used on the UI thread. */
internal class ReelStopTrace {
    private var spin: Span? = null
    private var spinContext: Context? = null
    private var stop: Span? = null
    private var stopContext: Context? = null
    private var plot: Span? = null
    private val wheels = mutableMapOf<Int, Span>()
    private val stoppedWheels = mutableSetOf<Int>()

    fun startSpin() {
        cancel()
        val context = Context.current()
        val span = tracer.spanBuilder("slot_machine.spin").setParent(context).startSpan()
        spin = span
        spinContext = context.with(span)
    }

    fun startStop() {
        val context = spinContext ?: return
        if (stop != null) return
        val span = tracer.spanBuilder("slot_machine.stop").setParent(context).startSpan()
        stop = span
        stopContext = context.with(span)
    }

    fun startWheel(index: Int) {
        val context = stopContext ?: return
        if (index in wheels || index in stoppedWheels) return
        wheels[index] = tracer.spanBuilder("slot_machine.wheel.decelerate")
            .setParent(context)
            .setAttribute("lazybonez.wheel.number", (index + 1).toLong())
            .startSpan()
    }

    fun recordWheelEmoji(index: Int, emoji: String) {
        wheels[index]?.setAttribute("lazybonez.emoji", emoji)
    }

    fun finishWheel(index: Int) {
        val span = wheels.remove(index) ?: return
        span.end()
        stoppedWheels.add(index)
    }

    fun startPlotGeneration() {
        val context = stopContext ?: return
        if (stoppedWheels.size != 3 || plot != null) return
        plot = tracer.spanBuilder("slot_machine.plot.generate")
            .setParent(context)
            .startSpan()
    }

    fun finishPlotGeneration(): Context {
        plot?.end()
        stop?.end()
        plot = null
        stop = null
        stopContext = null
        return spinContext ?: Context.current()
    }

    fun finishSpin() = finish()

    fun cancel() = finish(cancelled = true)

    fun fail(failure: Exception) = finish(failure = failure)

    private fun finish(cancelled: Boolean = false, failure: Exception? = null) {
        if (cancelled) {
            spin?.setAttribute("lazybonez.spin.cancelled", true)
            stop?.setAttribute("lazybonez.stop.cancelled", true)
            plot?.setAttribute("lazybonez.plot.cancelled", true)
        }
        // Close children before their parent, including when the screen or animation is cancelled.
        for (activeSpan in wheels.values.toList() + listOfNotNull(plot, stop, spin)) {
            if (failure != null) {
                activeSpan.recordException(failure)
                activeSpan.setStatus(StatusCode.ERROR, "Slot machine operation failed")
            }
            activeSpan.end()
        }
        wheels.clear()
        stoppedWheels.clear()
        plot = null
        stop = null
        stopContext = null
        spin = null
        spinContext = null
    }

    private companion object {
        val tracer: Tracer by lazy {
            LazyBonezApplication.rum.openTelemetry.getTracer("com.lazybonez.app.slotmachine")
        }
    }
}
