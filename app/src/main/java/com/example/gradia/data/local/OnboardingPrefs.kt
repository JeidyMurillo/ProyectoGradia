package com.example.gradia.data.local

import android.content.Context
import android.content.SharedPreferences

object OnboardingPrefs {
    private const val PREFS_NAME = "onboarding"
    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isDismissed(context: Context, screen: String): Boolean =
        prefs(context).getBoolean(screen, false)

    fun dismiss(context: Context, screen: String) {
        prefs(context).edit().putBoolean(screen, true).apply()
    }
}
