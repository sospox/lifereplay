package com.LCM.lifereplayapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.LCM.lifereplayapp.data.UserPreferencesRepository
import com.LCM.lifereplayapp.repositories.GenerativeAiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
data class Memory(
    val id: String = java.util.UUID.randomUUID().toString(),
    val type: MemoryType,
    val contentUri: String? = null,
    val text: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
enum class MemoryType {
    IMAGE, VOICE, MUSIC
}

@Serializable
data class UserState(
    val name: String = "",
    val email: String = "",
    val isLoggedIn: Boolean = false,
    val memories: List<Memory> = emptyList(),
    val aiGeneratedStory: String? = null
)

class UserViewModel(
    private val repository: UserPreferencesRepository,
    private val aiRepository: GenerativeAiRepository? = null
) : ViewModel() {
    private val _userState = MutableStateFlow(UserState())
    val userState: StateFlow<UserState> = _userState.asStateFlow()

    private val _isGeneratingStory = MutableStateFlow(false)
    val isGeneratingStory: StateFlow<Boolean> = _isGeneratingStory.asStateFlow()

    init {
        viewModelScope.launch {
            repository.userStateFlow.collectLatest { savedState ->
                _userState.value = savedState
            }
        }
    }

    fun generateAiStory() {
        val memories = _userState.value.memories
        if (memories.isEmpty() || aiRepository == null) return

        viewModelScope.launch {
            _isGeneratingStory.value = true
            try {
                val memoryPairs = memories.map { it.contentUri to it.text }
                val story = aiRepository.generateStory(memoryPairs)
                if (story != null) {
                    updateState(_userState.value.copy(aiGeneratedStory = story))
                }
            } catch (e: Exception) {
                // Log error
            } finally {
                _isGeneratingStory.value = false
            }
        }
    }

    fun addMemory(type: MemoryType, contentUri: String? = null, text: String? = null) {
        val newMemory = Memory(type = type, contentUri = contentUri, text = text)
        val updatedMemories = _userState.value.memories + newMemory
        updateState(_userState.value.copy(memories = updatedMemories))
    }

    fun login(email: String) {
        val name = email.split("@").firstOrNull()?.replaceFirstChar { it.uppercase() } ?: "User"
        updateState(UserState(
            name = name,
            email = email,
            isLoggedIn = true,
            memories = _userState.value.memories
        ))
    }

    fun logout() {
        viewModelScope.launch {
            repository.clearUserState()
        }
    }

    private fun updateState(newState: UserState) {
        viewModelScope.launch {
            repository.saveUserState(newState)
        }
    }
}
