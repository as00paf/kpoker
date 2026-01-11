package com.pafoid.kpoker.data

data class GameState(
    val players: List<Player>,
    val deck: Deck,
    val communityCards: List<Card>,
    val pot: Int,
    val dealerPosition: Int,
    val smallBlindPosition: Int,
    val bigBlindPosition: Int,
    val currentBet: Int,
    val currentPlayerId: String,
    val gamePhase: GamePhase
)

enum class GamePhase {
    PRE_FLOP, FLOP, TURN, RIVER, SHOWDOWN
}
