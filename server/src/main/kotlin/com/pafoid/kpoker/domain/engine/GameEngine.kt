package com.pafoid.kpoker.domain.engine

import com.pafoid.kpoker.domain.evaluator.HandEvaluator
import com.pafoid.kpoker.domain.evaluator.WinnerDeterminer
import com.pafoid.kpoker.domain.evaluator.PotManager
import com.pafoid.kpoker.domain.model.*

class GameEngine {
    private var state = GameState()
    private var deck = Deck()

    fun addPlayer(id: String, name: String, chips: Long) {
        val newPlayer = Player(id = id, name = name, chips = chips)
        state = state.copy(players = state.players + newPlayer)
    }

    fun startNewHand() {
        if (state.players.size < 2) return
        
        deck = Deck()
        deck.shuffle()
        
        val playersWithCards = state.players.map { player ->
            player.copy(
                holeCards = listOf(deck.draw(), deck.draw()),
                isFolded = false,
                currentBet = 0,
                isAllIn = false
            )
        }
        
        state = state.copy(
            players = playersWithCards,
            board = emptyList(),
            pot = 0,
            stage = GameStage.PRE_FLOP,
            currentMaxBet = 0,
            playersActedThisRound = emptySet()
        )

        postBlinds()
    }

    private fun postBlinds() {
        val sbIndex = (state.dealerIndex + 1) % state.players.size
        val bbIndex = (state.dealerIndex + 2) % state.players.size
        
        updatePlayerBet(sbIndex, state.smallBlind)
        updatePlayerBet(bbIndex, state.bigBlind)
        
        state = state.copy(
            currentMaxBet = state.bigBlind,
            minRaise = state.bigBlind,
            activePlayerIndex = (bbIndex + 1) % state.players.size,
            lastRaiserIndex = bbIndex
        )
    }

    private fun updatePlayerBet(playerIndex: Int, amount: Long) {
        val player = state.players[playerIndex]
        val actualBet = minOf(player.chips, amount)
        val newPlayers = state.players.toMutableList()
        newPlayers[playerIndex] = player.copy(
            chips = player.chips - actualBet,
            currentBet = player.currentBet + actualBet,
            isAllIn = player.chips == actualBet
        )
        state = state.copy(players = newPlayers)
    }

    fun handleAction(playerId: String, action: BettingAction) {
        val playerIndex = state.players.indexOfFirst { it.id == playerId }
        if (playerIndex != state.activePlayerIndex) return

        val player = state.players[playerIndex]
        
        when (action) {
            is BettingAction.Fold -> {
                val newPlayers = state.players.toMutableList()
                newPlayers[playerIndex] = player.copy(isFolded = true)
                state = state.copy(players = newPlayers)
            }
            is BettingAction.Check -> {
                if (player.currentBet < state.currentMaxBet) return // Cannot check if there's a bet
            }
            is BettingAction.Call -> {
                val callAmount = state.currentMaxBet - player.currentBet
                updatePlayerBet(playerIndex, callAmount)
            }
            is BettingAction.Raise -> {
                val totalBet = action.amount
                val raiseAmount = totalBet - player.currentBet
                if (totalBet < state.currentMaxBet + state.minRaise) return // Not a legal raise
                
                val newMinRaise = totalBet - state.currentMaxBet
                updatePlayerBet(playerIndex, raiseAmount)
                state = state.copy(
                    currentMaxBet = totalBet,
                    minRaise = newMinRaise,
                    lastRaiserIndex = playerIndex
                )
            }
            is BettingAction.AllIn -> {
                val allInAmount = player.chips
                val totalBet = player.currentBet + allInAmount
                updatePlayerBet(playerIndex, allInAmount)
                if (totalBet > state.currentMaxBet) {
                    val newMinRaise = maxOf(state.minRaise, totalBet - state.currentMaxBet)
                    state = state.copy(
                        currentMaxBet = totalBet,
                        minRaise = newMinRaise,
                        lastRaiserIndex = playerIndex
                    )
                }
            }
        }

        state = state.copy(playersActedThisRound = state.playersActedThisRound + playerId)
        moveToNextPlayer()
    }

    private fun moveToNextPlayer() {
        val activeNotFolded = state.players.filter { !it.isFolded }
        if (activeNotFolded.size <= 1) {
            collectBetsIntoPot()
            determineWinners()
            state = state.copy(stage = GameStage.SHOWDOWN, activePlayerIndex = -1)
            return
        }

        if (isBettingRoundOver()) {
            collectBetsIntoPot()
            nextStage()
            return
        }

        var nextIndex = (state.activePlayerIndex + 1) % state.players.size
        while (state.players[nextIndex].isFolded || state.players[nextIndex].isAllIn) {
            nextIndex = (nextIndex + 1) % state.players.size
            if (nextIndex == state.activePlayerIndex) break 
        }
        
        state = state.copy(activePlayerIndex = nextIndex)
    }

    private fun isBettingRoundOver(): Boolean {
        val activeCanAct = state.players.filter { !it.isFolded && !it.isAllIn }
        
        // If no one can act, round is over
        if (activeCanAct.isEmpty()) return true
        
        // If only one player can act, they must have matched the bet
        if (activeCanAct.size == 1) {
            val p = activeCanAct.first()
            if (state.playersActedThisRound.contains(p.id) && p.currentBet == state.currentMaxBet) {
                return true
            }
            // Exception: BB can check in Pre-flop if no one raised
            if (state.stage == GameStage.PRE_FLOP && p.currentBet == state.currentMaxBet && state.lastRaiserIndex == state.players.indexOf(p)) {
                 // wait, if BB is the last raiser and it's pre-flop, and everyone else just called/folded
            }
        }

        // Standard condition: Everyone not folded/all-in has acted AND bets match
        val allActed = activeCanAct.all { state.playersActedThisRound.contains(it.id) }
        val betsMatch = state.players.filter { !it.isFolded }.all { it.isAllIn || it.currentBet == state.currentMaxBet }

        return allActed && betsMatch
    }

    private fun collectBetsIntoPot() {
        val roundPot = state.players.sumOf { it.currentBet }
        val resetPlayers = state.players.map { it.copy(currentBet = 0) }
        state = state.copy(
            players = resetPlayers,
            pot = state.pot + roundPot,
            currentMaxBet = 0,
            minRaise = state.bigBlind,
            playersActedThisRound = emptySet(),
            lastRaiserIndex = -1
        )
    }

    fun nextStage() {
        if (state.stage == GameStage.SHOWDOWN) return

        state = when (state.stage) {
            GameStage.PRE_FLOP -> {
                val flop = listOf(deck.draw(), deck.draw(), deck.draw())
                state.copy(stage = GameStage.FLOP, board = flop, activePlayerIndex = firstToActAfterFlop())
            }
            GameStage.FLOP -> {
                val turn = state.board + deck.draw()
                state.copy(stage = GameStage.TURN, board = turn, activePlayerIndex = firstToActAfterFlop())
            }
            GameStage.TURN -> {
                val river = state.board + deck.draw()
                state.copy(stage = GameStage.RIVER, board = river, activePlayerIndex = firstToActAfterFlop())
            }
            GameStage.RIVER -> {
                determineWinners()
                state.copy(stage = GameStage.SHOWDOWN, activePlayerIndex = -1)
            }
            else -> state
        }
    }

    private fun firstToActAfterFlop(): Int {
        var idx = (state.dealerIndex + 1) % state.players.size
        // Find first player not folded
        for (i in 0 until state.players.size) {
            val currentIdx = (idx + i) % state.players.size
            if (!state.players[currentIdx].isFolded && !state.players[currentIdx].isAllIn) {
                return currentIdx
            }
        }
        // If everyone is all-in or folded, return -1 (will transition automatically probably)
        return -1
    }

    private fun determineWinners() {
        val activePlayers = state.players.filter { !it.isFolded }
        if (activePlayers.isEmpty()) return

        if (activePlayers.size == 1) {
            val winner = activePlayers.first()
            val share = state.pot
            val newPlayers = state.players.toMutableList()
            val idx = newPlayers.indexOfFirst { it.id == winner.id }
            newPlayers[idx] = newPlayers[idx].copy(chips = newPlayers[idx].chips + share)
            state = state.copy(players = newPlayers, pot = 0)
            return
        }

        val playerHands = activePlayers.associate { it.id to HandEvaluator.evaluate(state.board + it.holeCards) }
        val winners = WinnerDeterminer.determineWinners(state.board, activePlayers.associate { it.id to it.holeCards })
        
        val share = state.pot / winners.size
        val remainder = state.pot % winners.size
        
        val newPlayers = state.players.toMutableList()
        winners.forEachIndexed { index, winnerId ->
            val idx = newPlayers.indexOfFirst { it.id == winnerId }
            val extra = if (index == 0) remainder else 0
            newPlayers[idx] = newPlayers[idx].copy(chips = newPlayers[idx].chips + share + extra)
        }
        state = state.copy(players = newPlayers, pot = 0)
    }

    fun getState() = state
}