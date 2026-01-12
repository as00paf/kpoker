package com.pafoid.kpoker.domain.engine

import com.pafoid.kpoker.domain.evaluator.HandEvaluator
import com.pafoid.kpoker.domain.model.*
import kotlinx.coroutines.delay

import com.pafoid.kpoker.network.AiDifficulty

object AiService {
    
    suspend fun decideAction(state: GameState, aiPlayerId: String, difficulty: AiDifficulty): BettingAction {
        // Dealer should play in maximum 5 seconds. Delay 1-2s for realism.
        delay((1000..2000).random().toLong())
        
        val aiPlayer = state.players.find { it.id == aiPlayerId } ?: return BettingAction.Fold
        val callAmount = state.currentMaxBet - aiPlayer.currentBet
        
        // Basic hand evaluation for AI
        val bestHand = try {
            HandEvaluator.evaluate(state.board + aiPlayer.holeCards)
        } catch (e: Exception) {
            Hand(HandType.HIGH_CARD, emptyList())
        }
        val handRank = bestHand.type.rank

        val action = when (difficulty) {
            AiDifficulty.EASY -> decideEasy(callAmount, state.bigBlind, aiPlayer.chips)
            AiDifficulty.MEDIUM -> decideMedium(callAmount, state.bigBlind, handRank, aiPlayer.chips)
            AiDifficulty.HARD -> decideHard(callAmount, state.bigBlind, handRank, state.pot, aiPlayer.chips)
        }
        
        return validateAction(action, state, aiPlayer)
    }

    private fun validateAction(action: BettingAction, state: GameState, player: Player): BettingAction {
        return when (action) {
            is BettingAction.Raise -> {
                val raiseAmount = action.amount - player.currentBet
                val minRaiseTotal = state.currentMaxBet + state.minRaise
                if (action.amount < minRaiseTotal || raiseAmount > player.chips) {
                    if (state.currentMaxBet > player.currentBet) {
                        val callAmount = state.currentMaxBet - player.currentBet
                        if (callAmount <= player.chips) BettingAction.Call else BettingAction.AllIn
                    } else {
                        BettingAction.Check
                    }
                } else {
                    action
                }
            }
            is BettingAction.Call -> {
                val callAmount = state.currentMaxBet - player.currentBet
                if (callAmount > player.chips) BettingAction.AllIn else action
            }
            is BettingAction.Check -> {
                if (state.currentMaxBet > player.currentBet) {
                    val callAmount = state.currentMaxBet - player.currentBet
                    if (callAmount <= player.chips) BettingAction.Call else BettingAction.AllIn
                } else {
                    action
                }
            }
            else -> action
        }
    }

    private fun decideEasy(callAmount: Long, bb: Long, chips: Long): BettingAction {
        return when {
            callAmount == 0L -> BettingAction.Check
            callAmount <= bb -> BettingAction.Call
            else -> if ((0..10).random() > 7) BettingAction.Call else BettingAction.Fold
        }
    }

    private fun decideMedium(callAmount: Long, bb: Long, handRank: Int, chips: Long): BettingAction {
        val minRaiseTotal = (bb * 2) + callAmount + (0L) // simplified
        return when {
            handRank >= 4 -> {
                if (callAmount == 0L && chips >= bb * 2) BettingAction.Raise(bb * 2) 
                else if (callAmount <= chips) BettingAction.Call 
                else BettingAction.AllIn
            }
            handRank >= 2 -> {
                if (callAmount <= bb * 3 && callAmount <= chips) BettingAction.Call 
                else if (callAmount == 0L) BettingAction.Check
                else BettingAction.Fold
            }
            else -> {
                if (callAmount == 0L) BettingAction.Check 
                else BettingAction.Fold
            }
        }
    }

    private fun decideHard(callAmount: Long, bb: Long, handRank: Int, pot: Long, chips: Long): BettingAction {
        // More aggressive and pot-aware
        return when {
            handRank >= 6 -> {
                val raiseTo = maxOf(bb * 4, callAmount * 2, pot / 2)
                if (chips >= (raiseTo - (callAmount + bb))) BettingAction.Raise(raiseTo)
                else BettingAction.AllIn
            }
            handRank >= 4 -> if (callAmount <= pot && callAmount <= chips) BettingAction.Call else if (callAmount == 0L) BettingAction.Check else BettingAction.Fold
            handRank >= 2 -> if (callAmount <= pot / 3 && callAmount <= chips) BettingAction.Call else if (callAmount == 0L) BettingAction.Check else BettingAction.Fold
            else -> {
                // Bluffing logic
                if (callAmount == 0L && (0..100).random() > 85 && chips >= bb * 3) BettingAction.Raise(bb * 3)
                else if (callAmount == 0L) BettingAction.Check
                else BettingAction.Fold
            }
        }
    }
}
