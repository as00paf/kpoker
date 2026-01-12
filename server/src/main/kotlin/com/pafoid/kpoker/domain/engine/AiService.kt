package com.pafoid.kpoker.domain.engine

import com.pafoid.kpoker.domain.evaluator.HandEvaluator
import com.pafoid.kpoker.domain.model.*
import kotlinx.coroutines.delay

import com.pafoid.kpoker.network.AiDifficulty

object AiService {
    
    suspend fun decideAction(state: GameState, aiPlayerId: String, difficulty: AiDifficulty): BettingAction {
        delay(1000)
        
        val aiPlayer = state.players.find { it.id == aiPlayerId } ?: return BettingAction.Fold
        val callAmount = state.currentMaxBet - aiPlayer.currentBet
        
        // Basic hand evaluation for AI
        val bestHand = HandEvaluator.evaluate(state.board + aiPlayer.holeCards)
        val handRank = bestHand.type.rank

        return when (difficulty) {
            AiDifficulty.EASY -> decideEasy(callAmount, state.bigBlind)
            AiDifficulty.MEDIUM -> decideMedium(callAmount, state.bigBlind, handRank)
            AiDifficulty.HARD -> decideHard(callAmount, state.bigBlind, handRank, state.pot)
        }
    }

    private fun decideEasy(callAmount: Long, bb: Long): BettingAction {
        return when {
            callAmount == 0L -> BettingAction.Check
            callAmount <= bb -> BettingAction.Call
            else -> if ((0..10).random() > 5) BettingAction.Call else BettingAction.Fold
        }
    }

    private fun decideMedium(callAmount: Long, bb: Long, handRank: Int): BettingAction {
        return when {
            handRank >= 4 -> if (callAmount == 0L) BettingAction.Raise(bb * 2) else BettingAction.Call
            handRank >= 2 -> if (callAmount <= bb * 3) BettingAction.Call else BettingAction.Fold
            else -> if (callAmount == 0L) BettingAction.Check else BettingAction.Fold
        }
    }

    private fun decideHard(callAmount: Long, bb: Long, handRank: Int, pot: Long): BettingAction {
        // More aggressive and pot-aware
        return when {
            handRank >= 6 -> BettingAction.Raise(maxOf(callAmount * 2, pot / 2))
            handRank >= 4 -> if (callAmount <= pot) BettingAction.Call else BettingAction.Fold
            handRank >= 2 -> if (callAmount <= pot / 3) BettingAction.Call else BettingAction.Fold
            else -> {
                // Bluffing logic
                if (callAmount == 0L && (0..100).random() > 80) BettingAction.Raise(bb * 3)
                else if (callAmount == 0L) BettingAction.Check
                else BettingAction.Fold
            }
        }
    }
}
