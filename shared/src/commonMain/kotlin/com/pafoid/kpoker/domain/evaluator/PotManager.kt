package com.pafoid.kpoker.domain.evaluator

import com.pafoid.kpoker.domain.model.Hand
import com.pafoid.kpoker.domain.model.Player

data class Pot(
    val amount: Long,
    val eligiblePlayerIds: Set<String>
)

object PotManager {

    /**
     * Calculates the main pot and side pots based on player contributions.
     * This should be called when a round ends or someone is all-in.
     */
    fun calculatePots(players: List<Player>): List<Pot> {
        val contributions = players.filter { it.currentBet > 0 }
            .map { it.id to it.currentBet }
            .sortedBy { it.second }

        if (contributions.isEmpty()) return emptyList()

        val pots = mutableListOf<Pot>()
        var previousContribution = 0L

        val activePlayerIds = contributions.map { it.first }.toSet()
        
        // This is a simplified version. Real side pots depend on when players went all-in.
        // For a single round's distribution:
        val uniqueBets = contributions.map { it.second }.distinct().sorted()
        
        for (bet in uniqueBets) {
            val levelAmount = bet - previousContribution
            val eligiblePlayers = players.filter { it.currentBet >= bet && !it.isFolded }.map { it.id }.toSet()
            
            if (eligiblePlayers.isNotEmpty()) {
                val potAmount = levelAmount * players.filter { it.currentBet >= bet }.size
                pots.add(Pot(potAmount, eligiblePlayers))
            }
            previousContribution = bet
        }

        return pots
    }

    /**
     * Distributes the pots to the winners.
     * winnersMap: playerId to their Hand (already evaluated)
     */
    fun distribute(pots: List<Pot>, playerHands: Map<String, Hand>): Map<String, Long> {
        val distribution = mutableMapOf<String, Long>()

        for (pot in pots) {
            val eligibleHands = playerHands.filter { pot.eligiblePlayerIds.contains(it.key) }
            if (eligibleHands.isEmpty()) continue

            val bestHand = eligibleHands.values.maxOrNull() ?: continue
            val winners = eligibleHands.filter { it.value.compareTo(bestHand) == 0 }.keys

            val winAmount = pot.amount / winners.size
            val remainder = pot.amount % winners.size

            winners.forEach { playerId ->
                distribution[playerId] = (distribution[playerId] ?: 0L) + winAmount
            }
            
            // Remainder goes to the first winner (usually position based, but here just the first in set)
            if (remainder > 0) {
                val firstWinner = winners.first()
                distribution[firstWinner] = (distribution[firstWinner] ?: 0L) + remainder
            }
        }

        return distribution
    }
}
