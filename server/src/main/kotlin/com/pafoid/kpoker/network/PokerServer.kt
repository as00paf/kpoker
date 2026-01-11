package com.pafoid.kpoker.network

import com.pafoid.kpoker.domain.engine.GameEngine
import com.pafoid.kpoker.domain.model.GameStage
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class PokerServer {
    private val engine = GameEngine()
    private val sessions = ConcurrentHashMap<String, DefaultWebSocketServerSession>()
    private val mutex = Mutex()
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun handleConnection(session: DefaultWebSocketServerSession) {
        val playerId = UUID.randomUUID().toString()
        sessions[playerId] = session

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
                        is GameMessage.Join -> handleJoin(playerId, message.playerName)
                        is GameMessage.Action -> handleAction(playerId, message)
                        else -> {
                            session.send(Frame.Text(json.encodeToString(GameMessage.Error("Invalid message"))))
                        }
                    }
                }
            }
        } finally {
            sessions.remove(playerId)
            // Handle player disconnection logic (e.g., fold or wait)
        }
    }

    private suspend fun handleJoin(playerId: String, playerName: String) = mutex.withLock {
        engine.addPlayer(playerId, playerName, 1000)
        broadcastState()
        
        // Auto-start for now if 2+ players
        if (engine.getState().stage == GameStage.WAITING && engine.getState().players.size >= 2) {
            engine.startNewHand()
            broadcastState()
        }
    }

    private suspend fun handleAction(playerId: String, actionMsg: GameMessage.Action) = mutex.withLock {
        engine.handleAction(playerId, actionMsg.action)
        broadcastState()
    }

    private suspend fun broadcastState() {
        val state = engine.getState()
        val message = json.encodeToString(GameMessage.StateUpdate(state))
        sessions.values.forEach { session ->
            try {
                session.send(Frame.Text(message))
            } catch (e: Exception) {
                // Session might be closed
            }
        }
    }
}
