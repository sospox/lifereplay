package com.LCM.lifereplayapp.data.models

import kotlinx.serialization.Serializable

@Serializable
data class UserModel(
    val email:String = "",
    val password:String = "",
    val name:String = ""
)
