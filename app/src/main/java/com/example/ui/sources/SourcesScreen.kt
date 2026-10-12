package com.example.ui.sources

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Stream
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.StreamDarkBackground
import com.example.ui.theme.StreamDarkBorder
import com.example.ui.theme.StreamDarkSurface
import com.example.ui.theme.StreamDarkSurfaceVariant
import com.example.ui.theme.StreamPrimary
import com.example.ui.theme.StreamTextPrimary
import com.example.ui.theme.StreamTextSecondary
import com.example.ui.viewmodel.CatalogViewModel
import kotlinx.coroutines.launch

@Composable
fun SourcesScreen(
    viewModel: CatalogViewModel
) {
    val customSources by viewModel.customSources.collectAsState()
    val scope = rememberCoroutineScope()

    var sourceName by remember { mutableStateOf("") }
    var sourceUrl by remember { mutableStateOf("") }
    var isCheckingUrl by remember { mutableStateOf(false) }
    var urlTestResult by remember { mutableStateOf<Boolean?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(StreamDarkBackground)
            .testTag("sources_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 90.dp)
    ) {
        // Screen Header
        item {
            Column(modifier = Modifier.padding(vertical = 16.dp)) {
                Text(
                    text = "Streaming Sources",
                    color = StreamTextPrimary,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Import custom stream links, HLS channels, and repositories",
                    color = StreamTextSecondary,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        // Add Custom Stream Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = StreamDarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, StreamDarkBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
                    .testTag("add_source_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(StreamPrimary.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = StreamPrimary, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Import Stream or Repository",
                            color = StreamTextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Source Name Input
                    OutlinedTextField(
                        value = sourceName,
                        onValueChange = { sourceName = it },
                        label = { Text("Source / Channel Name", color = StreamTextSecondary) },
                        placeholder = { Text("e.g. My HLS Channel, Open Anime Feed", color = StreamTextSecondary) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = StreamDarkSurfaceVariant,
                            unfocusedContainerColor = StreamDarkSurfaceVariant,
                            focusedBorderColor = StreamPrimary,
                            unfocusedBorderColor = StreamDarkBorder,
                            focusedTextColor = StreamTextPrimary,
                            unfocusedTextColor = StreamTextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("source_name_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Stream URL Input
                    OutlinedTextField(
                        value = sourceUrl,
                        onValueChange = {
                            sourceUrl = it
                            urlTestResult = null
                            errorMessage = null
                        },
                        label = { Text("Stream URL (.m3u8, .mp4, feed)", color = StreamTextSecondary) },
                        placeholder = { Text("https://example.com/live/stream.m3u8", color = StreamTextSecondary) },
                        leadingIcon = {
                            Icon(Icons.Default.Link, contentDescription = null, tint = StreamPrimary)
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = StreamDarkSurfaceVariant,
                            unfocusedContainerColor = StreamDarkSurfaceVariant,
                            focusedBorderColor = StreamPrimary,
                            unfocusedBorderColor = StreamDarkBorder,
                            focusedTextColor = StreamTextPrimary,
                            unfocusedTextColor = StreamTextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("source_url_input")
                    )

                    // URL Connection test feedback
                    if (urlTestResult != null) {
                        Row(
                            modifier = Modifier.padding(top = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (urlTestResult == true) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF22C55E), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Connection verified! Stream is reachable.", color = Color(0xFF22C55E), fontSize = 12.sp)
                            } else {
                                Icon(Icons.Default.Error, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Could not connect to URL. Check address.", color = Color(0xFFEF4444), fontSize = 12.sp)
                            }
                        }
                    }

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage ?: "",
                            color = Color(0xFFEF4444),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Test URL button
                        OutlinedButton(
                            onClick = {
                                val trimmed = sourceUrl.trim()
                                if (trimmed.isBlank()) {
                                    errorMessage = "Silakan masukkan URL sumber stream."
                                    return@OutlinedButton
                                }
                                if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
                                    errorMessage = "URL harus diawali dengan http:// atau https://"
                                    urlTestResult = null
                                    return@OutlinedButton
                                }
                                isCheckingUrl = true
                                errorMessage = null
                                urlTestResult = null
                                scope.launch {
                                    val isOk = viewModel.testUrl(trimmed)
                                    urlTestResult = isOk
                                    isCheckingUrl = false
                                }
                            },
                            enabled = !isCheckingUrl,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("test_source_button")
                        ) {
                            if (isCheckingUrl) {
                                CircularProgressIndicator(color = StreamPrimary, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                            } else {
                                Icon(Icons.Default.NetworkCheck, contentDescription = null, tint = StreamPrimary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Test URL", color = StreamPrimary, fontSize = 13.sp)
                            }
                        }

                        // Add Source button
                        Button(
                            onClick = {
                                val trimmed = sourceUrl.trim()
                                if (trimmed.isBlank()) {
                                    errorMessage = "Silakan masukkan URL sumber stream."
                                    return@Button
                                }
                                if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
                                    errorMessage = "URL harus diawali dengan http:// atau https://"
                                    return@Button
                                }
                                val name = if (sourceName.isBlank()) "Stream Source #${customSources.size + 1}" else sourceName.trim()
                                val isHls = trimmed.substringBefore('?').substringBefore('#').endsWith(".m3u8", ignoreCase = true)
                                val type = if (isHls) "HLS" else "DIRECT"
                                viewModel.addCustomSource(name, trimmed, type)
                                sourceName = ""
                                sourceUrl = ""
                                urlTestResult = null
                                errorMessage = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = StreamPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("add_source_button")
                        ) {
                            Text("Add Source", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // Quick Preset Channels
        item {
            Column(modifier = Modifier.padding(bottom = 20.dp)) {
                Text(
                    text = "Quick Stream Presets",
                    color = StreamTextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                val presets = listOf(
                    Triple("Apple HLS BipBop Test", "https://devstreaming-cdn.apple.com/videos/streaming/examples/bipbop_4x3/bipbop_4x3_variant.m3u8", "HLS"),
                    Triple("Unified Streaming Demo", "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8", "HLS"),
                    Triple("Mux Adaptive CDN Feed", "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8", "HLS")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presets.forEach { (name, url, type) ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = StreamDarkSurface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, StreamDarkBorder),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = name,
                                    color = StreamTextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = type,
                                    color = StreamPrimary,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(top = 2.dp, bottom = 6.dp)
                                )
                                OutlinedButton(
                                    onClick = {
                                        viewModel.addCustomSource(name, url, type)
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Icon(Icons.Default.CloudDownload, contentDescription = null, tint = StreamPrimary, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("+ Import", color = StreamPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Active Repositories List
        item {
            Text(
                text = "Active Catalog Repositories (${customSources.size + 1})",
                color = StreamTextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        // Built-in Default Repository
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = StreamDarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, StreamPrimary.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(StreamPrimary.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Stream, contentDescription = null, tint = StreamPrimary, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Curated Open Media Hub",
                                color = StreamTextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = StreamPrimary.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "OFFICIAL",
                                    color = StreamPrimary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Blender Open Movies, Sintel, Tears of Steel, Wild Horizons",
                            color = StreamTextSecondary,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0x3322C55E)
                    ) {
                        Text(
                            text = "Active",
                            color = Color(0xFF22C55E),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Custom Sources List
        if (customSources.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = StreamDarkSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No custom sources imported yet",
                            color = StreamTextSecondary,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Use the form above or pick a quick preset to add custom feeds",
                            color = StreamTextSecondary,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        } else {
            items(customSources) { source ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = StreamDarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StreamDarkBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .testTag("custom_source_${source.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = source.name,
                                    color = StreamTextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = StreamDarkSurfaceVariant
                                ) {
                                    Text(
                                        text = source.type,
                                        color = StreamPrimary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = source.url,
                                color = StreamTextSecondary,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                        // Enable/Disable switch
                        Switch(
                            checked = source.isEnabled,
                            onCheckedChange = { isChecked ->
                                viewModel.toggleCustomSource(source.id, isChecked)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = StreamPrimary,
                                uncheckedThumbColor = StreamTextSecondary,
                                uncheckedTrackColor = StreamDarkSurfaceVariant
                            )
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        // Delete button
                        IconButton(
                            onClick = { viewModel.deleteCustomSource(source.id) }
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete source", tint = StreamTextSecondary)
                        }
                    }
                }
            }
        }
    }
}
