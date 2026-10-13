package com.example.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ProfileRepository
import com.example.data.local.ProfileEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModel(private val profileRepository: ProfileRepository) : ViewModel() {

    val profiles: StateFlow<List<ProfileEntity>> = profileRepository.profiles
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val activeProfileId: StateFlow<Long> = profileRepository.activeProfileId

    val activeProfile: StateFlow<ProfileEntity?> = combine(profiles, activeProfileId) { list, activeId ->
        list.find { it.id == activeId } ?: list.firstOrNull()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    fun switchProfile(profileId: Long) {
        profileRepository.setActiveProfile(profileId)
    }

    fun addProfile(name: String, avatarId: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = profileRepository.addProfile(name, avatarId)
            onResult(success)
        }
    }

    fun updateProfile(profile: ProfileEntity) {
        viewModelScope.launch {
            profileRepository.updateProfile(profile)
        }
    }

    fun deleteProfile(profileId: Long, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = profileRepository.deleteProfile(profileId)
            onResult(success)
        }
    }
}
