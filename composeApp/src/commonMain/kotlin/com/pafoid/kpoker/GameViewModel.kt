package com.pafoid.kpoker

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.pafoid.kpoker.audio.createAudioPlayer
import com.pafoid.kpoker.domain.model.BettingAction
import com.pafoid.kpoker.domain.model.GameState
import com.pafoid.kpoker.domain.model.Settings
import com.pafoid.kpoker.network.AiDifficulty
import com.pafoid.kpoker.network.GameMessage
import com.pafoid.kpoker.network.PokerClient
import com.pafoid.kpoker.network.RoomInfo
import com.pafoid.kpoker.network.ServerStatus
import com.pafoid.kpoker.network.SettingsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class GameViewModel(private val scope: CoroutineScope) {
    private val client = PokerClient()
    private val audioPlayer = createAudioPlayer(scope)
    private val settingsManager = SettingsManager()
    
    var currentScreen by mutableStateOf(AppScreen.HOME)
        private set
    
    var myPlayerId by mutableStateOf<String?>(null)
        private set
        
    var myUsername by mutableStateOf(settingsManager.savedUsername ?: "")
        private set

    var savedPassword by mutableStateOf(settingsManager.savedPassword ?: "")
        private set

    var rememberMe by mutableStateOf(settingsManager.rememberMe)
        private set

    var myBankroll by mutableStateOf<Long?>(null) // New state for player bankroll
        private set

    var gameState by mutableStateOf<GameState?>(null)
        private set

    var rooms by mutableStateOf<List<RoomInfo>>(emptyList())
        private set

    var selectedDifficulty by mutableStateOf(AiDifficulty.MEDIUM)
        private set

    var settings by mutableStateOf(settingsManager.loadAppSettings())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var serverStatus by mutableStateOf(ServerStatus.Disconnected)
        private set

    private var isLeaving = false

    private val _events = MutableSharedFlow<String>()
    val events = _events.asSharedFlow()

        init {
            client.connect(scope)
    
            // Observe server status
            scope.launch {
                client.serverStatus.collect {
                    serverStatus = it
                }
            }
    
            // Initial music
            audioPlayer.playMusic("Home.mp3", settings.musicVolume)
            scope.launch {
            client.gameState.collect { newState ->
                if (isLeaving) return@collect
                val oldState = gameState
                gameState = newState
                
                // Check for win/lose
                val newResult = newState?.lastHandResult
                if (oldState?.lastHandResult != newResult && newResult != null) {
                    val amIPlaying = newState.players.any { it.id == myPlayerId }
                    if (amIPlaying) {
                        if (newResult.winners.contains(myPlayerId)) {
                            playSound("game_win.mp3")
                        } else {
                            playSound("game_lose.mp3")
                        }
                    }
                }
                
                // If we just entered a game (gameState was null, now isn't)
                // Also ensure we are actually IN the game (our ID is in the player list)
                // This prevents re-joining if we just left but the server sends a final state update
                val amIInGame = newState?.players?.any { it.id == myPlayerId } == true
                if (newState != null && currentScreen != AppScreen.GAME && amIInGame) {
                    navigateToScreen(AppScreen.GAME)
                }
            }
        }
        
        scope.launch {
            client.rooms.collect { rooms = it }
        }
        
        scope.launch {
            client.authResponse.collect { response ->
                isLoading = false
                if (response.success) {
                    if (response.playerId != null) {
                        myPlayerId = response.playerId
                        myBankroll = response.bankroll // Set the bankroll from response
                        navigateToScreen(AppScreen.LOBBY)
                    }
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

    private fun navigateToScreen(screen: AppScreen) {
        val oldScreen = currentScreen
        currentScreen = screen
        
        if (oldScreen != screen) {
            if (screen == AppScreen.GAME) {
                audioPlayer.playMusic("Game.mp3", settings.musicVolume)
            } else if (oldScreen == AppScreen.GAME || oldScreen == AppScreen.HOME) {
                // If we leave the game or start from home, ensure Home music plays
                if (screen != AppScreen.GAME) {
                    audioPlayer.playMusic("Home.mp3", settings.musicVolume)
                }
            }
        }
    }

    fun login(user: String, pass: String) {
        if (user.isBlank() || pass.isBlank()) return
        isLoading = true
        myUsername = user
        savedPassword = pass
        scope.launch {
            client.sendMessage(GameMessage.Login(user, pass))
        }
    }

    fun register(user: String, pass: String) {
        if (user.isBlank() || pass.isBlank()) return
        isLoading = true
        myUsername = user
        savedPassword = pass
        scope.launch {
            client.sendMessage(GameMessage.Register(user, pass))
        }
    }

    fun updateRememberMe(value: Boolean) {
        rememberMe = value
        settingsManager.rememberMe = value
        if (!value) {
            settingsManager.clearSavedCredentials()
        }
    }

    fun createRoom(name: String) {
        isLeaving = false
        scope.launch {
            client.sendMessage(GameMessage.CreateRoom(name))
        }
    }

    fun updateDifficulty(difficulty: AiDifficulty) {
        selectedDifficulty = difficulty
    }

    fun createSinglePlayerRoom() {
        isLeaving = false
        scope.launch {
            client.sendMessage(GameMessage.CreateSinglePlayerRoom(selectedDifficulty))
        }
    }

    fun joinRoom(roomId: String) {
        isLeaving = false
        scope.launch {
            client.sendMessage(GameMessage.JoinRoom(roomId, myUsername))
        }
    }

    fun performAction(action: BettingAction) {
        when (action) {
            is BettingAction.AllIn -> playSound("all_in.mp3")
            is BettingAction.Raise, BettingAction.Call -> playSound("chips.mp3")
            else -> playButtonSound()
        }
        scope.launch {
            client.sendMessage(GameMessage.Action(action))
        }
    }

    fun leaveRoom() {
        isLeaving = true
        scope.launch {
            client.sendMessage(GameMessage.LeaveRoom)
            client.clearState()
            navigateToScreen(AppScreen.LOBBY)
            gameState = null
        }
    }

    fun startGame() {
        scope.launch {
            client.sendMessage(GameMessage.StartGame)
        }
    }

    fun changePassword(newPass: String) {
        scope.launch {
            client.sendMessage(GameMessage.ChangePassword(newPass))
        }
    }

    fun changeUsername(newName: String) {
        if (newName.isBlank()) return
        myUsername = newName
        scope.launch {
            client.sendMessage(GameMessage.ChangeUsername(newName))
        }
    }

    fun logout() {
        isLeaving = true
        scope.launch {
            client.sendMessage(GameMessage.Logout)
            myPlayerId = null
            if (!rememberMe) {
                myUsername = ""
                savedPassword = ""
            }
            navigateToScreen(AppScreen.HOME)
            gameState = null
            client.clearState()
        }
    }
    
    fun navigateToGame() {
        if (gameState != null) {
            navigateToScreen(AppScreen.GAME)
        }
    }

    fun navigateToSettings() {
        navigateToScreen(AppScreen.SETTINGS)
    }

    fun updateSettings(newSettings: Settings) {
        if (newSettings.musicVolume != settings.musicVolume) {
            audioPlayer.setMusicVolume(newSettings.musicVolume)
        }
        if (newSettings.sfxVolume != settings.sfxVolume) {
            audioPlayer.setSfxVolume(newSettings.sfxVolume)
        }
        settings = newSettings
        settingsManager.saveAppSettings(newSettings)
        
        // Sync with server if we are in a room
        if (myPlayerId != null) {
            scope.launch {
                client.sendMessage(GameMessage.SyncSettings(newSettings))
            }
        }
    }

    fun goBack() {
        navigateToScreen(if (myPlayerId != null) AppScreen.LOBBY else AppScreen.HOME)
    }

    fun playButtonSound() {
        playSound("button_tap.mp3")
    }

    private fun playSound(path: String) {
        audioPlayer.playSound(path, 1.0f)
    }
}
