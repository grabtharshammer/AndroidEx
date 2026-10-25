package com.androidex.trailglance

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

/**
 * Re-posts Trailforks' ride stats as an Android 16+ "Live Update" (a promoted ongoing
 * notification), which the system shows on the lock screen, always-on display and as a
 * status bar chip.
 */
object LiveUpdate {
    private const val CHANNEL_ID = "ride"
    private const val NOTIFICATION_ID = 1

    private fun manager(context: Context) = context.getSystemService(NotificationManager::class.java)

    private fun ensureChannel(context: Context) {
        // Live Updates are not shown for IMPORTANCE_MIN channels; keep it audible-free instead.
        val channel = NotificationChannel(CHANNEL_ID, "Ride", NotificationManager.IMPORTANCE_DEFAULT).apply {
            description = "Live ride stats mirrored from Trailforks"
            setSound(null, null)
            enableVibration(false)
            setShowBadge(false)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }
        manager(context).createNotificationChannel(channel)
    }

    fun post(context: Context, stats: RideStats, sourcePackage: String?) {
        ensureChannel(context)
        val now = System.currentTimeMillis()
        val builder = Notification.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_bike)
            .setContentTitle(stats.distance ?: "Recording")
            .setContentText(if (stats.paused) "Paused · ${stats.raw}" else stats.raw)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setShowWhen(true)
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .setCategory(Notification.CATEGORY_PROGRESS)

        val elapsed = stats.elapsedSeconds
        if (elapsed != null && !stats.paused) {
            // Ticks every second on its own; resynced each time Trailforks updates.
            builder.setWhen(now - elapsed * 1000).setUsesChronometer(true)
        } else {
            builder.setWhen(now).setUsesChronometer(false)
        }

        sourcePackage?.let { context.packageManager.getLaunchIntentForPackage(it) }?.let { launch ->
            builder.setContentIntent(
                PendingIntent.getActivity(
                    context, 0, launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
                )
            )
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
            builder.setRequestPromotedOngoing(true)
            stats.distance?.let { builder.setShortCriticalText(it.replace(" ", "")) }
        }

        manager(context).notify(NOTIFICATION_ID, builder.build())
    }

    fun cancel(context: Context) = manager(context).cancel(NOTIFICATION_ID)

    /** null when no update is showing; otherwise whether the system actually promoted it. */
    fun isShowingPromoted(context: Context): Boolean? {
        val ours = manager(context).activeNotifications.firstOrNull { it.id == NOTIFICATION_ID } ?: return null
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.BAKLAVA) return false
        return ours.notification.flags and Notification.FLAG_PROMOTED_ONGOING != 0
    }

    fun canPromote(context: Context): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA && manager(context).canPostPromotedNotifications()
}
