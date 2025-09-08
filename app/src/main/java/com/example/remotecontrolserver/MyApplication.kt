package com.example.remotecontrolserver

import android.app.Application
import com.google.crypto.tink.AeadConfig
import timber.log.Timber

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Init Timber
        Timber.plant(Timber.DebugTree())

        // Initialize Tink AEAD
        try {
            AeadConfig.register()
        } catch (e: Exception) {
            Timber.w(e, "Tink init failed")
        }
    }
}