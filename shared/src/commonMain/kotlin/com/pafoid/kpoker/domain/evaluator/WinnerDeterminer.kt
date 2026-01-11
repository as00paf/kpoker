package com.pafoid.kpoker.domain.evaluator

import com.pafoid.kpoker.domain.model.Card
import com.pafoid.kpoker.domain.model.Hand

object WinnerDeterminer {
    
    data class PlayerResult(
        val playerId: String,
        val bestHand: Hand
    )

    /**
     * Determines the winner(s) among players.
     * Returns a list of player IDs because multiple players can tie for the best hand.
     */
    fun determineWinners(
        board: List<Card>,
        playersCards: Map<String, List<Card>>
    ): List<String> {
        if (playersCards.isEmpty()) return emptyList()

        val results = playersCards.map { (playerId, holeCards) ->
            val allCards = board + holeCards
            PlayerResult(playerId, HandEvaluator.evaluate(allCards))
        }

        val bestHand = results.maxOf { it.bestHand }
        
        return results
            .filter { it.bestHand.compareTo(bestHand) == 0 }
            .map { it.playerId }
    }
}
