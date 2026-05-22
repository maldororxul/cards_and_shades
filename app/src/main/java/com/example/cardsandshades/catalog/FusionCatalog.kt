package com.example.cardsandshades.catalog

import android.content.Context
import org.yaml.snakeyaml.Yaml

data class FusionIngredient(val key: String, val count: Int)
data class FusionRecipe(val id: String, val outputKey: String, val inputs: List<FusionIngredient>)

object FusionCatalog {
    private var _recipes: List<FusionRecipe> = emptyList()
    val recipes: List<FusionRecipe> get() = _recipes

    @Suppress("UNCHECKED_CAST")
    fun init(context: Context) {
        try {
            val yaml = Yaml()
            val inputStream = context.assets.open("fusions.yaml")
            val data: Map<String, Any> = yaml.load(inputStream)
            val fusionsList = data["fusions"] as List<Map<String, Any>>

            _recipes = fusionsList.map { map ->
                val inputsList = map["inputs"] as List<Map<String, Any>>
                FusionRecipe(
                    id = map["id"] as String,
                    outputKey = map["output_key"] as String,
                    inputs = inputsList.map { FusionIngredient(it["key"] as String, it["count"] as Int) }
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
