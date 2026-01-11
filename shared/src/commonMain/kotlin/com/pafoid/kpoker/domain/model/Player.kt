package com.pafoid.kpoker.domain.model

data class Player(
    val id: String,
    val name: String,
    val chips: Long,
    val holeCards: List<Card> = emptyList(),
    val isFolded: Boolean = false,
    val currentBet: Long = 0,
    val isAllIn: Boolean = false
)
