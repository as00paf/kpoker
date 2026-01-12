package com.pafoid.kpoker.network

import com.pafoid.kpoker.domain.model.BettingAction
import com.pafoid.kpoker.getCurrentTimeMillis
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class PokerServer {

    private val authService = AuthService()

    private val sessions = ConcurrentHashMap<String, WebSocketSession>() // SessionID to WebSocketSession

    private val authenticatedPlayers = ConcurrentHashMap<String, String>() // SessionID to PlayerID

    private val rooms = ConcurrentHashMap<String, Room>()

    private val playerToRoom = ConcurrentHashMap<String, String>() // PlayerID to RoomID

    private val playerNames = ConcurrentHashMap<String, String>()

    private val mutex = Mutex()

    private val json = Json { ignoreUnknownKeys = true }

    private val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Default)



    suspend fun handleConnection(session: WebSocketSession) {

        val sessionId = UUID.randomUUID().toString()

        sessions[sessionId] = session



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



                                            is GameMessage.Register -> handleRegister(sessionId, message)



                                            is GameMessage.Login -> handleLogin(sessionId, message)



                                            is GameMessage.CreateRoom -> {



                                                val playerId = authenticatedPlayers[sessionId]



                                                if (playerId != null) handleCreateRoom(playerId, message.roomName)



                                                else sendError(session, "Not authenticated")



                                            }



                                            is GameMessage.CreateSinglePlayerRoom -> {



                                                val playerId = authenticatedPlayers[sessionId]



                                                if (playerId != null) handleCreateSinglePlayerRoom(playerId)



                                                else sendError(session, "Not authenticated")



                                            }



                                            is GameMessage.JoinRoom -> {

                            val playerId = authenticatedPlayers[sessionId]

                            if (playerId != null) handleJoinRoom(playerId, message.roomId, message.playerName)

                            else sendError(session, "Not authenticated")

                        }

                        is GameMessage.LeaveRoom -> {

                            val playerId = authenticatedPlayers[sessionId]

                            if (playerId != null) handleLeaveRoom(playerId)

                        }

                                                is GameMessage.Action -> {

                                                    val playerId = authenticatedPlayers[sessionId]

                                                    if (playerId != null) handleAction(playerId, message.action)

                                                }

                                                is GameMessage.ChangePassword -> {

                                                    val playerId = authenticatedPlayers[sessionId]

                                                    if (playerId != null) handlePasswordChange(playerId, message.newPassword)

                                                }

                                                is GameMessage.StartGame -> {

                            val playerId = authenticatedPlayers[sessionId]

                            if (playerId != null) handleStartGame(playerId)

                        }

                        else -> {}

                    }

                }

            }

        } finally {

            val playerId = authenticatedPlayers.remove(sessionId)

            if (playerId != null) {

                handleDisconnect(playerId)

            }

            sessions.remove(sessionId)

        }

    }



    private suspend fun handleRegister(sessionId: String, msg: GameMessage.Register) {
        val (success, result) = authService.register(msg.username, msg.password)
        val session = sessions[sessionId] ?: return
        if (success && result != null) {
            authenticatedPlayers[sessionId] = result
            playerNames[result] = msg.username
            session.send(Frame.Text(json.encodeToString<GameMessage>(GameMessage.AuthResponse(true, "Registration successful", result))))
        } else {
            session.send(Frame.Text(json.encodeToString<GameMessage>(GameMessage.AuthResponse(false, result ?: "Unknown error"))))
        }
    }



    private suspend fun handleLogin(sessionId: String, msg: GameMessage.Login) {

        val (success, result) = authService.login(msg.username, msg.password)

        val session = sessions[sessionId] ?: return

        if (success && result != null) {

            authenticatedPlayers[sessionId] = result

            playerNames[result] = msg.username

            session.send(Frame.Text(json.encodeToString<GameMessage>(GameMessage.AuthResponse(true, "Login successful", result))))

        } else {

            session.send(Frame.Text(json.encodeToString<GameMessage>(GameMessage.AuthResponse(false, result ?: "Unknown error"))))

        }

    }



    private suspend fun sendError(session: WebSocketSession, error: String) {

        session.send(Frame.Text(json.encodeToString<GameMessage>(GameMessage.Error(error))))

    }

    private suspend fun handleCreateRoom(playerId: String, name: String) = mutex.withLock {
        val roomId = UUID.randomUUID().toString()
        val room = Room(roomId, name)
        rooms[roomId] = room
        broadcastRoomList()
    }

    private suspend fun handleCreateSinglePlayerRoom(playerId: String) = mutex.withLock {
        val roomId = UUID.randomUUID().toString()
        val room = Room(roomId, "${playerNames[playerId]}'s Single Player Game")
        rooms[roomId] = room
        
        // Add human
        playerToRoom[playerId] = roomId
        val sessionId = authenticatedPlayers.entries.find { it.value == playerId }?.key ?: return@withLock
        room.addPlayer(playerId, playerNames[playerId]!!, sessions[sessionId]!!)
        
        // Add AI
        val aiId = "ai_${UUID.randomUUID()}"
        room.addAiPlayer(aiId, "The House")
        
        broadcastRoomList()
    }

    private suspend fun handleJoinRoom(playerId: String, roomId: String, playerName: String) = mutex.withLock {
        val room = rooms[roomId] ?: return@withLock
        playerToRoom[playerId] = roomId
        // Find session for this player
        val sessionId = authenticatedPlayers.entries.find { it.value == playerId }?.key ?: return@withLock
        val session = sessions[sessionId] ?: return@withLock
        
        room.addPlayer(playerId, playerName, session)
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
            val sessionId = authenticatedPlayers.entries.find { it.value == playerId }?.key
            if (sessionId != null) {
                sendError(sessions[sessionId]!!, "Not your turn")
            }
            return
        }

        room.handleAction(playerId, action)
        
        if (room.engine.getState().stage == com.pafoid.kpoker.domain.model.GameStage.SHOWDOWN) {
            // Set when next hand starts (40 seconds from now)
            val nextStart = getCurrentTimeMillis() + 40000
            room.engine.updateNextHandTime(nextStart)
            room.broadcastState()

            scope.launch {
                delay(40000)
                room.startGame()
            }
        }
    }

    private suspend fun handlePasswordChange(playerId: String, newPass: String) {
        val success = authService.changePassword(playerId, newPass)
        val sessionId = authenticatedPlayers.entries.find { it.value == playerId }?.key ?: return
        val session = sessions[sessionId] ?: return
        
        if (success) {
            session.send(Frame.Text(json.encodeToString<GameMessage>(GameMessage.AuthResponse(true, "Password changed successfully"))))
        } else {
            sendError(session, "Failed to change password")
        }
    }

    private suspend fun handleStartGame(playerId: String) {
        val roomId = playerToRoom[playerId] ?: return
        val room = rooms[roomId] ?: return
        room.startGame()
    }

    private suspend fun handleDisconnect(playerId: String) {
        handleLeaveRoom(playerId)
    }

    private suspend fun sendRoomList(session: WebSocketSession) {
        val info = rooms.values.map { it.getInfo() }
        session.send(Frame.Text(json.encodeToString<GameMessage>(GameMessage.RoomList(info))))
    }

    private suspend fun broadcastRoomList() {
        val info = rooms.values.map { it.getInfo() }
        val msg = json.encodeToString<GameMessage>(GameMessage.RoomList(info))
        sessions.values.forEach { it.send(Frame.Text(msg)) }
    }
}