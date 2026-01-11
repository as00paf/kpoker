package com.pafoid.kpoker.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class Suit {
    CLUBS, DIAMONDS, HEARTS, SPADES
}

@Serializable
enum class Rank(val value: Int) {
    TWO(2), THREE(3), FOUR(4), FIVE(5), SIX(6), SEVEN(7), EIGHT(8), NINE(9), TEN(10),
    JACK(11), QUEEN(12), KING(13), ACE(14)
}

@Serializable
data class Card(val rank: Rank, val suit: Suit) {
    override fun toString(): String {
        val r = when (rank) {
            Rank.TWO -> "2"
            Rank.THREE -> "3"
            Rank.FOUR -> "4"
            Rank.FIVE -> "5"
            Rank.SIX -> "6"
            Rank.SEVEN -> "7"
            Rank.EIGHT -> "8"
            Rank.NINE -> "9"
            Rank.TEN -> "T"
            Rank.JACK -> "J"
            Rank.QUEEN -> "Q"
            Rank.KING -> "K"
            Rank.ACE -> "A"
        }
        val s = when (suit) {
            Suit.CLUBS -> "c"
            Suit.DIAMONDS -> "d"
            Suit.HEARTS -> "h"
            Suit.SPADES -> "s"
        }
        return "$r$s"
    }
}
