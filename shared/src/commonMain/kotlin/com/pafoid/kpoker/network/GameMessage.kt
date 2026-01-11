package com.pafoid.kpoker.network

import com.pafoid.kpoker.domain.model.BettingAction
import com.pafoid.kpoker.domain.model.GameState
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
sealed class GameMessage {
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
