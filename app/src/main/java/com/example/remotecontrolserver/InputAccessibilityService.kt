package com.example.remotecontrolserver

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.view.accessibility.AccessibilityEvent
import timber.log.Timber

class InputAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // not used
    }

    override fun onInterrupt() {
        // not used
    }

    /**
     * Build and dispatch a multi-touch gesture composed of multiple strokes.
     * Points: list of Pair(x, y) per pointer. All pointers are started at t=0 with same duration for genuine multi-touch.
     */
    fun dispatchMultiTouch(points: List<Pair<Float, Float>>, durationMs: Long = 300L) {
        val builder = GestureDescription.Builder()
        val duration = durationMs
        points.forEach { (x, y) ->
            val p = Path()
            p.moveTo(x, y)
            val stroke = GestureDescription.StrokeDescription(p, 0, duration)
            builder.addStroke(stroke)
        }
        val gesture = builder.build()
        dispatchGesture(gesture, object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                Timber.i("Gesture completed")
            }
            override fun onCancelled(gestureDescription: GestureDescription?) {
                Timber.i("Gesture cancelled")
            }
        }, null)
    }

    /**
     * Simple tap
     */
    fun tap(x: Float, y: Float) {
        dispatchMultiTouch(listOf(x to y), 50)
    }

    /**
     * Swipe between two points with simultaneous multi-strokes (for single pointer we still use one stroke)
     */
    fun swipe(startX: Float, startY: Float, endX: Float, endY: Float, durationMs: Long = 300L) {
        val path = Path()
        path.moveTo(startX, startY)
        path.lineTo(endX, endY)
        val stroke = GestureDescription.StrokeDescription(path, 0, durationMs)
        val g = GestureDescription.Builder().addStroke(stroke).build()
        dispatchGesture(g, null, null)
    }
}