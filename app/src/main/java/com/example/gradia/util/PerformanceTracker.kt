package com.example.gradia.util

import android.util.Log
import kotlin.system.measureTimeMillis

object PerformanceTracker {

    private const val TAG = "PerfTracker"

    fun track(operation: String, block: () -> Unit) {
        val ms = measureTimeMillis(block)
        Log.d(TAG, "$operation completado en ${ms}ms")
    }

    suspend fun <T> trackSuspend(operation: String, block: suspend () -> T): T {
        val start = System.currentTimeMillis()
        val result = block()
        val elapsed = System.currentTimeMillis() - start
        Log.d(TAG, "$operation completado en ${elapsed}ms")
        return result
    }

    fun assertUnder(maxMs: Long, operation: String, block: () -> Unit) {
        val ms = measureTimeMillis(block)
        if (ms > maxMs) {
            Log.w(TAG, "$operation superó el límite de ${maxMs}ms: ${ms}ms")
        } else {
            Log.d(TAG, "$operation dentro del límite: ${ms}ms")
        }
    }
}
