package com.LCM.lifereplayapp.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.LCM.lifereplayapp.viewmodel.UserState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val USER_STATE = stringPreferencesKey("user_state")
    }

    val userStateFlow: Flow<UserState> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val userStateJson = preferences[PreferencesKeys.USER_STATE]
            if (userStateJson != null) {
                try {
                    Json.decodeFromString<UserState>(userStateJson)
                } catch (e: Exception) {
                    UserState()
                }
            } else {
                UserState()
            }
        }

    suspend fun saveUserState(userState: UserState) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_STATE] = Json.encodeToString(userState)
        }
    }

    suspend fun clearUserState() {
        context.dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.USER_STATE)
        }
    }
}
