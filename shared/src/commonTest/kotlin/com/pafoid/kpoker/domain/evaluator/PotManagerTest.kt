package com.pafoid.kpoker.domain.evaluator

import com.pafoid.kpoker.domain.model.*
import kotlin.test.Test
import kotlin.test.assertEquals

class PotManagerTest {

    @Test
    fun testSimplePotDistribution() {
        val players = listOf(
            Player("p1", "Player 1", 1000, currentBet = 100),
            Player("p2", "Player 2", 1000, currentBet = 100)
        )
        
        val pots = PotManager.calculatePots(players)
        assertEquals(1, pots.size)
        assertEquals(200L, pots[0].amount)
        
        val hand1 = Hand(HandType.PAIR, emptyList(), description = "Pair of Aces")
        val hand2 = Hand(HandType.HIGH_CARD, emptyList(), description = "King High")
        
        val distribution = PotManager.distribute(pots, mapOf("p1" to hand1, "p2" to hand2))
        assertEquals(200L, distribution["p1"])
        assertEquals(null, distribution["p2"])
    }

    @Test
    fun testSplitPotDistribution() {
        val players = listOf(
            Player("p1", "Player 1", 1000, currentBet = 100),
            Player("p2", "Player 2", 1000, currentBet = 100)
        )
        
        val pots = PotManager.calculatePots(players)
        val hand = Hand(HandType.STRAIGHT, emptyList(), description = "Straight")
        
        val distribution = PotManager.distribute(pots, mapOf("p1" to hand, "p2" to hand))
        assertEquals(100L, distribution["p1"])
        assertEquals(100L, distribution["p2"])
    }
}
