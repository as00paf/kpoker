package com.pafoid.kpoker.domain.evaluator

import com.pafoid.kpoker.domain.model.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HandEvaluatorTest {

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
    fun testRoyalFlush() {
        val hand = HandEvaluator.evaluate(cards("As", "Ks", "Qs", "Js", "Ts", "2h", "5d"))
        assertEquals(HandType.ROYAL_FLUSH, hand.type)
    }

    @Test
    fun testStraightFlush() {
        val hand = HandEvaluator.evaluate(cards("9s", "Ks", "Qs", "Js", "Ts", "2h", "5d"))
        assertEquals(HandType.STRAIGHT_FLUSH, hand.type)
        assertEquals(Rank.KING, hand.cards.first().rank)
    }

    @Test
    fun testFourOfAKind() {
        val hand = HandEvaluator.evaluate(cards("As", "Ah", "Ad", "Ac", "Ks", "2h", "5d"))
        assertEquals(HandType.FOUR_OF_A_KIND, hand.type)
        assertEquals(Rank.ACE, hand.cards.first().rank)
        assertEquals(Rank.KING, hand.kickers.first().rank)
    }

    @Test
    fun testFullHouse() {
        val hand = HandEvaluator.evaluate(cards("As", "Ah", "Ad", "Ks", "Kh", "2h", "5d"))
        assertEquals(HandType.FULL_HOUSE, hand.type)
        assertEquals(Rank.ACE, hand.cards[0].rank)
        assertEquals(Rank.KING, hand.cards[3].rank)
    }

    @Test
    fun testFlush() {
        val hand = HandEvaluator.evaluate(cards("As", "Qs", "9s", "5s", "2s", "Kh", "Jd"))
        assertEquals(HandType.FLUSH, hand.type)
        assertEquals(Rank.ACE, hand.cards.first().rank)
    }

    @Test
    fun testStraight() {
        val hand = HandEvaluator.evaluate(cards("As", "Kh", "Qd", "Jc", "Ts", "2h", "5d"))
        assertEquals(HandType.STRAIGHT, hand.type)
        assertEquals(Rank.ACE, hand.cards.first().rank)
    }

    @Test
    fun testLowStraight() {
        val hand = HandEvaluator.evaluate(cards("As", "2h", "3d", "4c", "5s", "Th", "Kd"))
        assertEquals(HandType.STRAIGHT, hand.type)
        assertEquals(Rank.FIVE, hand.cards.first().rank)
        assertEquals(Rank.ACE, hand.cards.last().rank)
    }

    @Test
    fun testThreeOfAKind() {
        val hand = HandEvaluator.evaluate(cards("As", "Ah", "Ad", "Ks", "Qh", "2h", "5d"))
        assertEquals(HandType.THREE_OF_A_KIND, hand.type)
        assertEquals(Rank.ACE, hand.cards.first().rank)
        assertEquals(Rank.KING, hand.kickers[0].rank)
        assertEquals(Rank.QUEEN, hand.kickers[1].rank)
    }

    @Test
    fun testTwoPair() {
        val hand = HandEvaluator.evaluate(cards("As", "Ah", "Ks", "Kh", "Qh", "2h", "5d"))
        assertEquals(HandType.TWO_PAIR, hand.type)
        assertEquals(Rank.ACE, hand.cards[0].rank)
        assertEquals(Rank.KING, hand.cards[2].rank)
        assertEquals(Rank.QUEEN, hand.kickers[0].rank)
    }

    @Test
    fun testPair() {
        val hand = HandEvaluator.evaluate(cards("As", "Ah", "Ks", "Qh", "Jh", "2h", "5d"))
        assertEquals(HandType.PAIR, hand.type)
        assertEquals(Rank.ACE, hand.cards[0].rank)
        assertEquals(Rank.KING, hand.kickers[0].rank)
    }

    @Test
    fun testHighCard() {
        val hand = HandEvaluator.evaluate(cards("As", "Ks", "Qh", "Jh", "9h", "2h", "5d"))
        assertEquals(HandType.HIGH_CARD, hand.type)
        assertEquals(Rank.ACE, hand.cards[0].rank)
        assertEquals(Rank.KING, hand.kickers[0].rank)
    }

    @Test
    fun testComparison() {
        val hand1 = HandEvaluator.evaluate(cards("As", "Ah", "Ks", "Kh", "Qh", "2h", "5d")) // Two Pair AA KK Q
        val hand2 = HandEvaluator.evaluate(cards("As", "Ah", "Qs", "Qh", "Jh", "2h", "5d")) // Two Pair AA QQ J
        assertTrue(hand1 > hand2)
        
        val hand3 = HandEvaluator.evaluate(cards("As", "Ah", "Ks", "Kh", "Jh", "2h", "5d")) // Two Pair AA KK J
        assertTrue(hand1 > hand3) // Q kicker vs J kicker
    }
}
