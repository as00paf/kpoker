package com.pafoid.kpoker

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.pafoid.kpoker.audio.createAudioPlayer
import com.pafoid.kpoker.domain.model.BettingAction
import com.pafoid.kpoker.domain.model.GameState
import com.pafoid.kpoker.domain.model.Settings
import com.pafoid.kpoker.network.GameMessage
import com.pafoid.kpoker.network.PokerClient
import com.pafoid.kpoker.network.RoomInfo
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

    var gameState by mutableStateOf<GameState?>(null)
        private set

    var rooms by mutableStateOf<List<RoomInfo>>(emptyList())
        private set

    var selectedDifficulty by mutableStateOf(AiDifficulty.MEDIUM)
        private set

    var settings by mutableStateOf(Settings())
        private set

    var isLoading by mutableStateOf(false)
        private set

    private val _events = MutableSharedFlow<String>()
    val events = _events.asSharedFlow()

    init {
        client.connect(scope)
        
        // Initial music
        audioPlayer.playMusic("Home.mp3", settings.musicVolume)

        scope.launch {
            client.gameState.collect { 
                val oldState = gameState
                gameState = it
                
                // If we just entered a game, switch music
                if (oldState == null && it != null) {
                    audioPlayer.playMusic("Game.mp3", settings.musicVolume)
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
                        currentScreen = AppScreen.LOBBY
                    }
                    
                    if (rememberMe) {
                        settingsManager.rememberMe = true
                        settingsManager.savedUsername = myUsername
                        if (savedPassword.isNotEmpty()) {
                            settingsManager.savedPassword = savedPassword
                        }
                    } else if (response.playerId != null) {
                        // Only clear if we are doing a full login/register and rememberMe is false
                        settingsManager.clearSavedCredentials()
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
        scope.launch {
            client.sendMessage(GameMessage.CreateRoom(name))
        }
    }

    fun updateDifficulty(difficulty: AiDifficulty) {
        selectedDifficulty = difficulty
    }

    fun createSinglePlayerRoom() {
        scope.launch {
            client.sendMessage(GameMessage.CreateSinglePlayerRoom(selectedDifficulty))
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
            currentScreen = AppScreen.LOBBY
            gameState = null
            audioPlayer.playMusic("Home.mp3", settings.musicVolume)
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
        myPlayerId = null
        myUsername = ""
        currentScreen = AppScreen.HOME
        gameState = null
        audioPlayer.playMusic("Home.mp3", settings.musicVolume)
    }
    
    fun navigateToGame() {
        if (gameState != null) {
            currentScreen = AppScreen.GAME
            audioPlayer.playMusic("Game.mp3", settings.musicVolume)
        }
    }

    fun navigateToSettings() {
        currentScreen = AppScreen.SETTINGS
    }

    fun updateSettings(newSettings: Settings) {
        if (newSettings.musicVolume != settings.musicVolume) {
            audioPlayer.setMusicVolume(newSettings.musicVolume)
        }
        if (newSettings.sfxVolume != settings.sfxVolume) {
            audioPlayer.setSfxVolume(newSettings.sfxVolume)
        }
        settings = newSettings
    }

    fun goBack() {
        currentScreen = if (myPlayerId != null) AppScreen.LOBBY else AppScreen.HOME
    }
}