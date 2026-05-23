package com.example.cardsandshades.catalog

import android.content.Context
import com.example.cardsandshades.model.RewardSetModel
import org.yaml.snakeyaml.Yaml

enum class AchievementType { COLLECTION_SIZE, EPIC_COLLECTION, CAMPAIGN_LEVEL, FORGE_COUNT }

data class AchievementTier(val goal: Int, val reward: RewardSetModel)
data class AchievementGroup(val id: String, val nameKey: String, val descKey: String, val type: AchievementType, val tiers: List<AchievementTier>)

object AchievementCatalog {
    private var _groups: List<AchievementGroup> = emptyList()
    val groups: List<AchievementGroup> get() = _groups

    @Suppress("UNCHECKED_CAST")
    fun init(context: Context) {
        try {
            val yaml = Yaml()
            val inputStream = context.assets.open("achievements.yaml")
            val data: Map<String, Any> = yaml.load(inputStream)
            val groupsList = data["achievement_groups"] as List<Map<String, Any>>

            _groups = groupsList.map { map ->
                val tiersList = map["tiers"] as List<Map<String, Any>>
                AchievementGroup(
                    id = map["id"] as String,
                    nameKey = map["name"] as String,
                    descKey = map["desc_key"] as String,
                    type = AchievementType.valueOf(map["type"] as String),
                    tiers = tiersList.map { tierMap ->
                        AchievementTier(
                            goal = tierMap["goal"] as Int,
                            reward = RewardSetModel(
                                gold = tierMap["reward_gold"] as? Int ?: 0,
                                crystals = tierMap["reward_crystals"] as? Int ?: 0,
                                dustCommon = tierMap["reward_dust_common"] as? Int ?: 0,
                                dustRare = tierMap["reward_dust_rare"] as? Int ?: 0,
                                dustEpic = tierMap["reward_dust_epic"] as? Int ?: 0,
                                dustLegendary = tierMap["reward_dust_legendary"] as? Int ?: 0,
                                dustMythic = tierMap["reward_dust_mythic"] as? Int ?: 0
                            )
                        )
                    }
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
