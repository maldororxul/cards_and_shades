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
    fun testSplashEffect() {
        val player = PlayerModel("Player")
        val opponent = PlayerModel("Opponent")
        val state = GameState(player, opponent, Turn.PLAYER)

        val attacker = CardModel("1", "Mage", 1, 2, 2, Rarity.COMMON, effectTags = listOf(EffectTag.SPLASH))
        val target = CardModel("2", "Target", 1, 1, 5, Rarity.COMMON)
        val neighbor = CardModel("3", "Neighbor", 1, 1, 1, Rarity.COMMON)

        player.board.add(attacker)
        opponent.board.add(neighbor)
        opponent.board.add(target)

        GameEngine.calculateCombat(state, attacker, target)

        // Target took 2 damage
        assertEquals(3, target.currentHealth)
        // Neighbor took 1 splash damage
        assertEquals(0, neighbor.currentHealth)
        assertTrue(neighbor.isTakingDamage)
    }
}
