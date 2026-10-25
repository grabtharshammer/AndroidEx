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
        assertFalse(s.paused)
    }

    @Test
    fun parsesHoursAndMiles() {
        val s = TrailforksParser.parse("Recording 1:02:03 - 12.4 mi")
        assertEquals(3723L, s.elapsedSeconds)
        assertEquals("12.4 mi", s.distance)
    }

    @Test
    fun parsesThousandsSeparator() {
        assertEquals("1,250 ft", TrailforksParser.parse("Recording 9:59 - 1,250ft").distance)
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
