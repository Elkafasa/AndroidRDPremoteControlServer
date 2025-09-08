package com.example.remotecontrolserver

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.MotionEvent
import android.graphics.PointF

class InputHandler : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}

    private val activePointers = mutableMapOf<Int, PointF>()

    fun handleTouch(event: MotionEvent) {
        val pointerCount = event.pointerCount
        for (i in 0 until pointerCount) {
            val id = event.getPointerId(i)
            val x = event.getX(i)
            val y = event.getY(i)
            activePointers[id] = PointF(x, y)
        }
        RemoteInput.sendMultiTouch(activePointers)
    }

    fun handleSwipe(x: Float, y: Float) {
        RemoteInput.sendSwipe(x, y)
    }

    fun handleMultiTouch(points: List<Pair<Float, Float>>) {
        points.forEach { (x, y) ->
            val motionEvent = MotionEvent.obtain(System.currentTimeMillis(), System.currentTimeMillis(), MotionEvent.ACTION_DOWN, x, y, 0)
            handleTouch(motionEvent)
        }
    }
}