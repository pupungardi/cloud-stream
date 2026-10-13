package com.example

import com.example.player.SrtParser
import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun srtParser_parsesCuesCorrectly() {
    val sampleSrt = """
1
00:00:01,000 --> 00:00:04,500
Welcome to CloudStream!

2
00:00:05,000 --> 00:00:08,200
Enjoy legal open streams.
    """.trimIndent()

    val cues = SrtParser.parse(sampleSrt)
    assertEquals(2, cues.size)
    assertEquals(1000L, cues[0].startTimeMs)
    assertEquals(4500L, cues[0].endTimeMs)
    assertEquals("Welcome to CloudStream!", cues[0].text)
  }
}
