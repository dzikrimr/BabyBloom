package com.example.bubtrack.utill

import android.content.SharedPreferences
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrefHelper @Inject constructor(
    private val sharedPreferences: SharedPreferences
) {
    fun saveString(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }

    fun getString(key: String, default: String): String {
        return sharedPreferences.getString(key, default) ?: default
    }

    fun saveBoolean(key: String, value: Boolean) {
        sharedPreferences.edit().putBoolean(key, value).apply()
    }

    fun getBoolean(key: String, default: Boolean): Boolean {
        return sharedPreferences.getBoolean(key, default)
    }

    fun clear() {
        sharedPreferences.edit().clear().apply()
    }

    fun getUserId(): String {
        return getString("user_id", generateNewUserId())
    }

    private fun generateNewUserId(): String {
        val existingId = getString("user_id", "")
        return if (existingId.isEmpty()) {
            val newId = UUID.randomUUID().toString()
            saveString("user_id", newId)
            newId
        } else {
            existingId
        }
    }
}