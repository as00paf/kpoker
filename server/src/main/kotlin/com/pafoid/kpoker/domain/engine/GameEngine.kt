package com.pafoid.kpoker.domain.engine

import com.pafoid.kpoker.domain.evaluator.HandEvaluator
import com.pafoid.kpoker.domain.evaluator.WinnerDeterminer
import com.pafoid.kpoker.domain.evaluator.PotManager
import com.pafoid.kpoker.domain.model.*

import com.pafoid.kpoker.getCurrentTimeMillis

class GameEngine {
    private var state = GameState()
    private var deck = Deck()

    fun addPlayer(id: String, name: String, chips: Long) {
        val newPlayer = Player(id = id, name = name, chips = chips)
        state = state.copy(players = state.players + newPlayer)
    }

    fun startNewHand() {
        if (state.players.count { it.chips > 0 } < 2) return
        
        // Rotate dealer
        val nextDealerIndex = if (state.stage == GameStage.WAITING) 0 else (state.dealerIndex + 1) % state.players.size
        
        deck = Deck()
        deck.shuffle()
        
        val playersWithCards = state.players.map { player ->
            if (player.chips > 0) {
                player.copy(
                    holeCards = listOf(deck.draw(), deck.draw()),
                    isFolded = false,
                    currentBet = 0,
                    totalContribution = 0,
                    isAllIn = false
                )
            } else {
                player.copy(isFolded = true, holeCards = emptyList(), currentBet = 0, totalContribution = 0)
            }
        }
        
        state = state.copy(
            players = playersWithCards,
            board = emptyList(),
            pot = 0,
            stage = GameStage.PRE_FLOP,
            currentMaxBet = 0,
            playersActedThisRound = emptySet(),
            dealerIndex = nextDealerIndex,
            lastHandResult = null,
            nextHandAt = null
        )

        postBlinds()
    }

    private fun postBlinds() {
        val activePlayers = state.players.filter { it.chips > 0 }
        if (activePlayers.size < 2) return

        val activePlayersCount = activePlayers.size

        if (activePlayersCount == 2) {
            // Heads-up: Dealer is SB, other is BB
            val sbIndex = state.dealerIndex
            val bbIndex = (state.dealerIndex + 1) % state.players.size
            
            updatePlayerBet(sbIndex, state.smallBlind)
            updatePlayerBet(bbIndex, state.bigBlind)
            
            state = state.copy(
                currentMaxBet = state.bigBlind,
                minRaise = state.bigBlind,
                activePlayerIndex = sbIndex,
                lastRaiserIndex = bbIndex,
                turnStartedAt = getCurrentTimeMillis()
            )
        } else {
            // 3+ players: SB is Dealer+1, BB is Dealer+2
            val sbIndex = (state.dealerIndex + 1) % state.players.size
            val bbIndex = (state.dealerIndex + 2) % state.players.size
            
            updatePlayerBet(sbIndex, state.smallBlind)
            updatePlayerBet(bbIndex, state.bigBlind)
            
            state = state.copy(
                currentMaxBet = state.bigBlind,
                minRaise = state.bigBlind,
                activePlayerIndex = (bbIndex + 1) % state.players.size,
                lastRaiserIndex = bbIndex,
                turnStartedAt = getCurrentTimeMillis()
            )
        }
    }

    private fun updatePlayerBet(playerIndex: Int, amount: Long) {
        val player = state.players[playerIndex]
        if (player.chips <= 0 && amount > 0) return

        val actualBet = minOf(player.chips, amount)
        val newPlayers = state.players.toMutableList()
        newPlayers[playerIndex] = player.copy(
            chips = player.chips - actualBet,
            currentBet = player.currentBet + actualBet,
            totalContribution = player.totalContribution + actualBet,
            isAllIn = (player.chips == actualBet) && (player.chips > 0)
        )
        state = state.copy(players = newPlayers)
    }

    fun handleAction(playerId: String, action: BettingAction) {
        if (state.stage == GameStage.SHOWDOWN || state.stage == GameStage.WAITING) return

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
                if (player.currentBet < state.currentMaxBet) return 
            }
            is BettingAction.Call -> {
                val callAmount = state.currentMaxBet - player.currentBet
                updatePlayerBet(playerIndex, callAmount)
            }
            is BettingAction.Raise -> {
                val totalBet = action.amount
                val raiseAmount = totalBet - player.currentBet
                if (totalBet < state.currentMaxBet + state.minRaise) return 
                if (raiseAmount > player.chips) return 
                
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

        state = state.copy(
            playersActedThisRound = state.playersActedThisRound + playerId
        )
        moveToNextPlayer()
    }

    fun checkTimeouts() {
        val startedAt = state.turnStartedAt ?: return
        if (getCurrentTimeMillis() - startedAt > state.turnTimeoutMillis) {
            val activePlayer = state.activePlayer ?: return
            if (activePlayer.currentBet >= state.currentMaxBet) {
                handleAction(activePlayer.id, BettingAction.Check)
            } else {
                handleAction(activePlayer.id, BettingAction.Fold)
            }
        }
    }

    private fun moveToNextPlayer() {
        val activeNotFolded = state.players.filter { !it.isFolded }
        if (activeNotFolded.size <= 1) {
            collectBetsIntoPot()
            distributePots()
            state = state.copy(stage = GameStage.SHOWDOWN, activePlayerIndex = -1, turnStartedAt = null)
            return
        }

        if (isBettingRoundOver()) {
            collectBetsIntoPot()
            
            val activeNotAllIn = state.players.filter { !it.isFolded && !it.isAllIn }
            if (activeNotAllIn.size <= 1) {
                while (state.board.size < 5) {
                    state = state.copy(board = state.board + deck.draw())
                }
                distributePots()
                state = state.copy(stage = GameStage.SHOWDOWN, activePlayerIndex = -1, turnStartedAt = null)
                return
            }
            
            nextStage()
            return
        }

        var nextIndex = (state.activePlayerIndex + 1) % state.players.size
        var loopCount = 0
        while ((state.players[nextIndex].isFolded || state.players[nextIndex].isAllIn) && loopCount < state.players.size) {
            nextIndex = (nextIndex + 1) % state.players.size
            loopCount++
        }
        
        state = state.copy(activePlayerIndex = nextIndex, turnStartedAt = getCurrentTimeMillis())
    }

    private fun isBettingRoundOver(): Boolean {
        val activeCanAct = state.players.filter { !it.isFolded && !it.isAllIn }
        
        if (activeCanAct.isEmpty()) return true
        
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
                state.copy(stage = GameStage.FLOP, board = flop, activePlayerIndex = firstToActAfterFlop(), turnStartedAt = getCurrentTimeMillis())
            }
            GameStage.FLOP -> {
                val turn = state.board + deck.draw()
                state.copy(stage = GameStage.TURN, board = turn, activePlayerIndex = firstToActAfterFlop(), turnStartedAt = getCurrentTimeMillis())
            }
            GameStage.TURN -> {
                val river = state.board + deck.draw()
                state.copy(stage = GameStage.RIVER, board = river, activePlayerIndex = firstToActAfterFlop(), turnStartedAt = getCurrentTimeMillis())
            }
            GameStage.RIVER -> {
                distributePots()
                state.copy(stage = GameStage.SHOWDOWN, activePlayerIndex = -1, turnStartedAt = null)
            }
            else -> state
        }
    }

    private fun firstToActAfterFlop(): Int {
        var idx = (state.dealerIndex + 1) % state.players.size
        for (i in 0 until state.players.size) {
            val currentIdx = (idx + i) % state.players.size
            if (!state.players[currentIdx].isFolded && !state.players[currentIdx].isAllIn) {
                return currentIdx
            }
        }
        return -1
    }

    private fun distributePots() {
        val pots = PotManager.calculatePots(state.players)
        val activePlayers = state.players.filter { !it.isFolded }
        
        val playerHands = if (activePlayers.size > 1 && state.board.size == 5) {
            activePlayers.associate { it.id to HandEvaluator.evaluate(state.board + it.holeCards) }
        } else {
            activePlayers.associate { it.id to Hand(HandType.HIGH_CARD, emptyList(), description = "Winner") }
        }

        val distribution = PotManager.distribute(pots, playerHands)
        
        val newPlayers = state.players.map { player ->
            val winAmount = distribution[player.id] ?: 0L
            player.copy(chips = player.chips + winAmount)
        }
        
        val winners = distribution.keys.toList()
        state = state.copy(
            players = newPlayers, 
            pot = 0,
            lastHandResult = HandResult(winners, distribution, playerHands)
        )
    }

    fun updateNextHandTime(time: Long?) {
        state = state.copy(nextHandAt = time)
    }

    fun getState() = state
}
