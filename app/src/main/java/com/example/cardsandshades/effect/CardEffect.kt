package com.example.cardsandshades.effect

import com.example.cardsandshades.model.CardModel
import com.example.cardsandshades.model.GameState

interface CardEffect {
    val name: String
    val description: String

    // Срабатывает в момент призыва карты на стол
    fun onSummon(state: GameState, owner: com.example.cardsandshades.model.PlayerModel, card: CardModel) {}

    // Срабатывает в начале хода владельца
    fun onStartTurn(state: GameState, owner: com.example.cardsandshades.model.PlayerModel, card: CardModel) {}

    // Модифицирует входящий урон по этой карте (например, щиты)
    fun modifyIncomingDamage(card: CardModel, amount: Int): Int = amount

    // Модифицирует исходящий урон от этой карты (например, криты)
    fun modifyOutgoingDamage(attacker: CardModel, target: CardModel, amount: Int): Int = amount

    // Модифицирует ответный урон, который наносит цель (для Стрелков)
    fun modifyCounterDamage(attacker: CardModel, target: CardModel, originalCounterDamage: Int): Int = originalCounterDamage

    // Срабатывает ПОСЛЕ того, как карта нанесла урон (для Магов/АОЕ/Вампиризма)
    fun onAfterAttack(state: GameState, attacker: CardModel, target: CardModel) {}

    // Срабатывает при нанесении любого урона (для Вампиризма)
    fun onDamageDealt(state: GameState, attacker: CardModel, amount: Int) {}
}
