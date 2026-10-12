# CloudStream - Media Catalog & Video Streamer

A modern, high-performance streaming catalog and video player for Android inspired by CloudStream. Features custom source/repository URL imports, an advanced Media3 ExoPlayer with multi-speed controls, video quality selection, subtitle support, alongside a persistent Room database tracking watch history, resume timestamps, and favorites.

## Core Architecture & Principles

- **Custom Sources Only**: No bundled demo streams or hardcoded sample movies. All catalog items are populated exclusively from user-added, enabled streaming feeds or URLs.
- **Dynamic Categorization**: Category chips and swimlanes are computed in real time from active stream sources.
- **Media3 / ExoPlayer Engine**: Robust playback engine supporting HLS adaptive live streams and direct video feeds, playback speed, aspect ratio options, and subtitle overlay.
- **Room Persistence**: Stores watch progress, bookmarks, and custom sources locally with versioned database migrations (Migration 1->2 cleans past demo artifacts).
