package com.example.player

import com.example.model.SubtitleCue

object SrtParser {

    fun parse(content: String): List<SubtitleCue> {
        if (content.isBlank()) return emptyList()

        val cues = mutableListOf<SubtitleCue>()
        val blocks = content.replace("\r\n", "\n").replace("\r", "\n").split("\n\n")

        for (block in blocks) {
            val lines = block.lines().map { it.trim() }.filter { it.isNotEmpty() }
            if (lines.isEmpty()) continue

            // Find the line containing "-->"
            val arrowIndex = lines.indexOfFirst { it.contains("-->") }
            if (arrowIndex == -1) continue

            val timeLine = lines[arrowIndex]
            val timeParts = timeLine.split("-->").map { it.trim() }
            if (timeParts.size < 2) continue

            val startTime = parseTimestamp(timeParts[0])
            val endTime = parseTimestamp(timeParts[1].split(" ").firstOrNull() ?: timeParts[1])

            if (startTime >= 0 && endTime >= startTime) {
                val textLines = lines.subList(arrowIndex + 1, lines.size)
                val text = textLines.joinToString("\n").replace(Regex("<[^>]*>"), "")
                if (text.isNotBlank()) {
                    cues.add(SubtitleCue(startTime, endTime, text))
                }
            }
        }

        return cues.sortedBy { it.startTimeMs }
    }

    private fun parseTimestamp(timeStr: String): Long {
        try {
            val clean = timeStr.replace(',', '.')
            val parts = clean.split(":")
            return when (parts.size) {
                3 -> {
                    val hours = parts[0].toLong()
                    val minutes = parts[1].toLong()
                    val secParts = parts[2].split(".")
                    val seconds = secParts[0].toLong()
                    val millis = if (secParts.size > 1) secParts[1].padEnd(3, '0').take(3).toLong() else 0L
                    (hours * 3600 + minutes * 60 + seconds) * 1000 + millis
                }
                2 -> {
                    val minutes = parts[0].toLong()
                    val secParts = parts[1].split(".")
                    val seconds = secParts[0].toLong()
                    val millis = if (secParts.size > 1) secParts[1].padEnd(3, '0').take(3).toLong() else 0L
                    (minutes * 60 + seconds) * 1000 + millis
                }
                else -> -1L
            }
        } catch (_: Exception) {
            return -1L
        }
    }
}
