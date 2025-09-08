package com.example.remotecontrolserver

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.util.Log
import android.view.accessibility.AccessibilityEvent

/**
 * AccessibilityService used to inject touch/swipe events without root.
 * Must be enabled in Settings > Accessibility.
 */
class RemoteAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "RemoteAccessibilityService"
        var instance: RemoteAccessibilityService? = null
            private set
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        Log.i(TAG, "Accessibility service connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Not used (we only inject events)
    }

    override fun onInterrupt() {
        Log.w(TAG, "Accessibility service interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        Log.i(TAG, "Accessibility service destroyed")
    }

    /**
     * Simulate a tap at (x, y).
     */
    fun performTouch(x: Float, y: Float) {
        try {
            val path = Path().apply {
                moveTo(x, y)
            }
            val stroke = GestureDescription.StrokeDescription(path, 0, 100)
            val gesture = GestureDescription.Builder()
                .addStroke(stroke)
                .build()

            dispatchGesture(gesture, null, null)
            Log.i(TAG, "Performed touch at ($x, $y)")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to perform touch: ${e.message}", e)
        }
    }

    /**
     * Simulate a swipe from center of screen to (x, y).
     */
    fun performSwipe(x: Float, y: Float) {
        try {
            // Example: swipe from screen center to (x, y)
            val metrics = resources.displayMetrics
            val startX = metrics.widthPixels / 2f
            val startY = metrics.heightPixels / 2f

            val path = Path().apply {
                moveTo(startX, startY)
                lineTo(x, y)
            }
            val stroke = GestureDescription.StrokeDescription(path, 0, 300)
            val gesture = GestureDescription.Builder()
                .addStroke(stroke)
                .build()

            dispatchGesture(gesture, null, null)
            Log.i(TAG, "Performed swipe from ($startX, $startY) to ($x, $y)")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to perform swipe: ${e.message}", e)
        }
    }
}
