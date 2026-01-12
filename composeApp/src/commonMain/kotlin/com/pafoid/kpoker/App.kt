package com.pafoid.kpoker

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.pafoid.kpoker.ui.component.RulesDialog
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
        var showRules by remember { mutableStateOf(false) }

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
                        serverStatus = viewModel.serverStatus, // Pass serverStatus here
                        language = viewModel.settings.language,
                        onLogin = { user, pass -> 
                            viewModel.playButtonSound()
                            viewModel.login(user, pass) 
                        },
                        onRegister = { user, pass -> 
                            viewModel.playButtonSound()
                            viewModel.register(user, pass) 
                        },
                        onQuit = {
                            viewModel.playButtonSound()
                            onQuit()
                        },
                        onSettingsClick = { 
                            viewModel.playButtonSound()
                            viewModel.navigateToSettings() 
                        },
                        rememberMe = viewModel.rememberMe,
                        onRememberMeChanged = { 
                            viewModel.playButtonSound()
                            viewModel.updateRememberMe(it) 
                        },
                        initialUsername = viewModel.myUsername,
                        initialPassword = viewModel.savedPassword
                    )
                }
                AppScreen.LOBBY -> {
                    LobbyScreen(
                        myPlayerId = viewModel.myPlayerId,
                        myUsername = viewModel.myUsername,
                        myBankroll = viewModel.myBankroll,
                        rooms = viewModel.rooms,
                        language = viewModel.settings.language,
                        selectedDifficulty = viewModel.selectedDifficulty,
                        onDifficultyChanged = { 
                            viewModel.playButtonSound()
                            viewModel.updateDifficulty(it) 
                        },
                        onCreateRoom = { name -> 
                            viewModel.playButtonSound()
                            viewModel.createRoom(name) 
                        },
                        onCreateSinglePlayerRoom = { 
                            viewModel.playButtonSound()
                            viewModel.createSinglePlayerRoom() 
                        },
                        onJoinRoom = { roomId -> 
                            viewModel.playButtonSound()
                            viewModel.joinRoom(roomId) 
                        },
                        onSettingsClick = { 
                            viewModel.playButtonSound()
                            viewModel.navigateToSettings() 
                        },
                        onRulesClick = { 
                            viewModel.playButtonSound()
                            showRules = true 
                        },
                        onLogout = { 
                            viewModel.playButtonSound()
                            viewModel.logout() 
                        },
                        onQuit = {
                            viewModel.playButtonSound()
                            onQuit()
                        }
                    )
                }
                AppScreen.GAME -> {
                    viewModel.gameState?.let { state ->
                        GameScreen(
                            state = state,
                            playerId = viewModel.myPlayerId ?: "",
                            onAction = { action -> viewModel.performAction(action) },
                            onLeave = { 
                                viewModel.playButtonSound()
                                viewModel.leaveRoom() 
                            },
                            onRulesClick = { 
                                viewModel.playButtonSound()
                                showRules = true 
                            },
                            onStartGame = { 
                                viewModel.playButtonSound()
                                viewModel.startGame() 
                            },
                            onPlaySound = viewModel::playButtonSound
                        )
                    }
                }
                AppScreen.SETTINGS -> {
                    SettingsScreen(
                        settings = viewModel.settings,
                        showProfile = viewModel.myPlayerId != null,
                        onSettingsChanged = { viewModel.updateSettings(it) },
                        onChangePassword = { 
                            viewModel.playButtonSound()
                            viewModel.changePassword(it) 
                        },
                        onChangeUsername = { 
                            viewModel.playButtonSound()
                            viewModel.changeUsername(it) 
                        },
                        onBack = { 
                            viewModel.playButtonSound()
                            viewModel.goBack() 
                        }
                    )
                }
            }

            if (showRules) {
                RulesDialog(
                    language = viewModel.settings.language,
                    onDismiss = { 
                        viewModel.playButtonSound()
                        showRules = false 
                    },
                    onPlaySound = viewModel::playButtonSound
                )
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .fillMaxWidth(0.25f)
                    .padding(16.dp),
                snackbar = { data ->
                    Snackbar(
                        snackbarData = data,
                        containerColor = Color.Black.copy(alpha = 0.9f),
                        contentColor = Gold,
                        actionColor = Gold,
                        shape = MaterialTheme.shapes.medium
                    )
                }
            )
        }
    }
}

enum class AppScreen {
    HOME, LOBBY, GAME, SETTINGS
}
