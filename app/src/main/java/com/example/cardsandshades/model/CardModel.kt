package com.example.cardsandshades.model

import com.example.cardsandshades.effect.*

// Перечисление всех ККИ-эффектов для безопасного сохранения в JSON
enum class EffectTag {
    RUSH, TAUNT, RANGED, SPLASH, LIFESTEAL, BUFF,
    BLEED, POISON, BURN,
    IMMUNE_BLEED, IMMUNE_POISON, IMMUNE_BURN,
    DEBUFF_ATTACK,
    FREEZE, STUN, CRIT, RETRIBUTION, HEAL, MASS_HEAL,
    AUTO_ATTACK_PLAYED, NEIGHBOR_BUFF_ATTACK, NEIGHBOR_BUFF_HEALTH,
    IMMUNE_FREEZE, IMMUNE_STUN, IMMUNE_CRIT
}

enum class GroupTag {
    UNDEAD, DRAGON, ELEMENTAL, BEAST, ABSTRACTION, HUMAN, MECHANICAL
}

enum class Rarity { COMMON, UNCOMMON, RARE, EPIC, LEGENDARY, MYTHIC }

data class BuffModel(
    val id: String,
    val name: String,
    val attackBonus: Int,
    val healthBonus: Int,
    var duration: Int, // Ходов осталось
    val tag: EffectTag? = null // Привязка к эффекту (яд, кровотечение и т.д.)
)

data class CardModel(
    val id: String,
    val name: String,
    val manaCost: Int,
    val baseAttack: Int,
    val baseHealth: Int,
    val rarity: Rarity,
    var currentAttack: Int = baseAttack,
    var currentHealth: Int = baseHealth,

    // ГРУППЫ КАРТЫ
    private val groupTags: List<GroupTag>? = emptyList(),

    // Gson может записать сюда null при десериализации старого кэша, если поле отсутствовало
    private val effectTags: List<EffectTag>? = emptyList(),
    private val activeBuffs: List<BuffModel>? = emptyList(),

    var isSleeping: Boolean = true,
    var hasAttackedThisTurn: Boolean = false,
    var isAttacking: Boolean = false,
    var isTakingDamage: Boolean = false,
    var lastDamageTaken: Int = 0,
    var isDying: Boolean = false,

    // НОВЫЕ СОСТОЯНИЯ
    var isFrozen: Boolean = false,
    var isStunned: Boolean = false,
    var critMultiplier: Float = 2.0f
) {
    val isDead: Boolean get() = currentHealth <= 0

    // ИСПРАВЛЕНИЕ: Безопасный публичный доступ к тегам
    val activeTags: List<EffectTag> get() = effectTags ?: emptyList()
    val groups: List<GroupTag> get() = groupTags ?: emptyList()

    // ИСПРАВЛЕНИЕ: Проверка нежити
    val isUndead: Boolean get() = groups.contains(GroupTag.UNDEAD)

    // ИСПРАВЛЕНИЕ: Безопасный доступ к баффам (теперь они приватные и копируются при изменении)
    private var currentBuffs: List<BuffModel> = activeBuffs ?: emptyList()

    val buffs: List<BuffModel> get() = currentBuffs

    fun addBuff(buff: BuffModel) {
        currentBuffs = currentBuffs + buff
    }

    fun removeBuffs(expired: List<BuffModel>) {
        currentBuffs = currentBuffs.filter { !expired.contains(it) }
    }

    fun clearBuffs() {
        currentBuffs = emptyList()
    }

    // ИСПРАВЛЕНИЕ: Проверка танка через activeTags
    val hasTaunt: Boolean get() = activeTags.contains(EffectTag.TAUNT)

    // ИСПРАВЛЕНИЕ: Развертывание эффектов через activeTags
    val activeEffects: List<CardEffect>
        get() = activeTags.map { tag ->
            when (tag) {
                EffectTag.RUSH -> RushEffect()
                EffectTag.TAUNT -> TauntEffect()
                EffectTag.RANGED -> RangedEffect()
                EffectTag.SPLASH -> SplashEffect()
                EffectTag.LIFESTEAL -> LifestealEffect()
                EffectTag.BUFF -> BuffEffect()
                EffectTag.BLEED -> BleedEffect()
                EffectTag.POISON -> PoisonEffect()
                EffectTag.BURN -> BurnEffect()
                EffectTag.IMMUNE_BLEED -> ImmuneBleedEffect()
                EffectTag.IMMUNE_POISON -> ImmunePoisonEffect()
                EffectTag.IMMUNE_BURN -> ImmuneBurnEffect()
                EffectTag.DEBUFF_ATTACK -> DebuffAttackEffect()
                EffectTag.FREEZE -> FreezeEffect()
                EffectTag.STUN -> StunEffect()
                EffectTag.CRIT -> CritEffect()
                EffectTag.RETRIBUTION -> RetributionEffect()
                EffectTag.HEAL -> HealEffect()
                EffectTag.MASS_HEAL -> MassHealEffect()
                EffectTag.AUTO_ATTACK_PLAYED -> AutoAttackPlayedEffect()
                EffectTag.NEIGHBOR_BUFF_ATTACK -> NeighborBuffAttackEffect()
                EffectTag.NEIGHBOR_BUFF_HEALTH -> NeighborBuffHealthEffect()
                EffectTag.IMMUNE_FREEZE -> ImmuneFreezeEffect()
                EffectTag.IMMUNE_STUN -> ImmuneStunEffect()
                EffectTag.IMMUNE_CRIT -> ImmuneCritEffect()
            }
        }

    fun resetTurnState() {
        isSleeping = false
        hasAttackedThisTurn = false
    }

    fun deepCopy(): CardModel {
        val copy = this.copy()
        copy.currentBuffs = this.currentBuffs.map { it.copy() }
        return copy
    }

    fun reset() {
        currentAttack = baseAttack
        currentHealth = baseHealth
        clearBuffs()
        isAttacking = false
        isTakingDamage = false
        isDying = false
        lastDamageTaken = 0
        isSleeping = !activeTags.contains(EffectTag.RUSH)
        isFrozen = false
        isStunned = false
    }
}
