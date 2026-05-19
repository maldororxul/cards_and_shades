package com.example.cardsandshades.effect

import com.example.cardsandshades.model.CardModel
import com.example.cardsandshades.model.GameState

interface CardEffect {
    val name: String
    val description: String

    // Срабатывает в момент призыва карты на стол
    fun onSummon(card: CardModel) {}

    // Модифицирует входящий урон по этой карте (например, щиты)
    fun modifyIncomingDamage(card: CardModel, amount: Int): Int = amount

    // Модифицирует ответный урон, который наносит цель (для Стрелков)
    fun modifyCounterDamage(attacker: CardModel, target: CardModel, originalCounterDamage: Int): Int = originalCounterDamage

    // Срабатывает ПОСЛЕ того, как карта нанесла урон (для Магов/АОЕ)
    fun onAfterAttack(state: GameState, attacker: CardModel, target: CardModel) {}
}
