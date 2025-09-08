package com.example.remotecontrolserver

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences

object RemoteServiceHelper {

    private const val PREFS_NAME = "RemoteServicePrefs"
    private const val KEY_PROJECTION_INTENT = "ProjectionIntent"

    fun storeProjectionIntent(context: Context, intent: Intent?) {
        intent ?: return
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString(KEY_PROJECTION_INTENT, IntentSerializer.toString(intent))
        editor.apply()
    }

    fun retrieveProjectionIntent(context: Context): Intent? {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val serialized = prefs.getString(KEY_PROJECTION_INTENT, null) ?: return null
        return IntentSerializer.fromString(serialized)
    }
}
