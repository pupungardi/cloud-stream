package com.example.ui.player

import android.media.MediaPlayer
import android.media.PlaybackParams
import android.net.Uri
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.StreamCardOverlay
import com.example.ui.theme.StreamDarkSurface
import com.example.ui.theme.StreamPrimary
import com.example.ui.theme.StreamTextPrimary
import com.example.ui.theme.StreamTextSecondary
import com.example.ui.viewmodel.AspectRatioMode
import com.example.ui.viewmodel.PlayerViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloudStreamPlayer(
    viewModel: PlayerViewModel,
    onClose: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var surfaceHolder by remember { mutableStateOf<SurfaceHolder?>(null) }
    var isSeeking by remember { mutableStateOf(false) }
    var scrubPositionMs by remember { mutableLongStateOf(0L) }
    var doubleTapFlashText by remember { mutableStateOf<String?>(null) }

    // Intercept back button to persist position and close player cleanly
    BackHandler {
        viewModel.persistWatchProgress()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        viewModel.closePlayer()
        onClose()
    }

    // Video engine lifecycle & stream loading
    val streamUrl = uiState.currentSource?.url
    LaunchedEffect(streamUrl, surfaceHolder) {
        val holder = surfaceHolder ?: return@LaunchedEffect
        val url = streamUrl ?: return@LaunchedEffect

        try {
            mediaPlayer?.release()
            val player = MediaPlayer().apply {
                setDisplay(holder)
                setDataSource(context, Uri.parse(url))
                setOnPreparedListener { mp ->
                    viewModel.onPlaybackStateChanged(isPlaying = true, isBuffering = false)
                    val duration = mp.duration.toLong()
                    if (uiState.currentPositionMs > 0 && uiState.currentPositionMs < duration) {
                        mp.seekTo(uiState.currentPositionMs.toInt())
                    }
                    if (uiState.playbackSpeed != 1.0f) {
                        try {
                            mp.playbackParams = PlaybackParams().setSpeed(uiState.playbackSpeed)
                        } catch (_: Exception) {}
                    }
                    mp.start()
                }
                setOnBufferingUpdateListener { _, _ -> }
                setOnInfoListener { _, what, _ ->
                    when (what) {
                        MediaPlayer.MEDIA_INFO_BUFFERING_START -> viewModel.onPlaybackStateChanged(isPlaying = true, isBuffering = true)
                        MediaPlayer.MEDIA_INFO_BUFFERING_END -> viewModel.onPlaybackStateChanged(isPlaying = true, isBuffering = false)
                    }
                    true
                }
                setOnCompletionListener {
                    viewModel.onPlaybackStateChanged(isPlaying = false, isBuffering = false)
                }
                setOnErrorListener { _, what, extra ->
                    viewModel.onError("Playback error ($what, $extra). Try another source.")
                    true
                }
                prepareAsync()
            }
            mediaPlayer = player
        } catch (e: Exception) {
            viewModel.onError("Failed to load stream: ${e.localizedMessage}")
        }
    }

    // Dynamic playback speed synchronization
    LaunchedEffect(uiState.playbackSpeed) {
        mediaPlayer?.let { mp ->
            try {
                if (mp.isPlaying) {
                    mp.playbackParams = PlaybackParams().setSpeed(uiState.playbackSpeed)
                }
            } catch (_: Exception) {}
        }
    }

    // Periodic time update ticker
    LaunchedEffect(mediaPlayer, isSeeking) {
        while (true) {
            delay(400)
            mediaPlayer?.let { mp ->
                if (mp.isPlaying && !isSeeking) {
                    val pos = mp.currentPosition.toLong()
                    val dur = mp.duration.toLong()
                    viewModel.onPositionChanged(pos, dur)
                }
            }
        }
    }

    // Flash animation cleanup
    LaunchedEffect(doubleTapFlashText) {
        if (doubleTapFlashText != null) {
            delay(750)
            doubleTapFlashText = null
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                mediaPlayer?.stop()
                mediaPlayer?.release()
                mediaPlayer = null
            } catch (_: Exception) {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("cloud_stream_player")
    ) {
        // Video Surface
        val videoModifier = when (uiState.aspectRatioMode) {
            AspectRatioMode.FIT -> Modifier
                .fillMaxSize()
            AspectRatioMode.FILL -> Modifier
                .fillMaxSize()
            AspectRatioMode.SIXTEEN_NINE -> Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .align(Alignment.Center)
        }

        AndroidView(
            factory = { ctx ->
                SurfaceView(ctx).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                    )
                    holder.addCallback(object : SurfaceHolder.Callback {
                        override fun surfaceCreated(h: SurfaceHolder) {
                            surfaceHolder = h
                            mediaPlayer?.setDisplay(h)
                        }

                        override fun surfaceChanged(h: SurfaceHolder, format: Int, w: Int, hInt: Int) {
                            surfaceHolder = h
                        }

                        override fun surfaceDestroyed(h: SurfaceHolder) {
                            surfaceHolder = null
                            mediaPlayer?.setDisplay(null)
                        }
                    })
                }
            },
            modifier = videoModifier
        )

        // Gesture Detection Overlay: Single tap to show controls, double tap left/right to skip 10s
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(uiState.isLocked) {
                    detectTapGestures(
                        onTap = {
                            viewModel.toggleControls()
                        },
                        onDoubleTap = { offset ->
                            if (!uiState.isLocked) {
                                val isLeft = offset.x < size.width / 2
                                if (isLeft) {
                                    doubleTapFlashText = "-10s"
                                    viewModel.skipBy(-10000L)
                                    mediaPlayer?.let { mp ->
                                        val newPos = (mp.currentPosition - 10000).coerceAtLeast(0)
                                        mp.seekTo(newPos)
                                    }
                                } else {
                                    doubleTapFlashText = "+10s"
                                    viewModel.skipBy(10000L)
                                    mediaPlayer?.let { mp ->
                                        val newPos = mp.currentPosition + 10000
                                        mp.seekTo(newPos)
                                    }
                                }
                            }
                        }
                    )
                }
        )

        // Animated double tap feedback flash
        AnimatedVisibility(
            visible = doubleTapFlashText != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Surface(
                shape = CircleShape,
                color = StreamCardOverlay,
                modifier = Modifier.size(90.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = doubleTapFlashText ?: "",
                        color = StreamTextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Live Subtitle Display
        if (uiState.currentSubtitleText.isNotBlank()) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = if (uiState.isControlsVisible) 110.dp else 40.dp)
                    .padding(horizontal = 24.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xCC000000),
                    modifier = Modifier.padding(4.dp)
                ) {
                    Text(
                        text = uiState.currentSubtitleText,
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Buffering Indicator
        if (uiState.isBuffering && uiState.errorMessage == null) {
            Box(
                modifier = Modifier.align(Alignment.Center)
            ) {
                CircularProgressIndicator(
                    color = StreamPrimary,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(54.dp)
                )
            }
        }

        // Error Banner
        if (uiState.errorMessage != null) {
            Surface(
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.9f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = uiState.errorMessage ?: "",
                        color = Color.White,
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    TextButton(
                        onClick = {
                            uiState.currentSource?.let { viewModel.setQuality(it) }
                        }
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Retry", tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Retry Playback", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Lock button only if screen is locked
        if (uiState.isLocked) {
            AnimatedVisibility(
                visible = uiState.isControlsVisible,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
                    .padding(16.dp)
            ) {
                IconButton(
                    onClick = { viewModel.toggleLock() },
                    modifier = Modifier
                        .size(48.dp)
                        .background(StreamCardOverlay, CircleShape)
                        .testTag("unlock_screen_button")
                ) {
                    Icon(Icons.Default.Lock, contentDescription = "Unlock", tint = StreamPrimary)
                }
            }
        }

        // Full Interactive Player Controls Overlay
        if (!uiState.isLocked) {
            AnimatedVisibility(
                visible = uiState.isControlsVisible,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xB3000000),
                                    Color(0x33000000),
                                    Color(0x33000000),
                                    Color(0xCC000000)
                                )
                            )
                        )
                ) {
                    // Top Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                viewModel.persistWatchProgress()
                                mediaPlayer?.stop()
                                mediaPlayer?.release()
                                mediaPlayer = null
                                viewModel.closePlayer()
                                onClose()
                            },
                            modifier = Modifier.testTag("player_back_button")
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = StreamTextPrimary
                            )
                        }

                        Column(modifier = Modifier.weight(1f).padding(horizontal = 8.dp)) {
                            Text(
                                text = uiState.mediaItem?.title ?: "Stream Player",
                                color = StreamTextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = uiState.currentSource?.label ?: "Auto",
                                    color = StreamPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = uiState.mediaItem?.sourceRepo ?: "",
                                    color = StreamTextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // Aspect Ratio button
                        IconButton(
                            onClick = { viewModel.cycleAspectRatio() },
                            modifier = Modifier.testTag("aspect_ratio_button")
                        ) {
                            Icon(
                                Icons.Default.AspectRatio,
                                contentDescription = "Aspect Ratio",
                                tint = StreamTextPrimary
                            )
                        }

                        // Lock Screen button
                        IconButton(
                            onClick = { viewModel.toggleLock() },
                            modifier = Modifier.testTag("lock_screen_button")
                        ) {
                            Icon(
                                Icons.Default.LockOpen,
                                contentDescription = "Lock Controls",
                                tint = StreamTextPrimary
                            )
                        }
                    }

                    // Center Playback Controls: Rewind 10s, Play/Pause, Forward 10s
                    Row(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(32.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                viewModel.skipBy(-10000L)
                                mediaPlayer?.let { mp ->
                                    val newPos = (mp.currentPosition - 10000).coerceAtLeast(0)
                                    mp.seekTo(newPos)
                                }
                            },
                            modifier = Modifier
                                .size(52.dp)
                                .background(StreamCardOverlay, CircleShape)
                                .testTag("player_rewind_button")
                        ) {
                            Icon(
                                Icons.Default.FastRewind,
                                contentDescription = "Rewind 10 seconds",
                                tint = StreamTextPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                mediaPlayer?.let { mp ->
                                    if (mp.isPlaying) {
                                        mp.pause()
                                        viewModel.onPlaybackStateChanged(isPlaying = false, isBuffering = false)
                                        viewModel.persistWatchProgress()
                                    } else {
                                        mp.start()
                                        viewModel.onPlaybackStateChanged(isPlaying = true, isBuffering = false)
                                    }
                                }
                            },
                            modifier = Modifier
                                .size(72.dp)
                                .background(StreamPrimary, CircleShape)
                                .testTag("player_play_pause_button")
                        ) {
                            Icon(
                                imageVector = if (uiState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (uiState.isPlaying) "Pause" else "Play",
                                tint = Color.Black,
                                modifier = Modifier.size(38.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                viewModel.skipBy(10000L)
                                mediaPlayer?.let { mp ->
                                    val newPos = mp.currentPosition + 10000
                                    mp.seekTo(newPos)
                                }
                            },
                            modifier = Modifier
                                .size(52.dp)
                                .background(StreamCardOverlay, CircleShape)
                                .testTag("player_forward_button")
                        ) {
                            Icon(
                                Icons.Default.FastForward,
                                contentDescription = "Forward 10 seconds",
                                tint = StreamTextPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    // Bottom Bar: Timeline Scrubber + Action Badges (Speed, Quality, Subtitles)
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        // Time Scrubber
                        val currentMs = if (isSeeking) scrubPositionMs else uiState.currentPositionMs
                        val durMs = uiState.durationMs

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = formatTime(currentMs),
                                color = StreamTextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )

                            Slider(
                                value = if (durMs > 0) currentMs.toFloat() / durMs.toFloat() else 0f,
                                onValueChange = { frac ->
                                    isSeeking = true
                                    scrubPositionMs = (frac * durMs).toLong()
                                },
                                onValueChangeFinished = {
                                    isSeeking = false
                                    viewModel.seekTo(scrubPositionMs)
                                    mediaPlayer?.seekTo(scrubPositionMs.toInt())
                                },
                                colors = SliderDefaults.colors(
                                    thumbColor = StreamPrimary,
                                    activeTrackColor = StreamPrimary,
                                    inactiveTrackColor = Color(0x66FFFFFF)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 8.dp)
                                    .testTag("player_timeline_slider")
                            )

                            Text(
                                text = if (durMs > 0) formatTime(durMs) else "LIVE",
                                color = StreamTextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Bottom Actions: Speed, Quality, Subtitles
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                // Speed Chip
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = StreamDarkSurface,
                                    modifier = Modifier
                                        .clickable { viewModel.toggleSpeedDialog(true) }
                                        .testTag("player_speed_chip")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Speed, contentDescription = null, tint = StreamPrimary, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = "${uiState.playbackSpeed}x", color = StreamTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                // Quality Chip
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = StreamDarkSurface,
                                    modifier = Modifier
                                        .clickable { viewModel.toggleQualityDialog(true) }
                                        .testTag("player_quality_chip")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.HighQuality, contentDescription = null, tint = StreamPrimary, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = uiState.currentSource?.quality ?: "Auto", color = StreamTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                // Subtitle Chip
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = StreamDarkSurface,
                                    modifier = Modifier
                                        .clickable { viewModel.toggleSubtitleDialog(true) }
                                        .testTag("player_subtitle_chip")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.ClosedCaption,
                                            contentDescription = null,
                                            tint = if (uiState.selectedSubtitle != null) StreamPrimary else StreamTextSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = uiState.selectedSubtitle?.language ?: "CC Off",
                                            color = if (uiState.selectedSubtitle != null) StreamTextPrimary else StreamTextSecondary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Speed Selection Bottom Sheet
        if (uiState.showSpeedDialog) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.toggleSpeedDialog(false) },
                containerColor = StreamDarkSurface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Playback Speed",
                        color = StreamTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    val speeds = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)
                    speeds.forEach { sp ->
                        val isSelected = uiState.playbackSpeed == sp
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) StreamPrimary.copy(alpha = 0.2f) else Color.Transparent)
                                .clickable { viewModel.setPlaybackSpeed(sp) }
                                .padding(vertical = 12.dp, horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${sp}x ${if (sp == 1.0f) "(Normal)" else ""}",
                                color = if (isSelected) StreamPrimary else StreamTextPrimary,
                                fontSize = 16.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }

        // Quality Selection Bottom Sheet
        if (uiState.showQualityDialog) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.toggleQualityDialog(false) },
                containerColor = StreamDarkSurface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Select Stream Quality",
                        color = StreamTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    uiState.mediaItem?.streamSources?.forEach { source ->
                        val isSelected = uiState.currentSource?.url == source.url
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) StreamPrimary.copy(alpha = 0.2f) else Color.Transparent)
                                .clickable { viewModel.setQuality(source) }
                                .padding(vertical = 12.dp, horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = source.label,
                                color = if (isSelected) StreamPrimary else StreamTextPrimary,
                                fontSize = 16.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                            Text(
                                text = source.quality,
                                color = StreamPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Subtitle Selection Bottom Sheet
        if (uiState.showSubtitleDialog) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.toggleSubtitleDialog(false) },
                containerColor = StreamDarkSurface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Subtitles / Captions",
                        color = StreamTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Option: Off
                    val isOff = uiState.selectedSubtitle == null
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isOff) StreamPrimary.copy(alpha = 0.2f) else Color.Transparent)
                            .clickable { viewModel.setSubtitleTrack(null) }
                            .padding(vertical = 12.dp, horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Off",
                            color = if (isOff) StreamPrimary else StreamTextPrimary,
                            fontSize = 16.sp,
                            fontWeight = if (isOff) FontWeight.Bold else FontWeight.Normal
                        )
                    }

                    // Available Subtitle Tracks
                    uiState.mediaItem?.subtitles?.forEach { track ->
                        val isSelected = uiState.selectedSubtitle?.code == track.code
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) StreamPrimary.copy(alpha = 0.2f) else Color.Transparent)
                                .clickable { viewModel.setSubtitleTrack(track) }
                                .padding(vertical = 12.dp, horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = track.language,
                                color = if (isSelected) StreamPrimary else StreamTextPrimary,
                                fontSize = 16.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                            Text(
                                text = track.code.uppercase(),
                                color = StreamTextSecondary,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatTime(millis: Long): String {
    if (millis <= 0) return "00:00"
    val totalSeconds = millis / 1000
    val seconds = totalSeconds % 60
    val minutes = (totalSeconds / 60) % 60
    val hours = totalSeconds / 3600

    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}
