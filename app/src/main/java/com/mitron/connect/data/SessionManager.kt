package com.mitron.connect.data

import android.content.Context
import android.content.SharedPreferences

object SessionManager {
    private const val PREFS_NAME = "MitronPrefs"
    private const val KEY_USER_ID = "USER_ID"
    
    private lateinit var prefs: SharedPreferences
    
    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    fun saveUserId(userId: String) {
        prefs.edit().putString(KEY_USER_ID, userId).apply()
    }
    
    fun getUserId(): String? {
        if (!::prefs.isInitialized) return "user_gagan" // Fallback if not initialized
        return prefs.getString(KEY_USER_ID, null)
    }
    
    fun clearSession() {
        prefs.edit().remove(KEY_USER_ID).apply()
    }
}
