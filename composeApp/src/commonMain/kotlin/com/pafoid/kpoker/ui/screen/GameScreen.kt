package com.pafoid.kpoker.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
import com.pafoid.kpoker.network.LocalizationService

@Composable
fun GameScreen(
    state: GameState,
    playerId: String,
    onAction: (BettingAction) -> Unit,
    onLeave: () -> Unit,
    onRulesClick: () -> Unit,
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
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Player's Cards (Centered and Tilted)
                    Box(
                        modifier = Modifier.height(160.dp).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        myPlayer.holeCards.forEachIndexed { index, card ->
                            val rotation = if (index == 0) -10f else 10f
                            val offsetX = if (index == 0) (-30).dp else 30.dp
                            val offsetY = if (index == 0) 10.dp else 0.dp
                            
                            PokerCard(
                                card = card,
                                modifier = Modifier
                                    .offset(x = offsetX, y = offsetY)
                                    .graphicsLayer(rotationZ = rotation)
                                    .size(100.dp, 150.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Surface(
                        modifier = Modifier.widthIn(max = 800.dp).fillMaxWidth(),
                        color = Color.Black.copy(alpha = 0.8f),
                        shape = MaterialTheme.shapes.medium,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Gold.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(LocalizationService.getString("chips", state.settings.language), style = MaterialTheme.typography.labelSmall, color = Gold)
                                Text("${myPlayer.chips}", style = MaterialTheme.typography.headlineSmall, color = Gold, fontWeight = FontWeight.Bold)
                            }
                            
                            if (myPlayer.currentBet > 0) {
                                Spacer(modifier = Modifier.width(24.dp))
                                Column {
                                    Text(LocalizationService.getString("current_bet", state.settings.language), style = MaterialTheme.typography.labelSmall, color = Gold)
                                    Text("${myPlayer.currentBet}", style = MaterialTheme.typography.headlineSmall, color = Gold, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            if (isMyTurn) {
                                var betSliderValue by remember(state.activePlayerIndex) { 
                                    val minRaiseTotal = state.currentMaxBet + state.minRaise
                                    mutableStateOf(minRaiseTotal.toFloat()) 
                                }

                                Row(
                                    modifier = Modifier.weight(1f),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val minRaiseTotal = state.currentMaxBet + state.minRaise
                                    val maxRaiseTotal = myPlayer.chips + myPlayer.currentBet
                                    
                                    if (maxRaiseTotal > minRaiseTotal) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "${LocalizationService.getString("bet_amount", state.settings.language)}: ${betSliderValue.toLong()}",
                                                color = Gold,
                                                style = MaterialTheme.typography.labelMedium
                                            )
                                            Slider(
                                                value = betSliderValue,
                                                onValueChange = { betSliderValue = it },
                                                valueRange = minRaiseTotal.toFloat()..maxRaiseTotal.toFloat(),
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(onClick = { onAction(BettingAction.Fold) }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                                            Text(LocalizationService.getString("fold", state.settings.language))
                                        }
                                        if (myPlayer.currentBet >= state.currentMaxBet) {
                                            Button(onClick = { onAction(BettingAction.Check) }) {
                                                Text(LocalizationService.getString("check", state.settings.language))
                                            }
                                        } else {
                                            Button(onClick = { onAction(BettingAction.Call) }) {
                                                val toCall = state.currentMaxBet - myPlayer.currentBet
                                                Text("${LocalizationService.getString("call", state.settings.language)} $toCall")
                                            }
                                        }
                                        
                                        if (myPlayer.chips >= (minRaiseTotal - myPlayer.currentBet)) {
                                            Button(onClick = { onAction(BettingAction.Raise(betSliderValue.toLong())) }) {
                                                Text("${LocalizationService.getString("raise_to", state.settings.language)} ${betSliderValue.toLong()}")
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
