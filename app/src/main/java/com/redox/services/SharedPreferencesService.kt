package com.ts.tech.maa.main.services

import android.content.Context

class SharedPreferencesService(context: Context) {
    val PREFS_FILENAME = "com.ts.tech.jugmug.prefs"
    val prefs = context.getSharedPreferences(PREFS_FILENAME, 0)
    val editor = prefs!!.edit()

    fun getString(key: String, defaultValue: String): String{
        return prefs.getString(key, defaultValue)
    }

    fun setString(key: String, value: String?){
        editor.putString(key, value)
        editor.apply()
    }
}