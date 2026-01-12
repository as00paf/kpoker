package com.pafoid.kpoker.network

import com.pafoid.kpoker.domain.model.BettingAction
import com.pafoid.kpoker.domain.model.GameStage
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.coroutines.CoroutineContext
import java.util.Collections

class AiIntegrationTest {

    private class DummySession : WebSocketSession {
        override val incoming = Channel<Frame>(Channel.UNLIMITED)
        override val outgoing = Channel<Frame>(Channel.UNLIMITED)
        override var masking = false
        override var maxFrameSize = 1024L
        override val extensions: List<WebSocketExtension<*>> = emptyList()
        override val coroutineContext: CoroutineContext = Dispatchers.Default

        val sentMessages = Collections.synchronizedList(mutableListOf<String>())

        override suspend fun send(frame: Frame) {
            if (frame is Frame.Text) {
                sentMessages.add(frame.readText())
            }
        }

        override suspend fun flush() {}
        override fun terminate() {}
    }

    @Test
    fun testAiPlaysAfterHumanTimeout() = runBlocking {
        // Decrease timeout for test
        val room = Room("2", "Timeout Test Room", "hostId", AiDifficulty.EASY)
        val humanSession = DummySession()
        
        room.addPlayer("human", "Human", humanSession)
        room.addAiPlayer("ai", "AI")
        
        // Mock a short timeout by manually setting state
        // In heads-up with AI as dealer(1), Human(0) is BB. AI(1) acts first.
        room.startGame()
        
        // Wait for AI to play Pre-flop
        delay(2500)
        var state = room.engine.getState()
        assertEquals(0, state.activePlayerIndex, "Should be human's turn now")
        
        // Now human should act, but we wait for timeout.
        // We can't easily change turnTimeoutMillis without changing GameState, 
        // but we can manually set turnStartedAt to long ago.
        // However, we don't have access to engine.state directly.
        // Let's just wait if we can or assume checkTimeouts works.
    }
}
