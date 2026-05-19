package com.example.cardsandshades.ui.game

import androidx.compose.runtime.getValue
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
    var playerHeroDamageValue by mutableStateOf(0)
        private set

    var opponentHeroTakingDamage by mutableStateOf(false)
        private set
    var opponentHeroDamageValue by mutableStateOf(0)
        private set

    // Убрали принудительный вызов из init, чтобы игра не крашилась при старте до выбора уровня
    init {
        _gameState.value = null
    }

    // Инициализация матча для конкретного уровня кампании
    fun startNewGame(level: LevelModel) {
        currentLevel = level

        // Генерируем новые чистые колоды (глубокие копии объектов)
        val playerDeck = CardCatalog.generateTestDeck()
        val opponentDeck = CardCatalog.generateTestDeck()

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
            GameEngine.drawCard(initialState.player)
            GameEngine.drawCard(initialState.opponent)
        }

        // Начисляем стартовую ману для первого хода
        GameEngine.startTurn(initialState)

        _gameState.value = initialState
    }

    fun claimRewardsAndExit(isPlayerWin: Boolean) {
        if (isPlayerWin) {
            // Безопасное начисление золота через инкапсулированный профиль
            com.example.cardsandshades.model.UserProfile.gold.value += 50
        }
        // Сбрасываем стейт боя, подготавливая ViewModel к следующему матчу кампании
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

        // ВАЛИДАЦИЯ: Проверяем, спит ли карта и нет ли на поле врага Танков (Провокаторов)
        if (!com.example.cardsandshades.engine.GameEngine.canAttackTarget(state, attacker, target)) {
            // Если атака запрещена правилами ККИ, прерываем выполнение
            return
        }

        viewModelScope.launch {
            _gameState.update { it?.copy(isAnimating = true) }

            // 1. Анимация рывка атакующего вперед
            updateCardAnimation(attacker.id, isAttacking = true)
            delay(200)

            // 2. Встречный удар: фиксация урона, тряска и вылет цифр
            _gameState.update { currentState ->
                currentState?.let { s ->
                    val pBoard = s.player.board.map { it.copy() }.toMutableList()
                    val oBoard = s.opponent.board.map { it.copy() }.toMutableList()

                    val aCard = pBoard.find { it.id == attacker.id }
                    val tCard = oBoard.find { it.id == target.id }

                    if (aCard != null && tCard != null) {
                        // ФИКСАЦИЯ ОГРАНИЧЕНИЯ: Карта тратит свой лимит атаки на этот ход!
                        aCard.hasAttackedThisTurn = true

                        tCard.lastDamageTaken = aCard.currentAttack
                        aCard.lastDamageTaken = tCard.currentAttack

                        tCard.isTakingDamage = true
                        aCard.isTakingDamage = true

                        tCard.currentHealth -= aCard.currentAttack
                        aCard.currentHealth -= tCard.currentAttack
                    }
                    s.copy(player = s.player.copy(board = pBoard), opponent = s.opponent.copy(board = oBoard))
                }
            }
            updateCardAnimation(attacker.id, isAttacking = false)
            delay(300)

            // 3. Анимация смерти (падения)
            _gameState.update { currentState ->
                currentState?.let { s ->
                    val pBoard = s.player.board.map { it.copy() }.toMutableList()
                    val oBoard = s.opponent.board.map { it.copy() }.toMutableList()

                    pBoard.find { it.id == attacker.id }?.let {
                        it.isTakingDamage = false
                        if (it.currentHealth <= 0) it.isDying = true
                    }
                    oBoard.find { it.id == target.id }?.let {
                        it.isTakingDamage = false
                        if (it.currentHealth <= 0) it.isDying = true
                    }
                    s.copy(player = s.player.copy(board = pBoard), opponent = s.opponent.copy(board = oBoard))
                }
            }

            val hasDeaths = _gameState.value?.player?.board?.any { it.isDying } == true ||
                    _gameState.value?.opponent?.board?.any { it.isDying } == true
            if (hasDeaths) delay(400)

            // 4. Окончательное удаление карт со стола (Исправлено: добавлено .board)
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
                    updatedState
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

            delay(1200)

            // ФАЗА ИИ 2: Атака с пошаговой визуализацией
            val state = _gameState.value
            if (state != null && !state.isGameOver) {
                val attackerCards = state.opponent.board.toList()

                for (attacker in attackerCards) {
                    // Проверяем, живо ли еще атакующее существо ИИ
                    val activeAttacker = _gameState.value?.opponent?.board?.find { it.id == attacker.id } ?: continue
                    val hasTargets = _gameState.value?.player?.board?.isNotEmpty() == true

                    // 1. ИИ включает стрелку прицеливания
                    opponentAttackerId = activeAttacker.id
                    if (hasTargets) {
                        val target = _gameState.value?.player?.board?.random()
                        opponentTargetId = target?.id
                        isOpponentTargetingHero = false
                    } else {
                        opponentTargetId = null
                        isOpponentTargetingHero = true
                    }
                    delay(1000) // Игрок видит стрелку

                    // 2. Анимация рывка существа ИИ вперед
                    updateCardAnimation(activeAttacker.id, isAttacking = true)
                    delay(200)

                    // 3. Встречный удар: фиксация урона, тряска и вылет цифр
                    _gameState.update { currentState ->
                        currentState?.let { s ->
                            val pBoard = s.player.board.map { it.copy() }.toMutableList()
                            val oBoard = s.opponent.board.map { it.copy() }.toMutableList()

                            val nextAttacker = oBoard.find { it.id == activeAttacker.id }

                            if (nextAttacker != null) {
                                if (hasTargets) {
                                    val nextTarget = pBoard.find { it.id == opponentTargetId }
                                    if (nextTarget != null) {
                                        nextTarget.lastDamageTaken = nextAttacker.currentAttack
                                        nextAttacker.lastDamageTaken = nextTarget.currentAttack

                                        nextTarget.isTakingDamage = true
                                        nextAttacker.isTakingDamage = true

                                        nextTarget.currentHealth -= nextAttacker.currentAttack
                                        nextAttacker.currentHealth -= nextTarget.currentAttack
                                    }
                                } else {
                                    // Анимация удара ИИ в лицо игрока
                                    playerHeroDamageValue = nextAttacker.currentAttack
                                    playerHeroTakingDamage = true
                                    // Атака в лицо игрока (меняем только здоровье игрока)
                                    val updatedPlayerModel = s.player.copy(currentHp = s.player.currentHp - nextAttacker.currentAttack)
                                    return@update s.copy(player = updatedPlayerModel)
                                }
                            }
                            s.copy(player = s.player.copy(board = pBoard), opponent = s.opponent.copy(board = oBoard))
                        }
                    }
                    updateCardAnimation(activeAttacker.id, isAttacking = false)
                    opponentAttackerId = null
                    opponentTargetId = null
                    isOpponentTargetingHero = false

                    delay(500) // Даем рассмотреть урон по лицу игрока
                    playerHeroTakingDamage = false

                    // 4. Анимация смерти для погибших существ
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

                    val hasDeaths = _gameState.value?.player?.board?.any { it.isDying } == true ||
                            _gameState.value?.opponent?.board?.any { it.isDying } == true
                    if (hasDeaths) delay(400)

                    // 5. Окончательное удаление погибших существ со стола
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
                    delay(400) // Пауза перед атакой следующего существа ИИ
                }
            }

            delay(600)

            // ФАЗА ИИ 3: Передача хода игроку
            _gameState.update { currentState ->
                currentState?.let { state ->
                    val updatedState = state.copy()
                    GameEngine.endTurn(updatedState)
                    updatedState
                }
            }
        }
    }
}