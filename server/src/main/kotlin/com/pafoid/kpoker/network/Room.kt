package com.pafoid.kpoker.network

import com.pafoid.kpoker.domain.engine.AiService
import com.pafoid.kpoker.domain.engine.GameEngine
import com.pafoid.kpoker.domain.model.BettingAction
import io.ktor.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
    val difficulty: AiDifficulty = AiDifficulty.MEDIUM
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
                    engine.checkTimeouts()
                }
                broadcastState()
            }
        }
    }

    suspend fun addPlayer(playerId: String, playerName: String, session: WebSocketSession) = mutex.withLock {
        playerSessions[playerId] = session
        engine.addPlayer(playerId, playerName, 1000)
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
    }

    suspend fun handleAction(playerId: String, action: BettingAction) = mutex.withLock {
        engine.handleAction(playerId, action)
        broadcastState()
        
        if (engine.getState().stage == com.pafoid.kpoker.domain.model.GameStage.SHOWDOWN) {
            scope.launch {
                delay(40000) // 40 second wait before next hand
                roomStartGame()
            }
        }
        
        checkAiTurn()
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
}