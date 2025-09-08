package com.example.remotecontrolserver

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

class ScreenCaptureAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Handle touch events from remote client if implemented
    }

    override fun onInterrupt() {
        // Required override
    }
}
