package com.example.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.sources.SourcesScreen
import com.example.ui.viewmodel.CatalogViewModel
import com.example.ui.viewmodel.LibraryViewMode

sealed class ProfileSubScreen {
    object Main : ProfileSubScreen()
    object Sources : ProfileSubScreen()
    object LayoutSettings : ProfileSubScreen()
    object PlaybackSettings : ProfileSubScreen()
}

@Composable
fun ProfileSettingsScreen(
    viewModel: CatalogViewModel
) {
    var currentSubScreen by remember { mutableStateOf<ProfileSubScreen>(ProfileSubScreen.Main) }
    var updateChannel by remember { mutableStateOf("Stable") }
    var showUpdateChannelDialog by remember { mutableStateOf(false) }

    var comingSoonTitle by remember { mutableStateOf<String?>(null) }
    var showComingSoonDialog by remember { mutableStateOf(false) }

    val libraryViewMode by viewModel.libraryViewMode.collectAsState()

    // Sub-screen routing
    when (currentSubScreen) {
        ProfileSubScreen.Sources -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                // Header with back button for Sources
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { currentSubScreen = ProfileSubScreen.Main }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Konten & Penemuan",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                SourcesScreen(viewModel = viewModel)
            }
            return
        }
        ProfileSubScreen.LayoutSettings -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { currentSubScreen = ProfileSubScreen.Main }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Tata Letak",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text("Gaya Tampilan Pustaka Default", color = Color(0xFF9A9AA0), fontSize = 14.sp)
                Spacer(modifier = Modifier.height(12.dp))

                SettingsCard {
                    Column {
                        SettingsRow(
                            icon = Icons.Default.Palette,
                            title = "Mode Baris (Row)",
                            subtitle = "Tampilkan katalog berkelompok dengan poster landscape 16:9",
                            trailingText = if (libraryViewMode == LibraryViewMode.ROW) "Aktif" else null,
                            onClick = { viewModel.setLibraryViewMode(LibraryViewMode.ROW) }
                        )
                        Divider(color = Color.White.copy(alpha = 0.08f), thickness = 1.dp, modifier = Modifier.padding(start = 72.dp))
                        SettingsRow(
                            icon = Icons.Default.Palette,
                            title = "Mode Grid",
                            subtitle = "Tampilkan grid poster portrait 3 kolom",
                            trailingText = if (libraryViewMode == LibraryViewMode.GRID) "Aktif" else null,
                            onClick = { viewModel.setLibraryViewMode(LibraryViewMode.GRID) }
                        )
                    }
                }
            }
            return
        }
        ProfileSubScreen.PlaybackSettings -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { currentSubScreen = ProfileSubScreen.Main }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Pemutaran",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                SettingsCard {
                    Column {
                        SettingsRow(
                            icon = Icons.Default.PlayArrow,
                            title = "Putar Otomatis Episode Berikutnya",
                            subtitle = "Lanjut otomatis ke episode selanjutnya saat selesai",
                            onClick = {
                                comingSoonTitle = "Putar Otomatis"
                                showComingSoonDialog = true
                            }
                        )
                        Divider(color = Color.White.copy(alpha = 0.08f), thickness = 1.dp, modifier = Modifier.padding(start = 72.dp))
                        SettingsRow(
                            icon = Icons.Default.PlayArrow,
                            title = "Kecepatan Putar Default",
                            subtitle = "Atur kecepatan playback awal (1.0x)",
                            trailingText = "1.0x",
                            onClick = {
                                comingSoonTitle = "Kecepatan Putar Default"
                                showComingSoonDialog = true
                            }
                        )
                    }
                }
            }
            return
        }
        ProfileSubScreen.Main -> {}
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("profile_settings_screen")
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 180.dp)
        ) {
            // Sticky-like Header
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black)
                        .padding(horizontal = 16.dp, vertical = 20.dp)
                ) {
                    Text(
                        text = "Pengaturan",
                        color = Color.White,
                        fontSize = 40.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                // Thin gradient line below header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.15f), Color.Transparent)
                            )
                        )
                )
            }

            // === AKUN ===
            item {
                SettingsSectionLabel("AKUN")
            }
            item {
                SettingsCard {
                    Column {
                        SettingsRow(
                            icon = Icons.Default.People,
                            title = "Ganti Profil",
                            subtitle = "Beralih ke profil yang berbeda.",
                            onClick = {
                                comingSoonTitle = "Ganti Profil"
                                showComingSoonDialog = true
                            }
                        )
                        Divider(color = Color.White.copy(alpha = 0.08f), thickness = 1.dp, modifier = Modifier.padding(start = 72.dp))
                        SettingsRow(
                            icon = Icons.Default.AccountCircle,
                            title = "Akun",
                            subtitle = "Status akun dan sinkronisasi",
                            onClick = {
                                comingSoonTitle = "Akun"
                                showComingSoonDialog = true
                            }
                        )
                        Divider(color = Color.White.copy(alpha = 0.08f), thickness = 1.dp, modifier = Modifier.padding(start = 72.dp))
                        SettingsRow(
                            icon = Icons.Default.Sync,
                            title = "Tracking",
                            subtitle = "Hubungkan penyedia tracking dan pilih yang menjalankan Pustaka dan Lanjut Menonton.",
                            onClick = {
                                comingSoonTitle = "Tracking"
                                showComingSoonDialog = true
                            }
                        )
                    }
                }
            }

            // === UMUM ===
            item {
                SettingsSectionLabel("UMUM")
            }
            item {
                SettingsCard {
                    Column {
                        SettingsRow(
                            icon = Icons.Default.Palette,
                            title = "Tata Letak",
                            subtitle = "Struktur beranda dan gaya poster",
                            onClick = {
                                currentSubScreen = ProfileSubScreen.LayoutSettings
                            }
                        )
                        Divider(color = Color.White.copy(alpha = 0.08f), thickness = 1.dp, modifier = Modifier.padding(start = 72.dp))
                        SettingsRow(
                            icon = Icons.Default.Extension,
                            title = "Konten & Penemuan",
                            subtitle = "Kelola addon dan sumber penemuan.",
                            onClick = {
                                currentSubScreen = ProfileSubScreen.Sources
                            }
                        )
                        Divider(color = Color.White.copy(alpha = 0.08f), thickness = 1.dp, modifier = Modifier.padding(start = 72.dp))
                        SettingsRow(
                            icon = Icons.Default.CloudDownload,
                            title = "Unduhan",
                            subtitle = "Kelola film dan episode yang telah diunduh.",
                            onClick = {
                                comingSoonTitle = "Unduhan"
                                showComingSoonDialog = true
                            }
                        )
                        Divider(color = Color.White.copy(alpha = 0.08f), thickness = 1.dp, modifier = Modifier.padding(start = 72.dp))
                        SettingsRow(
                            icon = Icons.Default.PlayArrow,
                            title = "Pemutaran",
                            subtitle = "Pemutar, subtitle, dan putar otomatis",
                            onClick = {
                                currentSubScreen = ProfileSubScreen.PlaybackSettings
                            }
                        )
                        Divider(color = Color.White.copy(alpha = 0.08f), thickness = 1.dp, modifier = Modifier.padding(start = 72.dp))
                        SettingsRow(
                            icon = Icons.Default.Link,
                            title = "Integrasi",
                            subtitle = "Kelola integrasi yang tersedia",
                            onClick = {
                                comingSoonTitle = "Integrasi"
                                showComingSoonDialog = true
                            }
                        )
                        Divider(color = Color.White.copy(alpha = 0.08f), thickness = 1.dp, modifier = Modifier.padding(start = 72.dp))
                        SettingsRow(
                            icon = Icons.Default.Notifications,
                            title = "Notifikasi",
                            subtitle = "Kelola peringatan rilis episode dan kirim notifikasi uji coba.",
                            onClick = {
                                comingSoonTitle = "Notifikasi"
                                showComingSoonDialog = true
                            }
                        )
                    }
                }
            }

            // === TENTANG ===
            item {
                SettingsSectionLabel("TENTANG")
            }
            item {
                SettingsCard {
                    Column {
                        SettingsRow(
                            icon = Icons.Default.Favorite,
                            title = "Kontributor & Donatur",
                            subtitle = "Apresiasi terbuka dan kredit proyek",
                            onClick = {
                                comingSoonTitle = "Kontributor & Donatur"
                                showComingSoonDialog = true
                            }
                        )
                        Divider(color = Color.White.copy(alpha = 0.08f), thickness = 1.dp, modifier = Modifier.padding(start = 72.dp))
                        SettingsRow(
                            icon = Icons.Default.PrivacyTip,
                            title = "Privacy Policy",
                            subtitle = "Pelajari bagaimana CloudStream menangani data Anda",
                            onClick = {
                                comingSoonTitle = "Privacy Policy"
                                showComingSoonDialog = true
                            }
                        )
                        Divider(color = Color.White.copy(alpha = 0.08f), thickness = 1.dp, modifier = Modifier.padding(start = 72.dp))
                        SettingsRow(
                            icon = Icons.Default.Info,
                            title = "Lisensi & Atribusi",
                            subtitle = "Sumber data, pengakuan, dan lisensi platform",
                            onClick = {
                                comingSoonTitle = "Lisensi & Atribusi"
                                showComingSoonDialog = true
                            }
                        )
                        Divider(color = Color.White.copy(alpha = 0.08f), thickness = 1.dp, modifier = Modifier.padding(start = 72.dp))
                        SettingsRow(
                            icon = Icons.Default.SystemUpdate,
                            title = "Update channel",
                            subtitle = "Pilih jenis pembaruan aplikasi yang diterima",
                            trailingText = updateChannel,
                            onClick = {
                                showUpdateChannelDialog = true
                            }
                        )
                        Divider(color = Color.White.copy(alpha = 0.08f), thickness = 1.dp, modifier = Modifier.padding(start = 72.dp))
                        SettingsRow(
                            icon = Icons.Default.CloudDownload,
                            title = "Periksa pembaruan",
                            subtitle = "Unduh versi terbaru",
                            onClick = {
                                comingSoonTitle = "Periksa pembaruan"
                                showComingSoonDialog = true
                            }
                        )
                    }
                }
            }
        }
    }

    // Dialog Update Channel
    if (showUpdateChannelDialog) {
        AlertDialog(
            onDismissRequest = { showUpdateChannelDialog = false },
            containerColor = Color(0xFF1C1C1E),
            title = { Text("Update channel", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { updateChannel = "Stable"; showUpdateChannelDialog = false }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = (updateChannel == "Stable"), onClick = { updateChannel = "Stable"; showUpdateChannelDialog = false })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Stable (Stabil)", color = Color.White)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { updateChannel = "Beta"; showUpdateChannelDialog = false }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = (updateChannel == "Beta"), onClick = { updateChannel = "Beta"; showUpdateChannelDialog = false })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Beta (Pratinjau)", color = Color.White)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showUpdateChannelDialog = false }) {
                    Text("Tutup", color = Color(0xFF2F8CF0))
                }
            }
        )
    }

    // Dialog Coming Soon
    if (showComingSoonDialog && comingSoonTitle != null) {
        AlertDialog(
            onDismissRequest = { showComingSoonDialog = false },
            containerColor = Color(0xFF1C1C1E),
            title = { Text(comingSoonTitle!!, color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Text("Fitur $comingSoonTitle akan segera hadir di pembaruan mendatang CloudStream.", color = Color(0xFF9A9AA0))
            },
            confirmButton = {
                TextButton(onClick = { showComingSoonDialog = false }) {
                    Text("OK", color = Color(0xFF2F8CF0))
                }
            }
        )
    }
}

@Composable
fun SettingsSectionLabel(text: String) {
    Text(
        text = text,
        color = Color(0xFF9A9AA0),
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.5.sp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 20.dp, bottom = 8.dp)
    )
}

@Composable
fun SettingsCard(content: @Composable () -> Unit) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = null,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        content()
    }
}

@Composable
fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    trailingText: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(Color(0xFF0F2540), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF2F8CF0),
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = Color(0xFF9A9AA0),
                fontSize = 14.sp,
                maxLines = 3
            )
        }

        if (trailingText != null) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = trailingText,
                color = Color(0xFF9A9AA0),
                fontSize = 14.sp,
                maxLines = 1,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
