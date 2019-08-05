package com.minim.messenger.util

import android.content.Context

object SharedPrefHelper {

    private const val SHARED_PREF = "SECRETS"

    fun addString(context: Context, key: String, value: String) {
        val sharedPref = context.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString(key, value)
            apply()
        }
    }

    fun getString(context: Context, key: String): String {
        val sharedPref = context.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE)
        return sharedPref.getString(key, "").toString()
    }

}