package com.example.data

import com.example.model.MediaItem
import com.example.model.StreamSource
import com.example.model.SubtitleTrack

object CuratedMediaCatalog {

    val items: List<MediaItem> = listOf(
        MediaItem(
            id = "cs-sintel",
            title = "Sintel",
            description = "A lonely young woman, Sintel, helps and bonds with an orphaned baby dragon she names Scales. But when a full-grown dragon snatches Scales away, Sintel embarks on an epic, heart-wrenching quest across treacherous mountain peaks and desolate ruins to rescue her beloved companion.",
            category = "Animation",
            posterUrl = "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=600&auto=format&fit=crop&q=80",
            backdropUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=1200&auto=format&fit=crop&q=80",
            durationText = "15 min",
            durationMs = 918000L,
            releaseYear = "2010",
            rating = 8.6f,
            streamSources = listOf(
                StreamSource(
                    label = "Direct MP4 Feed",
                    url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
                    quality = "Auto"
                )
            ),
            subtitles = listOf(
                SubtitleTrack(
                    language = "English",
                    code = "en",
                    srtContent = """
1
00:00:01,500 --> 00:00:05,200
Deep within the barren mountains, a relentless blizzard raged.

2
00:00:06,000 --> 00:00:10,000
Sintel pressed forward, guided only by memory.

3
00:00:11,000 --> 00:00:16,000
"I won't give up on you, Scales..." she whispered into the wind.

4
00:00:20,000 --> 00:00:26,000
Memories of their days in the courtyard kept the cold at bay.
                    """.trimIndent()
                ),
                SubtitleTrack(
                    language = "Español",
                    code = "es",
                    srtContent = """
1
00:00:01,500 --> 00:00:05,200
En lo profundo de las montañas yermas, rugía una tormenta implacable.

2
00:00:06,000 --> 00:00:10,000
Sintel avanzaba guiada únicamente por sus recuerdos.

3
00:00:11,000 --> 00:00:16,000
"No me rendiré, Scales..." susurró al viento.
                    """.trimIndent()
                )
            ),
            sourceRepo = "Blender Open Studio"
        ),

        MediaItem(
            id = "cs-tears-of-steel",
            title = "Tears of Steel",
            description = "Set in a dystopian future Amsterdam, a squad of cyber-enhanced fighters and scientists convene in the Oude Kerk cathedral in a desperate last stand to alter history and prevent an all-out global AI robot apocalypse sparked by a broken romance.",
            category = "Sci-Fi",
            posterUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=600&auto=format&fit=crop&q=80",
            backdropUrl = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=1200&auto=format&fit=crop&q=80",
            durationText = "12 min",
            durationMs = 734000L,
            releaseYear = "2012",
            rating = 8.4f,
            streamSources = listOf(
                StreamSource(
                    label = "Adaptive HLS Stream",
                    url = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8",
                    quality = "Auto"
                ),
                StreamSource(
                    label = "Direct MP4 Feed",
                    url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
                    quality = "1080p"
                )
            ),
            subtitles = listOf(
                SubtitleTrack(
                    language = "English",
                    code = "en",
                    srtContent = """
1
00:00:02,000 --> 00:00:06,000
"Target approaching sector 4. Prepare the neural link."

2
00:00:07,000 --> 00:00:11,500
"If this doesn't reset the timeline, none of us walk out of here."

3
00:00:13,000 --> 00:00:18,000
"Initiating synchronization sequence in three, two, one..."
                    """.trimIndent()
                )
            ),
            sourceRepo = "Mango Open Film"
        ),

        MediaItem(
            id = "cs-big-buck-bunny",
            title = "Big Buck Bunny",
            description = "A cheerful giant forest rabbit wakes up to a sunny morning, delighting in colorful butterflies and blossoms. When a mischievous trio of woodland bullies disrupt his peace, Bunny engineers brilliant, comical traps.",
            category = "Animation",
            posterUrl = "https://images.unsplash.com/photo-1535083783855-76ae62b2914e?w=600&auto=format&fit=crop&q=80",
            backdropUrl = "https://images.unsplash.com/photo-1470246973918-29a93221c455?w=1200&auto=format&fit=crop&q=80",
            durationText = "10 min",
            durationMs = 596000L,
            releaseYear = "2008",
            rating = 8.8f,
            streamSources = listOf(
                StreamSource(
                    label = "Adaptive HLS Stream",
                    url = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
                    quality = "Auto"
                ),
                StreamSource(
                    label = "Direct MP4 Feed",
                    url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                    quality = "1080p"
                )
            ),
            subtitles = listOf(
                SubtitleTrack(
                    language = "English",
                    code = "en",
                    srtContent = """
1
00:00:02,000 --> 00:00:06,000
[Birdsong and gentle breeze rustling the meadow canopy]

2
00:00:08,000 --> 00:00:12,000
[Bunny yawns, stretching out into the morning sunlight]

3
00:00:14,000 --> 00:00:19,000
[Playful laughter echoes from behind the hollow oak]
                    """.trimIndent()
                )
            ),
            sourceRepo = "Peach Open Movie"
        ),

        MediaItem(
            id = "cs-elephants-dream",
            title = "Elephants Dream",
            description = "Proog and Emo wander through a surreal mechanical wonder world known as the Machine. As older guide Proog marvels at the infinite clockwork architecture, rebellious youth Emo begins questioning if the Machine is real or merely an illusion of a collective psyche.",
            category = "Sci-Fi",
            posterUrl = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=600&auto=format&fit=crop&q=80",
            backdropUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=1200&auto=format&fit=crop&q=80",
            durationText = "11 min",
            durationMs = 654000L,
            releaseYear = "2006",
            rating = 8.1f,
            streamSources = listOf(
                StreamSource(
                    label = "Direct MP4 Feed",
                    url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                    quality = "Auto"
                )
            ),
            subtitles = listOf(
                SubtitleTrack(
                    language = "English",
                    code = "en",
                    srtContent = """
1
00:00:02,000 --> 00:00:07,000
"Look at this place, Emo. Every cog, every piston in perfect harmony."

2
00:00:08,000 --> 00:00:12,000
"Or in perpetual imprisonment, Proog."
                    """.trimIndent()
                )
            ),
            sourceRepo = "Orange Open Project"
        ),

        MediaItem(
            id = "cs-bipbop-live",
            title = "Global Stream Test Feed",
            description = "High-efficiency adaptive multi-bitrate HLS live stream test channel providing synchronized audio/video diagnostics, continuous timecodes, and real-time buffer analysis.",
            category = "Space & Live",
            posterUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=600&auto=format&fit=crop&q=80",
            backdropUrl = "https://images.unsplash.com/photo-1446776811953-b23d57bd21aa?w=1200&auto=format&fit=crop&q=80",
            durationText = "LIVE",
            durationMs = 0L,
            releaseYear = "2026",
            rating = 9.2f,
            isLive = true,
            streamSources = listOf(
                StreamSource(
                    label = "Adaptive HLS Stream",
                    url = "https://devstreaming-cdn.apple.com/videos/streaming/examples/bipbop_4x3/bipbop_4x3_variant.m3u8",
                    quality = "Auto"
                )
            ),
            sourceRepo = "HLS Broadcast Feed"
        ),

        MediaItem(
            id = "cs-nature-escapes",
            title = "Wild Horizons: Alpine Escapes",
            description = "Immerse yourself in breathtaking 4K panoramic vistas across the Swiss Alps, glacial river valleys, and misty Scandinavian fjords. Recorded with high-dynamic-range spatial acoustics.",
            category = "Documentary",
            posterUrl = "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=600&auto=format&fit=crop&q=80",
            backdropUrl = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=1200&auto=format&fit=crop&q=80",
            durationText = "14 min",
            durationMs = 852000L,
            releaseYear = "2024",
            rating = 9.0f,
            streamSources = listOf(
                StreamSource(
                    label = "Direct MP4 Feed",
                    url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
                    quality = "Auto"
                )
            ),
            subtitles = listOf(
                SubtitleTrack(
                    language = "English",
                    code = "en",
                    srtContent = """
1
00:00:01,000 --> 00:00:05,000
Above three thousand meters, silence is broken only by the wind.

2
00:00:06,000 --> 00:00:10,000
Ancient granite peaks withstand centuries of glacial flow.
                    """.trimIndent()
                )
            ),
            sourceRepo = "Open Earth Archives"
        ),

        MediaItem(
            id = "cs-fire-blazes",
            title = "Inferno: Secrets of Fire and Plasma",
            description = "A cinematic exploration of thermic reactions, solar flares, molten lava flows, and advanced propulsion physics captured in ultra-high frame rate photography.",
            category = "Documentary",
            posterUrl = "https://images.unsplash.com/photo-1497436072909-60f360e1d4b1?w=600&auto=format&fit=crop&q=80",
            backdropUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=1200&auto=format&fit=crop&q=80",
            durationText = "15 min",
            durationMs = 900000L,
            releaseYear = "2023",
            rating = 8.7f,
            streamSources = listOf(
                StreamSource(
                    label = "Direct MP4 Feed",
                    url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                    quality = "Auto"
                )
            ),
            sourceRepo = "Open Earth Archives"
        )
    )

    val samplePresetRepositories = listOf(
        Pair("Apple HLS BipBop Test", "https://devstreaming-cdn.apple.com/videos/streaming/examples/bipbop_4x3/bipbop_4x3_variant.m3u8"),
        Pair("Unified Streaming Tears of Steel", "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8"),
        Pair("Mux Adaptive CDN Feed", "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8")
    )
}
