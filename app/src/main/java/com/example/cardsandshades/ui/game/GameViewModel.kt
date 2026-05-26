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
import com.example.cardsandshades.sound.SoundManager
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

    private var autoTurnJob: Job? = null

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

    var isAutoBattleActive by mutableStateOf(false)
        private set

    var animationSpeed by mutableIntStateOf(1)
        private set

    fun cycleAnimationSpeed() {
        animationSpeed = if (animationSpeed >= 4) 1 else animationSpeed + 1
    }

    private fun getDelay(ms: Long): Long = ms / animationSpeed

    fun toggleAutoBattle() {
        isAutoBattleActive = !isAutoBattleActive
        val state = _gameState.value
        if (isAutoBattleActive && state != null && state.currentTurn == Turn.PLAYER && !state.isGameOver) {
            autoTurnJob?.cancel()
            autoTurnJob = executeGenericTurn(isOpponent = false)
        }
    }

    init {
        _gameState.value = null
        
        // Таймер времени в игре
        viewModelScope.launch {
            while (true) {
                delay(5000) // Раз в 5 сек
                MissionManager.tickPlaytime(5)
            }
        }
    }

    fun startNewGame(level: LevelModel) {
        autoTurnJob?.cancel()
        currentLevel = level

        val musicName = level.musicRes ?: "battle_music_default"
        SoundManager.playMusicByName(null, musicName)

        // PLAYER DECK LOGIC
        val sourceDeck = if (UserProfile.selectedDeck.isNotEmpty()) {
            UserProfile.selectedDeck.toList()
        } else {
            UserProfile.collection.toList()
        }

        val shuffledSource = sourceDeck.shuffled().map { 
            val safe = it.deepCopy()
            CardCatalog.createCardInstance(safe.name)?.apply { reset() } ?: safe
        }

        val maxPlayerCards = level.opponentDeckPreset.size
        val playerDeck = shuffledSource.take(maxPlayerCards).toMutableList()

        // OPPONENT DECK LOGIC
        val opponentDeck = level.opponentDeckPreset.map { cardName ->
            (CardCatalog.createCardInstance(cardName) ?: CardCatalog.createCardInstance("card_shadow_recruit")!!).apply { reset() }
        }.toMutableList()

        opponentDeck.shuffle()

        val player = PlayerModel(
            name = "player_name",
            deck = playerDeck,
            hand = mutableListOf(),
            board = arrayOfNulls(5)
        )

        val opponent = PlayerModel(
            name = level.opponentName,
            maxHp = level.opponentMaxHp,
            currentHp = level.opponentMaxHp,
            deck = opponentDeck,
            hand = mutableListOf(),
            board = arrayOfNulls(5)
        )

        val initialState = GameState(
            player = player,
            opponent = opponent,
            currentTurn = Turn.PLAYER,
            turnNumber = 1,
            isGameOver = false,
            winnerName = null,
            logHistory = mutableListOf(LogEntry("battle_start_msg", LogType.SYSTEM, 1))
        )

        repeat(4) {
            GameEngine.drawCard(initialState.player, initialState)
            GameEngine.drawCard(initialState.opponent, initialState)
        }

        GameEngine.startTurn(initialState)

        _gameState.value = initialState
        
        if (isAutoBattleActive) {
            autoTurnJob = executeGenericTurn(isOpponent = false)
        }
    }

    fun claimRewardsAndExit(isPlayerWin: Boolean) {
        autoTurnJob?.cancel()
        
        // Count participation only when battle is finished (win or loss)
        MissionManager.updateProgress("daily_battles", 1, false)
        MissionManager.updateProgress("weekly_battles", 1, true)

        if (isPlayerWin) {
            SoundManager.playSoundByName(null, "victory")
            currentLevel?.let { level ->
                val isFirstTime = level.id >= UserProfile.maxUnlockedLevel.value
                val rewards = if (isFirstTime) level.firstTimeReward else level.repeatReward
                UserProfile.applyReward(rewards)

                if (level.id == UserProfile.maxUnlockedLevel.value) {
                    UserProfile.maxUnlockedLevel.value = level.id + 1
                }
                UserProfile.save()
            }
            MissionManager.updateProgress("daily_win", 1, false)
            MissionManager.updateProgress("weekly_win_streak", 1, true)
        } else {
            SoundManager.playSoundByName(null, "defeat")
        }
        _gameState.value = null
        SoundManager.startMusic(null)
    }

    fun restartCurrentGame() {
        currentLevel?.let { startNewGame(it) }
    }

    fun playCard(card: CardModel, slotIndex: Int): Boolean {
        var isPlayed = false
        _gameState.update { currentState ->
            currentState?.deepCopy()?.apply {
                if (currentTurn == Turn.PLAYER) {
                    val cardInHand = player.hand.find { it.id == card.id }
                    if (cardInHand != null && player.currentMana >= cardInHand.manaCost && player.board[slotIndex] == null) {
                        GameEngine.playCard(this, cardInHand, slotIndex)
                        SoundManager.playSoundByName(null, cardInHand.playSound)
                        logHistory.add(LogEntry("battle_card_played|player|card_${cardInHand.name}", LogType.PLAYER, turnNumber))
                        isPlayed = true
                    }
                }
            } ?: currentState
        }
        return isPlayed
    }

    fun attackEnemyCard(attacker: CardModel, target: CardModel) {
        val state = _gameState.value ?: return
        if (state.isAnimating || state.currentTurn != Turn.PLAYER) return

        viewModelScope.launch {
            _gameState.update { it?.copy(isAnimating = true) }
            try {
                updateCardAnimation(attacker.id, isAttacking = true)
                delay(getDelay(200))

                SoundManager.playSoundByName(null, attacker.attackSound)

                _gameState.update { currentState ->
                    currentState?.deepCopy()?.apply {
                        val aCard = player.board.filterNotNull().find { it.id == attacker.id }
                        val tCard = opponent.board.filterNotNull().find { it.id == target.id }

                        if (aCard != null && tCard != null) {
                            tCard.isTakingDamage = true
                            aCard.isTakingDamage = true
                            if (aCard.groups.contains(GroupTag.MELEE)) {
                                tCard.isAttacking = true
                            }
                            
                            val atkVal = aCard.currentAttack
                            val retVal = if (aCard.groups.contains(GroupTag.MELEE)) tCard.currentAttack else 0
                            
                            GameEngine.calculateCombat(this, aCard, tCard)
                            
                            logHistory.add(LogEntry("battle_card_attack|player|card_${aCard.name}|card_${tCard.name}|$atkVal", LogType.PLAYER, turnNumber))
                            if (aCard.groups.contains(GroupTag.MELEE)) {
                                logHistory.add(LogEntry("battle_card_retaliate|opponent|card_${tCard.name}|card_${aCard.name}|$retVal", LogType.OPPONENT, turnNumber))
                            }
                        }
                    }
                }
                
                delay(getDelay(400)) // Время на показ ударов

                _gameState.update { currentState ->
                    currentState?.deepCopy()?.apply {
                        player.board.filterNotNull().forEach { it.isAttacking = false }
                        opponent.board.filterNotNull().forEach { it.isAttacking = false }
                        player.board.filterNotNull().forEach { if (it.lastDamageTaken > 0) it.isTakingDamage = true }
                        opponent.board.filterNotNull().forEach { if (it.lastDamageTaken > 0) it.isTakingDamage = true }
                    }
                }
                
                delay(getDelay(400))

                _gameState.update { currentState ->
                    val newState = currentState?.deepCopy()
                    newState?.apply {
                        player.board.filterNotNull().forEach { card ->
                            card.isTakingDamage = false
                            if (card.currentHealth <= 0) {
                                card.isDying = true
                                SoundManager.playSoundByName(null, card.deathSound)
                                // Log card death
                                logHistory.add(LogEntry("battle_card_died|player|card_${card.name}", LogType.PLAYER, turnNumber))
                            }
                        }
                        opponent.board.filterNotNull().forEach { card ->
                            card.isTakingDamage = false
                            if (card.currentHealth <= 0) {
                                card.isDying = true
                                SoundManager.playSoundByName(null, card.deathSound)
                                // Log card death
                                logHistory.add(LogEntry("battle_card_died|opponent|card_${card.name}", LogType.OPPONENT, turnNumber))
                            }
                        }
                    }
                    newState
                }

                val hasDeaths = _gameState.value?.player?.board?.filterNotNull()?.any { it.isDying } == true ||
                        _gameState.value?.opponent?.board?.filterNotNull()?.any { it.isDying } == true
                if (hasDeaths) {
                    delay(getDelay(400))
                }

                _gameState.update { currentState ->
                    currentState?.deepCopy()?.apply {
                        for (i in player.board.indices) {
                            if (player.board[i]?.currentHealth ?: 1 <= 0) player.board[i] = null
                        }
                        for (i in opponent.board.indices) {
                            if (opponent.board[i]?.currentHealth ?: 1 <= 0) opponent.board[i] = null
                        }
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

    private fun updateCardAnimation(cardId: String, isAttacking: Boolean = false) {
        _gameState.update { currentState ->
            currentState?.let { s ->
                val pBoard = s.player.board.map { if (it?.id == cardId) it.copy(isAttacking = isAttacking) else it }.toTypedArray()
                val oBoard = s.opponent.board.map { if (it?.id == cardId) it.copy(isAttacking = isAttacking) else it }.toTypedArray()
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
                delay(getDelay(200))
                SoundManager.playSoundByName(null, attacker.attackSound)

                opponentHeroDamageValue = attacker.currentAttack
                opponentHeroTakingDamage = true

                _gameState.update { currentState ->
                    currentState?.deepCopy()?.apply {
                        val aCard = player.board.filterNotNull().find { it.id == attacker.id }
                        if (aCard != null) {
                            GameEngine.attackHero(this, aCard)
                            logHistory.add(LogEntry("battle_attack_hero|player|${aCard.currentAttack}", LogType.PLAYER, turnNumber))
                        }
                    }
                }

                updateCardAnimation(attacker.id, isAttacking = false)
                delay(getDelay(500))
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
                    logHistory.add(LogEntry("battle_round_end|${turnNumber}", LogType.SYSTEM, turnNumber))
                    GameEngine.endTurn(this)
                }
            } ?: currentState
        }

        val state = _gameState.value
        if (state != null && state.currentTurn == Turn.OPPONENT && !state.isGameOver) {
            autoTurnJob?.cancel()
            autoTurnJob = executeGenericTurn(isOpponent = true)
        } else if (state != null && state.currentTurn == Turn.PLAYER && isAutoBattleActive && !state.isGameOver) {
            autoTurnJob?.cancel()
            autoTurnJob = executeGenericTurn(isOpponent = false)
        }
    }

    private fun executeGenericTurn(isOpponent: Boolean): Job = viewModelScope.launch {
        delay(getDelay(1000))
        if (!isActive) return@launch

        _gameState.update { currentState ->
            currentState?.deepCopy()?.apply {
                val actor = if (isOpponent) opponent else player
                val actorKey = if (isOpponent) "opponent" else "player"
                val cardsInHand = actor.hand.sortedByDescending { it.manaCost }
                for (card in cardsInHand) {
                    val emptySlot = actor.board.indexOfFirst { it == null }
                    if (emptySlot != -1) {
                        GameEngine.playCard(this, card, emptySlot)
                        SoundManager.playSoundByName(null, card.playSound)
                        logHistory.add(LogEntry("battle_card_played|$actorKey|card_${card.name}", if (isOpponent) LogType.OPPONENT else LogType.PLAYER, turnNumber))
                    }
                }
            }
        }

        delay(getDelay(500))
        if (!isActive) return@launch

        val state = _gameState.value
        if (state != null && !state.isGameOver) {
            val actor = if (isOpponent) state.opponent else state.player
            val attackerCards = actor.board.filterNotNull().toList()

            for (attacker in attackerCards) {
                if (!isActive) break
                val currentGameState = _gameState.value ?: break
                val currentActor = if (isOpponent) currentGameState.opponent else currentGameState.player
                val activeAttacker = currentActor.board.filterNotNull().find { it.id == attacker.id } ?: continue

                if (activeAttacker.isSleeping || activeAttacker.hasAttackedThisTurn) continue

                val defender = if (isOpponent) currentGameState.player else currentGameState.opponent
                val defenderBoard = defender.board.filterNotNull()
                val validEnemyCards = defenderBoard.filter { enemyCard ->
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

                delay(getDelay(1000))
                if (!isActive) break

                updateCardAnimation(activeAttacker.id, isAttacking = true)
                delay(getDelay(200))
                SoundManager.playSoundByName(null, activeAttacker.attackSound)

                _gameState.update { currentState ->
                    val newState = currentState?.deepCopy()
                    newState?.apply {
                        val cActor = if (isOpponent) opponent else player
                        val cDefender = if (isOpponent) player else opponent
                        val nextAttacker = cActor.board.filterNotNull().find { it.id == activeAttacker.id }
                        if (nextAttacker != null) {
                            val actorKey = if (isOpponent) "opponent" else "player"
                            val opponentKey = if (isOpponent) "player" else "opponent"
                            
                            if (!isOpponentTargetingHero) {
                                val nextTarget = cDefender.board.filterNotNull().find { it.id == opponentTargetId }
                                if (nextTarget != null) {
                                    nextTarget.isTakingDamage = true
                                    nextAttacker.isTakingDamage = true
                                    if (nextAttacker.groups.contains(GroupTag.MELEE)) {
                                        nextTarget.isAttacking = true
                                    }
                                    val atkVal = nextAttacker.currentAttack
                                    val retVal = if (nextAttacker.groups.contains(GroupTag.MELEE)) nextTarget.currentAttack else 0
                                    GameEngine.calculateCombat(this, nextAttacker, nextTarget)
                                    logHistory.add(LogEntry("battle_card_attack|$actorKey|card_${nextAttacker.name}|card_${nextTarget.name}|$atkVal", if (isOpponent) LogType.OPPONENT else LogType.PLAYER, turnNumber))
                                    if (nextAttacker.groups.contains(GroupTag.MELEE)) {
                                        logHistory.add(LogEntry("battle_card_retaliate|$opponentKey|card_${nextTarget.name}|card_${nextAttacker.name}|$retVal", if (isOpponent) LogType.PLAYER else LogType.OPPONENT, turnNumber))
                                    }
                                }
                            } else {
                                if (isOpponent) {
                                    playerHeroDamageValue = nextAttacker.currentAttack
                                    playerHeroTakingDamage = true
                                } else {
                                    opponentHeroDamageValue = nextAttacker.currentAttack
                                    opponentHeroTakingDamage = true
                                }
                                GameEngine.attackHero(this, nextAttacker)
                                logHistory.add(LogEntry("battle_attack_hero|$actorKey|${nextAttacker.currentAttack}", if (isOpponent) LogType.OPPONENT else LogType.PLAYER, turnNumber))
                            }
                        }
                    }
                    newState
                }

                delay(getDelay(400))
                _gameState.update { currentState ->
                    currentState?.deepCopy()?.apply {
                        player.board.filterNotNull().forEach { it.isAttacking = false }
                        opponent.board.filterNotNull().forEach { it.isAttacking = false }
                        playerHeroTakingDamage = false
                        opponentHeroTakingDamage = false
                    }
                }
                
                delay(getDelay(400))
                _gameState.update { currentState ->
                    val newState = currentState?.deepCopy()
                    newState?.apply {
                        player.board.filterNotNull().forEach { card ->
                            card.isTakingDamage = false
                            if (card.currentHealth <= 0) {
                                card.isDying = true
                                SoundManager.playSoundByName(null, card.deathSound)
                                // Log card death
                                logHistory.add(LogEntry("battle_card_died|player|card_${card.name}", LogType.PLAYER, turnNumber))
                            }
                        }
                        opponent.board.filterNotNull().forEach { card ->
                            card.isTakingDamage = false
                            if (card.currentHealth <= 0) {
                                card.isDying = true
                                SoundManager.playSoundByName(null, card.deathSound)
                                // Log card death
                                logHistory.add(LogEntry("battle_card_died|opponent|card_${card.name}", LogType.OPPONENT, turnNumber))
                            }
                        }
                    }
                    newState
                }

                opponentAttackerId = null
                opponentTargetId = null
                isOpponentTargetingHero = false

                val hasDeaths = _gameState.value?.player?.board?.filterNotNull()?.any { it.isDying } == true ||
                        _gameState.value?.opponent?.board?.filterNotNull()?.any { it.isDying } == true
                if (hasDeaths) {
                    delay(getDelay(400))
                }

                _gameState.update { currentState ->
                    currentState?.deepCopy()?.apply {
                        for (i in player.board.indices) {
                            if ((player.board[i]?.currentHealth ?: 1) <= 0) player.board[i] = null
                        }
                        for (i in opponent.board.indices) {
                            if ((opponent.board[i]?.currentHealth ?: 1) <= 0) opponent.board[i] = null
                        }
                        if (player.currentHp <= 0 || opponent.currentHp <= 0) {
                            isGameOver = true
                            winnerName = if (opponent.currentHp <= 0) player.name else opponent.name
                        }
                    }
                }
                delay(getDelay(400))
            }
        }

        if (!isActive) return@launch
        delay(getDelay(600))

        _gameState.update { currentState ->
            currentState?.deepCopy()?.apply {
                GameEngine.endTurn(this)
                if (currentTurn == Turn.PLAYER) {
                    logHistory.add(LogEntry("battle_round_start|${turnNumber}", LogType.SYSTEM, turnNumber))
                }
            }
        }
        
        val finalState = _gameState.value
        if (finalState != null && !finalState.isGameOver) {
            if (finalState.currentTurn == Turn.OPPONENT) {
                autoTurnJob = executeGenericTurn(isOpponent = true)
            } else if (finalState.currentTurn == Turn.PLAYER && isAutoBattleActive) {
                autoTurnJob = executeGenericTurn(isOpponent = false)
            }
        }
    }
}
