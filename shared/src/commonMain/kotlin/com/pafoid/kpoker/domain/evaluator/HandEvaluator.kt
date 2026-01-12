package com.pafoid.kpoker.domain.evaluator

import com.pafoid.kpoker.domain.model.*

object HandEvaluator {

    fun evaluate(allCards: List<Card>): Hand {
        require(allCards.size >= 5) { "At least 5 cards are required to evaluate a hand" }

        // Sort cards by rank descending
        val sortedCards = allCards.sortedByDescending { it.rank.value }

        return findStraightFlush(sortedCards)
            ?: findFourOfAKind(sortedCards)
            ?: findFullHouse(sortedCards)
            ?: findFlush(sortedCards)
            ?: findStraight(sortedCards)
            ?: findThreeOfAKind(sortedCards)
            ?: findTwoPair(sortedCards)
            ?: findPair(sortedCards)
            ?: findHighCard(sortedCards)
    }

    private fun findStraightFlush(cards: List<Card>): Hand? {
        val flushCards = getFlushCards(cards) ?: return null
        return findStraight(flushCards)?.let { straightHand ->
            if (straightHand.cards.first().rank == Rank.ACE && straightHand.cards.last().rank == Rank.TEN) {
                Hand(HandType.ROYAL_FLUSH, straightHand.cards, description = "Royal Flush")
            } else {
                val highRank = straightHand.cards.first().rank
                Hand(HandType.STRAIGHT_FLUSH, straightHand.cards, description = "Straight Flush, ${highRank.name.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }}-high")
            }
        }
    }

    private fun findFourOfAKind(cards: List<Card>): Hand? {
        val groups = cards.groupBy { it.rank }
        val quads = groups.filter { it.value.size == 4 }.keys.maxByOrNull { it.value } ?: return null
        val quadCards = groups[quads]!!
        val kicker = cards.filter { it.rank != quads }.take(1)
        return Hand(
            HandType.FOUR_OF_A_KIND, 
            quadCards, 
            kicker, 
            description = "Four of a Kind, ${quads.name.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }}s"
        )
    }

    private fun findFullHouse(cards: List<Card>): Hand? {
        val groups = cards.groupBy { it.rank }
        val trips = groups.filter { it.value.size >= 3 }.keys.sortedByDescending { it.value }
        if (trips.isEmpty()) return null
        
        for (tripRank in trips) {
            val pairs = groups.filter { it.key != tripRank && it.value.size >= 2 }.keys.sortedByDescending { it.value }
            if (pairs.isNotEmpty()) {
                val pairRank = pairs.first()
                return Hand(
                    HandType.FULL_HOUSE, 
                    groups[tripRank]!!.take(3) + groups[pairRank]!!.take(2),
                    description = "Full House, ${tripRank.name.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }}s full of ${pairRank.name.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }}s"
                )
            }
        }
        return null
    }

    private fun findFlush(cards: List<Card>): Hand? {
        val flushCards = getFlushCards(cards) ?: return null
        val highRank = flushCards.first().rank
        return Hand(
            HandType.FLUSH, 
            flushCards.take(5), 
            description = "Flush, ${highRank.name.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }}-high"
        )
    }

    private fun findStraight(cards: List<Card>): Hand? {
        val uniqueRanks = cards.distinctBy { it.rank }.sortedByDescending { it.rank.value }
        if (uniqueRanks.size < 5) {
            // Check for low straight (A, 5, 4, 3, 2)
            if (uniqueRanks.any { it.rank == Rank.ACE } &&
                uniqueRanks.any { it.rank == Rank.FIVE } &&
                uniqueRanks.any { it.rank == Rank.FOUR } &&
                uniqueRanks.any { it.rank == Rank.THREE } &&
                uniqueRanks.any { it.rank == Rank.TWO }
            ) {
                val ace = uniqueRanks.first { it.rank == Rank.ACE }
                val lowCards = uniqueRanks.filter { it.rank.value <= 5 }.take(4)
                return Hand(
                    HandType.STRAIGHT, 
                    lowCards + ace, 
                    description = "Straight, Five-high"
                )
            }
            return null
        }

        for (i in 0..uniqueRanks.size - 5) {
            if (uniqueRanks[i].rank.value - uniqueRanks[i + 4].rank.value == 4) {
                val highRank = uniqueRanks[i].rank
                return Hand(
                    HandType.STRAIGHT, 
                    uniqueRanks.subList(i, i + 5), 
                    description = "Straight, ${highRank.name.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }}-high"
                )
            }
        }

        // Low straight check again
        if (uniqueRanks.any { it.rank == Rank.ACE } &&
            uniqueRanks.any { it.rank == Rank.FIVE } &&
            uniqueRanks.any { it.rank == Rank.FOUR } &&
            uniqueRanks.any { it.rank == Rank.THREE } &&
            uniqueRanks.any { it.rank == Rank.TWO }
        ) {
            val lowStraight = listOf(
                uniqueRanks.first { it.rank == Rank.FIVE },
                uniqueRanks.first { it.rank == Rank.FOUR },
                uniqueRanks.first { it.rank == Rank.THREE },
                uniqueRanks.first { it.rank == Rank.TWO },
                uniqueRanks.first { it.rank == Rank.ACE }
            )
            return Hand(HandType.STRAIGHT, lowStraight, description = "Straight, Five-high")
        }

        return null
    }

    private fun findThreeOfAKind(cards: List<Card>): Hand? {
        val groups = cards.groupBy { it.rank }
        val trips = groups.filter { it.value.size == 3 }.keys.maxByOrNull { it.value } ?: return null
        val tripCards = groups[trips]!!
        val kickers = cards.filter { it.rank != trips }.take(2)
        return Hand(
            HandType.THREE_OF_A_KIND, 
            tripCards, 
            kickers, 
            description = "Three of a Kind, ${trips.name.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }}s"
        )
    }

    private fun findTwoPair(cards: List<Card>): Hand? {
        val groups = cards.groupBy { it.rank }
        val pairs = groups.filter { it.value.size >= 2 }.keys.sortedByDescending { it.value }
        if (pairs.size < 2) return null
        
        val highPair = groups[pairs[0]]!!.take(2)
        val lowPair = groups[pairs[1]]!!.take(2)
        val kickers = cards.filter { it.rank != pairs[0] && it.rank != pairs[1] }.take(1)
        return Hand(
            HandType.TWO_PAIR, 
            highPair + lowPair, 
            kickers, 
            description = "Two Pair, ${pairs[0].name.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }}s and ${pairs[1].name.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }}s"
        )
    }

    private fun findPair(cards: List<Card>): Hand? {
        val groups = cards.groupBy { it.rank }
        val pairRank = groups.filter { it.value.size == 2 }.keys.maxByOrNull { it.value } ?: return null
        val pairCards = groups[pairRank]!!
        val kickers = cards.filter { it.rank != pairRank }.take(3)
        return Hand(
            HandType.PAIR, 
            pairCards, 
            kickers, 
            description = "Pair of ${pairRank.name.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }}s"
        )
    }

    private fun findHighCard(cards: List<Card>): Hand {
        val highRank = cards.first().rank
        return Hand(
            HandType.HIGH_CARD, 
            cards.take(1), 
            cards.drop(1).take(4), 
            description = "High Card, ${highRank.name.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }}"
        )
    }

    private fun getFlushCards(cards: List<Card>): List<Card>? {
        val suitGroups = cards.groupBy { it.suit }
        val flushSuit = suitGroups.filter { it.value.size >= 5 }.keys.firstOrNull() ?: return null
        return suitGroups[flushSuit]!!.sortedByDescending { it.rank.value }
    }
}
