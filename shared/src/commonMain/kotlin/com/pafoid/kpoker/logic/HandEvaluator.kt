package com.pafoid.kpoker.logic

import com.pafoid.kpoker.data.Card
import com.pafoid.kpoker.data.Rank
import com.pafoid.kpoker.data.Suit

enum class HandRank {
    HIGH_CARD,
    PAIR,
    TWO_PAIR,
    THREE_OF_A_KIND,
    STRAIGHT,
    FLUSH,
    FULL_HOUSE,
    FOUR_OF_A_KIND,
    STRAIGHT_FLUSH,
    ROYAL_FLUSH
}

class HandEvaluator {

    fun evaluateHand(cards: List<Card>): HandRank {
        require(cards.size >= 5) { "A hand must have at least 5 cards to be evaluated." }

        // Sort cards for easier evaluation (e.g., for straights)
        val sortedCards = cards.sortedWith(compareBy(Card::rank, Card::suit))

        // Group cards by rank and suit for various checks
        val ranks = sortedCards.map { it.rank }.groupingBy { it }.eachCount()
        val suits = sortedCards.map { it.suit }.groupingBy { it }.eachCount()

        // Helper flags
        val isFlush = suits.any { it.value >= 5 }
        val isStraight = checkIfStraight(sortedCards)

        // Check for Straight Flush and Royal Flush first
        if (isStraight && isFlush) {
            val straightFlushRank = checkStraightFlush(sortedCards)
            if (straightFlushRank != null) {
                return straightFlushRank
            }
        }

        // Check for Four of a Kind
        if (ranks.any { it.value == 4 }) return HandRank.FOUR_OF_A_KIND

        // Check for Full House
        val hasThreeOfAKind = ranks.any { it.value == 3 }
        val hasPair = ranks.any { it.value == 2 }
        if (hasThreeOfAKind && hasPair) return HandRank.FULL_HOUSE

        // Check for Flush
        if (isFlush) return HandRank.FLUSH

        // Check for Straight
        if (isStraight) return HandRank.STRAIGHT

        // Check for Three of a Kind
        if (hasThreeOfAKind) return HandRank.THREE_OF_A_KIND

        // Check for Two Pair or Pair
        val pairs = ranks.filter { it.value == 2 }
        if (pairs.size >= 2) return HandRank.TWO_PAIR
        if (pairs.size == 1) return HandRank.PAIR

        return HandRank.HIGH_CARD
    }

    private fun checkIfStraight(cards: List<Card>): Boolean {
        // Remove duplicate ranks and sort unique ranks
        val distinctRanks = cards.map { it.rank.ordinal }.distinct().sorted()

        if (distinctRanks.size < 5) return false

        // Check for standard straight
        for (i in 0..(distinctRanks.size - 5)) {
            if (distinctRanks[i + 4] - distinctRanks[i] == 4) {
                return true
            }
        }

        // Check for A-5 straight (Ace as low)
        val hasAce = distinctRanks.contains(Rank.ACE.ordinal)
        val has2 = distinctRanks.contains(Rank.TWO.ordinal)
        val has3 = distinctRanks.contains(Rank.THREE.ordinal)
        val has4 = distinctRanks.contains(Rank.FOUR.ordinal)
        val has5 = distinctRanks.contains(Rank.FIVE.ordinal)

        if (hasAce && has2 && has3 && has4 && has5) return true

        return false
    }

    private fun checkStraightFlush(cards: List<Card>): HandRank? {
        val groupedBySuit = cards.groupBy { it.suit }

        for ((suit, suitedCards) in groupedBySuit) {
            if (suitedCards.size >= 5) {
                val sortedSuitedCards = suitedCards.sortedBy { it.rank.ordinal }
                val distinctSuitedRanks = sortedSuitedCards.map { it.rank.ordinal }.distinct().sorted()

                if (distinctSuitedRanks.size < 5) continue

                // Check for standard straight flush
                for (i in 0..(distinctSuitedRanks.size - 5)) {
                    if (distinctSuitedRanks[i + 4] - distinctSuitedRanks[i] == 4) {
                        // Check for Royal Flush
                        if (distinctSuitedRanks[i] == Rank.TEN.ordinal && distinctSuitedRanks[i + 4] == Rank.ACE.ordinal) {
                            return HandRank.ROYAL_FLUSH
                        }
                        return HandRank.STRAIGHT_FLUSH
                    }
                }

                // Check for A-5 straight flush (Ace as low)
                val hasAce = distinctSuitedRanks.contains(Rank.ACE.ordinal)
                val has2 = distinctSuitedRanks.contains(Rank.TWO.ordinal)
                val has3 = distinctSuitedRanks.contains(Rank.THREE.ordinal)
                val has4 = distinctSuitedRanks.contains(Rank.FOUR.ordinal)
                val has5 = distinctSuitedRanks.contains(Rank.FIVE.ordinal)

                if (hasAce && has2 && has3 && has4 && has5) return HandRank.STRAIGHT_FLUSH
            }
        }
        return null
    }
}
