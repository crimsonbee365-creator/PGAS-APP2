package com.pierregasly.app.data.api

import com.pierregasly.app.data.model.supabase.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

interface SupabaseAuthService {

    @POST("auth/v1/signup")
    suspend fun signUp(@Body body: SupabaseSignUpRequest): Response<SupabaseAuthResponse>

    @POST("auth/v1/token")
    suspend fun signInWithPassword(
        @Query("grant_type") grantType: String = "password",
        @Body body: SupabasePasswordGrantRequest
    ): Response<SupabaseAuthResponse>

    /**
     * Verify OTP tokens (email signup confirmation, recovery, magiclink/otp, etc).
     * type examples: "signup", "recovery", "email"
     */
    @POST("auth/v1/verify")
    suspend fun verifyOtp(@Body body: SupabaseVerifyOtpRequest): Response<SupabaseAuthResponse>

    /**
     * Resend OTP email (signup/email_change/etc). type examples: "signup"
     */
    @POST("auth/v1/resend")
    suspend fun resend(@Body body: SupabaseResendRequest): Response<Map<String, Any>>

    /**
     * Start password recovery. If your email template uses {{ .Token }} Supabase sends a 6-digit OTP.
     */
    @POST("auth/v1/recover")
    suspend fun recover(@Body body: SupabaseRecoverRequest): Response<Map<String, Any>>

    @PUT("auth/v1/user")
    suspend fun updateUser(
        @Header("Authorization") bearer: String,
        @Body body: SupabaseUpdateUserRequest
    ): Response<SupabaseUserResponse>

    @POST("auth/v1/logout")
    suspend fun logout(@Body body: Map<String, String> = emptyMap()): Response<Map<String, Any>>
}
