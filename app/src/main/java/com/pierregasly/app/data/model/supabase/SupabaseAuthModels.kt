package com.pierregasly.app.data.model.supabase

import com.google.gson.annotations.SerializedName

data class SupabaseSignUpRequest(
    val email: String,
    val password: String,
    val data: Map<String, Any>? = null
)

data class SupabasePasswordGrantRequest(
    val email: String,
    val password: String
)

data class SupabaseRecoverRequest(
    val email: String
)

data class SupabaseAuthResponse(
    @SerializedName("access_token") val accessToken: String? = null,
    @SerializedName("refresh_token") val refreshToken: String? = null,
    @SerializedName("token_type") val tokenType: String? = null,
    @SerializedName("expires_in") val expiresIn: Long? = null,
    val user: SupabaseUser? = null
)

data class SupabaseUser(
    val id: String? = null,
    val email: String? = null,
    @SerializedName("email_confirmed_at") val emailConfirmedAt: String? = null
)

/**
 * Payload for your `public.users` table.
 * Your schema (based on earlier errors) expects `name`, `email`, `phone`.
 */
data class UserRowUpsert(
    @SerializedName("auth_user_id") val authUserId: String,
    @SerializedName("name") val name: String,
    val email: String,
    val phone: String,
    val role: String = "customer",
    val status: String = "active"
)


data class SupabaseVerifyOtpRequest(
    val type: String,
    val email: String,
    val token: String
)

data class SupabaseResendRequest(
    val type: String,
    val email: String
)

data class SupabaseUpdateUserRequest(
    val password: String
)

data class SupabaseUserResponse(
    val user: SupabaseUser? = null
)
