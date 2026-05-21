package com.example.cardsandshades.catalog

import android.content.Context
import org.yaml.snakeyaml.Yaml

object BackgroundCatalog {
    private var _backgrounds: Map<String, String> = emptyMap()

    @Suppress("UNCHECKED_CAST")
    fun init(context: Context) {
        try {
            val yaml = Yaml()
            val inputStream = context.assets.open("backgrounds.yaml")
            val data: Map<String, Any> = yaml.load(inputStream)
            val bgMap = data["backgrounds"] as? Map<String, Any>
            
            _backgrounds = bgMap?.mapValues { it.value.toString() } ?: emptyMap()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getBackgroundForScreen(screenId: String): String? {
        return _backgrounds[screenId]
    }
}
