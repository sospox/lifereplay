package com.LCM.lifereplayapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

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
    val memories: List<Memory> = emptyList()
)

import com.LCM.lifereplayapp.data.UserPreferencesRepository
import kotlinx.coroutines.flow.collectLatest

class UserViewModel(private val repository: UserPreferencesRepository) : ViewModel() {
    private val _userState = MutableStateFlow(UserState())
    val userState: StateFlow<UserState> = _userState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.userStateFlow.collectLatest { savedState ->
                _userState.value = savedState
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
