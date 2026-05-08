package com.LCM.lifereplayapp.repositories

import com.LCM.lifereplayapp.data.models.UserModel

interface AuthService {
    suspend fun registerUser(userDetails: UserModel)
    suspend fun loginUser(user: UserModel)
    suspend fun resetPassword(email: String)
    suspend fun getUserProfile(user: UserModel)
    suspend fun logoutUser()
}