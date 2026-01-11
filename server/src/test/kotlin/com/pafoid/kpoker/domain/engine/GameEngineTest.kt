package com.pafoid.kpoker.domain.engine

import com.pafoid.kpoker.domain.model.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GameEngineTest {

    @Test
    fun testBettingRoundTransitions() {
        val engine = GameEngine()
        engine.addPlayer("p1", "Player 1", 1000)
        engine.addPlayer("p2", "Player 2", 1000)
        engine.addPlayer("p3", "Player 3", 1000)
        
        // Dealer is p1 (index 0). Blinds: p2(SB), p3(BB). 
        // Active player should be p1 (UTG).
        engine.startNewHand()
        var state = engine.getState()
        assertEquals(GameStage.PRE_FLOP, state.stage)
        assertEquals(0, state.activePlayerIndex) // p1
        
        // p1 calls BB (20)
        engine.handleAction("p1", BettingAction.Call)
        state = engine.getState()
        assertEquals(1, state.activePlayerIndex) // p2 (SB)
        
        // p2 calls (needs to add 10 to his 10 SB)
        engine.handleAction("p2", BettingAction.Call)
        state = engine.getState()
        assertEquals(2, state.activePlayerIndex) // p3 (BB)
        
        // p3 checks
        engine.handleAction("p3", BettingAction.Check)
        state = engine.getState()
        
        // Round should transition to FLOP
        assertEquals(GameStage.FLOP, state.stage)
        assertEquals(3, state.board.size)
        assertEquals(60, state.pot)
        
        // First to act after flop should be p2 (SB)
        assertEquals(1, state.activePlayerIndex)
    }

    @Test
    fun testFoldAllButOne() {
        val engine = GameEngine()
        engine.addPlayer("p1", "Player 1", 1000)
        engine.addPlayer("p2", "Player 2", 1000)
        
        engine.startNewHand()
        // p1 (Dealer/SB in heads up? Actually standard is Dealer is SB)
        // Let's assume my logic is simple: (dealer+1)%size is SB, (dealer+2)%size is BB.
        // For 2 players: d=0, SB=(1)%2=1, BB=(2)%2=0.
        // Active: (BB+1)%2 = (0+1)%2 = 1.
        
        engine.handleAction("p2", BettingAction.Fold)
        val state = engine.getState()
        
        // p1 should win the pot
        assertTrue(state.players[0].chips > 1000)
        assertEquals(GameStage.SHOWDOWN, state.stage)
    }
}
