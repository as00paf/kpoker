package com.pafoid.kpoker.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.pafoid.kpoker.domain.model.Settings
import kpoker.composeapp.generated.resources.Res
import kpoker.composeapp.generated.resources.home_screen_bg
import org.jetbrains.compose.resources.painterResource

@Composable
fun SettingsScreen(
    settings: Settings,
    onSettingsChanged: (Settings) -> Unit,
    onBack: () -> Unit
) {
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
                .width(500.dp)
                .padding(16.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text(
                    text = "SETTINGS",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                // Fullscreen Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Full Screen Mode", style = MaterialTheme.typography.bodyLarge)
                    Switch(
                        checked = settings.isFullscreen,
                        onCheckedChange = { onSettingsChanged(settings.copy(isFullscreen = it)) }
                    )
                }

                Divider(color = Color.Gray.copy(alpha = 0.3f))

                // Music Volume
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Music Volume", style = MaterialTheme.typography.bodyLarge)
                        Text("${(settings.musicVolume * 100).toInt()}%")
                    }
                    Slider(
                        value = settings.musicVolume,
                        onValueChange = { onSettingsChanged(settings.copy(musicVolume = it)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // SFX Volume
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Sound Effects", style = MaterialTheme.typography.bodyLarge)
                        Text("${(settings.sfxVolume * 100).toInt()}%")
                    }
                    Slider(
                        value = settings.sfxVolume,
                        onValueChange = { onSettingsChanged(settings.copy(sfxVolume = it)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Back")
                }
            }
        }
    }
}
