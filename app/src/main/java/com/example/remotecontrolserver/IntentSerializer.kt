package com.example.remotecontrolserver

import android.content.Intent

object IntentSerializer {
    fun getExtra(intent: Intent, key: String): String? {
        return intent.extras?.getString(key)
    }
}
