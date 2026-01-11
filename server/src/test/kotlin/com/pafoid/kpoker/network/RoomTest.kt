package com.pafoid.kpoker.network

import com.pafoid.kpoker.domain.model.BettingAction
import com.pafoid.kpoker.domain.model.GameStage
import io.ktor.websocket.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.Dispatchers
import java.util.Collections

class RoomTest {

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
    fun testRoomPlayerManagement() = runBlocking {
        val room = Room("1", "Test Room")
        val session = DummySession()
        
        room.addPlayer("p1", "Alice", session)
        
        assertEquals(1, room.engine.getState().players.size)
        assertEquals("Alice", room.engine.getState().players[0].name)
        assertTrue(room.hasPlayer("p1"))
        
        room.removePlayer("p1")
        assertFalse(room.hasPlayer("p1"))
    }

    @Test
    fun testRoomStartGame() = runBlocking {
        val room = Room("1", "Test Room")
        room.addPlayer("p1", "Alice", DummySession())
        room.addPlayer("p2", "Bob", DummySession())
        
        room.startGame()
        
        assertEquals(GameStage.PRE_FLOP, room.engine.getState().stage)
    }
}