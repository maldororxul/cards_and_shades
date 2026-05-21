package com.example.cardsandshades

import com.example.cardsandshades.engine.GameEngine
import com.example.cardsandshades.model.*
import org.junit.Assert.*
import org.junit.Test

class GameEngineTest {

    @Test
    fun testTauntLogic() {
        val player = PlayerModel("Player")
        val opponent = PlayerModel("Opponent")
        val state = GameState(player, opponent, Turn.PLAYER)

        val attacker = CardModel("1", "Attacker", 1, 2, 2, Rarity.COMMON, isSleeping = false)
        val tauntCard = CardModel("2", "Taunt", 1, 1, 5, Rarity.COMMON, effectTags = listOf(EffectTag.TAUNT))
        val normalCard = CardModel("3", "Normal", 1, 1, 1, Rarity.COMMON)

        opponent.board.add(tauntCard)
        opponent.board.add(normalCard)

        // Cannot attack hero if taunt is present
        assertFalse(GameEngine.canAttackHero(state, attacker))
        
        // Cannot attack normal card if taunt is present
        assertFalse(GameEngine.canAttackTarget(state, attacker, normalCard))
        
        // CAN attack taunt card
        assertTrue(GameEngine.canAttackTarget(state, attacker, tauntCard))
    }

    @Test
    fun testLifestealEffect() {
        val player = PlayerModel("Player", currentHp = 10, maxHp = 30)
        val opponent = PlayerModel("Opponent")
        val state = GameState(player, opponent, Turn.PLAYER)

        val attacker = CardModel("1", "Vampire", 1, 5, 5, Rarity.EPIC, effectTags = listOf(EffectTag.LIFESTEAL), isSleeping = false)
        player.board.add(attacker)

        GameEngine.attackHero(state, attacker)

        // Player healed for 5
        assertEquals(15, player.currentHp)
    }

    @Test
    fun testBuffEffect() {
        val player = PlayerModel("Player")
        val opponent = PlayerModel("Opponent")
        val state = GameState(player, opponent, Turn.PLAYER)

        val cardToBuff = CardModel("1", "Target", 1, 1, 1, Rarity.COMMON)
        player.board.add(cardToBuff)

        val buffer = CardModel("2", "Buffer", 1, 1, 1, Rarity.LEGENDARY, effectTags = listOf(EffectTag.BUFF))
        
        // Play card should trigger onSummon and apply buff
        player.hand.add(buffer)
        GameEngine.playCard(state, buffer)

        assertEquals(3, cardToBuff.currentAttack)
        assertEquals(3, cardToBuff.currentHealth)
        assertEquals(1, cardToBuff.buffs.size)
    }

    @Test
    fun testAutoWinCondition() {
        val player = PlayerModel("Player", currentHp = 5)
        val opponent = PlayerModel("Opponent", currentHp = 30)
        val state = GameState(player, opponent, Turn.OPPONENT)

        // Player has nothing
        player.hand.clear()
        player.board.clear()
        player.deck.clear()

        // Opponent has lethal
        val attacker = CardModel("1", "Killer", 1, 10, 10, Rarity.COMMON)
        opponent.board.add(attacker)

        GameEngine.checkAutoWinCondition(state)

        assertTrue(state.isGameOver)
        assertEquals("Opponent", state.winnerName)
    }
}
