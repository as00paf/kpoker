package com.pafoid.kpoker

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import org.jetbrains.compose.resources.painterResource
import kpoker.composeapp.generated.resources.Res
import kpoker.composeapp.generated.resources.kpoker_icon

fun main() = application {
    val windowState = rememberWindowState(
        width = 1280.dp,
        height = 800.dp,
        position = WindowPosition.Aligned(androidx.compose.ui.Alignment.Center)
    )

    Window(
        onCloseRequest = ::exitApplication,
        state = windowState,
        title = "KPoker",
        icon = painterResource(Res.drawable.kpoker_icon)
    ) {
        App()
    }
}