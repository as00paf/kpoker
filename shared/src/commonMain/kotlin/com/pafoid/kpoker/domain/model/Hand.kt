package com.pafoid.kpoker.domain.model

enum class HandType(val rank: Int) {
    HIGH_CARD(1),
    PAIR(2),
    TWO_PAIR(3),
    THREE_OF_A_KIND(4),
    STRAIGHT(5),
    FLUSH(6),
    FULL_HOUSE(7),
    FOUR_OF_A_KIND(8),
    STRAIGHT_FLUSH(9),
    ROYAL_FLUSH(10)
}

data class Hand(
    val type: HandType,
    val cards: List<Card>, // The 5 cards making the hand, sorted by importance
    val kickers: List<Card> = emptyList(), // Remaining cards used for tie-breaking
    val description: String = ""
) : Comparable<Hand> {
    
    override fun compareTo(other: Hand): Int {
        if (this.type != other.type) {
            return this.type.rank.compareTo(other.type.rank)
        }
        
        // Compare main cards first
        for (i in cards.indices) {
            val res = this.cards[i].rank.value.compareTo(other.cards[i].rank.value)
            if (res != 0) return res
        }
        
        // Compare kickers
        for (i in kickers.indices) {
            val res = this.kickers[i].rank.value.compareTo(other.kickers[i].rank.value)
            if (res != 0) return res
        }
        
        return 0
    }
}
