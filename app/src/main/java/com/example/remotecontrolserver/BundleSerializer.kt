package com.example.remotecontrolserver

import android.os.Bundle

object BundleSerializer {
    fun getString(bundle: Bundle, key: String): String? {
        return bundle.getString(key)
    }
}
