package com.pafoid.kpoker.domain.evaluator

import com.pafoid.kpoker.domain.model.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WinnerDeterminerTest {

    private fun c(s: String): Card {
        val rank = when (s.substring(0, s.length - 1)) {
            "2" -> Rank.TWO
            "3" -> Rank.THREE
            "4" -> Rank.FOUR
            "5" -> Rank.FIVE
            "6" -> Rank.SIX
            "7" -> Rank.SEVEN
            "8" -> Rank.EIGHT
            "9" -> Rank.NINE
            "T" -> Rank.TEN
            "J" -> Rank.JACK
            "Q" -> Rank.QUEEN
            "K" -> Rank.KING
            "A" -> Rank.ACE
            else -> throw IllegalArgumentException("Invalid rank")
        }
        val suit = when (s.last()) {
            'c' -> Suit.CLUBS
            'd' -> Suit.DIAMONDS
            'h' -> Suit.HEARTS
            's' -> Suit.SPADES
            else -> throw IllegalArgumentException("Invalid suit")
        }
        return Card(rank, suit)
    }

    private fun cards(vararg s: String) = s.map { c(it) }

    @Test
    fun testSimpleWinner() {
        val board = cards("2s", "3s", "4s", "7h", "9d")
        val players = mapOf(
            "p1" to cards("As", "5s"), // Flush
            "p2" to cards("2h", "2d")  // Set
        )
        
        val winners = WinnerDeterminer.determineWinners(board, players)
        assertEquals(1, winners.size)
        assertEquals("p1", winners[0])
    }

    @Test
    fun testSplitPot() {
        val board = cards("As", "Ks", "Qs", "Jh", "9d")
        val players = mapOf(
            "p1" to cards("Ts", "2h"), // Straight T-A
            "p2" to cards("Td", "3h")  // Straight T-A
        )
        
        val winners = WinnerDeterminer.determineWinners(board, players)
        assertEquals(2, winners.size)
        assertTrue(winners.contains("p1"))
        assertTrue(winners.contains("p2"))
    }

    @Test
    fun testKickerTieBreak() {
        val board = cards("As", "Ks", "2s", "3h", "4d")
        val players = mapOf(
            "p1" to cards("Ah", "Qh"), // Pair of Aces, Q kicker
            "p2" to cards("Ad", "Jh")  // Pair of Aces, J kicker
        )
        
        val winners = WinnerDeterminer.determineWinners(board, players)
        assertEquals(1, winners.size)
        assertEquals("p1", winners[0])
    }
}
