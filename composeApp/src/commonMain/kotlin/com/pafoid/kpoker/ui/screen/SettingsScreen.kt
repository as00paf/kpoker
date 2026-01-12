package com.pafoid.kpoker.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pafoid.kpoker.domain.model.Language
import com.pafoid.kpoker.domain.model.Settings
import com.pafoid.kpoker.network.LocalizationService
import kpoker.composeapp.generated.resources.Res
import kpoker.composeapp.generated.resources.home_screen_bg
import org.jetbrains.compose.resources.painterResource

@Composable
fun SettingsScreen(
    settings: Settings,
    showProfile: Boolean,
    onSettingsChanged: (Settings) -> Unit,
    onChangePassword: (String) -> Unit,
    onChangeUsername: (String) -> Unit,
    onBack: () -> Unit
) {
    var newPassword by remember { mutableStateOf("") }
    var newUsername by remember { mutableStateOf("") }
    var selectedTabIndex by remember { mutableStateOf(0) }
    val language = settings.language

    val tabs = buildList {
        add(LocalizationService.getString("display", language))
        add(LocalizationService.getString("audio", language))
        if (showProfile) {
            add(LocalizationService.getString("profile", language))
        }
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
                .width(600.dp)
                .heightIn(min = 500.dp)
                .padding(16.dp),
            color = Color.Black.copy(alpha = 0.85f),
            shape = MaterialTheme.shapes.large,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = LocalizationService.getString("settings", language).uppercase(),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary,
                    divider = {}
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(title, fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Box(modifier = Modifier.weight(1f)) {
                    when (selectedTabIndex) {
                        0 -> { // Display
                            Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(LocalizationService.getString("fullscreen", language), color = MaterialTheme.colorScheme.primary)
                                    Switch(
                                        checked = settings.isFullscreen,
                                        onCheckedChange = { onSettingsChanged(settings.copy(isFullscreen = it)) }
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(LocalizationService.getString("language", language), color = MaterialTheme.colorScheme.primary)
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Language.entries.forEach { lang ->
                                            FilterChip(
                                                selected = settings.language == lang,
                                                onClick = { onSettingsChanged(settings.copy(language = lang)) },
                                                label = { Text(if (lang == Language.ENGLISH) "English" else "Français") }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        1 -> { // Audio
                            Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                                Column {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(LocalizationService.getString("music_vol", language), color = MaterialTheme.colorScheme.primary)
                                        Text("${(settings.musicVolume * 100).toInt()}%", color = MaterialTheme.colorScheme.primary)
                                    }
                                    Slider(
                                        value = settings.musicVolume,
                                        onValueChange = { onSettingsChanged(settings.copy(musicVolume = it)) }
                                    )
                                }
                                Column {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(LocalizationService.getString("sfx_vol", language), color = MaterialTheme.colorScheme.primary)
                                        Text("${(settings.sfxVolume * 100).toInt()}%", color = MaterialTheme.colorScheme.primary)
                                    }
                                    Slider(
                                        value = settings.sfxVolume,
                                        onValueChange = { onSettingsChanged(settings.copy(sfxVolume = it)) }
                                    )
                                }
                            }
                        }
                        2 -> { // Profile
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                OutlinedTextField(
                                    value = newUsername,
                                    onValueChange = { newUsername = it },
                                    label = { Text(LocalizationService.getString("new_username", language)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    colors = TextFieldDefaults.colors(
                                        unfocusedLabelColor = MaterialTheme.colorScheme.primary,
                                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                                        focusedTextColor = MaterialTheme.colorScheme.primary,
                                        unfocusedTextColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                                Button(
                                    onClick = { if (newUsername.isNotBlank()) { onChangeUsername(newUsername); newUsername = "" } },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(LocalizationService.getString("change_username", language))
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = newPassword,
                                    onValueChange = { newPassword = it },
                                    label = { Text(LocalizationService.getString("new_password", language)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    colors = TextFieldDefaults.colors(
                                        unfocusedLabelColor = MaterialTheme.colorScheme.primary,
                                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                                        focusedTextColor = MaterialTheme.colorScheme.primary,
                                        unfocusedTextColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                                Button(
                                    onClick = { if (newPassword.isNotBlank()) { onChangePassword(newPassword); newPassword = "" } },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(LocalizationService.getString("change_password", language))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth(),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Text(LocalizationService.getString("back", language), color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
