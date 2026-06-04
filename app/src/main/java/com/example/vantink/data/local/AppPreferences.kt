package com.example.vantink.data.local

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object AppPreferences {
    private const val PREFS_NAME = "vantink_prefs"
    private const val KEY_LANG = "preferred_lang"

    private val _preferredLanguage = MutableStateFlow("es")
    val preferredLanguage: StateFlow<String> = _preferredLanguage

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        _preferredLanguage.value = prefs.getString(KEY_LANG, "es") ?: "es"
    }

    fun setLanguage(context: Context, lang: String) {
        _preferredLanguage.value = lang
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LANG, lang)
            .apply()
    }
}
