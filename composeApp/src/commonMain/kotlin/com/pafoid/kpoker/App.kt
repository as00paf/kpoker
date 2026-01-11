package com.pafoid.kpoker

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import com.pafoid.kpoker.network.GameMessage
import com.pafoid.kpoker.network.PokerClient
import com.pafoid.kpoker.ui.screen.GameScreen
import com.pafoid.kpoker.ui.screen.HomeScreen
import com.pafoid.kpoker.ui.screen.LobbyScreen
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview

enum class Screen {
    HOME, LOBBY, GAME
}

@Composable
@Preview
fun App() {
    MaterialTheme {
        val scope = rememberCoroutineScope()
        val snackbarHostState = remember { SnackbarHostState() }
        val client = remember { PokerClient() }
        
        var currentScreen by remember { mutableStateOf(Screen.HOME) }
        var myPlayerId by remember { mutableStateOf<String?>(null) }
        var myUsername by remember { mutableStateOf("") }
        
        val gameState by client.gameState.collectAsState()
        val rooms by client.rooms.collectAsState()

        LaunchedEffect(Unit) {
            client.connect(this)
            
            launch {
                client.authResponse.collect { response ->
                    if (response.success) {
                        myPlayerId = response.playerId
                        currentScreen = Screen.LOBBY
                    }
                    snackbarHostState.showSnackbar(response.message)
                }
            }

            launch {
                client.error.collect { error ->
                    snackbarHostState.showSnackbar(error)
                }
            }
        }

        // Auto-navigate to game screen if game state becomes active
        LaunchedEffect(gameState) {
            if (gameState != null && currentScreen == Screen.LOBBY) {
                currentScreen = Screen.GAME
            }
        }

        when (currentScreen) {
            Screen.HOME -> {
                HomeScreen(
                    onLogin = { user, pass ->
                        myUsername = user
                        scope.launch { client.sendMessage(GameMessage.Login(user, pass)) }
                    },
                    onRegister = { user, pass ->
                        myUsername = user
                        scope.launch { client.sendMessage(GameMessage.Register(user, pass)) }
                    }
                )
            }
            Screen.LOBBY -> {
                LobbyScreen(
                    rooms = rooms,
                    onCreateRoom = { name ->
                        scope.launch { client.sendMessage(GameMessage.CreateRoom(name)) }
                    },
                    onJoinRoom = { roomId ->
                        scope.launch { client.sendMessage(GameMessage.JoinRoom(roomId, myUsername)) }
                    },
                    onLogout = {
                        myPlayerId = null
                        myUsername = ""
                        currentScreen = Screen.HOME
                    }
                )
            }
            Screen.GAME -> {
                gameState?.let { state ->
                    GameScreen(
                        state = state,
                        playerId = myPlayerId ?: "",
                        onAction = { action ->
                            scope.launch { client.sendMessage(GameMessage.Action(action)) }
                        },
                        onLeave = {
                            scope.launch { client.sendMessage(GameMessage.LeaveRoom) }
                            currentScreen = Screen.LOBBY
                        },
                        onStartGame = {
                            scope.launch { client.sendMessage(GameMessage.StartGame) }
                        }
                    )
                }
            }
        }
        
        SnackbarHost(hostState = snackbarHostState)
    }
}
