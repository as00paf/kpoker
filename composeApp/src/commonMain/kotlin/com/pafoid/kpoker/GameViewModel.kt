package com.pafoid.kpoker

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.pafoid.kpoker.domain.model.BettingAction
import com.pafoid.kpoker.domain.model.GameState
import com.pafoid.kpoker.network.GameMessage
import com.pafoid.kpoker.network.PokerClient
import com.pafoid.kpoker.network.RoomInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class GameViewModel(private val scope: CoroutineScope) {
    private val client = PokerClient()
    
    var currentScreen by mutableStateOf(Screen.HOME)
        private set
    
    var myPlayerId by mutableStateOf<String?>(null)
        private set
        
    var myUsername by mutableStateOf("")
        private set

    var gameState by mutableStateOf<GameState?>(null)
        private set

    var rooms by mutableStateOf<List<RoomInfo>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    private val _events = MutableSharedFlow<String>()
    val events = _events.asSharedFlow()

    init {
        client.connect(scope)
        
        scope.launch {
            client.gameState.collect { gameState = it }
        }
        
        scope.launch {
            client.rooms.collect { rooms = it }
        }
        
        scope.launch {
            client.authResponse.collect { response ->
                isLoading = false
                if (response.success) {
                    myPlayerId = response.playerId
                    currentScreen = Screen.LOBBY
                }
                _events.emit(response.message)
            }
        }

        scope.launch {
            client.error.collect { error ->
                isLoading = false
                _events.emit(error)
            }
        }
    }

    fun login(user: String, pass: String) {
        if (user.isBlank() || pass.isBlank()) return
        isLoading = true
        myUsername = user
        scope.launch {
            client.sendMessage(GameMessage.Login(user, pass))
        }
    }

    fun register(user: String, pass: String) {
        if (user.isBlank() || pass.isBlank()) return
        isLoading = true
        myUsername = user
        scope.launch {
            client.sendMessage(GameMessage.Register(user, pass))
        }
    }

    fun createRoom(name: String) {
        scope.launch {
            client.sendMessage(GameMessage.CreateRoom(name))
        }
    }

    fun createSinglePlayerRoom() {
        scope.launch {
            client.sendMessage(GameMessage.CreateSinglePlayerRoom)
        }
    }

    fun joinRoom(roomId: String) {
        scope.launch {
            client.sendMessage(GameMessage.JoinRoom(roomId, myUsername))
        }
    }

    fun performAction(action: BettingAction) {
        scope.launch {
            client.sendMessage(GameMessage.Action(action))
        }
    }

    fun leaveRoom() {
        scope.launch {
            client.sendMessage(GameMessage.LeaveRoom)
            currentScreen = Screen.LOBBY
            gameState = null
        }
    }

    fun startGame() {
        scope.launch {
            client.sendMessage(GameMessage.StartGame)
        }
    }

    fun logout() {
        myPlayerId = null
        myUsername = ""
        currentScreen = Screen.HOME
        gameState = null
    }
    
    fun navigateToGame() {
        if (gameState != null) {
            currentScreen = Screen.GAME
        }
    }
}