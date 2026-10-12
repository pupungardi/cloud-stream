package com.example.model

data class MediaItem(
    val id: String,
    val title: String,
    val description: String = "",
    val category: String = "Video",
    val posterUrl: String? = null,
    val backdropUrl: String? = null,
    val durationText: String = "",
    val durationMs: Long = 0L,
    val releaseYear: String? = null,
    val rating: Float? = null,
    val streamSources: List<StreamSource>,
    val subtitles: List<SubtitleTrack> = emptyList(),
    val sourceRepo: String = "",
    val isLive: Boolean = false,
    val logoStyle: String? = null,
    val logoSubtitle: String? = null
)

data class StreamSource(
    val label: String,       // e.g. "Direct Video", "HLS Stream"
    val url: String,
    val quality: String = "Auto",
    val isDirect: Boolean = true
)

data class SubtitleTrack(
    val language: String,
    val code: String,
    val srtContent: String
)

data class SubtitleCue(
    val startTimeMs: Long,
    val endTimeMs: Long,
    val text: String
)
