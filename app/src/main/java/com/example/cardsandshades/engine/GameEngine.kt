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

        // ОБРАБОТКА БАФФОВ: Снижаем длительность и снимаем просроченные
        activePlayer.board.filterNotNull().forEach { card ->
            // Применяем эффекты Кровотечения, Яда, Горения
            card.buffs.forEach { buff ->
                when (buff.tag) {
                    com.example.cardsandshades.model.EffectTag.BLEED -> {
                        card.currentHealth -= 1
                        card.lastDamageTaken = 1
                        card.isTakingDamage = true
                    }
                    com.example.cardsandshades.model.EffectTag.POISON -> {
                        card.currentHealth -= 2
                        card.lastDamageTaken = 2
                        card.isTakingDamage = true
                    }
                    com.example.cardsandshades.model.EffectTag.BURN -> {
                        card.currentHealth -= 3
                        card.lastDamageTaken = 3
                        card.isTakingDamage = true
                    }
                    else -> {}
                }
            }

            val expired = card.buffs.filter { it.duration <= 0 }
            expired.forEach { buff ->
                card.currentAttack -= buff.attackBonus
                card.currentHealth -= buff.healthBonus
                if (card.currentHealth <= 0) card.currentHealth = 1 // Защита от смерти при снятии баффа
            }
            card.removeBuffs(expired)
            card.buffs.forEach { it.duration -= 1 }

            card.resetTurnState()
        }

        checkWinCondition(state)
        drawCard(activePlayer, state)
    }

    // 2. Добор карты из колоды в руку
    fun drawCard(player: PlayerModel, state: GameState) {
        if (player.deck.isNotEmpty()) {
            val card = player.deck.removeAt(0)
            player.hand.add(card)
        } else {
            // Если колода пуста — игрок получает урон от «усталости»
            player.currentHp -= 2
            checkWinCondition(state)
        }
    }

    // 3. Разыгрывание карты из руки на стол в конкретный слот
    fun playCard(state: GameState, card: CardModel, slotIndex: Int): Boolean {
        val activePlayer = if (state.currentTurn == Turn.PLAYER) state.player else state.opponent
        if (slotIndex !in 0 until MAX_BOARD_SIZE) return false
        
        if (activePlayer.currentMana >= card.manaCost && activePlayer.board[slotIndex] == null) {
            val wasRemoved = activePlayer.hand.removeIf { it.id == card.id }
            
            if (wasRemoved) {
                activePlayer.currentMana -= card.manaCost

                // ТРИГГЕР: Активируем эффекты при призыве
                card.activeEffects.forEach { it.onSummon(state, activePlayer, card) }

                activePlayer.board[slotIndex] = card
                checkAutoWinCondition(state)
                return true
            }
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
        val hasTauntOnBoard = enemyPlayer.board.any { it?.hasTaunt == true }
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
        
        // КРИТИЧЕСКОЕ ИСПРАВЛЕНИЕ: Эффекты АТАКУЮЩЕГО должны модифицировать ответный урон (например, Ranged)
        attacker.activeEffects.forEach { counterDamageToAttacker = it.modifyCounterDamage(attacker, target, counterDamageToAttacker) }

        // Нанесение урона существам
        target.currentHealth -= damageToTarget
        attacker.currentHealth -= counterDamageToAttacker

        target.lastDamageTaken = damageToTarget
        attacker.lastDamageTaken = counterDamageToAttacker

        // Триггеры нанесения урона (например, Вампиризм)
        attacker.activeEffects.forEach { it.onDamageDealt(state, attacker, damageToTarget) }
        target.activeEffects.forEach { it.onDamageDealt(state, target, counterDamageToAttacker) }

        // Пост-эффекты атаки (например, Маг бьет по соседям)
        attacker.activeEffects.forEach { it.onAfterAttack(state, attacker, target) }

        checkWinCondition(state)
        checkAutoWinCondition(state)
    }

    // Логика атаки героя, вынесенная в движок для учета эффектов
    fun attackHero(state: GameState, attacker: CardModel) {
        attacker.hasAttackedThisTurn = true
        val enemy = if (state.currentTurn == Turn.PLAYER) state.opponent else state.player
        enemy.currentHp -= attacker.currentAttack

        // Триггеры нанесения урона (Вампиризм)
        attacker.activeEffects.forEach { it.onDamageDealt(state, attacker, attacker.currentAttack) }

        checkWinCondition(state)
        checkAutoWinCondition(state)
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
            state.winnerName = "draw"
        } else if (state.opponent.isDead) {
            state.isGameOver = true
            state.winnerName = state.player.name
        } else if (state.player.isDead) {
            state.isGameOver = true
            state.winnerName = state.opponent.name
        }
    }

    // 8. Авто-победа/поражение, если у одной стороны нет шансов
    fun checkAutoWinCondition(state: GameState) {
        if (state.isGameOver) return

        val playerHasNoOptions = state.player.hand.isEmpty() && 
                state.player.board.all { it == null } && 
                state.player.deck.isEmpty()
        
        if (playerHasNoOptions) {
            val opponentLethal = state.opponent.board.filterNotNull().sumOf { it.currentAttack }
            if (opponentLethal >= state.player.currentHp) {
                state.isGameOver = true
                state.winnerName = state.opponent.name
            }
        }

        val opponentHasNoOptions = state.opponent.hand.isEmpty() && 
                state.opponent.board.all { it == null } && 
                state.opponent.deck.isEmpty()
                
        if (opponentHasNoOptions) {
            val playerLethal = state.player.board.filterNotNull().sumOf { it.currentAttack }
            if (playerLethal >= state.opponent.currentHp) {
                state.isGameOver = true
                state.winnerName = state.player.name
            }
        }
    }
}
