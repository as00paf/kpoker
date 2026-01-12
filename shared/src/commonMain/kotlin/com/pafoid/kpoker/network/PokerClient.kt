package com.pafoid.kpoker.network

import com.pafoid.kpoker.domain.model.GameState
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.websocket.*
import io.ktor.serialization.kotlinx.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class PokerClient(private val host: String = "localhost", private val port: Int = 8080) {
    private val client = HttpClient {
        install(WebSockets) {
            contentConverter = KotlinxWebsocketSerializationConverter(Json {
                ignoreUnknownKeys = true
            })
        }
    }

    private var session: DefaultClientWebSocketSession? = null
    private val json = Json { ignoreUnknownKeys = true }

    private val _gameState = MutableStateFlow<GameState?>(null)
    val gameState: StateFlow<GameState?> = _gameState.asStateFlow()

    private val _rooms = MutableStateFlow<List<RoomInfo>>(emptyList())
    val rooms: StateFlow<List<RoomInfo>> = _rooms.asStateFlow()

    private val _authResponse = MutableSharedFlow<GameMessage.AuthResponse>()
    val authResponse = _authResponse.asSharedFlow()

    private val _error = MutableSharedFlow<String>()
    val error = _error.asSharedFlow()

    fun connect(scope: CoroutineScope) {
        scope.launch {
            var connected = false
            while (!connected && isActive) {
                try {
                    client.webSocket(host = host, port = port, path = "/ws") {
                        connected = true
                        session = this
                        println("Connected to server")
                        for (frame in incoming) {
                            if (frame is Frame.Text) {
                                val text = frame.readText()
                                val message = try {
                                    json.decodeFromString<GameMessage>(text)
                                } catch (e: Exception) {
                                    null
                                }

                                when (message) {
                                    is GameMessage.StateUpdate -> _gameState.value = message.state
                                    is GameMessage.RoomList -> _rooms.value = message.rooms
                                    is GameMessage.AuthResponse -> _authResponse.emit(message)
                                    is GameMessage.Error -> _error.emit(message.message)
                                    else -> {}
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    println("Connection failed, retrying in 2 seconds... (${e.message})")
                    delay(2000)
                } finally {
                    session = null
                    connected = false
                }
            }
        }
    }

    suspend fun sendMessage(message: GameMessage) {
        session?.send(Frame.Text(json.encodeToString(message)))
    }

    fun disconnect() {
        client.close()
    }

    fun clearState() {
        _gameState.value = null
        _rooms.value = emptyList()
    }
}
