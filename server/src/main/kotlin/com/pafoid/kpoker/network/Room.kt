package com.pafoid.kpoker.network

import com.pafoid.kpoker.domain.engine.AiService
import com.pafoid.kpoker.domain.engine.GameEngine
import com.pafoid.kpoker.domain.model.BettingAction
import com.pafoid.kpoker.getCurrentTimeMillis
import io.ktor.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.concurrent.ConcurrentHashMap

class Room(
    val id: String,
    val name: String,
    val hostId: String,
    val difficulty: AiDifficulty = AiDifficulty.MEDIUM,
    private val authService: AuthService,
    private val playerBankrolls: ConcurrentHashMap<String, Long>
) {
    val engine = GameEngine()
    private val playerSessions = ConcurrentHashMap<String, WebSocketSession>()
    private val aiPlayerIds = mutableSetOf<String>()
    private val mutex = Mutex()
    private val json = Json { ignoreUnknownKeys = true }
    private val scope = CoroutineScope(Dispatchers.Default)

    init {
        startTimeoutCheckLoop()
    }

    private fun startTimeoutCheckLoop() {
        scope.launch {
            while (true) {
                delay(1000)
                mutex.withLock {
                    val oldActivePlayerId = engine.getState().activePlayer?.id
                    engine.checkTimeouts()
                    val newActivePlayerId = engine.getState().activePlayer?.id
                    
                    if (oldActivePlayerId != newActivePlayerId) {
                        broadcastState()
                        checkAiTurn()
                    }
                }
            }
        }
    }

    suspend fun addPlayer(playerId: String, playerName: String, bankroll: Long, session: WebSocketSession) = mutex.withLock {
        playerSessions[playerId] = session
        engine.addPlayer(playerId, playerName, bankroll) // Use provided bankroll as initial chips
        broadcastState()
    }

    suspend fun addAiPlayer(id: String, name: String) = mutex.withLock {
        aiPlayerIds.add(id)
        engine.addPlayer(id, name, 1000)
        broadcastState()
    }

    suspend fun removePlayer(playerId: String) = mutex.withLock {
        playerSessions.remove(playerId)
        // Fold if active?
        if (engine.getState().activePlayer?.id == playerId) {
            engine.handleAction(playerId, BettingAction.Fold)
        }
        broadcastState()
        checkAiTurn()
    }

    suspend fun handleAction(playerId: String, action: BettingAction) = mutex.withLock {
        // Capture initial chips before the action, which might lead to SHOWDOWN
        val initialPlayerChips = engine.getState().players.associate { it.id to it.chips }

        engine.handleAction(playerId, action)
        broadcastState()
        
        val currentState = engine.getState()
        if (currentState.stage == com.pafoid.kpoker.domain.model.GameStage.SHOWDOWN) {
            val nextStart = getCurrentTimeMillis() + 5000 // Reduced to 5 seconds
            engine.updateNextHandTime(nextStart)
            broadcastState() // Broadcast state with SHOWDOWN info before delaying for next hand
            
            scope.launch {
                delay(5000) // Wait for the delay before starting the new hand
                mutex.withLock {
                    // Check if still in SHOWDOWN before starting new hand, to avoid race conditions
                    if (engine.getState().stage == com.pafoid.kpoker.domain.model.GameStage.SHOWDOWN) {
                        // Resolve hand and update persistent bankrolls
                        val resolvedState = engine.getState() // Get state *after* hand resolution by engine
                        resolvedState.players.forEach { player ->
                            val initialChips = initialPlayerChips[player.id] ?: 0L
                            val chipChange = player.chips - initialChips
                            
                            if (chipChange != 0L) {
                                // Update persistent bankroll
                                authService.updateUserBankroll(player.id, chipChange)
                                // Update in-memory bankroll for current session
                                playerBankrolls[player.id] = player.chips
                            }
                        }
                        roomStartGame()
                    }
                }
            }
        } else {
            checkAiTurn()
        }
    }
    
    suspend fun startGame() = mutex.withLock {
        roomStartGame()
    }

    private suspend fun roomStartGame() {
        engine.startNewHand()
        broadcastState()
        checkAiTurn()
    }

    private fun checkAiTurn() {
        val state = engine.getState()
        val activePlayer = state.activePlayer ?: return
        
        if (aiPlayerIds.contains(activePlayer.id)) {
            scope.launch {
                // Ensure AI plays within a reasonable time (max 5s, but AiService uses 1-3s)
                val action = AiService.decideAction(state, activePlayer.id, difficulty)
                handleAction(activePlayer.id, action)
            }
        }
    }

    suspend fun broadcastState() {
        val state = engine.getState()
        val message = json.encodeToString<GameMessage>(GameMessage.StateUpdate(state))
        playerSessions.values.forEach { session ->
            try {
                session.send(Frame.Text(message))
            } catch (e: Exception) {}
        }
    }

    fun getInfo() = RoomInfo(id, name, engine.getState().players.size, engine.getState().stage != com.pafoid.kpoker.domain.model.GameStage.WAITING)
    
    fun hasPlayer(playerId: String) = playerSessions.containsKey(playerId)

    fun dispose() {
        scope.cancel()
    }
}