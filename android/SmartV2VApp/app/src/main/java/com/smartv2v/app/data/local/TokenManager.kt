package com.smartv2v.app.data.local

import android.content.Context
import android.content.SharedPreferences

object TokenManager {

    private const val PREF_NAME = "smartv2v_prefs"
    private const val KEY_TOKEN = "jwt_token"
    private const val KEY_USER_NAME = "user_name"
    private const val KEY_USER_EMAIL = "user_email"
    private const val KEY_ACTIVE_VEHICLE_ID = "active_vehicle_id"
    private const val KEY_SERVER_BASE_URL = "server_base_url"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveAuth(token: String, userName: String, email: String) {
        prefs.edit()
            .putString(KEY_TOKEN, token)
            .putString(KEY_USER_NAME, userName)
            .putString(KEY_USER_EMAIL, email)
            .apply()
    }

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    fun getUserName(): String? = prefs.getString(KEY_USER_NAME, null)

    fun getUserEmail(): String? = prefs.getString(KEY_USER_EMAIL, null)

    fun isLoggedIn(): Boolean = !getToken().isNullOrEmpty()

    fun saveActiveVehicleId(vehicleId: Int) {
        prefs.edit().putInt(KEY_ACTIVE_VEHICLE_ID, vehicleId).apply()
    }

    fun getActiveVehicleId(): Int = prefs.getInt(KEY_ACTIVE_VEHICLE_ID, -1)

    fun saveServerBaseUrl(url: String) {
        val cleanUrl = if (url.endsWith("/")) url.dropLast(1) else url
        prefs.edit().putString(KEY_SERVER_BASE_URL, cleanUrl).apply()
    }

    fun getServerBaseUrl(): String {
        return prefs.getString(KEY_SERVER_BASE_URL, "http://10.0.2.2:8080") ?: "http://10.0.2.2:8080"
    }

    fun clearSession() {
        prefs.edit()
            .remove(KEY_TOKEN)
            .remove(KEY_USER_NAME)
            .remove(KEY_USER_EMAIL)
            .remove(KEY_ACTIVE_VEHICLE_ID)
            .apply()
    }
}
