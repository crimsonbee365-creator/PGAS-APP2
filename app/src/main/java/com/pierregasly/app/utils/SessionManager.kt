package com.pierregasly.app.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("PierreGaslyPrefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_ACCESS_TOKEN   = "sb_access_token"
        private const val KEY_AUTH_USER_ID   = "sb_auth_user_id"
        private const val KEY_USER_NAME      = "user_name"
        private const val KEY_USER_EMAIL     = "user_email"
        private const val KEY_USER_PHONE     = "user_phone"
        private const val KEY_USER_ROLE      = "user_role"
        private const val KEY_IS_LOGGED_IN   = "is_logged_in"
        private const val KEY_THEME_MODE     = "theme_mode"
    }

    fun saveSession(
        accessToken: String,
        authUserId: String,
        name: String,
        email: String,
        phone: String?,
        role: String
    ) {
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_AUTH_USER_ID, authUserId)
            .putString(KEY_USER_NAME, name)
            .putString(KEY_USER_EMAIL, email)
            .putString(KEY_USER_PHONE, phone ?: "")
            .putString(KEY_USER_ROLE, role)
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .apply()
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)

    fun getAccessToken(): String? = prefs.getString(KEY_ACCESS_TOKEN, null)
    fun getAuthUserId(): String? = prefs.getString(KEY_AUTH_USER_ID, null)
    fun getUserName(): String = prefs.getString(KEY_USER_NAME, "") ?: ""
    fun getUserEmail(): String = prefs.getString(KEY_USER_EMAIL, "") ?: ""
    fun getUserPhone(): String = prefs.getString(KEY_USER_PHONE, "") ?: ""
    fun getUserRole(): String = prefs.getString(KEY_USER_ROLE, "") ?: ""

    fun setThemeMode(mode: String) {
        prefs.edit().putString(KEY_THEME_MODE, mode).apply()
    }

    fun getThemeMode(): String = prefs.getString(KEY_THEME_MODE, "system") ?: "system"
}
