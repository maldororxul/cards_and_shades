package com.example.cardsandshades.catalog

import android.content.Context
import com.example.cardsandshades.model.LevelModel

import org.yaml.snakeyaml.Yaml

object CampaignCatalog {
    private var _levels: List<LevelModel> = emptyList()
    val levels: List<LevelModel> get() = _levels

    fun init(context: Context) {
        val yaml = Yaml()
        val inputStream = context.assets.open("campaign.yaml")
        val data: Map<String, Any> = yaml.load(inputStream)
        val levelsList = data["levels"] as List<Map<String, Any>>

        _levels = levelsList.map { levelMap ->
            LevelModel(
                id = levelMap["id"] as Int,
                name = levelMap["name"] as String,
                opponentName = levelMap["opponentName"] as String,
                opponentMaxHp = levelMap["opponentMaxHp"] as Int,
                opponentStartMana = levelMap["opponentStartMana"] as Int,
                difficultyDescription = levelMap["difficultyDescription"] as String,
                opponentDeckPreset = levelMap["opponentDeckPreset"] as List<String>,
                rewardGold = levelMap["rewardGold"] as Int,
                rewardCardName = levelMap["rewardCardName"] as? String
            )
        }
    }
}
