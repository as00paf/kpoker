package com.pafoid.kpoker.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class GameStage {
    WAITING,
    PRE_FLOP,
    FLOP,
    TURN,
    RIVER,
    SHOWDOWN
}

@Serializable
data class HandResult(
    val winners: List<String>,
    val amountWon: Map<String, Long>,
    val playerHands: Map<String, Hand> = emptyMap()
)

@Serializable
data class GameState(
    val players: List<Player> = emptyList(),
    val board: List<Card> = emptyList(),
    val pot: Long = 0,
    val dealerIndex: Int = 0,
    val activePlayerIndex: Int = -1,
    val stage: GameStage = GameStage.WAITING,
    val smallBlind: Long = 10,
    val bigBlind: Long = 20,
    val currentMaxBet: Long = 0,
    val lastRaiserIndex: Int = -1,
    val minRaise: Long = 20,
    val playersActedThisRound: Set<String> = emptySet(),
    val lastHandResult: HandResult? = null,
    val turnTimeoutMillis: Long = 30000, // 30 seconds
    val turnStartedAt: Long? = null,
    val nextHandAt: Long? = null,
    val settings: Settings = Settings()
) {
    val activePlayer: Player? get() = if (activePlayerIndex in players.indices) players[activePlayerIndex] else null
}
