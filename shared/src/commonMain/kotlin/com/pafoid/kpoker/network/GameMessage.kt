package com.pafoid.kpoker.network

import com.pafoid.kpoker.domain.model.BettingAction
import com.pafoid.kpoker.domain.model.GameState
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
sealed class GameMessage {
    @Serializable
    @SerialName("register")
    data class Register(val username: String, val password: String) : GameMessage()

    @Serializable
    @SerialName("login")
    data class Login(val username: String, val password: String) : GameMessage()

    @Serializable
    @SerialName("auth_response")
    data class AuthResponse(val success: Boolean, val message: String, val playerId: String? = null) : GameMessage()

    @Serializable
    @SerialName("create_room")
    data class CreateRoom(val roomName: String) : GameMessage()

    @Serializable
    @SerialName("join_room")
    data class JoinRoom(val roomId: String, val playerName: String) : GameMessage()

    @Serializable
    @SerialName("leave_room")
    object LeaveRoom : GameMessage()

    @Serializable
    @SerialName("room_list")
    data class RoomList(val rooms: List<RoomInfo>) : GameMessage()

    @Serializable
    @SerialName("join")
    data class Join(val playerName: String) : GameMessage()

    @Serializable
    @SerialName("state_update")
    data class StateUpdate(val state: GameState) : GameMessage()

    @Serializable
    @SerialName("action")
    data class Action(val action: BettingAction) : GameMessage()

    @Serializable
    @SerialName("start_game")
    object StartGame : GameMessage()

    @Serializable
    @SerialName("error")
    data class Error(val message: String) : GameMessage()
}

@Serializable
data class RoomInfo(
    val id: String,
    val name: String,
    val playerCount: Int,
    val isStarted: Boolean
)
