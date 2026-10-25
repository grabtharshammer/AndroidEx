package com.androidex.trailglance

import android.content.Context
import android.content.SharedPreferences

/** The last Trailforks notification we saw, so the main screen can show exactly what was read. */
object DebugState {
    const val PREFS = "debug"
    const val KEY_PACKAGE = "package"
    const val KEY_TEXT = "text"
    const val KEY_ONGOING = "ongoing"
    const val KEY_TIME = "time"
    const val KEY_MIRRORING = "mirroring"

    fun prefs(context: Context): SharedPreferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun record(context: Context, pkg: String, ongoing: Boolean, text: String, mirroring: Boolean) {
        prefs(context).edit()
            .putString(KEY_PACKAGE, pkg)
            .putString(KEY_TEXT, text)
            .putBoolean(KEY_ONGOING, ongoing)
            .putLong(KEY_TIME, System.currentTimeMillis())
            .putBoolean(KEY_MIRRORING, mirroring)
            .apply()
    }

    fun setMirroring(context: Context, mirroring: Boolean) {
        prefs(context).edit().putBoolean(KEY_MIRRORING, mirroring).apply()
    }
}
