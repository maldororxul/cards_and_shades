package com.example.cardsandshades.catalog

import android.content.Context
import org.yaml.snakeyaml.Yaml

data class RewardModel(
    val day: Int,
    val type: String,
    val amount: Int = 0,
    val cardKey: String? = null
)

data class RewardBlock(
    val id: Int,
    val imageRes: String,
    val rewards: List<RewardModel>
)

object RewardsCatalog {
    private var _blocks: List<RewardBlock> = emptyList()
    val blocks: List<RewardBlock> get() = _blocks
    
    val allRewards: List<RewardModel> get() = _blocks.flatMap { it.rewards }

    @Suppress("UNCHECKED_CAST")
    fun init(context: Context) {
        try {
            val yaml = Yaml()
            val inputStream = context.assets.open("rewards.yaml")
            val data: Map<String, Any> = yaml.load(inputStream)
            val blocksList = data["reward_blocks"] as List<Map<String, Any>>

            _blocks = blocksList.map { blockMap ->
                val rewardsList = blockMap["rewards"] as List<Map<String, Any>>
                RewardBlock(
                    id = blockMap["id"] as Int,
                    imageRes = blockMap["image_res"] as String,
                    rewards = rewardsList.map { rewardMap ->
                        RewardModel(
                            day = rewardMap["day"] as Int,
                            type = rewardMap["type"] as String,
                            amount = rewardMap["amount"] as? Int ?: 0,
                            cardKey = rewardMap["card_key"] as? String
                        )
                    }
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
