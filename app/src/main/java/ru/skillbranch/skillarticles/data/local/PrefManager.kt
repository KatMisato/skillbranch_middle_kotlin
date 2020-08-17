package ru.skillbranch.skillarticles.data.local

import android.content.Context
import android.content.SharedPreferences

class PrefManager(context: Context) {
    val preferences: SharedPreferences = context.getSharedPreferences("APP_PREFERENCES", Context.MODE_PRIVATE)

    fun clearAll() {
        preferences.edit().clear().apply()
    }
}