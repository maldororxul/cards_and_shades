package com.example.cardsandshades.catalog

import android.content.Context
import org.yaml.snakeyaml.Yaml

object BackgroundCatalog {
    private var _backgrounds: Map<String, String> = emptyMap()

    fun init(context: Context) {
        val yaml = Yaml()
        val inputStream = context.assets.open("backgrounds.yaml")
        val data: Map<String, Any> = yaml.load(inputStream)
        _backgrounds = data["backgrounds"] as Map<String, String>
    }

    fun getBackgroundForScreen(screenId: String): String? {
        return _backgrounds[screenId]
    }
}
