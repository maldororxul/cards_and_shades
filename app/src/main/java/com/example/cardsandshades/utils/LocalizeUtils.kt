package com.example.cardsandshades.utils

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

fun changeLocale(context: Context, langCode: String) {
    val currentLocales = AppCompatDelegate.getApplicationLocales()
    if (!currentLocales.isEmpty && currentLocales[0]?.language == langCode) return

    val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(langCode)
    AppCompatDelegate.setApplicationLocales(appLocale)
}

fun getStringResourceByName(context: Context, name: String?): String {
    if (name == null) return ""
    val resId = context.resources.getIdentifier(name, "string", context.packageName)
    return if (resId != 0) {
        context.getString(resId)
    } else {
        name
    }
}
