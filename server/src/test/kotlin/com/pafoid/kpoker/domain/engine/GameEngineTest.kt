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
        // Active: p2 (SB) - in my simple 2-player logic SB is (0+1)%2=1
        engine.handleAction("p2", BettingAction.Fold)
        val state = engine.getState()
        
        // p1 should win the blinds
        assertTrue(state.players[0].chips > 1000)
        assertEquals(GameStage.SHOWDOWN, state.stage)
    }

    @Test
    fun testSidePotAllIn() {
        val engine = GameEngine()
        // p1: 100, p2: 1000, p3: 1000
        engine.addPlayer("p1", "Short Stack", 100)
        engine.addPlayer("p2", "Big Stack 1", 1000)
        engine.addPlayer("p3", "Big Stack 2", 1000)
        
        // Dealer p1(0), SB p2(1), BB p3(2). Active: p1(0)
        engine.startNewHand()
        
        // p1 goes all in for 100
        engine.handleAction("p1", BettingAction.AllIn) 
        // p2 calls 100
        engine.handleAction("p2", BettingAction.Call)
        // p3 checks (already 20 in, but wait, p3 is BB(20), p1 raised to 100, p3 needs to call 80)
        engine.handleAction("p3", BettingAction.Call)
        
        var state = engine.getState()
        assertEquals(GameStage.FLOP, state.stage)
        // Pot should be 300
        
        // Flop betting: p2 and p3 bet more
        engine.handleAction("p2", BettingAction.Raise(200)) // p2 bets 200
        engine.handleAction("p3", BettingAction.Call) // p3 calls 200
        
        // Turn: check check
        engine.handleAction("p2", BettingAction.Check)
        engine.handleAction("p3", BettingAction.Check)
        
        // River: check check -> Showdown
        engine.handleAction("p2", BettingAction.Check)
        engine.handleAction("p3", BettingAction.Check)
        
        state = engine.getState()
        assertEquals(GameStage.SHOWDOWN, state.stage)
        
        // Total chips in play: 2100.
        val totalChips = state.players.sumOf { it.chips }
        assertEquals(2100, totalChips)
    }
}
