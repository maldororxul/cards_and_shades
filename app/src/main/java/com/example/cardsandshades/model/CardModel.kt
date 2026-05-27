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
    UNDEAD, DRAGON, ELEMENTAL, BEAST, ABSTRACTION, HUMAN, MECHANICAL,
    MELEE, RANGER, MAGE, ELF, DEMON, GIANT, GOBLIN
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
    var currentAttack: Int = -1,
    var currentHealth: Int = -1,

    // backing fields for Gson null-safety
    private val groupTags: List<GroupTag>? = emptyList(),
    private val effectTags: List<EffectTag>? = emptyList(),
    private var activeBuffs: List<BuffModel>? = emptyList(),

    var isSleeping: Boolean = true,
    var hasAttackedThisTurn: Boolean = false,
    var isAttacking: Boolean = false,
    var isTakingDamage: Boolean = false,
    var lastDamageTaken: Int = 0,
    var isDying: Boolean = false,

    // НОВЫЕ СОСТОЯНИЯ
    var isFrozen: Boolean = false,
    var isStunned: Boolean = false,
    var critMultiplier: Float = 1.0f,

    // НОВЫЕ ПОЛЯ ИЗ YAML (с backing fields для Gson)
    private val limit: Int? = 3,
    private val dSound: String? = "card_death",
    private val pSound: String? = "card_place",
    private val aSound: String? = "attack",
    private val quoteList: List<String>? = emptyList()
) {
    init {
        if (currentAttack == -1) currentAttack = baseAttack
        if (currentHealth == -1) currentHealth = baseHealth
    }

    val deckLimit: Int get() = limit ?: 3
    val deathSound: String get() = dSound ?: "card_death"
    val playSound: String get() = pSound ?: "card_place"
    val attackSound: String get() = aSound ?: "attack"
    val quotes: List<String> get() = quoteList ?: emptyList()

    val isDead: Boolean get() = currentHealth <= 0

    // ИСПРАВЛЕНИЕ: Безопасный публичный доступ к тегам (Gson может подсунуть null в non-nullable поле)
    val activeTags: List<EffectTag> get() = effectTags ?: emptyList()
    val groups: List<GroupTag> get() = groupTags ?: emptyList()

    // ИСПРАВЛЕНИЕ: Проверка нежити
    val isUndead: Boolean get() = groups.contains(GroupTag.UNDEAD)

    val buffs: List<BuffModel> get() = activeBuffs ?: emptyList()

    fun addBuff(buff: BuffModel) {
        activeBuffs = (activeBuffs ?: emptyList()) + buff
    }

    fun removeBuffs(expired: List<BuffModel>) {
        activeBuffs = (activeBuffs ?: emptyList()).filter { !expired.contains(it) }
    }

    fun clearBuffs() {
        activeBuffs = emptyList()
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
        val b = (activeBuffs ?: emptyList()).map { it.copy() }
        // quotes safe check
        val q = (quoteList ?: emptyList()).toList()
        val copy = this.copy(activeBuffs = b, quoteList = q)
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
