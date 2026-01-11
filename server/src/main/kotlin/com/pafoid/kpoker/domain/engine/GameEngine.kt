package com.pafoid.kpoker.domain.engine

import com.pafoid.kpoker.domain.evaluator.WinnerDeterminer
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
            currentMaxBet = state.bigBlind
        )
        
        // Blind logic and initial active player would go here
    }

    fun nextStage() {
        state = when (state.stage) {
            GameStage.PRE_FLOP -> {
                val flop = listOf(deck.draw(), deck.draw(), deck.draw())
                state.copy(stage = GameStage.FLOP, board = flop)
            }
            GameStage.FLOP -> {
                val turn = state.board + deck.draw()
                state.copy(stage = GameStage.TURN, board = turn)
            }
            GameStage.TURN -> {
                val river = state.board + deck.draw()
                state.copy(stage = GameStage.RIVER, board = river)
            }
            GameStage.RIVER -> {
                determineWinners()
                state.copy(stage = GameStage.SHOWDOWN)
            }
            else -> state
        }
    }

    private fun determineWinners() {
        val activePlayers = state.players.filter { !it.isFolded }
        val playerHands = activePlayers.associate { it.id to it.holeCards }
        
        val winners = WinnerDeterminer.determineWinners(state.board, playerHands)
        // logic to distribute pot would go here
    }

    fun getState() = state
}
