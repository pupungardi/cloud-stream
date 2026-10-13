package com.example.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EmojiEmotions
import androidx.compose.material.icons.rounded.Face
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class AvatarOption(
    val id: String,
    val colors: List<Color>,
    val icon: ImageVector
)

val AVAILABLE_AVATARS = listOf(
    AvatarOption("avatar_1", listOf(Color(0xFF2F8CF0), Color(0xFF12304D)), Icons.Rounded.Person),
    AvatarOption("avatar_2", listOf(Color(0xFF9C27B0), Color(0xFF4A148C)), Icons.Rounded.Star),
    AvatarOption("avatar_3", listOf(Color(0xFFE91E63), Color(0xFF880E4F)), Icons.Rounded.Favorite),
    AvatarOption("avatar_4", listOf(Color(0xFFFF9800), Color(0xFFE65100)), Icons.Rounded.LocalFireDepartment),
    AvatarOption("avatar_5", listOf(Color(0xFF4CAF50), Color(0xFF1B5E20)), Icons.Rounded.Pets),
    AvatarOption("avatar_6", listOf(Color(0xFF00BCD4), Color(0xFF006064)), Icons.Rounded.Face),
    AvatarOption("avatar_7", listOf(Color(0xFFFFEB3B), Color(0xFFF57F17)), Icons.Rounded.EmojiEmotions),
    AvatarOption("avatar_8", listOf(Color(0xFF673AB7), Color(0xFF311B92)), Icons.Rounded.SportsEsports),
)

@Composable
fun ProfileAvatar(
    avatarId: String,
    size: Dp = 64.dp,
    modifier: Modifier = Modifier
) {
    val option = AVAILABLE_AVATARS.find { it.id == avatarId } ?: AVAILABLE_AVATARS[0]
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(Brush.linearGradient(option.colors)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = option.icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(size * 0.5f)
        )
    }
}
