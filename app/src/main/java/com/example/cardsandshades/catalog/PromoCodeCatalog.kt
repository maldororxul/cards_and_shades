package com.example.cardsandshades.catalog

import android.content.Context
import com.example.cardsandshades.model.UserProfile
import org.yaml.snakeyaml.Yaml

object PromoCodeCatalog {
    
    fun applyCode(context: Context, code: String): String {
        val cleanCode = code.lowercase().trim()
        
        return when (cleanCode) {
            "gold" -> {
                UserProfile.gold.value += 1000
                UserProfile.crystals.value += 500
                UserProfile.save()
                "✅ 1000 Gold and 500 Crystals added!"
            }
            "admin" -> {
                val allCards = CardCatalog.templates.mapNotNull { CardCatalog.createCardInstance(it.name) }
                UserProfile.collection.clear()
                UserProfile.collection.addAll(allCards)
                UserProfile.save()
                "✅ All cards added to collection!"
            }
            "reset" -> {
                // To reset, we can clear preferences and re-init
                val prefs = context.getSharedPreferences("cards_and_shades_prefs", Context.MODE_PRIVATE)
                prefs.edit().clear().apply()
                UserProfile.initDatabase(context)
                "✅ Profile reset to starter values!"
            }
            else -> "❌ Invalid promo code!"
        }
    }
}
