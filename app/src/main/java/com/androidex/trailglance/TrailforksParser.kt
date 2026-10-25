package com.androidex.trailglance

/** What we could pull out of a Trailforks recording notification, e.g. "Recording 5:10 - 75ft". */
data class RideStats(
    val elapsedSeconds: Long?,
    val distance: String?,
    val paused: Boolean,
    val raw: String,
) {
    val hasData: Boolean get() = elapsedSeconds != null || distance != null
}

object TrailforksParser {
    // "5:10" is read as m:ss and "1:05:10" as h:mm:ss.
    private val TIME = Regex("""(?<![\d:])(\d{1,3}):([0-5]\d)(?::([0-5]\d))?(?![\d:])""")
    private val DISTANCE = Regex("""(\d[\d,]*(?:\.\d+)?)\s*(ft|mi|km|m)\b""", RegexOption.IGNORE_CASE)

    fun parse(text: String): RideStats {
        val elapsed = TIME.find(text)?.let { m ->
            val (a, b, c) = m.destructured
            if (c.isEmpty()) a.toLong() * 60 + b.toLong()
            else a.toLong() * 3600 + b.toLong() * 60 + c.toLong()
        }
        val distance = DISTANCE.find(text)?.let { m ->
            "${m.groupValues[1]} ${m.groupValues[2].lowercase()}"
        }
        val paused = text.contains("pause", ignoreCase = true)
        return RideStats(elapsed, distance, paused, text.trim())
    }
}
