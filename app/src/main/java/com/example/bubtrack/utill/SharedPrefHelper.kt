package com.example.bubtrack.utill

import android.content.Context
import java.util.UUID
import javax.inject.Inject
import androidx.core.content.edit

class SharedPrefHelper @Inject constructor(
    context: Context
) {

    private val sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    companion object{
        private const val PREF_NAME = "babybloom_prefs"
        private const val USER_ID_KEY = "user_id_key"
    }

    fun getUserId() : String {
        val userId = sharedPref.getString(USER_ID_KEY, null)
        return if (userId.isNullOrEmpty()){
            val newUserId = UUID.randomUUID().toString().substring(0, 6)
            sharedPref.edit { putString(USER_ID_KEY, newUserId) }
            newUserId
        } else {
            userId
        }
    }
}