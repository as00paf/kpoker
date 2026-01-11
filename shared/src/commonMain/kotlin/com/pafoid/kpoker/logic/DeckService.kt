package com.pafoid.kpoker.logic

import com.pafoid.kpoker.data.Card
import com.pafoid.kpoker.data.Deck
import com.pafoid.kpoker.data.Rank
import com.pafoid.kpoker.data.Suit

class DeckService {

    fun createStandardDeck(): Deck {
        val cards = mutableListOf<Card>()
        for (suit in Suit.entries) {
            for (rank in Rank.entries) {
                cards.add(Card(rank, suit))
            }
        }
        return Deck(cards)
    }

    fun shuffle(deck: Deck): Deck {
        val shuffledCards = deck.cards.toMutableList()
        shuffledCards.shuffle()
        return Deck(shuffledCards)
    }

    fun dealCards(deck: Deck, count: Int): Pair<List<Card>, Deck> {
        require(count >= 0) { "Number of cards to deal must be non-negative." }
        require(deck.cards.size >= count) { "Not enough cards in the deck to deal $count cards." }

        val dealtCards = deck.cards.take(count)
        val remainingCards = deck.cards.drop(count)
        return Pair(dealtCards, Deck(remainingCards))
    }
}
