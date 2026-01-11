package com.pafoid.kpoker

import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import org.jetbrains.compose.resources.painterResource
import kpoker.composeapp.generated.resources.Res
import kpoker.composeapp.generated.resources.kpoker_icon

fun main() = application {
    val windowState = rememberWindowState(
        placement = WindowPlacement.Fullscreen
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