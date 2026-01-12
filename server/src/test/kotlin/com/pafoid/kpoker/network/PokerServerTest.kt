package com.pafoid.kpoker.network

import com.pafoid.kpoker.domain.model.BettingAction
import io.ktor.websocket.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.*
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.JsonBuilder

import kotlin.test.BeforeTest
import org.jetbrains.exposed.sql.deleteAll

class PokerServerTest {

    @BeforeTest
    fun setup() {
        DatabaseFactory.init()
        runBlocking {
            DatabaseFactory.dbQuery {
                Users.deleteAll()
            }
        }
    }

    private class TestSession : WebSocketSession {
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
    fun testTurnValidation() = runBlocking {
        val json = Json {
            ignoreUnknownKeys = true
        }
        val server = PokerServer()
        val session1 = TestSession()
        val session2 = TestSession()

        val job1 = launch { server.handleConnection(session1) }
        val job2 = launch { server.handleConnection(session2) }

        // Give server time to send initial room list
        kotlinx.coroutines.delay(200)

        // Register and Login session1
        session1.incoming.send(Frame.Text(json.encodeToString<GameMessage>(GameMessage.Register("Alice", "pass"))))
        kotlinx.coroutines.delay(100)
        session1.incoming.send(Frame.Text(json.encodeToString<GameMessage>(GameMessage.Login("Alice", "pass"))))
        
        // Register and Login session2
        session2.incoming.send(Frame.Text(json.encodeToString<GameMessage>(GameMessage.Register("Bob", "pass"))))
        kotlinx.coroutines.delay(100)
        session2.incoming.send(Frame.Text(json.encodeToString<GameMessage>(GameMessage.Login("Bob", "pass"))))
        
        kotlinx.coroutines.delay(200)

        // Create Room
        session1.incoming.send(Frame.Text(json.encodeToString<GameMessage>(GameMessage.CreateRoom("Room 1"))))
        
        // Small delay to process
        kotlinx.coroutines.delay(500)
        
        // Find the room list that contains our room
        val roomListMsg = session1.sentMessages
            .filter { it.contains("room_list") }
            .find { it.contains("Room 1") }
            
        assertTrue(roomListMsg != null, "room_list message with 'Room 1' not found")
        val roomList = json.decodeFromString<GameMessage>(roomListMsg) as GameMessage.RoomList
        val roomInfo = roomList.rooms[0]
        
        // Join Room
        session1.incoming.send(Frame.Text(json.encodeToString<GameMessage>(GameMessage.JoinRoom(roomInfo.id, "Alice"))))
        session2.incoming.send(Frame.Text(json.encodeToString<GameMessage>(GameMessage.JoinRoom(roomInfo.id, "Bob"))))
        
        kotlinx.coroutines.delay(500)
        
        // Start Game
        session1.incoming.send(Frame.Text(json.encodeToString<GameMessage>(GameMessage.StartGame)))
        
        kotlinx.coroutines.delay(500)
        
        // Find who is active
        val stateMsg = session1.sentMessages.last { it.contains("state_update") }
        val state = (json.decodeFromString<GameMessage>(stateMsg) as GameMessage.StateUpdate).state
        val activePlayerId = state.activePlayer?.id
        val inactiveSession = if (activePlayerId == state.players[0].id) session2 else session1
        
        // Try to act with inactive player
        inactiveSession.incoming.send(Frame.Text(json.encodeToString<GameMessage>(GameMessage.Action(BettingAction.Check))))
        
        kotlinx.coroutines.delay(500)
        
        assertTrue(inactiveSession.sentMessages.any { it.contains("Not your turn") })

        job1.cancel()
        job2.cancel()
    }
}