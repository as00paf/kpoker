package com.pafoid.kpoker.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import com.pafoid.kpoker.Gold
import com.pafoid.kpoker.network.RoomInfo
import kpoker.composeapp.generated.resources.Res
import kpoker.composeapp.generated.resources.home_screen_bg
import org.jetbrains.compose.resources.painterResource
import com.pafoid.kpoker.domain.model.Language
import com.pafoid.kpoker.network.AiDifficulty
import com.pafoid.kpoker.network.LocalizationService

@Composable
fun LobbyScreen(
    myPlayerId: String?,
    myUsername: String,
    myBankroll: Long?,
    rooms: List<RoomInfo>,
    language: Language,
    selectedDifficulty: AiDifficulty,
    onDifficultyChanged: (AiDifficulty) -> Unit,
    onCreateRoom: (String) -> Unit,
    onCreateSinglePlayerRoom: () -> Unit,
    onJoinRoom: (String) -> Unit,
    onRulesClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onLogout: () -> Unit,
    onQuit: () -> Unit
) {
    var newRoomName by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(Res.drawable.home_screen_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    LocalizationService.getString("lobby_title", language),
                    style = MaterialTheme.typography.displayMedium.copy(
                        shadow = androidx.compose.ui.graphics.Shadow(
                            color = Color.Black,
                            offset = androidx.compose.ui.geometry.Offset(4f, 4f),
                            blurRadius = 8f
                        )
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onRulesClick) {
                        Text(LocalizationService.getString("rules", language))
                    }
                    Button(onClick = onLogout) {
                        Text(LocalizationService.getString("logout", language))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // AI Difficulty and Single Player Button
            Surface(
                color = Color.Black.copy(alpha = 0.6f),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth(),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        LocalizationService.getString("select_difficulty", language) + ":",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    
                    AiDifficulty.entries.forEach { diff ->
                        FilterChip(
                            selected = selectedDifficulty == diff,
                            onClick = { onDifficultyChanged(diff) },
                            label = { Text(LocalizationService.getString(diff.name.lowercase(), language)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.Black
                            )
                        )
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    Button(
                        onClick = onCreateSinglePlayerRoom,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Icon(Icons.Default.Casino, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.Black)
                        Spacer(Modifier.width(8.dp))
                        Text(LocalizationService.getString("play_vs_house", language), color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newRoomName,
                    onValueChange = { newRoomName = it },
                    label = { Text(LocalizationService.getString("new_room_name", language)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                        focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                        unfocusedLabelColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )
                Button(
                    onClick = { if (newRoomName.isNotBlank()) onCreateRoom(newRoomName) },
                    modifier = Modifier.height(56.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(LocalizationService.getString("create_room", language))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Surface(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                shape = MaterialTheme.shapes.medium
            ) {
                if (rooms.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = LocalizationService.getString("no_rooms", language),
                            style = MaterialTheme.typography.bodyLarge,
                            color = Gold
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(rooms) { room ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { onJoinRoom(room.id) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(room.name, style = MaterialTheme.typography.titleLarge)
                                        Text("${room.playerCount} ${LocalizationService.getString("players", language)}", style = MaterialTheme.typography.bodyMedium)
                                    }
                                    if (room.isStarted) {
                                        Badge(containerColor = MaterialTheme.colorScheme.error) {
                                            Text(LocalizationService.getString("in_progress", language))
                                        }
                                    } else {
                                        Badge(containerColor = MaterialTheme.colorScheme.primary) {
                                            Text(LocalizationService.getString("waiting", language), color = Color.Black)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Reserve space for the floating bottom-right buttons
            Spacer(modifier = Modifier.height(80.dp))
        }

        // Bottom Right Buttons
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onSettingsClick,
                border = androidx.compose.foundation.BorderStroke(1.dp, Gold),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Gold),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(Icons.Filled.Settings, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(LocalizationService.getString("settings", language))
            }

            OutlinedButton(
                onClick = onQuit,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD32F2F)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD32F2F)),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(LocalizationService.getString("quit", language))
            }
        }
    }
}
