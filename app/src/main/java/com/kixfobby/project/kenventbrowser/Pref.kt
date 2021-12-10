package com.kixfobby.project.kenventbrowser

import android.content.Context
import androidx.annotation.NonNull
import androidx.preference.PreferenceManager

class Pref(@NonNull var context: Context) {

    //var one: String = get("bill", "quick_response_app_product_id").toString()

    fun put(key: String, value: String?) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(key, value)
            .apply()
    }

    fun put(key: String, value: Int) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(key, value).apply()
    }

    fun put(key: String, value: Boolean) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(key, value)
            .apply()
    }

    fun put(key: String, value: Long) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putLong(key, value)
            .apply()
    }

    fun put(key: String, value: Set<String?>) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putStringSet(key, value)
            .apply()
    }

    fun get(key: String, defValue: String?): String? {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(key, defValue)
    }

    fun get(key: String, defValue: Int): Int {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(key, defValue)
    }

    fun get(key: String, defValue: Boolean): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(key, defValue)
    }

    fun get(key: String, defValue: Long): Long {
        return PreferenceManager.getDefaultSharedPreferences(context).getLong(key, defValue)
    }

    fun get(key: String, defValue: Set<String>?): Set<String>? {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getStringSet(key, defValue)
    }

    fun remove(key: String) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().remove(key).apply()
    }

    fun clear() {
        PreferenceManager.getDefaultSharedPreferences(context).edit().clear().apply()
    }

}
