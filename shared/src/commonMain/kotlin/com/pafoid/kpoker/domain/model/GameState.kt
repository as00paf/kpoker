package com.pafoid.kpoker.domain.model

enum class GameStage {
    WAITING,
    PRE_FLOP,
    FLOP,
    TURN,
    RIVER,
    SHOWDOWN
}

data class GameState(
    val players: List<Player> = emptyList(),
    val board: List<Card> = emptyList(),
    val pot: Long = 0,
    val dealerIndex: Int = 0,
    val activePlayerIndex: Int = 0,
    val stage: GameStage = GameStage.WAITING,
    val smallBlind: Long = 10,
    val bigBlind: Long = 20,
    val currentMaxBet: Long = 0
)
