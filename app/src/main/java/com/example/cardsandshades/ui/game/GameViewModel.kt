package com.example.cardsandshades.ui.game

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cardsandshades.catalog.CardCatalog
import com.example.cardsandshades.engine.GameEngine
import com.example.cardsandshades.model.CardModel
import com.example.cardsandshades.model.GameState
import com.example.cardsandshades.model.LevelModel
import com.example.cardsandshades.model.PlayerModel
import com.example.cardsandshades.model.Turn
import com.example.cardsandshades.model.UserProfile
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class GameViewModel : ViewModel() {

    private val _gameState = MutableStateFlow<GameState?>(null)
    val gameState: StateFlow<GameState?> = _gameState.asStateFlow()

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

        // ИСПРАВЛЕНИЕ: Собираем уникальную тематическую колоду для ИИ на основе пресета уровня
        val opponentDeck = level.opponentDeckPreset.map { cardName ->
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
        if (isPlayerWin) {
            currentLevel?.let { level ->
                // 1. Начисляем золото, прописанное в наградах уровня
                UserProfile.gold.value += level.rewardGold

                // 2. Выдаем призовую ККИ-карту в коллекцию (если она указана)
                level.rewardCardName?.let { cardName ->
                    CardCatalog.createCardInstance(cardName)?.let { prizeCard ->
                        UserProfile.collection.add(prizeCard)
                    }
                }
                UserProfile.collection.notifyChanges()

                // 3. Рассчитываем прогресс открытия следующих глав
                if (level.id == UserProfile.maxUnlockedLevel.value) {
                    UserProfile.maxUnlockedLevel.value = level.id + 1
                }
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
            currentState?.let { state ->
                if (state.currentTurn == Turn.PLAYER) {
                    val updatedPlayer = state.player.copy(
                        hand = state.player.hand.toMutableList(),
                        board = state.player.board.toMutableList()
                    )
                    val updatedState = state.copy(player = updatedPlayer)

                    // Находим карту по ID именно в актуальной руке игрока из стейта
                    val cardInHand = updatedPlayer.hand.find { it.id == card.id }
                    if (cardInHand != null && updatedPlayer.currentMana >= cardInHand.manaCost && updatedPlayer.board.size < 5) {
                        com.example.cardsandshades.engine.GameEngine.playCard(updatedState, cardInHand)
                        isPlayed = true
                        updatedState
                    } else state
                } else state
            }
        }
        return isPlayed
    }

    // Атака карты на карту с глубоким клонированием стейта статов существ
    fun attackEnemyCard(attacker: CardModel, target: CardModel) {
        val state = _gameState.value ?: return
        if (state.isAnimating || state.currentTurn != Turn.PLAYER) return

        viewModelScope.launch {
            _gameState.update { it?.copy(isAnimating = true) }

            // 1. Анимация рывка атакующего вперед
            updateCardAnimation(attacker.id, isAttacking = true)
            delay(200)

            // 2. Встречный удар: Делегируем чистый расчет урона в GameEngine для работы Стрелков
            _gameState.update { currentState ->
                currentState?.let { s ->
                    val pBoard = s.player.board.map { it.copy() }.toMutableList()
                    val oBoard = s.opponent.board.map { it.copy() }.toMutableList()

                    val aCard = pBoard.find { it.id == attacker.id }
                    val tCard = oBoard.find { it.id == target.id }

                    if (aCard != null && tCard != null) {
                        tCard.isTakingDamage = true
                        aCard.isTakingDamage = true

                        // ИСПРАВЛЕНИЕ: Вызываем ООП-метод движка, чтобы применилось свойство Стрелка (без ответки)
                        com.example.cardsandshades.engine.GameEngine.calculateCombat(s, aCard, tCard)
                    }
                    s.copy(player = s.player.copy(board = pBoard), opponent = s.opponent.copy(board = oBoard))
                }
            }
            updateCardAnimation(attacker.id, isAttacking = false)
            delay(500) // Даем время рассмотреть цифры урона перед их исчезновением

            // 3. Анимация смерти (падения) И СБРОС ТРЯСКИ ДЛЯ ВЫЖИВШИХ
            _gameState.update { currentState ->
                currentState?.let { s ->
                    val pBoard = s.player.board.map { it.copy() }.toMutableList()
                    val oBoard = s.opponent.board.map { it.copy() }.toMutableList()

                    pBoard.find { it.id == attacker.id }?.let {
                        it.isTakingDamage = false // ИСПРАВЛЕНИЕ: Сбрасываем флаг, убирая цифру урона
                        if (it.currentHealth <= 0) it.isDying = true
                    }
                    oBoard.find { it.id == target.id }?.let {
                        it.isTakingDamage = false // ИСПРАВЛЕНИЕ: Сбрасываем флаг, убирая цифру урона
                        if (it.currentHealth <= 0) it.isDying = true
                    }
                    s.copy(player = s.player.copy(board = pBoard), opponent = s.opponent.copy(board = oBoard))
                }
            }

            val hasDeaths = _gameState.value?.player?.board?.any { it.isDying } == true ||
                    _gameState.value?.opponent?.board?.any { it.isDying } == true
            if (hasDeaths) delay(400)

            // 4. Окончательное удаление карт со стола
            _gameState.update { currentState ->
                currentState?.let { s ->
                    val pBoard = s.player.board.filter { it.currentHealth > 0 }.toMutableList()
                    val oBoard = s.opponent.board.filter { it.currentHealth > 0 }.toMutableList()

                    s.copy(
                        player = s.player.copy(board = pBoard),
                        opponent = s.opponent.copy(board = oBoard),
                        isAnimating = false
                    ).apply {
                        if (player.currentHp <= 0 || opponent.currentHp <= 0) {
                            this.isGameOver = true
                            this.winnerName = if (opponent.currentHp <= 0) player.name else opponent.name
                        }
                    }
                }
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

        // Проверяем жесткое ограничение перед ударом в лицо
        if (!GameEngine.canAttackHero(state, attacker)) return

        viewModelScope.launch {
            _gameState.update { it?.copy(isAnimating = true) }

            updateCardAnimation(attacker.id, isAttacking = true)
            delay(200)

            opponentHeroDamageValue = attacker.currentAttack
            opponentHeroTakingDamage = true

            _gameState.update { currentState ->
                currentState?.let { s ->
                    val pBoard = s.player.board.map { it.copy() }.toMutableList()
                    // Тратим ход карты при ударе в лицо
                    pBoard.find { it.id == attacker.id }?.hasAttackedThisTurn = true

                    s.copy(
                        player = s.player.copy(board = pBoard),
                        opponent = s.opponent.copy(currentHp = s.opponent.currentHp - attacker.currentAttack)
                    )
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
        }
    }


    fun endTurn() {
        _gameState.update { currentState ->
            currentState?.let { state ->
                if (state.currentTurn == Turn.PLAYER) {
                    val updatedState = state.copy()
                    GameEngine.endTurn(updatedState)

                    // БЕЗОПАСНЫЙ ФИКС: В момент передачи хода к ИИ, мы будим его СТАРЫЕ карты на столе
                    val activeOpponent = updatedState.opponent.copy(
                        board = updatedState.opponent.board.map { card ->
                            card.copy(isSleeping = false, hasAttackedThisTurn = false)
                        }.toMutableList()
                    )

                    updatedState.copy(opponent = activeOpponent)
                } else state
            }
        }

        val state = _gameState.value
        if (state != null && state.currentTurn == Turn.OPPONENT && !state.isGameOver) {
            executeOpponentTurn()
        }
    }

    private fun executeOpponentTurn() {
        viewModelScope.launch {
            delay(1000)

            // ФАЗА ИИ 1: Розыгрыш карт
            _gameState.update { currentState ->
                currentState?.let { state ->
                    val updatedOpponent = state.opponent.copy(
                        hand = state.opponent.hand.toMutableList(),
                        board = state.opponent.board.toMutableList()
                    )
                    val updatedState = state.copy(opponent = updatedOpponent)
                    val cardsInHand = updatedOpponent.hand.toList()

                    for (card in cardsInHand) {
                        com.example.cardsandshades.engine.GameEngine.playCard(updatedState, card)
                    }
                    updatedState
                }
            }

            delay(500)

            // ФАЗА ИИ 2: Атака с пошаговой визуализацией
            // ФАЗА ИИ 2: Атака со строгими правилами ККИ и пошаговой визуализацией
            val state = _gameState.value
            if (state != null && !state.isGameOver) {
                val attackerCards = state.opponent.board.toList()

                for (attacker in attackerCards) {
                    // 1. Проверяем, живо ли еще существо ИИ на столе
                    val activeAttacker = _gameState.value?.opponent?.board?.find { it.id == attacker.id } ?: continue

                    // 2. Проверяем, может ли вообще эта карта ходить (не спит ли)
                    val canAIAttack = com.example.cardsandshades.engine.GameEngine.canAttackHero(_gameState.value!!, activeAttacker)
                    if (!canAIAttack) continue

                    // Динамически считываем живой стол игрока на текущий шаг цикла
                    val currentGameState = _gameState.value ?: continue
                    val playerBoard = currentGameState.player.board

                    // ИСПРАВЛЕНИЕ БАГА: ИИ ищет цели строго через ККИ-валидатор движка GameEngine
                    // Это гарантирует, что ИИ никогда не выберет заблокированную Провокацией карту!
                    val validEnemyCards = playerBoard.filter { enemyCard ->
                        GameEngine.canAttackTarget(currentGameState, activeAttacker, enemyCard)
                    }

                    // Включаем стрелку прицеливания для ИИ
                    opponentAttackerId = activeAttacker.id

                    if (validEnemyCards.isNotEmpty()) {
                        // Если есть легальные цели среди существ (включая Танков, их движок подсунет первыми) — бьем случайное из них
                        val target = validEnemyCards.random()
                        opponentTargetId = target.id
                        isOpponentTargetingHero = false
                    } else {
                        // Если легальных существ для атаки нет, проверяем, можно ли ударить в лицо
                        val canStrikeHero = GameEngine.canAttackHero(currentGameState, activeAttacker)
                        if (canStrikeHero && playerBoard.isEmpty()) {
                            opponentTargetId = null
                            isOpponentTargetingHero = true
                        } else {
                            // Если стол не пуст, но атаковать никого нельзя (например, все закрыто маскировкой или баг флагов) — пропускаем эту карту
                            opponentAttackerId = null
                            continue
                        }
                    }

                    delay(1000) // Игрок видит стрелку ИИ

                    // Анимация рывка существа ИИ вперед
                    updateCardAnimation(activeAttacker.id, isAttacking = true)
                    delay(200)

                    // Встречный удар: фиксация урона, тряска и вылет цифр
                    _gameState.update { currentState ->
                        currentState?.let { s ->
                            val pBoard = s.player.board.map { it.copy() }.toMutableList()
                            val oBoard = s.opponent.board.map { it.copy() }.toMutableList()

                            val nextAttacker = oBoard.find { it.id == activeAttacker.id }

                            if (nextAttacker != null) {
                                if (!isOpponentTargetingHero) {
                                    val nextTarget = pBoard.find { it.id == opponentTargetId }
                                    if (nextTarget != null) {
                                        nextTarget.isTakingDamage = true
                                        nextAttacker.isTakingDamage = true

                                        // Вызываем ООП-расчет движка
                                        com.example.cardsandshades.engine.GameEngine.calculateCombat(s, nextAttacker, nextTarget)
                                    }
                                } else {
                                    nextAttacker.hasAttackedThisTurn = true
                                    playerHeroDamageValue = nextAttacker.currentAttack
                                    playerHeroTakingDamage = true
                                    val updatedPlayerModel = s.player.copy(currentHp = s.player.currentHp - nextAttacker.currentAttack)
                                    return@update s.copy(player = updatedPlayerModel)
                                }
                            }
                            s.copy(player = s.player.copy(board = pBoard), opponent = s.opponent.copy(board = oBoard))
                        }
                    }

                    // Выключаем анимации текущей атаки
                    updateCardAnimation(activeAttacker.id, isAttacking = false)

                    delay(500) // Даем рассмотреть цифры урона
                    playerHeroTakingDamage = false

                    // Анимация смерти для погибших существ
                    _gameState.update { currentState ->
                        currentState?.let { s ->
                            val pBoard = s.player.board.map { it.copy() }.toMutableList()
                            val oBoard = s.opponent.board.map { it.copy() }.toMutableList()

                            pBoard.find { it.id == opponentTargetId }?.let {
                                it.isTakingDamage = false
                                if (it.currentHealth <= 0) it.isDying = true
                            }
                            oBoard.find { it.id == activeAttacker.id }?.let {
                                it.isTakingDamage = false
                                if (it.currentHealth <= 0) it.isDying = true
                            }
                            s.copy(player = s.player.copy(board = pBoard), opponent = s.opponent.copy(board = oBoard))
                        }
                    }

                    opponentAttackerId = null
                    opponentTargetId = null
                    isOpponentTargetingHero = false

                    val hasDeaths = _gameState.value?.player?.board?.any { it.isDying } == true ||
                            _gameState.value?.opponent?.board?.any { it.isDying } == true
                    if (hasDeaths) delay(400)

                    // Окончательное удаление погибших существ со стола
                    _gameState.update { currentState ->
                        currentState?.let { s ->
                            val pBoard = s.player.board.filter { it.currentHealth > 0 }.toMutableList()
                            val oBoard = s.opponent.board.filter { it.currentHealth > 0 }.toMutableList()

                            s.copy(player = s.player.copy(board = pBoard), opponent = s.opponent.copy(board = oBoard)).apply {
                                if (player.currentHp <= 0 || opponent.currentHp <= 0) {
                                    this.isGameOver = true
                                    this.winnerName = if (opponent.currentHp <= 0) player.name else opponent.name
                                }
                            }
                        }
                    }
                    delay(400)
                }
            }

            delay(600)

            // ФАЗА ИИ 3: Передача хода игроку
            _gameState.update { currentState ->
                currentState?.let { state ->
                    val updatedState = state.copy()
                    GameEngine.endTurn(updatedState)

                    // БЕЗОПАСНЫЙ ФИКС: Ход вернулся к игроку — будим все ваши карты на столе для нового раунда
                    val activePlayer = updatedState.player.copy(
                        board = updatedState.player.board.map { card ->
                            card.copy(isSleeping = false, hasAttackedThisTurn = false)
                        }.toMutableList()
                    )

                    updatedState.copy(player = activePlayer)
                }
            }
        }
    }
}