package com.androidex.trailglance

/**
 * Keeps recent (elapsed time, distance) readings from Trailforks to work out speed over the
 * last few minutes. Uses Trailforks' own ride time, so paused time doesn't drag the speed down.
 */
class SpeedTracker(private val windowSeconds: Long = 300) {
    private val samples = ArrayDeque<Pair<Long, Double>>()

    fun reset() = samples.clear()

    fun add(elapsedSeconds: Long, meters: Double) {
        val last = samples.lastOrNull()
        // The clock going backwards means a new ride started.
        if (last != null && elapsedSeconds < last.first) samples.clear()
        if (last != null && elapsedSeconds == last.first) samples.removeLast()
        samples.addLast(elapsedSeconds to meters)
        // Keep one reading at or before the window start so the window is fully covered.
        while (samples.size >= 2 && samples[1].first <= elapsedSeconds - windowSeconds) samples.removeFirst()
    }

    /** Meters per second over the window (or since the start if the ride is shorter); null until there's enough data. */
    fun recentSpeed(): Double? {
        val first = samples.firstOrNull() ?: return null
        val last = samples.last()
        val seconds = last.first - first.first
        return if (seconds < MIN_SECONDS) null else (last.second - first.second).coerceAtLeast(0.0) / seconds
    }

    companion object {
        const val MIN_SECONDS = 30L

        fun averageSpeed(stats: RideStats): Double? {
            val seconds = stats.elapsedSeconds ?: return null
            val meters = stats.distanceMeters ?: return null
            return if (seconds < MIN_SECONDS) null else meters / seconds
        }

        /** e.g. "Avg 9.4 mph · 5 min 11.2 mph". */
        fun speedLine(stats: RideStats, recentMetersPerSecond: Double?): String {
            fun format(mps: Double?): String = when {
                mps == null -> "—"
                stats.imperial -> "%.1f mph".format(mps * 2.236936)
                else -> "%.1f km/h".format(mps * 3.6)
            }
            return "Avg ${format(averageSpeed(stats))} · 5 min ${format(recentMetersPerSecond)}"
        }
    }
}
