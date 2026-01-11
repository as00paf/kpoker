package com.pafoid.kpoker.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.pafoid.kpoker.network.RoomInfo
import kpoker.composeapp.generated.resources.Res
import kpoker.composeapp.generated.resources.home_screen_bg
import org.jetbrains.compose.resources.painterResource

@Composable
fun LobbyScreen(
    rooms: List<RoomInfo>,
    onCreateRoom: (String) -> Unit,
    onCreateSinglePlayerRoom: () -> Unit,
    onJoinRoom: (String) -> Unit,
    onSettingsClick: () -> Unit,
    onLogout: () -> Unit
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
                    "Game Lobby",
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onCreateSinglePlayerRoom,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Text("Play vs The House")
                    }
                    Button(onClick = onSettingsClick) {
                        Text("Settings")
                    }
                    Button(onClick = onLogout) {
                        Text("Logout")
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
                    label = { Text("New Room Name") },
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                        focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                    )
                )
                Button(
                    onClick = { if (newRoomName.isNotBlank()) onCreateRoom(newRoomName) },
                    modifier = Modifier.height(56.dp)
                ) {
                    Text("Create Room")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                shape = MaterialTheme.shapes.medium
            ) {
                if (rooms.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No active rooms. Create one to start!", style = MaterialTheme.typography.bodyLarge)
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
                                        Text("${room.playerCount} players", style = MaterialTheme.typography.bodyMedium)
                                    }
                                    if (room.isStarted) {
                                        Badge(containerColor = MaterialTheme.colorScheme.error) {
                                            Text("In Progress")
                                        }
                                    } else {
                                        Badge(containerColor = MaterialTheme.colorScheme.primary) {
                                            Text("Waiting")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
