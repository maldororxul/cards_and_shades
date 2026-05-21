package com.example.cardsandshades.ui.game

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cardsandshades.catalog.CardCatalog
import com.example.cardsandshades.engine.GameEngine
import com.example.cardsandshades.model.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch


class GameViewModel : ViewModel() {

    private val _gameState = MutableStateFlow<GameState?>(null)
    val gameState: StateFlow<GameState?> = _gameState.asStateFlow()

    private var opponentTurnJob: Job? = null

    var currentLevel: LevelModel? = null
        private set

    var opponentAttackerId by mutableStateOf<String?>(null)
        private set
    var opponentTargetId by mutableStateOf<String?>(null)
        private set
    var isOpponentTargetingHero by mutableStateOf(false)
        private set

    var playerHeroTakingDamage by mutableStateOf(false)
        private set
    var playerHeroDamageValue by mutableIntStateOf(0)
        private set

    var opponentHeroTakingDamage by mutableStateOf(false)
        private set
    var opponentHeroDamageValue by mutableIntStateOf(0)
        private set

    // Убрали принудительный вызов из init, чтобы игра не крашилась при старте до выбора уровня
    init {
        _gameState.value = null
    }

    // Инициализация матча для конкретного уровня кампании
    fun startNewGame(level: LevelModel) {
        opponentTurnJob?.cancel()
        currentLevel = level

        // ИСПРАВЛЕНИЕ: Берем сохраненную деку игрока из профиля и полностью восстанавливаем ей статы
        val playerDeck = if (UserProfile.selectedDeck.size == 20) {
            UserProfile.selectedDeck.map {
                it.copy(id = java.util.UUID.randomUUID().toString()).apply { reset() }
            }.toMutableList()
        } else {
            // Если игрок умудрился зайти без деки — используем случайный автонабор
            CardCatalog.generateTestDeck()
        }
        
        // ККИ-ПРАВИЛО: Колода игрока всегда перемешивается перед началом боя
        playerDeck.shuffle()

        // ИСПРАВЛЕНИЕ: Собираем уникальную тематическую колоду для ИИ на основе пресета уровня
        // Если в пресете мало карт — повторяем их до 20 штук для полноценной игры
        val opponentDeckNames = mutableListOf<String>()
        if (level.opponentDeckPreset.isNotEmpty()) {
            while (opponentDeckNames.size < 20) {
                opponentDeckNames.addAll(level.opponentDeckPreset)
            }
        }
        val opponentDeck = opponentDeckNames.take(20).map { cardName ->
            // Ищем карту в каталоге по имени. Если не нашли (опечатка) — подставляем Тень-новобранца
            (CardCatalog.createCardInstance(cardName) ?: CardCatalog.createCardInstance("Тень-новобранец")!!).apply { reset() }
        }.toMutableList()

        // Обязательно перемешиваем колоду босса перед началом матча
        opponentDeck.shuffle()

        val player = PlayerModel(
            name = "Игрок",
            deck = playerDeck,
            hand = mutableListOf(),
            board = mutableListOf()
        )

        val opponent = PlayerModel(
            name = level.opponentName,
            maxHp = level.opponentMaxHp,
            currentHp = level.opponentMaxHp,
            deck = opponentDeck,
            hand = mutableListOf(),
            board = mutableListOf()
        )

        val initialState = GameState(
            player = player,
            opponent = opponent,
            currentTurn = Turn.PLAYER,
            turnNumber = 1,
            isGameOver = false,
            winnerName = null
        )

        // Раздаем стартовые карты
        repeat(4) {
            GameEngine.drawCard(initialState.player, initialState)
            GameEngine.drawCard(initialState.opponent, initialState)
        }

        // Начисляем стартовую ману для первого хода
        GameEngine.startTurn(initialState)

        _gameState.value = initialState
    }

    fun claimRewardsAndExit(isPlayerWin: Boolean) {
        opponentTurnJob?.cancel()
        if (isPlayerWin) {
            currentLevel?.let { level ->
                val isFirstTime = level.id >= UserProfile.maxUnlockedLevel.value
                val rewards = if (isFirstTime) level.firstTimeReward else level.repeatReward

                // 1. Начисляем золото и кристаллы
                UserProfile.gold.value += rewards.gold
                UserProfile.crystals.value += rewards.crystals
                
                // 2. Начисляем пыль
                UserProfile.dustCommon.value += rewards.dustCommon
                UserProfile.dustRare.value += rewards.dustRare
                UserProfile.dustEpic.value += rewards.dustEpic
                UserProfile.dustLegendary.value += rewards.dustLegendary

                // 3. Выдаем призовую ККИ-карту
                rewards.cardName?.let { cardName ->
                    CardCatalog.createCardInstance(cardName)?.let { prizeCard ->
                        UserProfile.collection.add(prizeCard)
                    }
                }
                UserProfile.collection.notifyChanges()

                // 4. Рассчитываем прогресс
                if (level.id == UserProfile.maxUnlockedLevel.value) {
                    UserProfile.maxUnlockedLevel.value = level.id + 1
                }
                UserProfile.save()
            }
        }
        _gameState.value = null
    }

    fun restartCurrentGame() {
        currentLevel?.let { startNewGame(it) }
    }

    // Разыгрывание карты игроком (с глубоким копированием для Compose)
    fun playCard(card: CardModel): Boolean {
        var isPlayed = false
        _gameState.update { currentState ->
            currentState?.deepCopy()?.apply {
                if (currentTurn == Turn.PLAYER) {
                    val cardInHand = player.hand.find { it.id == card.id }
                    if (cardInHand != null && player.currentMana >= cardInHand.manaCost && player.board.size < 5) {
                        com.example.cardsandshades.engine.GameEngine.playCard(this, cardInHand)
                        isPlayed = true
                    }
                }
            } ?: currentState
        }
        return isPlayed
    }

    // Атака карты на карту с глубоким клонированием стейта статов существ
    fun attackEnemyCard(attacker: CardModel, target: CardModel) {
        val state = _gameState.value ?: return
        if (state.isAnimating || state.currentTurn != Turn.PLAYER) return

        viewModelScope.launch {
            _gameState.update { it?.copy(isAnimating = true) }
            try {
                // 1. Анимация рывка атакующего вперед
                updateCardAnimation(attacker.id, isAttacking = true)
                delay(200)

                // 2. Встречный удар: Делегируем чистый расчет урона в GameEngine для работы Стрелков
                _gameState.update { currentState ->
                    currentState?.deepCopy()?.apply {
                        val aCard = player.board.find { it.id == attacker.id }
                        val tCard = opponent.board.find { it.id == target.id }

                        if (aCard != null && tCard != null) {
                            tCard.isTakingDamage = true
                            aCard.isTakingDamage = true
                            com.example.cardsandshades.engine.GameEngine.calculateCombat(this, aCard, tCard)
                        }
                    }
                }
                updateCardAnimation(attacker.id, isAttacking = false)
                delay(500)

                // 3. Анимация смерти (падения) И СБРОС ТРЯСКИ ДЛЯ ВСЕХ УЧАСТНИКОВ (включая жертв Splash)
                _gameState.update { currentState ->
                    currentState?.deepCopy()?.apply {
                        player.board.forEach { card ->
                            card.isTakingDamage = false
                            if (card.currentHealth <= 0) card.isDying = true
                        }
                        opponent.board.forEach { card ->
                            card.isTakingDamage = false
                            if (card.currentHealth <= 0) card.isDying = true
                        }
                    }
                }

                val hasDeaths = _gameState.value?.player?.board?.any { it.isDying } == true ||
                        _gameState.value?.opponent?.board?.any { it.isDying } == true
                if (hasDeaths) delay(400)

                // 4. Окончательное удаление карт со стола
                _gameState.update { currentState ->
                    currentState?.deepCopy()?.apply {
                        player.board.removeAll { it.currentHealth <= 0 }
                        opponent.board.removeAll { it.currentHealth <= 0 }
                        isAnimating = false

                        if (player.currentHp <= 0 || opponent.currentHp <= 0) {
                            isGameOver = true
                            winnerName = if (opponent.currentHp <= 0) player.name else opponent.name
                        }
                    }
                }
            } catch (e: Exception) {
                _gameState.update { it?.copy(isAnimating = false) }
            }
        }
    }

    // Вспомогательный метод переключения флагов
    private fun updateCardAnimation(cardId: String, isAttacking: Boolean = false) {
        _gameState.update { currentState ->
            currentState?.let { s ->
                val pBoard = s.player.board.map { if (it.id == cardId) it.copy(isAttacking = isAttacking) else it }.toMutableList()
                val oBoard = s.opponent.board.map { if (it.id == cardId) it.copy(isAttacking = isAttacking) else it }.toMutableList()
                s.copy(player = s.player.copy(board = pBoard), opponent = s.opponent.copy(board = oBoard))
            }
        }
    }

    fun attackEnemyHero(attacker: CardModel) {
        val state = _gameState.value ?: return
        if (state.isAnimating || state.currentTurn != Turn.PLAYER) return

        if (!GameEngine.canAttackHero(state, attacker)) return

        viewModelScope.launch {
            _gameState.update { it?.copy(isAnimating = true) }
            try {
                updateCardAnimation(attacker.id, isAttacking = true)
                delay(200)

                opponentHeroDamageValue = attacker.currentAttack
                opponentHeroTakingDamage = true

                _gameState.update { currentState ->
                    currentState?.deepCopy()?.apply {
                        val aCard = player.board.find { it.id == attacker.id }
                        if (aCard != null) {
                            GameEngine.attackHero(this, aCard)
                        }
                    }
                }

                updateCardAnimation(attacker.id, isAttacking = false)
                delay(500)

                opponentHeroTakingDamage = false
                _gameState.update { it?.copy(isAnimating = false) }

                _gameState.value?.let { s ->
                    if (s.opponent.currentHp <= 0) {
                        _gameState.update { it?.copy(isGameOver = true, winnerName = s.player.name) }
                    }
                }
            } catch (e: Exception) {
                _gameState.update { it?.copy(isAnimating = false) }
            }
        }
    }


    fun endTurn() {
        _gameState.update { currentState ->
            currentState?.deepCopy()?.apply {
                if (currentTurn == Turn.PLAYER) {
                    GameEngine.endTurn(this)
                }
            } ?: currentState
        }

        val state = _gameState.value
        if (state != null && state.currentTurn == Turn.OPPONENT && !state.isGameOver) {
            opponentTurnJob?.cancel()
            opponentTurnJob = executeOpponentTurn()
        }
    }

    private fun executeOpponentTurn(): Job = viewModelScope.launch {
        delay(1000)
        if (!isActive) return@launch

        // ФАЗА ИИ 1: Розыгрыш карт
        _gameState.update { currentState ->
            currentState?.deepCopy()?.apply {
                val cardsInHand = opponent.hand.toList()
                for (card in cardsInHand) {
                    com.example.cardsandshades.engine.GameEngine.playCard(this, card)
                }
            }
        }

        delay(500)
        if (!isActive) return@launch

        // ФАЗА ИИ 2: Атака со строгими правилами ККИ
        val state = _gameState.value
        if (state != null && !state.isGameOver) {
            val attackerCards = state.opponent.board.toList()

            for (attacker in attackerCards) {
                if (!isActive) break
                val currentGameState = _gameState.value ?: break
                val activeAttacker = currentGameState.opponent.board.find { it.id == attacker.id } ?: continue

                // ОШИБКА: ИИ не должен скипать ход, если не может атаковать героя (например, из-за Таунта)
                if (activeAttacker.isSleeping || activeAttacker.hasAttackedThisTurn) continue

                val playerBoard = currentGameState.player.board
                val validEnemyCards = playerBoard.filter { enemyCard ->
                    GameEngine.canAttackTarget(currentGameState, activeAttacker, enemyCard)
                }

                opponentAttackerId = activeAttacker.id
                if (validEnemyCards.isNotEmpty()) {
                    val target = validEnemyCards.random()
                    opponentTargetId = target.id
                    isOpponentTargetingHero = false
                } else if (GameEngine.canAttackHero(currentGameState, activeAttacker)) {
                    opponentTargetId = null
                    isOpponentTargetingHero = true
                } else {
                    opponentAttackerId = null
                    continue
                }

                delay(1000)
                if (!isActive) break

                updateCardAnimation(activeAttacker.id, isAttacking = true)
                delay(200)

                _gameState.update { currentState ->
                    currentState?.deepCopy()?.apply {
                        val nextAttacker = opponent.board.find { it.id == activeAttacker.id }
                        if (nextAttacker != null) {
                            if (!isOpponentTargetingHero) {
                                val nextTarget = player.board.find { it.id == opponentTargetId }
                                if (nextTarget != null) {
                                    nextTarget.isTakingDamage = true
                                    nextAttacker.isTakingDamage = true
                                    com.example.cardsandshades.engine.GameEngine.calculateCombat(this, nextAttacker, nextTarget)
                                }
                            } else {
                                playerHeroDamageValue = nextAttacker.currentAttack
                                playerHeroTakingDamage = true
                                com.example.cardsandshades.engine.GameEngine.attackHero(this, nextAttacker)
                            }
                        }
                    }
                }

                updateCardAnimation(activeAttacker.id, isAttacking = false)
                delay(500)
                playerHeroTakingDamage = false

                _gameState.update { currentState ->
                    currentState?.deepCopy()?.apply {
                        player.board.forEach { card ->
                            card.isTakingDamage = false
                            if (card.currentHealth <= 0) card.isDying = true
                        }
                        opponent.board.forEach { card ->
                            card.isTakingDamage = false
                            if (card.currentHealth <= 0) card.isDying = true
                        }
                    }
                }

                opponentAttackerId = null
                opponentTargetId = null
                isOpponentTargetingHero = false

                val hasDeaths = _gameState.value?.player?.board?.any { it.isDying } == true ||
                        _gameState.value?.opponent?.board?.any { it.isDying } == true
                if (hasDeaths) delay(400)

                _gameState.update { currentState ->
                    currentState?.deepCopy()?.apply {
                        player.board.removeAll { it.currentHealth <= 0 }
                        opponent.board.removeAll { it.currentHealth <= 0 }
                        if (player.currentHp <= 0 || opponent.currentHp <= 0) {
                            isGameOver = true
                            winnerName = if (opponent.currentHp <= 0) player.name else opponent.name
                        }
                    }
                }
                delay(400)
            }
        }

        if (!isActive) return@launch
        delay(600)

        // ФАЗА ИИ 3: Передача хода игроку
        _gameState.update { currentState ->
            currentState?.deepCopy()?.apply {
                GameEngine.endTurn(this)
            }
        }
    }
}
