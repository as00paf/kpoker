package com.pafoid.kpoker

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import com.pafoid.kpoker.ui.screen.GameScreen
import com.pafoid.kpoker.ui.screen.HomeScreen
import com.pafoid.kpoker.ui.screen.LobbyScreen
import org.jetbrains.compose.ui.tooling.preview.Preview

val Gold = Color(0xFFFFD700)

enum class Screen {
    HOME, LOBBY, GAME
}

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
@Preview
fun App() {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Gold,
            onPrimary = Color.Black,
            secondary = Gold,
            onSecondary = Color.Black,
            tertiary = Gold
        )
    ) {
        val scope = rememberCoroutineScope()
        val snackbarHostState = remember { SnackbarHostState() }
        val viewModel = remember { GameViewModel(scope) }

        Box(modifier = Modifier.fillMaxSize()) {
            LaunchedEffect(Unit) {
                viewModel.events.collect { event ->
                    snackbarHostState.showSnackbar(event)
                }
            }

            // Auto-navigate to game screen if game state becomes active
            LaunchedEffect(viewModel.gameState) {
                if (viewModel.gameState != null && viewModel.currentScreen == Screen.LOBBY) {
                    viewModel.navigateToGame()
                }
            }

            when (viewModel.currentScreen) {
                Screen.HOME -> {
                    HomeScreen(
                        isLoading = viewModel.isLoading,
                        onLogin = { user, pass -> viewModel.login(user, pass) },
                        onRegister = { user, pass -> viewModel.register(user, pass) }
                    )
                }
                Screen.LOBBY -> {
                    LobbyScreen(
                        rooms = viewModel.rooms,
                        onCreateRoom = { name -> viewModel.createRoom(name) },
                        onCreateSinglePlayerRoom = { viewModel.createSinglePlayerRoom() },
                        onJoinRoom = { roomId -> viewModel.joinRoom(roomId) },
                        onLogout = { viewModel.logout() }
                    )
                }
                Screen.GAME -> {
                    viewModel.gameState?.let { state ->
                        GameScreen(
                            state = state,
                            playerId = viewModel.myPlayerId ?: "",
                            onAction = { action -> viewModel.performAction(action) },
                            onLeave = { viewModel.leaveRoom() },
                            onStartGame = { viewModel.startGame() }
                        )
                    }
                }
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .fillMaxWidth(0.25f)
                    .padding(16.dp)
            )
        }
    }
}
