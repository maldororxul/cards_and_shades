package com.example.cardsandshades.catalog

import android.content.Context
import com.example.cardsandshades.model.ChapterModel
import com.example.cardsandshades.model.LevelModel
import org.yaml.snakeyaml.Yaml

object CampaignCatalog {
    private var _chapters: List<ChapterModel> = emptyList()
    val chapters: List<ChapterModel> get() = _chapters

    fun init(context: Context) {
        val yaml = Yaml()
        val inputStream = context.assets.open("campaign.yaml")
        val data: Map<String, Any> = yaml.load(inputStream)
        val chaptersList = data["chapters"] as List<Map<String, Any>>

        _chapters = chaptersList.map { chapterMap ->
            val levelsList = chapterMap["levels"] as List<Map<String, Any>>
            ChapterModel(
                id = chapterMap["id"] as Int,
                name = chapterMap["name"] as String,
                levels = levelsList.map { levelMap ->
                    LevelModel(
                        id = levelMap["id"] as Int,
                        name = levelMap["name"] as String,
                        opponentName = levelMap["opponentName"] as String,
                        opponentMaxHp = levelMap["opponentMaxHp"] as Int,
                        opponentStartMana = levelMap["opponentStartMana"] as Int,
                        difficultyDescription = levelMap["difficultyDescription"] as String,
                        opponentDeckPreset = levelMap["opponentDeckPreset"] as List<String>,
                        rewardGold = levelMap["rewardGold"] as Int,
                        rewardCrystals = levelMap["rewardCrystals"] as? Int ?: 0,
                        rewardDustCommon = levelMap["rewardDustCommon"] as? Int ?: 0,
                        rewardDustRare = levelMap["rewardDustRare"] as? Int ?: 0,
                        rewardDustEpic = levelMap["rewardDustEpic"] as? Int ?: 0,
                        rewardDustLegendary = levelMap["rewardDustLegendary"] as? Int ?: 0,
                        rewardCardName = levelMap["rewardCardName"] as? String
                    )
                }
            )
        }
    }
}
