package com.example.model

data class MediaItem(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val posterUrl: String,
    val backdropUrl: String,
    val durationText: String,
    val durationMs: Long,
    val releaseYear: String,
    val rating: Float,
    val streamSources: List<StreamSource>,
    val subtitles: List<SubtitleTrack> = emptyList(),
    val sourceRepo: String = "Curated Open Media",
    val isLive: Boolean = false,
    val logoStyle: String? = null,
    val logoSubtitle: String? = null
)

data class StreamSource(
    val label: String,       // e.g. "1080p FHD", "720p HD", "480p SD", "HLS Stream"
    val url: String,
    val quality: String,     // "1080p", "720p", "480p", "Auto"
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
