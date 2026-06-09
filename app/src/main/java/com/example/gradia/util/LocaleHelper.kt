package com.example.gradia.util

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.util.Locale

object LocaleHelper {
    private const val PREFS_NAME = "gradia_prefs"
    private const val KEY_LANGUAGE = "app_language"

    fun getLanguage(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LANGUAGE, "es") ?: "es"
    }

    fun setLanguage(context: Context, languageCode: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANGUAGE, languageCode).apply()
    }

    fun wrap(context: Context): Context {
        val language = getLanguage(context)
        val locale = Locale.forLanguageTag(language)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration).apply {
            setLocale(locale)
        }

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.createConfigurationContext(config)
        } else {
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
            context
        }
    }

    fun getCurrentLanguageName(context: Context): String {
        return when (getLanguage(context)) {
            "en" -> "English"
            else -> "Español"
        }
    }

    fun getLanguageDisplayName(code: String): String {
        return when (code) {
            "en" -> "English"
            "es" -> "Español"
            else -> code
        }
    }
}
