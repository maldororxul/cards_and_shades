package com.example.cardsandshades.engine

import com.example.cardsandshades.model.*
import com.example.cardsandshades.model.EffectTag

object GameEngine {

    private const val MAX_BOARD_SIZE = 5
    private const val MAX_MANA_CAP = 10

    // 1. Старт нового хода
    fun startTurn(state: GameState) {
        if (state.isGameOver) return
        val activePlayer = if (state.currentTurn == Turn.PLAYER) state.player else state.opponent

        if (activePlayer.maxMana < MAX_MANA_CAP) activePlayer.maxMana += 1
        activePlayer.currentMana = activePlayer.maxMana

        // ОБРАБОТКА ХЕРОЯ
        activePlayer.buffs.forEach { buff ->
            when (buff.tag) {
                EffectTag.BLEED -> activePlayer.currentHp -= 1
                EffectTag.POISON -> activePlayer.currentHp -= 2
                EffectTag.BURN -> activePlayer.currentHp -= 3
                else -> {}
            }
        }
        val heroExpired = activePlayer.buffs.filter { it.duration <= 0 }
        activePlayer.buffs = activePlayer.buffs.filter { !heroExpired.contains(it) }
        activePlayer.buffs.forEach { it.duration -= 1 }

        // ОБРАБОТКА КАРТ НА СТОЛЕ
        activePlayer.board.filterNotNull().forEach { card ->
            card.activeEffects.forEach { it.onStartTurn(state, activePlayer, card) }

            card.buffs.forEach { buff ->
                when (buff.tag) {
                    EffectTag.BLEED -> applyPeriodicDamage(card, 1)
                    EffectTag.POISON -> if (!card.groups.contains(GroupTag.UNDEAD)) applyPeriodicDamage(card, 2)
                    EffectTag.BURN -> applyPeriodicDamage(card, 3)
                    else -> {}
                }
            }

            val expired = card.buffs.filter { it.duration <= 0 }
            expired.forEach { buff ->
                card.currentAttack -= buff.attackBonus
                card.currentHealth -= buff.healthBonus
                if (card.currentHealth <= 0) card.currentHealth = 1
            }
            card.removeBuffs(expired)
            card.buffs.forEach { it.duration -= 1 }

            if (card.isFrozen) {
                card.isFrozen = false
                card.hasAttackedThisTurn = true 
            } else if (card.isStunned) {
                card.isStunned = false
                card.hasAttackedThisTurn = true
            } else {
                card.resetTurnState()
            }
        }

        checkWinCondition(state)
        drawCard(activePlayer, state)
    }

    private fun applyPeriodicDamage(card: CardModel, amount: Int) {
        card.currentHealth -= amount
        card.lastDamageTaken = amount
        card.isTakingDamage = true
    }

    // 2. Добор карты
    fun drawCard(player: PlayerModel, state: GameState) {
        if (player.deck.isNotEmpty()) {
            val card = player.deck.removeAt(0)
            player.hand.add(card)
            
            val isOpponent = state.opponent == player
            val logType = if (isOpponent) LogType.OPPONENT else LogType.PLAYER
            val actorKey = if (isOpponent) "opponent" else "player"
            state.logHistory.add(LogEntry("battle_card_drawn|$actorKey|card_${card.name}", logType, state.turnNumber))
        }
    }

    // 3. Разыгрывание карты
    fun playCard(state: GameState, card: CardModel, slotIndex: Int): Boolean {
        val activePlayer = if (state.currentTurn == Turn.PLAYER) state.player else state.opponent
        val opponentPlayer = if (state.currentTurn == Turn.PLAYER) state.opponent else state.player
        
        if (slotIndex !in 0 until MAX_BOARD_SIZE) return false
        
        if (activePlayer.currentMana >= card.manaCost && activePlayer.board[slotIndex] == null) {
            val wasRemoved = activePlayer.hand.removeIf { it.id == card.id }
            
            if (wasRemoved) {
                activePlayer.currentMana -= card.manaCost
                card.activeEffects.forEach { it.onSummon(state, activePlayer, card) }
                applyNeighborBuffs(activePlayer, card, slotIndex)
                activePlayer.board[slotIndex] = card
                triggerOpponentAmbush(state, opponentPlayer, slotIndex, card)

                if (state.currentTurn == Turn.PLAYER) {
                    MissionManager.updateProgress("daily_play_cards", 1, false)
                }

                checkAutoWinCondition(state)
                return true
            }
        }
        return false
    }

    private fun applyNeighborBuffs(player: PlayerModel, card: CardModel, slotIndex: Int) {
        val left = if (slotIndex > 0) player.board[slotIndex - 1] else null
        val right = if (slotIndex < MAX_BOARD_SIZE - 1) player.board[slotIndex + 1] else null
        val neighbors = listOfNotNull(left, right)

        val atkBonusFromCard = if (card.activeTags.contains(EffectTag.NEIGHBOR_BUFF_ATTACK)) 1 else 0
        val hpBonusFromCard = if (card.activeTags.contains(EffectTag.NEIGHBOR_BUFF_HEALTH)) 2 else 0

        neighbors.forEach { neighbor ->
            if (atkBonusFromCard > 0 || hpBonusFromCard > 0) {
                val buff = BuffModel(java.util.UUID.randomUUID().toString(), "neighbor_buff", atkBonusFromCard, hpBonusFromCard, 999)
                neighbor.addBuff(buff)
                neighbor.currentAttack += atkBonusFromCard
                neighbor.currentHealth += hpBonusFromCard
            }
        }

        neighbors.forEach { neighbor ->
            val atkBonusFromNeighbor = if (neighbor.activeTags.contains(EffectTag.NEIGHBOR_BUFF_ATTACK)) 1 else 0
            val hpBonusFromNeighbor = if (neighbor.activeTags.contains(EffectTag.NEIGHBOR_BUFF_HEALTH)) 2 else 0

            if (atkBonusFromNeighbor > 0 || hpBonusFromNeighbor > 0) {
                val buff = BuffModel(java.util.UUID.randomUUID().toString(), "neighbor_buff", atkBonusFromNeighbor, hpBonusFromNeighbor, 999)
                card.addBuff(buff)
                card.currentAttack += atkBonusFromNeighbor
                card.currentHealth += hpBonusFromNeighbor
            }
        }
    }

    private fun triggerOpponentAmbush(state: GameState, opponent: PlayerModel, slotIndex: Int, playedCard: CardModel) {
        val indicesToCheck = listOf(slotIndex - 1, slotIndex, slotIndex + 1).filter { it in 0 until MAX_BOARD_SIZE }
        indicesToCheck.forEach { i ->
            val ambushCard = opponent.board[i]
            if (ambushCard != null && ambushCard.activeTags.contains(EffectTag.AUTO_ATTACK_PLAYED)) {
                if (!ambushCard.isFrozen && !ambushCard.isStunned && !ambushCard.isSleeping) {
                    calculateCombat(state, ambushCard, playedCard, isCounterRetaliation = true)
                }
            }
        }
    }

    fun canAttackTarget(state: GameState, attacker: CardModel, target: CardModel?): Boolean {
        if (attacker.isSleeping || attacker.hasAttackedThisTurn || attacker.isStunned || attacker.isFrozen) return false
        val enemyPlayer = if (state.currentTurn == Turn.PLAYER) state.opponent else state.player
        val hasTauntOnBoard = enemyPlayer.board.any { it?.hasTaunt == true }
        if (hasTauntOnBoard && (target == null || !target.hasTaunt)) return false
        return true
    }

    fun canAttackHero(state: GameState, attacker: CardModel): Boolean {
        return canAttackTarget(state, attacker, null)
    }

    fun calculateCombat(state: GameState, attacker: CardModel, target: CardModel, isCounterRetaliation: Boolean = false) {
        if (!isCounterRetaliation) attacker.hasAttackedThisTurn = true

        var damageToTarget = attacker.currentAttack
        attacker.activeEffects.forEach { damageToTarget = it.modifyOutgoingDamage(attacker, target, damageToTarget) }
        target.activeEffects.forEach { damageToTarget = it.modifyIncomingDamage(target, damageToTarget) }

        val isMeleeAttacker = attacker.groups.contains(GroupTag.MELEE)
        var counterDamageToAttacker = if (isMeleeAttacker) target.currentAttack else 0
        target.activeEffects.forEach { counterDamageToAttacker = it.modifyOutgoingDamage(target, attacker, counterDamageToAttacker) }
        attacker.activeEffects.forEach { counterDamageToAttacker = it.modifyCounterDamage(attacker, target, counterDamageToAttacker) }

        target.currentHealth -= damageToTarget
        attacker.currentHealth -= counterDamageToAttacker
        target.lastDamageTaken = damageToTarget
        attacker.lastDamageTaken = counterDamageToAttacker

        attacker.activeEffects.forEach { it.onDamageDealt(state, attacker, damageToTarget) }
        target.activeEffects.forEach { it.onDamageDealt(state, target, counterDamageToAttacker) }

        if (!isCounterRetaliation) triggerRetribution(state, target, attacker)
        attacker.activeEffects.forEach { it.onAfterAttack(state, attacker, target) }

        checkWinCondition(state)
        checkAutoWinCondition(state)
    }

    private fun triggerRetribution(state: GameState, attackedCard: CardModel, attacker: CardModel) {
        val owner = if (state.player.board.contains(attackedCard)) state.player else state.opponent
        val slot = owner.board.indexOf(attackedCard)
        if (slot == -1) return
        val neighbors = listOf(slot - 1, slot + 1).filter { it in 0 until MAX_BOARD_SIZE }.mapNotNull { owner.board[it] }
        neighbors.forEach { neighbor ->
            if (neighbor.activeTags.contains(EffectTag.RETRIBUTION)) {
                if (!neighbor.isFrozen && !neighbor.isStunned && !neighbor.isSleeping) {
                    calculateCombat(state, neighbor, attacker, isCounterRetaliation = true)
                }
            }
        }
    }

    fun attackHero(state: GameState, attacker: CardModel) {
        attacker.hasAttackedThisTurn = true
        val enemy = if (state.currentTurn == Turn.PLAYER) state.opponent else state.player
        var damage = attacker.currentAttack
        attacker.activeEffects.forEach { damage = it.modifyOutgoingDamage(attacker, attacker, damage) } 
        enemy.currentHp -= damage
        attacker.activeEffects.forEach { it.onDamageDealt(state, attacker, damage) }

        attacker.activeEffects.forEach { effect ->
            when (effect) {
                is com.example.cardsandshades.effect.BleedEffect -> effect.applyToHero(enemy)
                is com.example.cardsandshades.effect.PoisonEffect -> effect.applyToHero(enemy)
                is com.example.cardsandshades.effect.BurnEffect -> effect.applyToHero(enemy)
            }
        }
        checkWinCondition(state)
        checkAutoWinCondition(state)
    }

    fun endTurn(state: GameState) {
        state.currentTurn = if (state.currentTurn == Turn.PLAYER) Turn.OPPONENT else Turn.PLAYER
        if (state.currentTurn == Turn.PLAYER) {
            state.turnNumber += 1
        }
        startTurn(state)
    }

    private fun checkWinCondition(state: GameState) {
        if (state.player.isDead && state.opponent.isDead) {
            state.isGameOver = true; state.winnerName = "draw"
        } else if (state.opponent.isDead) {
            state.isGameOver = true; state.winnerName = state.player.name
        } else if (state.player.isDead) {
            state.isGameOver = true; state.winnerName = state.opponent.name
        }
    }

    fun checkAutoWinCondition(state: GameState) {
        if (state.isGameOver) return
        val p = state.player
        val o = state.opponent
        
        // КРИТЕРИЙ: Нет ресурсов для игры (пустая рука, пустой стол, пустая колода)
        val pNoRes = p.hand.isEmpty() && p.board.all { it == null } && p.deck.isEmpty()
        val oNoRes = o.hand.isEmpty() && o.board.all { it == null } && o.deck.isEmpty()

        if (pNoRes) {
             state.isGameOver = true
             state.winnerName = o.name
        } else if (oNoRes) {
             state.isGameOver = true
             state.winnerName = p.name
        }
    }
}
