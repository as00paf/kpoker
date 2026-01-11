package com.pafoid.kpoker.network

import com.pafoid.kpoker.domain.model.BettingAction
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class PokerServer {
    private val sessions = ConcurrentHashMap<String, DefaultWebSocketServerSession>()
    private val rooms = ConcurrentHashMap<String, Room>()
    private val playerToRoom = ConcurrentHashMap<String, String>() // PlayerID to RoomID
    private val playerNames = ConcurrentHashMap<String, String>()
    private val mutex = Mutex()
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun handleConnection(session: DefaultWebSocketServerSession) {
        val playerId = UUID.randomUUID().toString()
        sessions[playerId] = session

        try {
            // Initial send room list
            sendRoomList(session)

            for (frame in session.incoming) {
                if (frame is Frame.Text) {
                    val text = frame.readText()
                    val message = try {
                        json.decodeFromString<GameMessage>(text)
                    } catch (e: Exception) { null }

                    when (message) {
                        is GameMessage.CreateRoom -> handleCreateRoom(playerId, message.roomName)
                        is GameMessage.JoinRoom -> handleJoinRoom(playerId, message.roomId, message.playerName)
                        is GameMessage.LeaveRoom -> handleLeaveRoom(playerId)
                        is GameMessage.Action -> handleAction(playerId, message.action)
                        is GameMessage.StartGame -> handleStartGame(playerId)
                        else -> {}
                    }
                }
            }
        } finally {
            handleDisconnect(playerId)
        }
    }

    private suspend fun handleCreateRoom(playerId: String, name: String) = mutex.withLock {
        val roomId = UUID.randomUUID().toString()
        val room = Room(roomId, name)
        rooms[roomId] = room
        broadcastRoomList()
    }

    private suspend fun handleJoinRoom(playerId: String, roomId: String, playerName: String) = mutex.withLock {
        val room = rooms[roomId] ?: return@withLock
        playerNames[playerId] = playerName
        playerToRoom[playerId] = roomId
        room.addPlayer(playerId, playerName, sessions[playerId]!!)
        broadcastRoomList()
    }

    private suspend fun handleLeaveRoom(playerId: String) = mutex.withLock {
        val roomId = playerToRoom.remove(playerId) ?: return@withLock
        rooms[roomId]?.removePlayer(playerId)
        broadcastRoomList()
    }

    private suspend fun handleAction(playerId: String, action: BettingAction) {
        val roomId = playerToRoom[playerId] ?: return
        val room = rooms[roomId] ?: return
        
        // Validation: Is it this player's turn?
        val state = room.engine.getState()
        if (state.activePlayer?.id != playerId) {
            sessions[playerId]?.send(Frame.Text(json.encodeToString(GameMessage.Error("Not your turn"))))
            return
        }

        room.handleAction(playerId, action)
    }

    private suspend fun handleStartGame(playerId: String) {
        val roomId = playerToRoom[playerId] ?: return
        rooms[roomId]?.startGame()
    }

    private suspend fun handleDisconnect(playerId: String) {
        handleLeaveRoom(playerId)
        sessions.remove(playerId)
    }

    private suspend fun sendRoomList(session: DefaultWebSocketServerSession) {
        val info = rooms.values.map { it.getInfo() }
        session.send(Frame.Text(json.encodeToString(GameMessage.RoomList(info))))
    }

    private suspend fun broadcastRoomList() {
        val info = rooms.values.map { it.getInfo() }
        val msg = json.encodeToString(GameMessage.RoomList(info))
        sessions.values.forEach { it.send(Frame.Text(msg)) }
    }
}