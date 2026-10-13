package com.example.ui.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ProfileEntity

@Composable
fun ManageProfilesScreen(
    profileViewModel: ProfileViewModel,
    onBack: () -> Unit
) {
    val profiles by profileViewModel.profiles.collectAsState()
    var editingProfile by remember { mutableStateOf<ProfileEntity?>(null) }
    var isCreatingNew by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf<ProfileEntity?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("manage_back_button")
                ) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Kelola Profil",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Profile List
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(profiles) { profile ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { editingProfile = profile }
                            .testTag("manage_profile_row_${profile.id}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ProfileAvatar(avatarId = profile.avatarId, size = 56.dp)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = profile.name,
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Ketuk untuk mengedit",
                                    color = Color(0xFF9A9AA0),
                                    fontSize = 13.sp
                                )
                            }
                            IconButton(onClick = { editingProfile = profile }) {
                                Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Profil", tint = Color(0xFF2F8CF0))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Add Profile Button
            val canAdd = profiles.size < 5
            Button(
                onClick = {
                    if (canAdd) {
                        isCreatingNew = true
                        editingProfile = ProfileEntity(name = "", avatarId = "avatar_1")
                        errorMessage = null
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("add_profile_button"),
                enabled = canAdd,
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2F8CF0),
                    disabledContainerColor = Color(0xFF1C1C1E)
                )
            ) {
                Icon(imageVector = Icons.Default.PersonAdd, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (canAdd) "Tambah Profil" else "Maksimal 5 Profil",
                    color = if (canAdd) Color.White else Color(0xFF9A9AA0),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    // Edit / Create Dialog
    editingProfile?.let { profile ->
        var nameInput by remember { mutableStateOf(profile.name) }
        var selectedAvatarId by remember { mutableStateOf(profile.avatarId) }

        AlertDialog(
            onDismissRequest = {
                editingProfile = null
                isCreatingNew = false
            },
            containerColor = Color(0xFF1C1C1E),
            title = {
                Text(
                    text = if (isCreatingNew) "Tambah Profil Baru" else "Edit Profil",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    TextField(
                        value = nameInput,
                        onValueChange = { if (it.length <= 20) nameInput = it },
                        placeholder = { Text("Nama profil", color = Color(0xFF9A9AA0)) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF121214),
                            unfocusedContainerColor = Color(0xFF121214),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_name_input"),
                        maxLines = 1
                    )
                    
                    errorMessage?.let {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = it, color = Color(0xFFFF5252), fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Pilih Avatar", color = Color(0xFF9A9AA0), fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(AVAILABLE_AVATARS) { avatar ->
                            val isSelected = selectedAvatarId == avatar.id
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) Color(0xFF2F8CF0) else Color.Transparent)
                                    .clickable { selectedAvatarId = avatar.id }
                                    .padding(if (isSelected) 3.dp else 0.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                ProfileAvatar(avatarId = avatar.id, size = 48.dp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (!isCreatingNew && profiles.size > 1) {
                        TextButton(
                            onClick = {
                                showDeleteConfirmDialog = profile
                                editingProfile = null
                            },
                            modifier = Modifier.testTag("delete_profile_button")
                        ) {
                            Text("Hapus", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    Row {
                        TextButton(onClick = { editingProfile = null; isCreatingNew = false }) {
                            Text("Batal", color = Color(0xFF9A9AA0))
                        }
                        Button(
                            onClick = {
                                val trimmed = nameInput.trim()
                                if (trimmed.isBlank()) {
                                    errorMessage = "Nama tidak boleh kosong"
                                    return@Button
                                }
                                if (isCreatingNew) {
                                    profileViewModel.addProfile(trimmed, selectedAvatarId) { success ->
                                        if (success) {
                                            editingProfile = null
                                            isCreatingNew = false
                                        } else {
                                            errorMessage = "Gagal menambah profil"
                                        }
                                    }
                                } else {
                                    val updated = profile.copy(name = trimmed, avatarId = selectedAvatarId)
                                    profileViewModel.updateProfile(updated)
                                    editingProfile = null
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2F8CF0)),
                            modifier = Modifier.testTag("save_profile_button")
                        ) {
                            Text("Simpan", color = Color.White)
                        }
                    }
                }
            }
        )
    }

    // Delete Confirmation Dialog
    showDeleteConfirmDialog?.let { profileToDelete ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = null },
            containerColor = Color(0xFF1C1C1E),
            title = { Text("Hapus Profil ${profileToDelete.name}?", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text("Semua riwayat tonton dan bookmark milik profil ini akan ikut terhapus secara permanen.", color = Color(0xFF9A9AA0)) },
            confirmButton = {
                Button(
                    onClick = {
                        profileViewModel.deleteProfile(profileToDelete.id) { success ->
                            showDeleteConfirmDialog = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252)),
                    modifier = Modifier.testTag("confirm_delete_button")
                ) {
                    Text("Hapus", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = null }) {
                    Text("Batal", color = Color(0xFF9A9AA0))
                }
            }
        )
    }
}
