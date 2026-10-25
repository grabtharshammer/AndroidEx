package com.androidex.trailglance

import android.Manifest
import android.app.Activity
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.format.DateUtils
import android.view.WindowInsets
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class MainActivity : Activity() {

    private lateinit var status: TextView
    private lateinit var lastSeen: TextView

    // Held in a field: SharedPreferences only keeps a weak reference to listeners.
    private val prefsListener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ -> refresh() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val pad = (16 * resources.displayMetrics.density).toInt()
        val column = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad, pad, pad)
        }

        status = TextView(this).apply { textSize = 16f }
        lastSeen = TextView(this).apply {
            textSize = 14f
            setPadding(0, pad, 0, 0)
            setTextIsSelectable(true)
        }

        column.addView(status)
        column.addView(button("1. Allow notifications") {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 0)
        })
        column.addView(button("2. Grant notification access") {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        })
        column.addView(button("Blocked? App info → ⋮ → Allow restricted settings") {
            startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", packageName, null)))
        })
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
            column.addView(button("3. Live Update settings") {
                startActivity(
                    Intent(Settings.ACTION_APP_NOTIFICATION_PROMOTION_SETTINGS)
                        .putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                )
            })
        }
        column.addView(button("Test: show a fake ride") {
            LiveUpdate.post(this, TrailforksParser.parse("Recording 5:10 - 75ft"), null)
            status.postDelayed(::refresh, 500)
        })
        column.addView(button("Clear") {
            LiveUpdate.cancel(this)
            DebugState.setMirroring(this, false)
            status.postDelayed(::refresh, 500)
        })
        column.addView(lastSeen)

        val scroll = ScrollView(this).apply { addView(column) }
        scroll.setOnApplyWindowInsetsListener { v, insets ->
            val bars = insets.getInsets(WindowInsets.Type.systemBars() or WindowInsets.Type.displayCutout())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
        setContentView(scroll)
    }

    override fun onResume() {
        super.onResume()
        DebugState.prefs(this).registerOnSharedPreferenceChangeListener(prefsListener)
        refresh()
    }

    override fun onPause() {
        super.onPause()
        DebugState.prefs(this).unregisterOnSharedPreferenceChangeListener(prefsListener)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        refresh()
    }

    private fun button(label: String, onClick: () -> Unit) = Button(this).apply {
        text = label
        isAllCaps = false
        setOnClickListener { onClick() }
    }

    private fun refresh() {
        val nm = getSystemService(NotificationManager::class.java)
        val canNotify = checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        val hasAccess = nm.isNotificationListenerAccessGranted(ComponentName(this, TrailforksListener::class.java))
        val canPromote = LiveUpdate.canPromote(this)
        val promoted = when (LiveUpdate.isShowingPromoted(this)) {
            null -> "not showing"
            true -> "showing as Live Update ✓"
            false -> "showing, but NOT promoted to a Live Update"
        }
        status.text = buildString {
            appendLine("${mark(canNotify)} Notifications allowed")
            appendLine("${mark(hasAccess)} Notification access (reads Trailforks)")
            appendLine("${mark(canPromote)} Live Updates allowed")
            append("Ride notification: $promoted")
        }

        val p = DebugState.prefs(this)
        val time = p.getLong(DebugState.KEY_TIME, 0)
        lastSeen.text = if (time == 0L) {
            "No Trailforks notification seen yet. Start a recording in Trailforks."
        } else {
            val raw = p.getString(DebugState.KEY_TEXT, "").orEmpty()
            val stats = TrailforksParser.parse(raw)
            buildString {
                appendLine("Last Trailforks notification (${DateUtils.getRelativeTimeSpanString(time)}):")
                appendLine("  app: ${p.getString(DebugState.KEY_PACKAGE, "")}")
                appendLine("  text: \"$raw\"")
                appendLine("  ongoing: ${p.getBoolean(DebugState.KEY_ONGOING, false)}")
                appendLine("  parsed time: ${stats.elapsedSeconds?.let { DateUtils.formatElapsedTime(it) } ?: "—"}")
                appendLine("  parsed distance: ${stats.distance ?: "—"}")
                append("  mirroring: ${p.getBoolean(DebugState.KEY_MIRRORING, false)}")
            }
        }
    }

    private fun mark(ok: Boolean) = if (ok) "✓" else "✗"
}
