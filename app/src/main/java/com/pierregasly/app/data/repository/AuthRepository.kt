package com.pierregasly.app.data.repository

import com.pierregasly.app.data.api.SupabaseClient
import com.pierregasly.app.data.model.supabase.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Phase 1 repository:
 * - Email + Password signup/login
 * - Email OTP verification for signup and password recovery (6-digit)
 *
 * IMPORTANT:
 * - This uses the ANON key only.
 * - Ensure RLS policies are configured properly in Supabase for any table access.
 */
class AuthRepository {

    suspend fun signUpRequestOtp(fullName: String, email: String, phone: String?, password: String): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                val r = SupabaseClient.auth.signUp(
                    SupabaseSignUpRequest(
                        email = email.trim(),
                        password = password,
                        data = mapOf(
                            "full_name" to fullName.trim(),
                            "phone" to (phone ?: "")
                        )
                    )
                )
                if (r.isSuccessful) {
                    // If your confirmation email template uses {{ .Token }}, Supabase sends a 6-digit OTP.
                    Result.Success("OTP sent to your email. Enter the 6-digit code to activate your account.")
                } else {
                    Result.Error(parseError(r.errorBody()?.string()))
                }
            } catch (e: Exception) {
                Result.Error("Network error: ${e.message}")
            }
        }

    suspend fun resendSignupOtp(email: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val r = SupabaseClient.auth.resend(SupabaseResendRequest(type = "signup", email = email.trim()))
            if (r.isSuccessful) Result.Success("OTP resent. Please check your email.")
            else Result.Error(parseError(r.errorBody()?.string()))
        } catch (e: Exception) {
            Result.Error("Network error: ${e.message}")
        }
    }

    suspend fun verifySignupOtp(email: String, otp: String): Result<SupabaseAuthResponse> = withContext(Dispatchers.IO) {
        try {
            val r = SupabaseClient.auth.verifyOtp(
                SupabaseVerifyOtpRequest(
                    type = "signup",
                    email = email.trim(),
                    token = otp.trim()
                )
            )
            if (r.isSuccessful && r.body()?.accessToken != null) Result.Success(r.body()!!)
            else Result.Error(parseError(r.errorBody()?.string()) ?: "OTP verification failed.")
        } catch (e: Exception) {
            Result.Error("Network error: ${e.message}")
        }
    }

    suspend fun login(email: String, password: String): Result<SupabaseAuthResponse> = withContext(Dispatchers.IO) {
        try {
            val r = SupabaseClient.auth.signInWithPassword(body = SupabasePasswordGrantRequest(email.trim(), password))
            if (r.isSuccessful && r.body()?.accessToken != null) Result.Success(r.body()!!)
            else Result.Error(parseError(r.errorBody()?.string()) ?: "Login failed.")
        } catch (e: Exception) {
            Result.Error("Network error: ${e.message}")
        }
    }

    /**
     * Start password reset via email OTP.
     * To ensure Supabase sends a 6-digit OTP, your recovery email template should include {{ .Token }} (not {{ .ConfirmationURL }}).
     */
    suspend fun requestRecoveryOtp(email: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val r = SupabaseClient.auth.recover(SupabaseRecoverRequest(email.trim()))
            if (r.isSuccessful) Result.Success("OTP sent. Please check your email.")
            else Result.Error(parseError(r.errorBody()?.string()))
        } catch (e: Exception) {
            Result.Error("Network error: ${e.message}")
        }
    }

    suspend fun verifyRecoveryOtpAndSetPassword(email: String, otp: String, newPassword: String): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                val verify = SupabaseClient.auth.verifyOtp(
                    SupabaseVerifyOtpRequest(
                        type = "recovery",
                        email = email.trim(),
                        token = otp.trim()
                    )
                )
                if (!verify.isSuccessful || verify.body()?.accessToken.isNullOrBlank()) {
                    return@withContext Result.Error(parseError(verify.errorBody()?.string()) ?: "OTP verification failed.")
                }

                val accessToken = verify.body()!!.accessToken!!
                val upd = SupabaseClient.auth.updateUser(
                    bearer = "Bearer $accessToken",
                    body = SupabaseUpdateUserRequest(password = newPassword)
                )
                if (upd.isSuccessful) Result.Success("Password updated successfully. Please login.")
                else Result.Error(parseError(upd.errorBody()?.string()) ?: "Failed to update password.")
            } catch (e: Exception) {
                Result.Error("Network error: ${e.message}")
            }
        }

    /**
     * Minimal Phase 1: ensure a row exists in public.users.
     * Requires the user's JWT (accessToken) because RLS is enabled.
     */
    suspend fun upsertUserRow(
        accessToken: String,
        authUserId: String,
        email: String,
        fullName: String,
        role: String = "customer",
        phone: String = ""
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val payload = listOf(
                UserRowUpsert(
                    authUserId = authUserId,
                    name = fullName,
                    email = email,
                    phone = phone,
                    role = role
                )
            )
            SupabaseClient.rest.upsertUser(
                bearer = "Bearer $accessToken",
                body = payload
            )
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to save user profile")
        }
    }

    private fun parseError(raw: String?): String {
        if (raw.isNullOrBlank()) return "Request failed."
        val m = Regex("\"message\"\\s*:\\s*\"([^\"]+)\"").find(raw)?.groupValues?.get(1)
        val e = Regex("\"error_description\"\\s*:\\s*\"([^\"]+)\"").find(raw)?.groupValues?.get(1)
        val msg = m ?: e ?: raw
        return msg.take(250)
    }
}
