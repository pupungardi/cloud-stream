package com.example.ui.player

import android.net.Uri
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem as Media3Item
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.ui.theme.StreamCardOverlay
import com.example.ui.theme.StreamDarkSurface
import com.example.ui.theme.StreamPrimary
import com.example.ui.theme.StreamTextPrimary
import com.example.ui.theme.StreamTextSecondary
import com.example.ui.viewmodel.AspectRatioMode
import com.example.ui.viewmodel.PlayerViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@OptIn(UnstableApi::class)
@ExperimentalMaterial3Api
@Composable
fun CloudStreamPlayer(
    viewModel: PlayerViewModel,
    onClose: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Inisialisasi DefaultTrackSelector dan satu ExoPlayer per pemutar
    val trackSelector = remember {
        DefaultTrackSelector(context)
    }

    val exoPlayer = remember {
        ExoPlayer.Builder(context)
            .setTrackSelector(trackSelector)
            .build()
    }

    var isSeeking by remember { mutableStateOf(false) }
    var scrubPositionMs by remember { mutableLongStateOf(0L) }
    var doubleTapFlashText by remember { mutableStateOf<String?>(null) }
    var selectedHlsMaxHeight by remember { mutableIntStateOf(Int.MAX_VALUE) } // Untuk stream adaptif

    // Jalur tunggal untuk seek: langsung eksekusi ke ExoPlayer dan sinkronkan state
    fun performSeek(targetMs: Long) {
        val dur = exoPlayer.duration.coerceAtLeast(0L)
        val maxDur = if (dur > 0) dur else Long.MAX_VALUE
        val clamped = targetMs.coerceIn(0L, maxDur)
        exoPlayer.seekTo(clamped)
        viewModel.onPositionChanged(clamped, if (dur > 0) dur else uiState.durationMs)
        viewModel.persistWatchProgress()
        viewModel.resetControlsTimer()
    }

    fun skipBy(deltaMs: Long) {
        val current = exoPlayer.currentPosition.coerceAtLeast(0L)
        performSeek(current + deltaMs)
    }

    // Lifecycle & cleanup: pause saat ON_PAUSE, lepas player di onDispose
    DisposableEffect(lifecycleOwner, exoPlayer) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                exoPlayer.pause()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        val playerListener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                viewModel.onIsPlayingChanged(isPlaying)
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                val isBuffering = playbackState == Player.STATE_BUFFERING
                val isEnded = playbackState == Player.STATE_ENDED
                viewModel.onPlaybackStateChanged(isBuffering, isEnded)
            }

            override fun onPlayerError(error: PlaybackException) {
                viewModel.onError("Playback error: ${error.message ?: error.errorCodeName}")
            }
        }
        exoPlayer.addListener(playerListener)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            exoPlayer.removeListener(playerListener)
            exoPlayer.release()
        }
    }

    // BackHandler: simpan posisi dan tutup layar
    BackHandler {
        viewModel.persistWatchProgress()
        exoPlayer.stop()
        viewModel.closePlayer()
        onClose()
    }

    // Set media stream saat URL sumber berubah (atau pertama kali dimuat)
    val streamUrl = uiState.currentSource?.url
    LaunchedEffect(streamUrl) {
        val url = streamUrl ?: return@LaunchedEffect
        val mediaItem = Media3Item.fromUri(Uri.parse(url))
        val resumePos = uiState.currentPositionMs

        exoPlayer.setMediaItem(mediaItem, resumePos)
        exoPlayer.playbackParameters = PlaybackParameters(uiState.playbackSpeed)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
    }

    // Terapkan kecepatan putar secara reaktif (berlaku saat video play maupun pause)
    LaunchedEffect(uiState.playbackSpeed) {
        exoPlayer.playbackParameters = PlaybackParameters(uiState.playbackSpeed)
    }

    // Ticker posisi: berjalan setiap 200 ms untuk sinkronisasi posisi dan subtitle SRT
    LaunchedEffect(exoPlayer, isSeeking) {
        while (isActive) {
            delay(200)
            if (!isSeeking && exoPlayer.playbackState != Player.STATE_IDLE) {
                val pos = exoPlayer.currentPosition.coerceAtLeast(0L)
                val dur = exoPlayer.duration.coerceAtLeast(0L)
                viewModel.onPositionChanged(pos, dur)
            }
        }
    }

    // Flash animasi double tap cleanup
    LaunchedEffect(doubleTapFlashText) {
        if (doubleTapFlashText != null) {
            delay(750)
            doubleTapFlashText = null
        }
    }

    val isHlsStream = streamUrl?.contains(".m3u8", ignoreCase = true) == true
    val hasMultipleSources = (uiState.mediaItem?.streamSources?.size ?: 0) > 1

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("cloud_stream_player")
    ) {
        // PlayerView Media3
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    this.player = exoPlayer
                    useController = false
                    layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                    )
                }
            },
            update = { playerView ->
                playerView.player = exoPlayer
                playerView.resizeMode = when (uiState.aspectRatioMode) {
                    AspectRatioMode.FIT -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                    AspectRatioMode.FILL -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    AspectRatioMode.SIXTEEN_NINE -> AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Gesture Overlay: Single tap untuk toggle kontrol, double tap untuk skip 10s
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
                                    skipBy(-10000L)
                                } else {
                                    doubleTapFlashText = "+10s"
                                    skipBy(10000L)
                                }
                            }
                        }
                    )
                }
        )

        // Flash animasi double-tap
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

        // Overlay Subtitle SRT Kustom (tersinkronisasi setiap 200 ms)
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

        // Indikator Buffering
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

        // Banner Kesalahan Pemutaran
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
                            uiState.currentSource?.let {
                                val item = Media3Item.fromUri(Uri.parse(it.url))
                                exoPlayer.setMediaItem(item, uiState.currentPositionMs)
                                exoPlayer.prepare()
                                exoPlayer.playWhenReady = true
                            }
                        }
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Coba lagi", tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Coba Lagi", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Tombol Buka Kunci (saat layar terkunci)
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
                    Icon(Icons.Default.Lock, contentDescription = "Buka Kunci", tint = StreamPrimary)
                }
            }
        }

        // Kontrol Pemutar Lengkap
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
                    // Bilah Atas
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
                                exoPlayer.stop()
                                viewModel.closePlayer()
                                onClose()
                            },
                            modifier = Modifier.testTag("player_back_button")
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Kembali",
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

                        IconButton(
                            onClick = { viewModel.cycleAspectRatio() },
                            modifier = Modifier.testTag("aspect_ratio_button")
                        ) {
                            Icon(
                                Icons.Default.AspectRatio,
                                contentDescription = "Rasio Layar",
                                tint = StreamTextPrimary
                            )
                        }

                        IconButton(
                            onClick = { viewModel.toggleLock() },
                            modifier = Modifier.testTag("lock_screen_button")
                        ) {
                            Icon(
                                Icons.Default.LockOpen,
                                contentDescription = "Kunci Kontrol",
                                tint = StreamTextPrimary
                            )
                        }
                    }

                    // Kontrol Tengah: Mundur 10 detik, Putar/Jeda, Maju 10 detik
                    Row(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(32.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { skipBy(-10000L) },
                            modifier = Modifier
                                .size(52.dp)
                                .background(StreamCardOverlay, CircleShape)
                                .testTag("player_rewind_button")
                        ) {
                            Icon(
                                Icons.Default.FastRewind,
                                contentDescription = "Mundur 10 detik",
                                tint = StreamTextPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                if (exoPlayer.isPlaying) {
                                    exoPlayer.pause()
                                    viewModel.persistWatchProgress()
                                } else {
                                    exoPlayer.play()
                                }
                            },
                            modifier = Modifier
                                .size(72.dp)
                                .background(StreamPrimary, CircleShape)
                                .testTag("player_play_pause_button")
                        ) {
                            Icon(
                                imageVector = if (uiState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (uiState.isPlaying) "Jeda" else "Putar",
                                tint = Color.Black,
                                modifier = Modifier.size(38.dp)
                            )
                        }

                        IconButton(
                            onClick = { skipBy(10000L) },
                            modifier = Modifier
                                .size(52.dp)
                                .background(StreamCardOverlay, CircleShape)
                                .testTag("player_forward_button")
                        ) {
                            Icon(
                                Icons.Default.FastForward,
                                contentDescription = "Maju 10 detik",
                                tint = StreamTextPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    // Bilah Bawah: Garis Waktu & Opsi Cepat (Kecepatan, Kualitas, Subtitle)
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
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
                                    performSeek(scrubPositionMs)
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

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                // Chip Kecepatan
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

                                // Chip Kualitas: Tampilkan hanya jika stream adaptif atau ada beberapa sumber
                                if (isHlsStream || hasMultipleSources) {
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
                                            val qualityLabel = if (isHlsStream && selectedHlsMaxHeight != Int.MAX_VALUE) {
                                                "${selectedHlsMaxHeight}p"
                                            } else {
                                                uiState.currentSource?.quality ?: "Auto"
                                            }
                                            Text(text = qualityLabel, color = StreamTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                // Chip Subtitle
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

        // Bottom Sheet: Pilihan Kecepatan
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
                        text = "Kecepatan Pemutaran",
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

        // Bottom Sheet: Pilihan Kualitas (TrackSelectionParameters untuk HLS atau pilih mirror/sumber)
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
                        text = if (isHlsStream) "Resolusi Stream Adaptif (HLS)" else "Pilih Sumber & Kualitas",
                        color = StreamTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    if (isHlsStream) {
                        val resolutions = listOf(
                            Pair("Auto (Adaptif)", Int.MAX_VALUE),
                            Pair("1080p FHD Maks", 1080),
                            Pair("720p HD Maks", 720),
                            Pair("480p SD Maks", 480),
                            Pair("360p Hemat Data", 360)
                        )
                        resolutions.forEach { (label, maxH) ->
                            val isSelected = selectedHlsMaxHeight == maxH
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) StreamPrimary.copy(alpha = 0.2f) else Color.Transparent)
                                    .clickable {
                                        selectedHlsMaxHeight = maxH
                                        if (maxH == Int.MAX_VALUE) {
                                            trackSelector.setParameters(
                                                trackSelector.buildUponParameters()
                                                    .clearVideoSizeConstraints()
                                            )
                                        } else {
                                            trackSelector.setParameters(
                                                trackSelector.buildUponParameters()
                                                    .setMaxVideoSize(Int.MAX_VALUE, maxH)
                                            )
                                        }
                                        viewModel.toggleQualityDialog(false)
                                    }
                                    .padding(vertical = 12.dp, horizontal = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSelected) StreamPrimary else StreamTextPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    } else {
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
        }

        // Bottom Sheet: Pilihan Subtitle
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
                        text = "Subtitle / Teks Terjemahan",
                        color = StreamTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Opsi: Nonaktifkan
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
                            text = "Nonaktif (Off)",
                            color = if (isOff) StreamPrimary else StreamTextPrimary,
                            fontSize = 16.sp,
                            fontWeight = if (isOff) FontWeight.Bold else FontWeight.Normal
                        )
                    }

                    // Trek yang tersedia
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
