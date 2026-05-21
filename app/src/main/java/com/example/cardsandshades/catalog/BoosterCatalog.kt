package com.example.cardsandshades.catalog

import android.content.Context
import com.example.cardsandshades.model.BoosterChances
import com.example.cardsandshades.model.BoosterModel
import org.yaml.snakeyaml.Yaml

object BoosterCatalog {
    private var _boosters: List<BoosterModel> = emptyList()
    val boosters: List<BoosterModel> get() = _boosters

    fun init(context: Context) {
        val yaml = Yaml()
        val inputStream = context.assets.open("boosters.yaml")
        val data: Map<String, Any> = yaml.load(inputStream)
        val boosterList = data["boosters"] as List<Map<String, Any>>

        _boosters = boosterList.map { map ->
            val chancesMap = map["chances"] as Map<String, Int>
            BoosterModel(
                id = map["id"] as String,
                name = map["name"] as String,
                costAmount = map["costAmount"] as Int,
                costType = map["costType"] as String,
                description = map["description"] as String,
                chances = BoosterChances(
                    common = chancesMap["common"] ?: 0,
                    rare = chancesMap["rare"] ?: 0,
                    epic = chancesMap["epic"] ?: 0,
                    legendary = chancesMap["legendary"] ?: 0
                )
            )
        }
    }
}
