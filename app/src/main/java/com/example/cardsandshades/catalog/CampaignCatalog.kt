package com.example.cardsandshades.catalog

import android.content.Context
import com.example.cardsandshades.model.ChapterModel
import com.example.cardsandshades.model.LevelModel
import com.example.cardsandshades.model.RewardSetModel
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
                backgroundRes = chapterMap["backgroundRes"] as? String,
                levels = levelsList.map { levelMap ->
                    LevelModel(
                        id = levelMap["id"] as Int,
                        name = levelMap["name"] as String,
                        opponentName = levelMap["opponentName"] as String,
                        opponentMaxHp = levelMap["opponentMaxHp"] as Int,
                        opponentStartMana = levelMap["opponentStartMana"] as Int,
                        difficultyDescription = levelMap["difficultyDescription"] as String,
                        opponentDeckPreset = levelMap["opponentDeckPreset"] as List<String>,
                        firstTimeReward = parseReward(levelMap["firstTimeReward"] as? Map<String, Any>),
                        repeatReward = parseReward(levelMap["repeatReward"] as? Map<String, Any>),
                        backgroundRes = levelMap["backgroundRes"] as? String,
                        musicRes = levelMap["musicRes"] as? String
                    )
                }
            )
        }
    }

    private fun parseReward(rewardMap: Map<String, Any>?): RewardSetModel {
        if (rewardMap == null) return RewardSetModel()
        return RewardSetModel(
            gold = rewardMap["gold"] as? Int ?: 0,
            crystals = rewardMap["crystals"] as? Int ?: 0,
            dustCommon = rewardMap["dust_common"] as? Int ?: 0,
            dustRare = rewardMap["dust_rare"] as? Int ?: 0,
            dustEpic = rewardMap["dust_epic"] as? Int ?: 0,
            dustLegendary = rewardMap["dust_legendary"] as? Int ?: 0,
            cardName = rewardMap["card"] as? String
        )
    }
}
