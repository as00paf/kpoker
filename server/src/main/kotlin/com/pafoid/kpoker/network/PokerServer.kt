package com.pafoid.kpoker.network

import com.pafoid.kpoker.domain.engine.GameEngine
import com.pafoid.kpoker.domain.model.BettingAction
import com.pafoid.kpoker.domain.model.GameStage
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class PokerServer {
    private val engine = GameEngine()
    private val sessions = ConcurrentHashMap<String, DefaultWebSocketServerSession>()
    private val playerToSession = ConcurrentHashMap<String, String>() // PlayerID to SessionID
    private val mutex = Mutex()
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun handleConnection(session: DefaultWebSocketServerSession) {
        val sessionId = UUID.randomUUID().toString()
        var boundPlayerId: String? = null
        sessions[sessionId] = session

        try {
            for (frame in session.incoming) {
                if (frame is Frame.Text) {
                    val text = frame.readText()
                    val message = try {
                        json.decodeFromString<GameMessage>(text)
                    } catch (e: Exception) {
                        null
                    }

                    when (message) {
                        is GameMessage.Join -> {
                            boundPlayerId = sessionId // In this simple version, PlayerID = SessionID
                            handleJoin(sessionId, message.playerName)
                        }
                        is GameMessage.Action -> {
                            if (boundPlayerId != null) handleAction(boundPlayerId, message)
                        }
                        is GameMessage.StartGame -> handleStartGame()
                        else -> {
                            session.send(Frame.Text(json.encodeToString(GameMessage.Error("Invalid message"))))
                        }
                    }
                }
            }
        } finally {
            sessions.remove(sessionId)
            if (boundPlayerId != null) {
                handleDisconnect(boundPlayerId)
            }
        }
    }

    private suspend fun handleJoin(playerId: String, playerName: String) = mutex.withLock {
        engine.addPlayer(playerId, playerName, 1000)
        broadcastState()
    }

    private suspend fun handleStartGame() = mutex.withLock {
        if (engine.getState().stage == GameStage.WAITING && engine.getState().players.size >= 2) {
            engine.startNewHand()
            broadcastState()
        }
    }

    private suspend fun handleAction(playerId: String, actionMsg: GameMessage.Action) = mutex.withLock {
        engine.handleAction(playerId, actionMsg.action)
        broadcastState()
        
        if (engine.getState().stage == GameStage.SHOWDOWN) {
            // Hand finished, schedule next hand
            scheduleNextHand()
        }
    }

    private suspend fun handleDisconnect(playerId: String) = mutex.withLock {
        // If it's their turn, fold them
        if (engine.getState().activePlayerIndex != -1) {
            val activePlayer = engine.getState().activePlayer
            if (activePlayer?.id == playerId) {
                engine.handleAction(playerId, BettingAction.Fold)
                broadcastState()
            }
        }
        // In a real app, we might keep them in the game but auto-fold until they reconnect or time out
    }

    private suspend fun scheduleNextHand() {
        // We shouldn't hold the mutex while delaying, so we'll launch a new coroutine or handle carefully
        // But for this simple implementation, let's just wait then start
        kotlinx.coroutines.GlobalScope.let { 
            // Better to use a proper scope, but this is a CLI server
        }
        
        // Actually, let's just provide a manual start or a simple delay
        // We'll use a separate check to see if we should start
        delay(5000) // Wait 5 seconds for players to see the result
        mutex.withLock {
            if (engine.getState().stage == GameStage.SHOWDOWN) {
                engine.startNewHand()
                broadcastState()
            }
        }
    }

    private suspend fun broadcastState() {
        val state = engine.getState()
        val message = json.encodeToString(GameMessage.StateUpdate(state))
        sessions.values.forEach { session ->
            try {
                session.send(Frame.Text(message))
            } catch (e: Exception) {
            }
        }
    }
}
