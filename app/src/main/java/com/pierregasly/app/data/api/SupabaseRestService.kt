package com.pierregasly.app.data.api

import com.pierregasly.app.data.model.supabase.UserRowUpsert
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PATCH
import retrofit2.http.Query

interface SupabaseRestService {

    @POST("rest/v1/users?on_conflict=auth_user_id")
    suspend fun upsertUser(
        @Header("Authorization") bearer: String,
        @Header("Prefer") prefer: String = "resolution=merge-duplicates,return=representation",
        @Body body: List<UserRowUpsert>
    ): Response<List<UserRowUpsert>>

    @GET("rest/v1/users")
    suspend fun getUserByAuthId(
        @Header("Authorization") bearer: String,
        @Query("auth_user_id") authFilter: String, // e.g. "eq.<uuid>"
        @Query("select") select: String = "*"
    ): Response<List<UserRowUpsert>>

    @retrofit2.http.PATCH("rest/v1/users")
    suspend fun linkAuthIdByEmail(
        @Header("Authorization") bearer: String,
        @Header("Prefer") prefer: String = "return=representation",
        @Query("email") emailFilter: String, // e.g. "eq.user@gmail.com"
        @Body body: Map<String, Any?>
    ): Response<List<UserRowUpsert>>
}
