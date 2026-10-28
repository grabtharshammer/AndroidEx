package com.androidex.trailglance

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TrailforksParserTest {
    @Test
    fun parsesMinutesAndFeet() {
        val s = TrailforksParser.parse("Recording 5:10 - 75ft")
        assertEquals(310L, s.elapsedSeconds)
        assertEquals("75 ft", s.distance)
        assertEquals(22.86, s.distanceMeters!!, 0.001)
        assertTrue(s.imperial)
        assertFalse(s.paused)
    }

    @Test
    fun parsesHoursAndMiles() {
        val s = TrailforksParser.parse("Recording 1:02:03 - 12.4 mi")
        assertEquals(3723L, s.elapsedSeconds)
        assertEquals("12.4 mi", s.distance)
        assertEquals(12.4 * 1609.344, s.distanceMeters!!, 0.001)
    }

    @Test
    fun parsesThousandsSeparator() {
        assertEquals("1,250 ft", TrailforksParser.parse("Recording 9:59 - 1,250ft").distance)
    }

    @Test
    fun metricIsNotImperial() {
        val s = TrailforksParser.parse("Recording 10:00 - 2.5km")
        assertEquals(2500.0, s.distanceMeters!!, 0.001)
        assertFalse(s.imperial)
    }

    @Test
    fun detectsPause() {
        assertTrue(TrailforksParser.parse("Paused 5:10 - 75ft").paused)
    }

    @Test
    fun noDataInUnrelatedText() {
        val s = TrailforksParser.parse("Downloading maps")
        assertNull(s.elapsedSeconds)
        assertNull(s.distance)
        assertFalse(s.hasData)
    }
}
