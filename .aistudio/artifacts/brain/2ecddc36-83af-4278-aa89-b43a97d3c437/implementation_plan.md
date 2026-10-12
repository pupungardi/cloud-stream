# CloudStream - Media Catalog & Video Streamer

A modern, high-performance streaming catalog and video player for Android inspired by CloudStream. Features curated legal media streams, custom source/repository URL imports, an advanced media player with multi-speed controls, video quality options, subtitle toggles, alongside a persistent Room database tracking watch history, resume timestamps, and favorites.

## User Review & Critical Decisions

> [!IMPORTANT]
> The following user preferences were confirmed during clarification and will be used as the architectural baseline for the app:

- **Confirmed Decision 1 (Core Focus)**: Media catalog and video streamer with curated streaming channels and custom source URL imports.
- **Confirmed Decision 2 (Source Model)**: Curated default repositories (e.g., Blender Open Movies, Big Buck Bunny, Sintel, Tears of Steel, NASA TV, open educational channels, public domain cinema, anime shorts) with full capability to import, test, add, edit, and delete custom streaming JSON/M3U/HLS repository sources.
- **Confirmed Decision 3 (Player & Library)**: Advanced video player with real-time speed adjustments (0.5x to 2.0x), stream quality selection, subtitle support (embedded & custom SRT overlay), seek preview, 10s skip controls, aspect ratio modes, plus complete Room-backed Watch History (resume playback from last timestamp), Bookmarks / Favorites, and Watchlist organization.

---

## 1. Overview & Core Concept

- **What It Does**: CloudStream provides a cinematic streaming hub where users can discover and stream movies, shows, and web channels from curated sources or add their own custom stream repositories. The built-in media player offers granular playback controls (quality, speed, aspect ratio, subtitles, and progress tracking) while saving viewing history and bookmarks locally on-device.
- **Target Audience / Persona**: Media enthusiasts who want an open, flexible streaming client and catalog organizer that doesn't lock them into a single vendor, allowing personal stream links and community repositories.
- **Key Value**: A centralized, privacy-respecting media player and catalog that seamlessly merges online streaming streams, repository management, and robust playback session tracking into an immersive Material 3 Android interface.

---

## 2. User Experience & Visual Design

### Key User Flows

1. **Home / Discover Flow**:
   - Dynamic hero banner spotlighting featured streams with "Play Now" and "+ Add to Watchlist".
   - Categorized horizontal swimlanes ("Continue Watching" with resume progress bars, "Curated Open Movies", "Documentaries & Nature", "Live & Public Channels", "Bookmarked").
   - Filter chips to quickly switch between content categories and active sources.
2. **Media Details Flow**:
   - Detailed header banner with backdrop, title, tags, duration, release year, description, and source badge.
   - Stream source picker (choose stream server / mirror or quality preset).
   - Direct "Resume from XX:XX" or "Start from Beginning" button, Favorite/Bookmark toggle, and Share/Details metadata.
3. **Immersive Video Player Flow**:
   - Fullscreen video playback with gesture overlays (double-tap left/right to skip 10s, single tap to toggle controls).
   - Bottom scrubber timeline with buffer progress, current time, and remaining time.
   - Quick control bar: Play/Pause, Rewind/Fast Forward 10s, Playback Speed (0.5x, 0.75x, 1.0x, 1.25x, 1.5x, 2.0x), Quality selector (1080p, 720p, 480p, Auto), Subtitle toggle (Off, English, Spanish, Custom SRT), and Aspect Ratio (Fit, Fill, 16:9).
   - Auto-saving playback position every 3 seconds to Room database.
4. **Library & Watch History Flow**:
   - "Continue Watching" list with percentage progress indicators, quick resume, and clear history option.
   - "Bookmarks / Favorites" tab with quick filter tags and sorting (Recently Added, Title, Highest Rated).
5. **Sources & Extensions Flow**:
   - View active repository sources (Default Open Media Hub, Curated Public Streams, etc.).
   - Add new custom source by URL or JSON repository link (with instant URL validation and ping test).
   - Enable/disable individual sources or reload catalog streams.

### Visual Identity & Theme

- **Aesthetic Direction**: Premium cinematic dark theme inspired by top streaming services, featuring deep OLED blacks, subtle neon accents, soft glowing gradients, and high-contrast typography.
- **Color Palette**:
  - `Background`: `#0D0F14` (Deep obsidian black)
  - `Surface`: `#161922` (Subtle charcoal navy)
  - `Surface Variant`: `#222634` (Card elevation)
  - `Primary Accent`: `#3B82F6` (Vibrant electric azure)
  - `Secondary Accent`: `#8B5CF6` (Neon purple gradient accent)
  - `Highlight / Warning`: `#F59E0B` (Amber gold for ratings and bookmarks)
  - `Text Primary`: `#F8FAFC`
  - `Text Muted`: `#94A3B8`
- **Typography & Hierarchy**:
  - Display / Title: Bold sans-serif with tight tracking for film headers and hero titles.
  - Body / Captions: Clean, legible sans-serif with comfortable line height for synopsis descriptions.
  - Subtitle Overlay: Crisp white text with black semi-transparent outline and solid backing for maximum contrast on any video scene.
- **Component Styling & Layout**:
  - Hero carousel with smooth horizontal paging and radial darkening gradient overlay.
  - Media cards with rounded corners (16dp), 16:9 or 2:3 aspect ratios, and subtle hover/press elevation.
  - Floating player overlay with frosted dark scrim (`#00000088`) that fades out automatically after 4 seconds of inactivity.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Video Engine Architecture**:
  - *Chosen Approach*: Android `MediaPlayer` and Compose `AndroidView` with hardware-accelerated `SurfaceView`/`TextureView` integrated with custom Compose controls and an SRT subtitle parser.
  - *Why*: Maximum reliability across all Android API versions and container runtimes, full control over playback speed, custom buffer states, real-time resume hooks, and immediate compilation without heavy external binary discrepancies.
  - *Alternatives Considered*: Media3 ExoPlayer (can introduce dependency version mismatches with compose BOM or missing codecs in test containers) vs WebView player (poor touch ergonomics and high latency).
- **Decision 2: Local Persistence Engine (Room Database)**:
  - *Chosen Approach*: Room Database with entities for `WatchHistoryEntity`, `BookmarkEntity`, and `CustomSourceEntity`.
  - *Why*: Instant reactive updates using Kotlin `Flow`, offline resilience, fast reads on startup, and zero sign-in friction for the user.
  - *Alternatives Considered*: SharedPreferences / DataStore (unsuitable for querying sorted history lists, stream catalog records, and search queries).
- **Decision 3: Curated + Extensible Sources Model**:
  - *Chosen Approach*: Ship with a robust built-in legal streaming catalog (Big Buck Bunny, Sintel, Tears of Steel, Cosmos open streams, NASA live streams, open cinema) paired with a complete Source Manager allowing users to input any HTTP/HTTPS direct MP4, HLS (.m3u8), or JSON repository feed.
  - *Why*: Gives an instantly delightful, ready-to-play out-of-the-box experience while fulfilling the user's explicit requirement for custom stream URL imports.

---

## 4. Technical Architecture & Data Strategy

### System & Component Diagram

```
┌────────────────────────────────────────────────────────────────────────┐
│                          CloudStream Application                       │
└────────────────────────────────────┬───────────────────────────────────┘
                                     │
           ┌─────────────────────────┴────────────────────────┐
           ▼                                                  ▼
┌───────────────────────┐                          ┌───────────────────────┐
│     UI Layer (M3)     │                          │    State & ViewModel  │
├───────────────────────┤                          ├───────────────────────┤
│ • MainScreen          │ ◄─────── StateFlow ───── │ • MediaCatalogVM      │
│ • HomeScreen (Hero,   │                          │ • PlayerVM            │
│   Swimlanes, Chips)   │                          │ • LibraryVM           │
│ • MediaDetailSheet    │                          │ • SourceManagerVM     │
│ • CloudStreamPlayer   │ ─────── User Events ───► │                       │
│ • LibraryScreen       │                          │                       │
│ • SourceManagerSheet  │                          │                       │
└───────────────────────┘                          └──────────┬────────────┘
                                                              │
                                     ┌────────────────────────┴────────┐
                                     ▼                                 ▼
                          ┌─────────────────────┐           ┌────────────────────┐
                          │    Data Layer       │           │   Playback Engine  │
                          ├─────────────────────┤           ├────────────────────┤
                          │ • MediaRepository   │           │ • VideoController  │
                          │ • AppDatabase(Room) │           │ • Custom SRT Engine│
                          │   - WatchHistoryDao │           │ • Speed Controller │
                          │   - BookmarkDao     │           │ • Quality Switcher │
                          │   - SourceDao       │           └────────────────────┘
                          │ • NetworkStreamSync │
                          └─────────────────────┘
```

### Data Model & Entities

```kotlin
// 1. Room Entity: Watch History & Resume State
@Entity(tableName = "watch_history")
data class WatchHistoryEntity(
    @PrimaryKey val mediaId: String,
    val title: String,
    val posterUrl: String,
    val streamUrl: String,
    val lastPositionMs: Long,
    val durationMs: Long,
    val updatedAt: Long = System.currentTimeMillis()
)

// 2. Room Entity: Bookmarks / Watchlist
@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey val mediaId: String,
    val title: String,
    val category: String,
    val posterUrl: String,
    val streamUrl: String,
    val durationText: String,
    val rating: Float,
    val addedAt: Long = System.currentTimeMillis()
)

// 3. Room Entity: Custom Stream & Repository Sources
@Entity(tableName = "custom_sources")
data class CustomSourceEntity(
    @PrimaryKey val id: String,
    val name: String,
    val url: String,
    val type: String, // "HLS", "MP4_FEED", "JSON_CATALOG"
    val isEnabled: Boolean = true,
    val addedAt: Long = System.currentTimeMillis()
)

// 4. Domain Model: Media Item
data class MediaItem(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val posterUrl: String,
    val backdropUrl: String,
    val durationText: String,
    val releaseYear: String,
    val rating: Float,
    val streamSources: List<StreamSource>,
    val subtitles: List<SubtitleTrack> = emptyList()
)

data class StreamSource(
    val label: String,       // e.g. "1080p FHD", "720p HD", "480p SD"
    val url: String,
    val quality: String
)

data class SubtitleTrack(
    val language: String,
    val code: String,
    val srtContent: String
)
```

### Interactive State Transitions

- **Launch / Resume**: On app launch, `MediaCatalogVM` loads curated and custom sources, while `LibraryVM` streams `watch_history` to populate the "Continue Watching" carousel with visual progress bars.
- **Card Click**: Opens the `MediaDetailSheet` with stream source selection, metadata, and single-tap playback.
- **Playback Start**: Launches `CloudStreamPlayer`. If history exists for this `mediaId`, player prompts or resumes at `lastPositionMs`.
- **In-Player Adjustments**:
  - Speed selector updates `playbackParams.speed` dynamically (0.5x to 2.0x).
  - Quality selector seamlessly switches stream source while preserving current playback timestamp.
  - Subtitle toggle parses WebVTT/SRT cue timestamps and renders synced overlay text.
- **Source Management**: User can paste a custom stream URL or repo, test connectivity with an active ping, and see newly imported media items instantly appear in the catalog.
