package com.example.cardsandshades.catalog

import android.content.Context
import org.yaml.snakeyaml.Yaml

data class RewardModel(
    val day: Int,
    val type: String,
    val amount: Int
)

object RewardsCatalog {
    private var _rewards: List<RewardModel> = emptyList()
    val rewards: List<RewardModel> get() = _rewards

    fun init(context: Context) {
        val yaml = Yaml()
        val inputStream = context.assets.open("rewards.yaml")
        val data: Map<String, Any> = yaml.load(inputStream)
        val rewardsList = data["rewards"] as List<Map<String, Any>>

        _rewards = rewardsList.map { rewardMap ->
            RewardModel(
                day = rewardMap["day"] as Int,
                type = rewardMap["type"] as String,
                amount = rewardMap["amount"] as Int
            )
        }
    }
}
