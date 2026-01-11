package com.pafoid.kpoker.domain.evaluator

import com.pafoid.kpoker.domain.model.Hand
import com.pafoid.kpoker.domain.model.Player

data class Pot(
    val amount: Long,
    val eligiblePlayerIds: Set<String>
)

object PotManager {

    /**
     * Calculates the main pot and side pots based on player total contributions.
     */
    fun calculatePots(players: List<Player>): List<Pot> {
        val participants = players.filter { it.totalContribution > 0 }
            .sortedBy { it.totalContribution }

        if (participants.isEmpty()) return emptyList()

        val pots = mutableListOf<Pot>()
        var lastLevel = 0L

        val uniqueContributions = participants.map { it.totalContribution }.distinct().sorted()

        for (level in uniqueContributions) {
            val amountPerPlayer = level - lastLevel
            
            // Players eligible for this pot level are those who haven't folded AND contributed at least this level
            val eligiblePlayers = participants.filter { it.totalContribution >= level && !it.isFolded }
                .map { it.id }.toSet()
            
            if (eligiblePlayers.isNotEmpty()) {
                // The total amount in this pot level comes from ALL players who contributed at least this much
                val contributorsCount = participants.filter { it.totalContribution >= level }.size
                val potAmount = amountPerPlayer * contributorsCount
                pots.add(Pot(potAmount, eligiblePlayers))
            }
            lastLevel = level
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
