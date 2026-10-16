package com.example.zootr_buddy.helpers

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

object PreferencesHelper {
    var sharedPreferences: SharedPreferences? = null

    fun init(context: Context) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    }

    inline fun <reified T> getPref(key: String): T? {
        return when (T::class) {
            String::class -> sharedPreferences?.getString(key, "")
            Int::class -> sharedPreferences?.getInt(key, 0)
            Long::class -> sharedPreferences?.getLong(key, 0)
            Float::class -> sharedPreferences?.getFloat(key, 0.0F)
            Boolean::class -> sharedPreferences?.getBoolean(key, false)
            else -> null
        } as T?
    }

    inline fun <reified T> setPref(key: String, value: T): Boolean {
        if (sharedPreferences == null) {
            throw Exception("sharedPreferences has not been initialized")
        }

        sharedPreferences!!.edit().apply {
            when (T::class) {
                String::class -> putString(key, value as String?)
                Int::class -> putInt(key, value as Int)
                Long::class -> putLong(key, value as Long)
                Float::class -> putFloat(key, value as Float)
                Boolean::class -> putBoolean(key, value as Boolean)
                else -> throw Exception("unsupported type")
            }
            commit()
        }

        return getPref<T>(key) != null
    }
}