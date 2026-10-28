package com.androidex.trailglance

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SpeedTrackerTest {
    @Test
    fun usesOnlyTheLastFiveMinutes() {
        val t = SpeedTracker()
        t.add(0, 0.0)
        t.add(300, 300.0)   // 1 m/s for the first 5 min
        t.add(600, 1500.0)  // 4 m/s for the next 5 min
        assertEquals(4.0, t.recentSpeed()!!, 0.001)
    }

    @Test
    fun shortRideUsesWhatItHas() {
        val t = SpeedTracker()
        t.add(0, 0.0)
        t.add(60, 120.0)
        assertEquals(2.0, t.recentSpeed()!!, 0.001)
    }

    @Test
    fun needsThirtySeconds() {
        val t = SpeedTracker()
        t.add(0, 0.0)
        t.add(10, 50.0)
        assertNull(t.recentSpeed())
    }

    @Test
    fun clockGoingBackwardsStartsOver() {
        val t = SpeedTracker()
        t.add(0, 0.0)
        t.add(600, 6000.0)
        t.add(0, 0.0)
        t.add(60, 60.0)
        assertEquals(1.0, t.recentSpeed()!!, 0.001)
    }

    @Test
    fun formatsMphForImperial() {
        val stats = TrailforksParser.parse("Recording 1:00:00 - 10mi")
        assertEquals("Avg 10.0 mph · 5 min 4.5 mph", SpeedTracker.speedLine(stats, 2.0))
    }

    @Test
    fun formatsKmhForMetric() {
        val stats = TrailforksParser.parse("Recording 10:00 - 3km")
        assertEquals("Avg 18.0 km/h · 5 min —", SpeedTracker.speedLine(stats, null))
    }
}
