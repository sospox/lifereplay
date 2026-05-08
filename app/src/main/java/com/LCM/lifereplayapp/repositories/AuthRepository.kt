package com.LCM.lifereplayapp.repositories

import com.LCM.lifereplayapp.data.models.UserModel
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest

class AuthRepository : AuthService {
    private val supabase = createSupabaseClient(
        supabaseUrl = "https://xtzvalmhpywlnhzhsjhg.supabase.co",
        supabaseKey = "sb_publishable_0xmF5oJHztzL0pVctB81nw_YQ-48ItT"
    ) {
        install(Postgrest)
        install(Auth)
    }

    override suspend fun registerUser(userDetails: UserModel) {
        // 1. Sign up user in Supabase Auth (this manages authentication)
        supabase.auth.signUpWith(Email) {
            email = userDetails.email
            password = userDetails.password
        }

        // 2. Insert user details into your custom 'auth' table in the public schema
        // Note: Ensure your table columns match the UserModel property names (email, password, name)
        supabase.postgrest["auth"].insert(userDetails)
    }

    override suspend fun loginUser(user: UserModel) {
        supabase.auth.signInWith(Email) {
            email = user.email
            password = user.password
        }
    }

    override suspend fun resetPassword(email: String) {
        supabase.auth.resetPasswordForEmail(email)
    }

    override suspend fun getUserProfile(user: UserModel) {
        // Example: Fetch user data from the 'auth' table
        // val profile = supabase.postgrest["auth"].select {
        //    filter { eq("email", user.email) }
        // }.decodeSingle<UserModel>()
    }

    override suspend fun logoutUser() {
        supabase.auth.signOut()
    }
}
