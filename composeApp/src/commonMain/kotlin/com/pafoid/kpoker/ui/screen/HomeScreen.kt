package com.pafoid.kpoker.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import kpoker.composeapp.generated.resources.Res
import kpoker.composeapp.generated.resources.home_screen_bg
import org.jetbrains.compose.resources.painterResource

import com.pafoid.kpoker.domain.model.Language
import com.pafoid.kpoker.network.LocalizationService
import com.pafoid.kpoker.network.ServerStatus

// Helper function to get color and text for server status
@Composable
fun getServerStatusIndicator(status: ServerStatus): Pair<Color, String> {
    return when (status) {
        ServerStatus.Connected -> Pair(Color.Green, "Connected")
        ServerStatus.Connecting -> Pair(Color.Yellow, "Connecting...")
        ServerStatus.Disconnected -> Pair(Color.Red, "Disconnected")
        ServerStatus.Error -> Pair(Color.Red, "Error")
    }
}

@Composable
fun HomeScreen(
    isLoading: Boolean,
    serverStatus: ServerStatus,
    language: Language,
    onLogin: (String, String) -> Unit,
    onRegister: (String, String) -> Unit,
    onQuit: () -> Unit,
    onSettingsClick: () -> Unit,
    rememberMe: Boolean,
    onRememberMeChanged: (Boolean) -> Unit,
    initialUsername: String = "",
    initialPassword: String = "",
    // Added username and bankroll as placeholders for potential display
    username: String = "Guest",
    bankroll: Long = 0
) {
    val Gold = Color(0xFFFFD700)
    var localUsername by remember(initialUsername) { mutableStateOf(initialUsername) }
    var password by remember(initialPassword) { mutableStateOf(initialPassword) }
    var passwordVisible by remember { mutableStateOf(false) }

    val (statusColor, statusText) = getServerStatusIndicator(serverStatus)

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(Res.drawable.home_screen_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Server Status Indicator
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .padding(top = 8.dp), // Add slight padding to push it down a bit more
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            Icon(
                Icons.Default.Wifi, // Or another relevant icon
                contentDescription = "Server Status",
                tint = statusColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = statusText,
                style = MaterialTheme.typography.bodySmall,
                color = statusColor
            )
        }

        Surface(
            modifier = Modifier
                .align(Alignment.Center)
                .width(400.dp)
                .padding(16.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = localUsername,
                    onValueChange = { localUsername = it },
                    label = { Text(LocalizationService.getString("username", language)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isLoading
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(LocalizationService.getString("password", language)) },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    enabled = !isLoading,
                    trailingIcon = {
                        val image = if (passwordVisible)
                            Icons.Filled.Visibility
                        else Icons.Filled.VisibilityOff

                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, contentDescription = if (passwordVisible) "Hide password" else "Show password")
                        }
                    }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = rememberMe,
                        onCheckedChange = onRememberMeChanged,
                        enabled = !isLoading
                    )
                    Text(
                        text = LocalizationService.getString("remember_me", language),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 8.dp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(48.dp))
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onLogin(localUsername, password) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(LocalizationService.getString("login", language))
                        }
                        OutlinedButton(
                            onClick = { onRegister(localUsername, password) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(LocalizationService.getString("register", language))
                        }
                    }
                }
            }
        }

        // Bankroll and Username Indicator at Bottom Left
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(24.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(4.dp) // Space between username and bankroll
        ) {
            Text(
                text = "Username: $username",
                style = MaterialTheme.typography.bodyMedium.copy(color = Gold), // Using Gold for emphasis
            )
            Text(
                text = "Bankroll: $$$bankroll",
                style = MaterialTheme.typography.titleMedium, // More prominent style
                color = Gold
            )
        }

        // Bottom Right Buttons (Swapped: Quit on left, Settings on right)
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // New Quit Button (was Settings)
            OutlinedButton(
                onClick = onQuit, // Now triggers quit action
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD32F2F)), // Red color for quit
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD32F2F)),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, modifier = Modifier.size(18.dp)) // ExitToApp icon for Quit
                Spacer(Modifier.width(8.dp))
                Text(LocalizationService.getString("quit", language)) // Displays "Quit"
            }

            // New Settings Button (was Quit)
            OutlinedButton(
                onClick = onSettingsClick, // Now triggers settings action
                border = androidx.compose.foundation.BorderStroke(1.dp, Gold),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Gold),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(Icons.Filled.Settings, contentDescription = null, modifier = Modifier.size(18.dp)) // Settings icon for Settings
                Spacer(Modifier.width(8.dp))
                Text(LocalizationService.getString("settings", language)) // Displays "Settings"
            }
        }
    }
}
