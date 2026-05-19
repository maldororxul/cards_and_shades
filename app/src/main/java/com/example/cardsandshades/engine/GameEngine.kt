package com.example.cardsandshades.engine

import com.example.cardsandshades.model.CardModel
import com.example.cardsandshades.model.GameState
import com.example.cardsandshades.model.PlayerModel
import com.example.cardsandshades.model.Turn

object GameEngine {

    private const val MAX_BOARD_SIZE = 5
    private const val MAX_MANA_CAP = 10

    // 1. Старт нового хода: увеличиваем ману, добираем карту
    fun startTurn(state: GameState) {
        if (state.isGameOver) return
        val activePlayer = if (state.currentTurn == Turn.PLAYER) state.player else state.opponent

        if (activePlayer.maxMana < MAX_MANA_CAP) activePlayer.maxMana += 1
        activePlayer.currentMana = activePlayer.maxMana

        // ООП-сброс состояний сна и атак для всех существ на столе в начале их хода
        activePlayer.board.forEach { it.resetTurnState() }

        drawCard(activePlayer)
    }

    // 2. Добор карты из колоды в руку
    fun drawCard(player: PlayerModel) {
        if (player.deck.isNotEmpty()) {
            val card = player.deck.removeAt(0)
            player.hand.add(card)
        } else {
            // Если колода пуста — игрок получает урон от «усталости»
            player.currentHp -= 2
        }
    }

    // 3. Разыгрывание карты из руки на стол
    fun playCard(state: GameState, card: CardModel): Boolean {
        val activePlayer = if (state.currentTurn == Turn.PLAYER) state.player else state.opponent
        if (activePlayer.currentMana >= card.manaCost && activePlayer.board.size < MAX_BOARD_SIZE) {
            activePlayer.currentMana -= card.manaCost
            activePlayer.hand.remove(card)

            // ТРИГГЕР: Активируем эффекты при призыве (например, Рывок сразу разбудит карту)
            card.activeEffects.forEach { it.onSummon(card) }

            activePlayer.board.add(card)
            return true
        }
        return false
    }

    // Валидация атаки карты на карту врага
    fun canAttackTarget(state: GameState, attacker: CardModel, target: CardModel?): Boolean {
        // ОГРАНИЧЕНИЕ 1: Спящая карта (болезнь призыва) атаковать не может
        if (attacker.isSleeping) return false

        // ОГРАНИЧЕНИЕ 2: Карта уже атаковала в этот ход
        if (attacker.hasAttackedThisTurn) return false

        val enemyPlayer = if (state.currentTurn == Turn.PLAYER) state.opponent else state.player

        // ОГРАНИЧЕНИЕ 3: Правило Провокации (Танка)
        val hasTauntOnBoard = enemyPlayer.board.any { it.hasTaunt }
        if (hasTauntOnBoard && (target == null || !target.hasTaunt)) {
            return false
        }

        return true
    }

    // Вспомогательный метод для проверки атаки в лицо (чтобы использовать в UI и ИИ)
    fun canAttackHero(state: GameState, attacker: CardModel): Boolean {
        // Лицо — это частный случай атаки, когда цель равна null
        return canAttackTarget(state, attacker, null)
    }

    fun calculateCombat(state: GameState, attacker: CardModel, target: CardModel) {
        attacker.hasAttackedThisTurn = true

        // Базовый расчет урона
        var damageToTarget = attacker.currentAttack
        var counterDamageToAttacker = target.currentAttack

        // Применяем модификаторы эффектов
        target.activeEffects.forEach { damageToTarget = it.modifyIncomingDamage(target, damageToTarget) }
        attacker.activeEffects.forEach { counterDamageToAttacker = it.modifyCounterDamage(attacker, target, counterDamageToAttacker) }

        // Нанесение урона существам
        target.currentHealth -= damageToTarget
        attacker.currentHealth -= counterDamageToAttacker

        target.lastDamageTaken = damageToTarget
        attacker.lastDamageTaken = counterDamageToAttacker

        // Пост-эффекты атаки (например, Маг бьет по соседям)
        attacker.activeEffects.forEach { it.onAfterAttack(state, attacker, target) }
    }

    // 4. Бой: Атака карты на карту противника (Взаимный урон)
    fun attackCard(state: GameState, attacker: CardModel, target: CardModel) {
        val activePlayer = if (state.currentTurn == Turn.PLAYER) state.player else state.opponent
        val enemyPlayer = if (state.currentTurn == Turn.PLAYER) state.opponent else state.player

        if (!activePlayer.board.contains(attacker) || !enemyPlayer.board.contains(target)) return

        // Одновременный обмен уроном
        target.currentHealth -= attacker.currentAttack
        attacker.currentHealth -= target.currentAttack

        // Удаление погибших карт со стола
        if (target.isDead) enemyPlayer.board.remove(target)
        if (attacker.isDead) activePlayer.board.remove(attacker)

        checkWinCondition(state)
    }

    // 5. Атака напрямую в «лицо» героя, если у врага пустой стол
    fun attackHero(state: GameState, attacker: CardModel): Boolean {
        val activePlayer = if (state.currentTurn == Turn.PLAYER) state.player else state.opponent
        val enemyPlayer = if (state.currentTurn == Turn.PLAYER) state.opponent else state.player

        // Атаковать лицо можно только если у противника нет защитников на столе
        if (enemyPlayer.board.isNotEmpty()) return false

        enemyPlayer.currentHp -= attacker.currentAttack
        checkWinCondition(state)
        return true
    }

    // 6. Передача хода
    fun endTurn(state: GameState) {
        state.currentTurn = if (state.currentTurn == Turn.PLAYER) Turn.OPPONENT else Turn.PLAYER
        if (state.currentTurn == Turn.PLAYER) {
            state.turnNumber += 1
        }
        startTurn(state)
    }

    // 7. Проверка условий завершения игры
    private fun checkWinCondition(state: GameState) {
        if (state.player.isDead && state.opponent.isDead) {
            state.isGameOver = true
            state.winnerName = "Ничья"
        } else if (state.opponent.isDead) {
            state.isGameOver = true
            state.winnerName = state.player.name
        } else if (state.player.isDead) {
            state.isGameOver = true
            state.winnerName = state.opponent.name
        }
    }
}