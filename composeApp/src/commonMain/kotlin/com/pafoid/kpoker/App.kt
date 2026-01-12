package com.pafoid.kpoker

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.pafoid.kpoker.ui.screen.GameScreen
import com.pafoid.kpoker.ui.screen.HomeScreen
import com.pafoid.kpoker.ui.screen.LobbyScreen
import com.pafoid.kpoker.ui.screen.SettingsScreen
import org.jetbrains.compose.ui.tooling.preview.Preview

val Gold = Color(0xFFFFD700)

@Composable
@Preview
fun App(
    onFullscreenChanged: (Boolean) -> Unit = {},
    onQuit: () -> Unit = {}
) {
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

            // Sync full screen with main.kt
            LaunchedEffect(viewModel.settings.isFullscreen) {
                onFullscreenChanged(viewModel.settings.isFullscreen)
            }

            // Auto-navigate to game screen if game state becomes active
            LaunchedEffect(viewModel.gameState) {
                if (viewModel.gameState != null && viewModel.currentScreen == AppScreen.LOBBY) {
                    viewModel.navigateToGame()
                }
            }

            when (viewModel.currentScreen) {
                AppScreen.HOME -> {
                    HomeScreen(
                        isLoading = viewModel.isLoading,
                        language = viewModel.settings.language,
                        onLogin = { user, pass -> viewModel.login(user, pass) },
                        onRegister = { user, pass -> viewModel.register(user, pass) },
                        onQuit = onQuit,
                        rememberMe = viewModel.rememberMe,
                        onRememberMeChanged = { viewModel.updateRememberMe(it) },
                        initialUsername = viewModel.myUsername,
                        initialPassword = viewModel.savedPassword
                    )
                }
                AppScreen.LOBBY -> {
                    LobbyScreen(
                        rooms = viewModel.rooms,
                        language = viewModel.settings.language,
                        selectedDifficulty = viewModel.selectedDifficulty,
                        onDifficultyChanged = { viewModel.updateDifficulty(it) },
                        onCreateRoom = { name -> viewModel.createRoom(name) },
                        onCreateSinglePlayerRoom = { viewModel.createSinglePlayerRoom() },
                        onJoinRoom = { roomId -> viewModel.joinRoom(roomId) },
                        onSettingsClick = { viewModel.navigateToSettings() },
                        onLogout = { viewModel.logout() }
                    )
                }
                AppScreen.GAME -> {
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
                AppScreen.SETTINGS -> {
                    SettingsScreen(
                        settings = viewModel.settings,
                        onSettingsChanged = { viewModel.updateSettings(it) },
                        onChangePassword = { viewModel.changePassword(it) },
                        onChangeUsername = { viewModel.changeUsername(it) },
                        onBack = { viewModel.goBack() }
                    )
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

enum class AppScreen {
    HOME, LOBBY, GAME, SETTINGS
}
