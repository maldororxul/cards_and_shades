package com.example.cardsandshades.utils

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

object LocaleHelper {
    fun getLanguage(context: Context): String {
        val currentLocales = AppCompatDelegate.getApplicationLocales()
        return if (currentLocales.isEmpty) "en" else currentLocales[0]?.language ?: "en"
    }

    fun setLocale(context: Context, langCode: String) {
        val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(langCode)
        AppCompatDelegate.setApplicationLocales(appLocale)
    }
}

fun changeLocale(context: Context, langCode: String) {
    LocaleHelper.setLocale(context, langCode)
}

fun getStringResourceByName(context: Context, name: String?): String {
    if (name == null || name.isEmpty()) return ""
    val resId = context.resources.getIdentifier(name, "string", context.packageName)
    return if (resId != 0) {
        context.getString(resId)
    } else {
        // Fallback for card names: remove card_ prefix and capitalize words
        if (name.startsWith("card_")) {
            name.removePrefix("card_").split("_").joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
        } else {
            name
        }
    }
}
