package com.pafoid.kpoker.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import kpoker.composeapp.generated.resources.Res
import kpoker.composeapp.generated.resources.home_screen_bg
import org.jetbrains.compose.resources.painterResource

import com.pafoid.kpoker.domain.model.Language
import com.pafoid.kpoker.network.LocalizationService

@Composable
fun HomeScreen(
    isLoading: Boolean,
    language: Language,
    onLogin: (String, String) -> Unit,
    onRegister: (String, String) -> Unit,
    onQuit: () -> Unit,
    onSettingsClick: () -> Unit,
    rememberMe: Boolean,
    onRememberMeChanged: (Boolean) -> Unit,
    initialUsername: String = "",
    initialPassword: String = ""
) {
    var username by remember { mutableStateOf(initialUsername) }
    var password by remember { mutableStateOf(initialPassword) }
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(initialUsername, initialPassword) {
        if (username.isEmpty()) username = initialUsername
        if (password.isEmpty()) password = initialPassword
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(Res.drawable.home_screen_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

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
                    value = username,
                    onValueChange = { username = it },
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
                            onClick = { onLogin(username, password) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(LocalizationService.getString("login", language))
                        }
                        OutlinedButton(
                            onClick = { onRegister(username, password) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(LocalizationService.getString("register", language))
                        }
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onSettingsClick,
                            modifier = Modifier.weight(1f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Gold),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Gold)
                        ) {
                            Text(LocalizationService.getString("settings", language))
                        }
                        
                        OutlinedButton(
                            onClick = onQuit,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                        ) {
                            Text(LocalizationService.getString("quit", language))
                        }
                    }
                }
            }
        }
    }
}
