package com.example.searchphoto.common

import android.content.Context
import android.content.SharedPreferences

class MySharedPreferences(context: Context) {
    val PREFS_FILENAME: String = "prefs"
    val PREFS_DEVICEID: String = "deviceId"
    val PREFS_TOKEN: String = "token"

    val prefs: SharedPreferences = context.getSharedPreferences(PREFS_FILENAME, 0)

    var deviceId: String?
        get() = prefs.getString(PREFS_DEVICEID, "")
        set(value) = prefs.edit().putString(PREFS_DEVICEID, value).apply()

    var token: String?
        get() = prefs.getString(PREFS_TOKEN, "")
        set(value) = prefs.edit().putString(PREFS_TOKEN, value).apply()
}