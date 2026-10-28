package com.androidex.trailglance

/** What we could pull out of a Trailforks recording notification, e.g. "Recording 5:10 - 75ft". */
data class RideStats(
    val elapsedSeconds: Long?,
    val distance: String?,
    val distanceMeters: Double?,
    /** Trailforks is showing ft/mi, so speeds should be mph rather than km/h. */
    val imperial: Boolean,
    val paused: Boolean,
    val raw: String,
) {
    val hasData: Boolean get() = elapsedSeconds != null || distance != null
}

object TrailforksParser {
    // "5:10" is read as m:ss and "1:05:10" as h:mm:ss.
    private val TIME = Regex("""(?<![\d:])(\d{1,3}):([0-5]\d)(?::([0-5]\d))?(?![\d:])""")
    private val METERS_PER_UNIT = mapOf("ft" to 0.3048, "mi" to 1609.344, "m" to 1.0, "km" to 1000.0)
    private val DISTANCE = Regex("""(\d[\d,]*(?:\.\d+)?)\s*(ft|mi|km|m)\b""", RegexOption.IGNORE_CASE)

    fun parse(text: String): RideStats {
        val elapsed = TIME.find(text)?.let { m ->
            val (a, b, c) = m.destructured
            if (c.isEmpty()) a.toLong() * 60 + b.toLong()
            else a.toLong() * 3600 + b.toLong() * 60 + c.toLong()
        }
        val match = DISTANCE.find(text)
        val unit = match?.groupValues?.get(2)?.lowercase()
        val distance = match?.let { "${it.groupValues[1]} $unit" }
        val meters = match?.let { it.groupValues[1].replace(",", "").toDouble() * METERS_PER_UNIT.getValue(unit!!) }
        val paused = text.contains("pause", ignoreCase = true)
        return RideStats(elapsed, distance, meters, unit == "ft" || unit == "mi", paused, text.trim())
    }
}
