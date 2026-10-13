package com.example.data

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.ProfileEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ProfileRepository(private val database: AppDatabase, context: Context) {
    private val profileDao = database.profileDao()
    private val watchHistoryDao = database.watchHistoryDao()
    private val bookmarkDao = database.bookmarkDao()

    private val prefs = context.getSharedPreferences("cloudstream_profile_prefs", Context.MODE_PRIVATE)
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _activeProfileId = MutableStateFlow(prefs.getLong("active_profile_id", 1L))
    val activeProfileId: StateFlow<Long> = _activeProfileId.asStateFlow()

    val profiles = profileDao.getAllProfiles()

    init {
        scope.launch {
            ensureDefaultProfile()
        }
    }

    suspend fun ensureDefaultProfile() {
        val count = profileDao.getProfileCount()
        if (count == 0) {
            val defaultId = profileDao.insertProfile(
                ProfileEntity(
                    name = "Profil 1",
                    avatarId = "avatar_1",
                    createdAt = System.currentTimeMillis()
                )
            )
            setActiveProfile(defaultId)
        } else {
            val currentActive = prefs.getLong("active_profile_id", 1L)
            val activeProfile = profileDao.getProfileById(currentActive)
            if (activeProfile == null) {
                val firstProfile = profileDao.getAllProfiles().first().firstOrNull()
                if (firstProfile != null) {
                    setActiveProfile(firstProfile.id)
                }
            }
        }
    }

    fun setActiveProfile(profileId: Long) {
        prefs.edit().putLong("active_profile_id", profileId).apply()
        _activeProfileId.value = profileId
    }

    suspend fun addProfile(name: String, avatarId: String): Boolean {
        val count = profileDao.getProfileCount()
        if (count >= 5) return false
        val trimmedName = name.trim()
        if (trimmedName.isBlank() || trimmedName.length > 20) return false

        val newProfile = ProfileEntity(
            name = trimmedName,
            avatarId = avatarId,
            createdAt = System.currentTimeMillis()
        )
        profileDao.insertProfile(newProfile)
        return true
    }

    suspend fun updateProfile(profile: ProfileEntity) {
        profileDao.updateProfile(profile)
    }

    suspend fun deleteProfile(profileId: Long): Boolean {
        val count = profileDao.getProfileCount()
        if (count <= 1) return false // Cannot delete last profile

        watchHistoryDao.clearHistoryForProfile(profileId)
        bookmarkDao.deleteBookmarksForProfile(profileId)
        profileDao.deleteProfile(profileId)

        if (_activeProfileId.value == profileId) {
            val remaining = profileDao.getAllProfiles().first()
            remaining.firstOrNull()?.id?.let { setActiveProfile(it) }
        }
        return true
    }
}
