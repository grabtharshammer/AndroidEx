package com.androidex.trailglance

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

/** Watches for Trailforks' ongoing recording notification and mirrors it as a Live Update. */
class TrailforksListener : NotificationListenerService() {

    override fun onListenerConnected() {
        val active = activeNotifications?.filter(::isTrailforks).orEmpty()
        if (active.none { it.isOngoing }) stopMirroring()
        active.forEach(::handle)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (isTrailforks(sbn)) handle(sbn)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        if (!isTrailforks(sbn) || !sbn.isOngoing) return
        // Only stop if Trailforks has no other recording notification still up.
        val stillRecording = activeNotifications?.any { it.key != sbn.key && isTrailforks(it) && it.isOngoing } == true
        if (!stillRecording) stopMirroring()
    }

    private fun handle(sbn: StatusBarNotification) {
        val text = extractText(sbn.notification)
        val stats = TrailforksParser.parse(text)
        val mirror = sbn.isOngoing && stats.hasData
        DebugState.record(this, sbn.packageName, sbn.isOngoing, text, mirror)
        if (mirror) LiveUpdate.post(this, stats, sbn.packageName)
    }

    private fun stopMirroring() {
        LiveUpdate.cancel(this)
        DebugState.setMirroring(this, false)
    }

    private fun isTrailforks(sbn: StatusBarNotification): Boolean {
        val pkg = sbn.packageName
        return pkg != packageName && (pkg.contains("trailforks", ignoreCase = true) || pkg.contains("pinkbike", ignoreCase = true))
    }

    private fun extractText(n: Notification): String {
        val e = n.extras
        return listOf(
            Notification.EXTRA_TITLE,
            Notification.EXTRA_TEXT,
            Notification.EXTRA_SUB_TEXT,
            Notification.EXTRA_INFO_TEXT,
            Notification.EXTRA_BIG_TEXT,
        ).mapNotNull { e.getCharSequence(it)?.toString()?.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
            .joinToString(" · ")
            .ifEmpty { n.tickerText?.toString().orEmpty() }
    }
}
