package com.example.cardsandshades.catalog

import android.content.Context
import com.example.cardsandshades.model.MissionModel
import com.example.cardsandshades.model.PlaytimeRewardModel
import com.example.cardsandshades.model.RewardSetModel
import org.yaml.snakeyaml.Yaml

object MissionCatalog {
    var dailyMissions: List<MissionModel> = emptyList()
    var weeklyMissions: List<MissionModel> = emptyList()
    var playtimeRewards: List<PlaytimeRewardModel> = emptyList()

    fun init(context: Context) {
        val yaml = Yaml()
        val inputStream = context.assets.open("missions.yaml")
        val data: Map<String, Any> = yaml.load(inputStream)

        dailyMissions = (data["daily_missions"] as? List<Map<String, Any>>)?.map { parseMission(it) } ?: emptyList()
        weeklyMissions = (data["weekly_missions"] as? List<Map<String, Any>>)?.map { parseMission(it) } ?: emptyList()
        playtimeRewards = (data["playtime_rewards"] as? List<Map<String, Any>>)?.map { 
            PlaytimeRewardModel(
                minutes = it["minutes"] as Int,
                reward = parseReward(it["reward"] as Map<String, Any>)
            )
        } ?: emptyList()
    }

    private fun parseMission(map: Map<String, Any>): MissionModel {
        return MissionModel(
            id = map["id"] as String,
            nameKey = map["name_key"] as String,
            goal = map["goal"] as Int,
            reward = parseReward(map["reward"] as Map<String, Any>)
        )
    }

    private fun parseReward(map: Map<String, Any>): RewardSetModel {
        return RewardSetModel(
            gold = map["gold"] as? Int ?: 0,
            crystals = map["crystals"] as? Int ?: 0,
            dustCommon = map["dust_common"] as? Int ?: 0,
            dustUncommon = map["dust_uncommon"] as? Int ?: 0,
            dustRare = map["dust_rare"] as? Int ?: 0,
            dustEpic = map["dust_epic"] as? Int ?: 0,
            dustLegendary = map["dust_legendary"] as? Int ?: 0,
            dustMythic = map["dust_mythic"] as? Int ?: 0,
            // Hammers will be added to RewardSetModel in the next step
            hammerCommon = map["hammer_common"] as? Int ?: 0,
            hammerUncommon = map["hammer_uncommon"] as? Int ?: 0,
            hammerRare = map["hammer_rare"] as? Int ?: 0,
            hammerEpic = map["hammer_epic"] as? Int ?: 0,
            hammerLegendary = map["hammer_legendary"] as? Int ?: 0,
            hammerMythic = map["hammer_mythic"] as? Int ?: 0
        )
    }
}
