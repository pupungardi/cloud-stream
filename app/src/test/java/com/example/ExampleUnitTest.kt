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
<b>Welcome to CloudStream!</b>
   
  
2
01:20.500 --> 01:25.000
Enjoy <i>legal</i> open streams.
    """.trimIndent()

    val cues = SrtParser.parse(sampleSrt)
    assertEquals(2, cues.size)
    assertEquals(1000L, cues[0].startTimeMs)
    assertEquals(4500L, cues[0].endTimeMs)
    assertEquals("Welcome to CloudStream!", cues[0].text)

    assertEquals(80500L, cues[1].startTimeMs)
    assertEquals(85000L, cues[1].endTimeMs)
    assertEquals("Enjoy legal open streams.", cues[1].text)
  }
}
