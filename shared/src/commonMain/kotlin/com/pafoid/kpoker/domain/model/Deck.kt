package com.pafoid.kpoker.domain.model

class Deck {
    private val cards = Suit.entries.flatMap { suit ->
        Rank.entries.map { rank -> Card(rank, suit) }
    }.toMutableList()

    fun shuffle() {
        cards.shuffle()
    }

    fun draw(): Card {
        if (cards.isEmpty()) throw NoSuchElementException("Deck is empty")
        return cards.removeAt(0)
    }

    fun size() = cards.size
}
