package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.MediaRepository
import com.example.model.MediaItem
import com.example.model.StreamSource
import com.example.model.SubtitleCue
import com.example.model.SubtitleTrack
import com.example.player.SrtParser
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class PlayerUiState(
    val mediaItem: MediaItem? = null,
    val currentSource: StreamSource? = null,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = true,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val playbackSpeed: Float = 1.0f,
    val selectedSubtitle: SubtitleTrack? = null,
    val parsedCues: List<SubtitleCue> = emptyList(),
    val currentSubtitleText: String = "",
    val aspectRatioMode: AspectRatioMode = AspectRatioMode.FIT,
    val isControlsVisible: Boolean = true,
    val isLocked: Boolean = false,
    val errorMessage: String? = null,
    val showSpeedDialog: Boolean = false,
    val showQualityDialog: Boolean = false,
    val showSubtitleDialog: Boolean = false
)

enum class AspectRatioMode {
    FIT, FILL, SIXTEEN_NINE
}

class PlayerViewModel(private val repository: MediaRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState = _uiState.asStateFlow()

    private var progressTrackingJob: Job? = null
    private var controlsDismissJob: Job? = null

    fun loadMedia(media: MediaItem, initialSource: StreamSource? = null, startFromBeginning: Boolean = false) {
        viewModelScope.launch {
            val source = initialSource ?: media.streamSources.firstOrNull()
            val history = if (!startFromBeginning) repository.getWatchHistoryItem(media.id) else null
            val resumePos = history?.lastPositionMs ?: 0L

            val initialSub = media.subtitles.firstOrNull()
            val cues = if (initialSub != null) SrtParser.parse(initialSub.srtContent) else emptyList()

            _uiState.value = PlayerUiState(
                mediaItem = media,
                currentSource = source,
                isPlaying = true,
                isBuffering = true,
                currentPositionMs = resumePos,
                durationMs = if (media.durationMs > 0) media.durationMs else 0L,
                playbackSpeed = 1.0f,
                selectedSubtitle = initialSub,
                parsedCues = cues,
                currentSubtitleText = "",
                aspectRatioMode = AspectRatioMode.FIT,
                isControlsVisible = true,
                isLocked = false,
                errorMessage = null
            )

            startProgressTicker()
            resetControlsTimer()
        }
    }

    fun onPlaybackStateChanged(isPlaying: Boolean, isBuffering: Boolean) {
        _uiState.value = _uiState.value.copy(
            isPlaying = isPlaying,
            isBuffering = isBuffering
        )
        if (isPlaying) {
            resetControlsTimer()
        }
    }

    fun onPositionChanged(positionMs: Long, durationMs: Long) {
        val state = _uiState.value
        val effectiveDuration = if (durationMs > 0) durationMs else state.durationMs

        // Find active subtitle cue
        val activeCueText = state.parsedCues.find {
            positionMs >= it.startTimeMs && positionMs <= it.endTimeMs
        }?.text ?: ""

        _uiState.value = state.copy(
            currentPositionMs = positionMs,
            durationMs = effectiveDuration,
            currentSubtitleText = activeCueText
        )
    }

    fun seekTo(positionMs: Long) {
        _uiState.value = _uiState.value.copy(currentPositionMs = positionMs)
        persistWatchProgress()
        resetControlsTimer()
    }

    fun skipBy(deltaMs: Long) {
        val state = _uiState.value
        val newPos = (state.currentPositionMs + deltaMs).coerceIn(0L, if (state.durationMs > 0) state.durationMs else Long.MAX_VALUE)
        seekTo(newPos)
    }

    fun setPlaybackSpeed(speed: Float) {
        _uiState.value = _uiState.value.copy(playbackSpeed = speed, showSpeedDialog = false)
        resetControlsTimer()
    }

    fun setQuality(source: StreamSource) {
        val currentPos = _uiState.value.currentPositionMs
        _uiState.value = _uiState.value.copy(
            currentSource = source,
            currentPositionMs = currentPos,
            isBuffering = true,
            showQualityDialog = false
        )
        resetControlsTimer()
    }

    fun setSubtitleTrack(track: SubtitleTrack?) {
        val cues = if (track != null) SrtParser.parse(track.srtContent) else emptyList()
        _uiState.value = _uiState.value.copy(
            selectedSubtitle = track,
            parsedCues = cues,
            currentSubtitleText = "",
            showSubtitleDialog = false
        )
        resetControlsTimer()
    }

    fun cycleAspectRatio() {
        val nextMode = when (_uiState.value.aspectRatioMode) {
            AspectRatioMode.FIT -> AspectRatioMode.FILL
            AspectRatioMode.FILL -> AspectRatioMode.SIXTEEN_NINE
            AspectRatioMode.SIXTEEN_NINE -> AspectRatioMode.FIT
        }
        _uiState.value = _uiState.value.copy(aspectRatioMode = nextMode)
        resetControlsTimer()
    }

    fun toggleControls() {
        if (_uiState.value.isLocked) {
            // If locked, single tap briefly shows unlock button
            _uiState.value = _uiState.value.copy(isControlsVisible = !_uiState.value.isControlsVisible)
            return
        }
        val target = !_uiState.value.isControlsVisible
        _uiState.value = _uiState.value.copy(isControlsVisible = target)
        if (target) {
            resetControlsTimer()
        }
    }

    fun toggleLock() {
        val newLock = !_uiState.value.isLocked
        _uiState.value = _uiState.value.copy(isLocked = newLock)
        resetControlsTimer()
    }

    fun toggleSpeedDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showSpeedDialog = show)
    }

    fun toggleQualityDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showQualityDialog = show)
    }

    fun toggleSubtitleDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showSubtitleDialog = show)
    }

    fun onError(message: String) {
        _uiState.value = _uiState.value.copy(
            errorMessage = message,
            isBuffering = false,
            isPlaying = false
        )
    }

    fun resetControlsTimer() {
        controlsDismissJob?.cancel()
        controlsDismissJob = viewModelScope.launch {
            delay(4000)
            if (_uiState.value.isPlaying && !_uiState.value.showSpeedDialog &&
                !_uiState.value.showQualityDialog && !_uiState.value.showSubtitleDialog) {
                _uiState.value = _uiState.value.copy(isControlsVisible = false)
            }
        }
    }

    private fun startProgressTicker() {
        progressTrackingJob?.cancel()
        progressTrackingJob = viewModelScope.launch {
            while (isActive) {
                delay(3000)
                if (_uiState.value.isPlaying && _uiState.value.mediaItem != null) {
                    persistWatchProgress()
                }
            }
        }
    }

    fun persistWatchProgress() {
        val state = _uiState.value
        val media = state.mediaItem ?: return
        val currentSource = state.currentSource?.url ?: return
        if (state.currentPositionMs > 1000) {
            viewModelScope.launch {
                repository.saveWatchProgress(
                    mediaId = media.id,
                    title = media.title,
                    posterUrl = media.posterUrl,
                    streamUrl = currentSource,
                    lastPositionMs = state.currentPositionMs,
                    durationMs = state.durationMs,
                    category = media.category
                )
            }
        }
    }

    fun closePlayer() {
        persistWatchProgress()
        progressTrackingJob?.cancel()
        controlsDismissJob?.cancel()
        _uiState.value = PlayerUiState()
    }

    override fun onCleared() {
        super.onCleared()
        persistWatchProgress()
        progressTrackingJob?.cancel()
        controlsDismissJob?.cancel()
    }
}
