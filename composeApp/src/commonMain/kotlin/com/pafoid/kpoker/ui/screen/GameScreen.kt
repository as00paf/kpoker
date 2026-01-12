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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import com.pafoid.kpoker.domain.model.BettingAction
import com.pafoid.kpoker.domain.model.GameState
import com.pafoid.kpoker.ui.component.PokerCard
import kpoker.composeapp.generated.resources.Res
import kpoker.composeapp.generated.resources.game_screen_bg
import org.jetbrains.compose.resources.painterResource

import androidx.compose.ui.text.style.TextAlign
import com.pafoid.kpoker.Gold
import com.pafoid.kpoker.domain.model.GameStage

import com.pafoid.kpoker.getCurrentTimeMillis

@Composable
fun GameScreen(
    state: GameState,
    playerId: String,
    onAction: (BettingAction) -> Unit,
    onLeave: () -> Unit,
    onStartGame: () -> Unit
) {
    var currentTime by remember { mutableStateOf(getCurrentTimeMillis()) }
    
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(100)
            currentTime = getCurrentTimeMillis()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(Res.drawable.game_screen_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = onLeave) {
                    Text("Leave Game")
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Hand Status
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                        shape = MaterialTheme.shapes.extraSmall,
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        Text(
                            text = state.stage.name.replace("_", " "),
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            "${LocalizationService.getString("pot", state.settings.language)}: ${state.pot}",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    // Turn Timer
                    state.turnStartedAt?.let { startedAt ->
                        val elapsed = currentTime - startedAt
                        val remaining = maxOf(0, state.turnTimeoutMillis - elapsed)
                        val progress = remaining.toFloat() / state.turnTimeoutMillis
                        
                        Column(
                            modifier = Modifier.width(200.dp).padding(top = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier.fillMaxWidth().height(8.dp),
                                color = if (progress < 0.3f) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                strokeCap = StrokeCap.Round
                            )
                            Text(
                                text = "${(remaining / 1000)}s remaining",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                if (state.stage == GameStage.WAITING && state.players.size >= 2) {
                    Button(onClick = onStartGame) {
                        Text("Start Hand")
                    }
                } else {
                    Spacer(modifier = Modifier.width(100.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Other Players
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                state.players.filter { it.id != playerId }.forEach { player ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = player.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = if (state.activePlayer?.id == player.id) MaterialTheme.colorScheme.primary else Color.White
                        )
                        Row {
                            if (state.stage == GameStage.SHOWDOWN && !player.isFolded) {
                                player.holeCards.forEach { card ->
                                    PokerCard(card = card, modifier = Modifier.size(40.dp, 60.dp).padding(2.dp))
                                }
                            } else if (!player.isFolded && player.holeCards.isNotEmpty()) {
                                repeat(2) {
                                    PokerCard(card = null, modifier = Modifier.size(40.dp, 60.dp).padding(2.dp))
                                }
                            }
                        }
                        Text("${player.chips} chips", color = Color.LightGray, style = MaterialTheme.typography.labelSmall)
                        if (player.isFolded) Text("FOLDED", color = Color.Red, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Board Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                state.board.forEach { card ->
                    PokerCard(card = card, modifier = Modifier.padding(4.dp))
                }
                repeat(5 - state.board.size) {
                    Surface(
                        modifier = Modifier.size(80.dp, 120.dp).padding(4.dp),
                        color = Color.Black.copy(alpha = 0.3f),
                        shape = MaterialTheme.shapes.small,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                    ) {}
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // My Hand and Controls
            val myPlayer = state.players.find { it.id == playerId }
            val isMyTurn = state.activePlayer?.id == playerId

            if (myPlayer != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text("YOUR HAND", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                Row {
                                    myPlayer.holeCards.forEach { card ->
                                        PokerCard(card = card, modifier = Modifier.padding(4.dp).size(70.dp, 105.dp))
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.width(24.dp))
                            Column {
                                Text(LocalizationService.getString("chips", state.settings.language), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                Text("${myPlayer.chips}", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            }
                            
                            if (myPlayer.currentBet > 0) {
                                Spacer(modifier = Modifier.width(24.dp))
                                Column {
                                    Text(LocalizationService.getString("current_bet", state.settings.language), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                    Text("${myPlayer.currentBet}", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            if (isMyTurn) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(onClick = { onAction(BettingAction.Fold) }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                                        Text("Fold")
                                    }
                                    if (myPlayer.currentBet >= state.currentMaxBet) {
                                        Button(onClick = { onAction(BettingAction.Check) }) {
                                            Text("Check")
                                        }
                                    } else {
                                        Button(onClick = { onAction(BettingAction.Call) }) {
                                            val toCall = state.currentMaxBet - myPlayer.currentBet
                                            Text("Call $toCall")
                                        }
                                    }
                                    val minRaiseTotal = state.currentMaxBet + state.minRaise
                                    if (myPlayer.chips >= (minRaiseTotal - myPlayer.currentBet)) {
                                        Button(onClick = { onAction(BettingAction.Raise(minRaiseTotal)) }) {
                                            Text("Raise to $minRaiseTotal")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Winner Overlay
        state.lastHandResult?.let { result ->
            Surface(
                modifier = Modifier.align(Alignment.Center).padding(32.dp),
                color = Color.Black.copy(alpha = 0.9f),
                shape = MaterialTheme.shapes.large,
                border = androidx.compose.foundation.BorderStroke(2.dp, Gold)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = if (result.winners.size > 1) "SPLIT POT!" else "WINNER!",
                        style = MaterialTheme.typography.displayMedium,
                        color = Gold
                    )
                    
                    result.winners.forEach { winnerId ->
                        val winnerName = state.players.find { it.id == winnerId }?.name ?: "Unknown"
                        val wonAmount = result.amountWon[winnerId] ?: 0
                        val winningHand = result.playerHands[winnerId]
                        val handDesc = winningHand?.description ?: ""
                        
                        Text(
                            text = "$winnerName won $wonAmount chips",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White
                        )
                        
                        if (winningHand != null && winningHand.cards.isNotEmpty()) {
                            Row(
                                modifier = Modifier.padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                winningHand.cards.forEach { card ->
                                    PokerCard(card = card, modifier = Modifier.size(50.dp, 75.dp))
                                }
                            }
                        }

                        if (handDesc.isNotBlank()) {
                            Text(
                                text = "with $handDesc",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.LightGray
                            )
                        }
                    }
                    
                    Text(
                        text = "Next hand starting soon...",
                        style = MaterialTheme.typography.labelMedium,
                        color = Gold.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            }
        }
    }
}
