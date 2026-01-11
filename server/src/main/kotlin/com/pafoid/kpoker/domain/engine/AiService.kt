package com.pafoid.kpoker.domain.engine

import com.pafoid.kpoker.domain.evaluator.HandEvaluator
import com.pafoid.kpoker.domain.model.*
import kotlinx.coroutines.delay

object AiService {
    
    suspend fun decideAction(state: GameState, aiPlayerId: String): BettingAction {
        // Add a small delay to simulate "thinking"
        delay(1000)
        
        val aiPlayer = state.players.find { it.id == aiPlayerId } ?: return BettingAction.Fold
        val callAmount = state.currentMaxBet - aiPlayer.currentBet
        
        // Simple logic for now
        return when {
            callAmount == 0L -> BettingAction.Check
            callAmount <= state.bigBlind * 2 -> BettingAction.Call
            else -> {
                // If it's more than 2 BBs, maybe fold or call based on some randomness
                if ((0..10).random() > 3) BettingAction.Call else BettingAction.Fold
            }
        }
    }
}
