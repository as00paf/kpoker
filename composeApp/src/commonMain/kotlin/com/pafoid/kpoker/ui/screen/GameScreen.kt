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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
    onStartGame: () -> Unit,
    onPlaySound: () -> Unit
) {
    var currentTime by remember { mutableStateOf(getCurrentTimeMillis()) }
    val myPlayer = state.players.find { it.id == playerId }
    val isMyTurn = state.activePlayer?.id == playerId
    
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
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onLeave,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                    ) {
                        Text("Leave Game", color = Color.White)
                    }
                    
                    if (state.stage == GameStage.WAITING && state.players.size >= 2) {
                        Button(onClick = onStartGame) {
                            Text("Start Hand")
                        }
                    }
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

                // Top Right HUD - Chips
                Surface(
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = MaterialTheme.shapes.medium,
                    border = androidx.compose.foundation.BorderStroke(2.dp, Gold)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = Gold, modifier = Modifier.size(24.dp))
                        Column {
                            Text(
                                text = myPlayer?.name ?: "",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "${myPlayer?.chips ?: 0}",
                                style = MaterialTheme.typography.titleLarge,
                                color = Gold,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Other Players
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                state.players.filter { it.id != playerId }.forEach { player ->
                    val isActive = state.activePlayer?.id == player.id
                    val isHouse = player.name.contains("House", ignoreCase = true)
                    val scale = if (isHouse) 1.4f else 1.0f
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(8.dp).graphicsLayer(scaleX = scale, scaleY = scale)
                    ) {
                        Surface(
                            color = when {
                                isActive -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                isHouse -> Gold.copy(alpha = 0.1f)
                                else -> Color.Transparent
                            },
                            shape = MaterialTheme.shapes.medium,
                            border = when {
                                isActive -> androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                                isHouse -> androidx.compose.foundation.BorderStroke(2.dp, Gold)
                                else -> null
                            },
                            modifier = Modifier.padding(bottom = 4.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(8.dp)
                            ) {
                                Text(
                                    text = player.name,
                                    style = if (isHouse) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleMedium,
                                    color = if (isActive) MaterialTheme.colorScheme.primary else (if (isHouse) Gold else Color.White),
                                    fontWeight = FontWeight.Bold
                                )
                                if (isActive) {
                                    Text(
                                        text = "THINKING...",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }

                        Box(contentAlignment = Alignment.Center) {
                            Row {
                                if (state.stage == GameStage.SHOWDOWN && !player.isFolded) {
                                    player.holeCards.forEach { card ->
                                        PokerCard(card = card, modifier = Modifier.size((40 * scale).dp, (60 * scale).dp).padding(2.dp))
                                    }
                                } else if (!player.isFolded && player.holeCards.isNotEmpty()) {
                                    repeat(2) {
                                        PokerCard(card = null, modifier = Modifier.size((40 * scale).dp, (60 * scale).dp).padding(2.dp))
                                    }
                                }
                            }
                            
                            // Action indicator
                            player.lastAction?.let { action ->
                                Surface(
                                    color = Gold,
                                    shape = MaterialTheme.shapes.extraSmall,
                                    modifier = Modifier.offset(y = (20 * scale).dp)
                                ) {
                                    Text(
                                        text = action,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = Color.Black,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }
                        
                        Spacer(Modifier.height(8.dp))
                        Text("${player.chips} chips", color = Color.LightGray, style = if (isHouse) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.labelSmall)
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
            if (myPlayer != null) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Player's Cards (Raised and tilted)
                    Box(
                        modifier = Modifier.height(180.dp).fillMaxWidth().offset(y = (-20).dp),
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
                                    .size(110.dp, 165.dp) // Slightly bigger cards
                            )
                        }

                        // My Action indicator
                        myPlayer.lastAction?.let { action ->
                            Surface(
                                color = Gold,
                                shape = MaterialTheme.shapes.extraSmall,
                                modifier = Modifier.offset(y = 70.dp)
                            ) {
                                Text(
                                    text = action,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.Black,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }

                    Box(modifier = Modifier.height(40.dp), contentAlignment = Alignment.Center) {
                        if (isMyTurn) {
                            Surface(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                shape = MaterialTheme.shapes.small,
                                border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                            ) {
                                Text(
                                    "YOUR TURN",
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Surface(
                        modifier = Modifier.widthIn(max = 800.dp).fillMaxWidth()
                            .graphicsLayer(alpha = if (isMyTurn) 1.0f else 0.6f),
                        color = Color.Black.copy(alpha = 0.8f),
                        shape = MaterialTheme.shapes.medium,
                        border = androidx.compose.foundation.BorderStroke(if (isMyTurn) 2.dp else 1.dp, if (isMyTurn) MaterialTheme.colorScheme.primary else Gold.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (myPlayer.currentBet > 0) {
                                Column {
                                    Text(LocalizationService.getString("current_bet", state.settings.language), style = MaterialTheme.typography.labelSmall, color = Gold)
                                    Text("${myPlayer.currentBet}", style = MaterialTheme.typography.headlineSmall, color = Gold, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.width(24.dp))
                            }

                            val minRaiseTotal = state.currentMaxBet + state.minRaise
                            val maxRaiseTotal = myPlayer.chips + myPlayer.currentBet
                            
                            var betSliderValue by remember(state.activePlayerIndex) { 
                                mutableStateOf(minOf(maxRaiseTotal, minRaiseTotal).toFloat()) 
                            }

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (maxRaiseTotal >= minRaiseTotal && myPlayer.chips > (state.currentMaxBet - myPlayer.currentBet)) {
                                    // Quick Bet Buttons
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        val potOptions = listOf(
                                            "1/2 Pot" to (state.pot / 2 + state.currentMaxBet),
                                            "3/4 Pot" to (state.pot * 3 / 4 + state.currentMaxBet),
                                            "Pot" to (state.pot + state.currentMaxBet)
                                        )
                                        
                                        potOptions.forEach { (label, value) ->
                                            val targetValue = minOf(maxRaiseTotal, maxOf(minRaiseTotal, value))
                                            OutlinedButton(
                                                onClick = { 
                                                    onPlaySound()
                                                    betSliderValue = targetValue.toFloat() 
                                                },
                                                enabled = isMyTurn,
                                                modifier = Modifier.weight(1f),
                                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Gold),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, Gold.copy(alpha = if (isMyTurn) 0.5f else 0.2f))
                                            ) {
                                                Text(label, style = MaterialTheme.typography.labelSmall)
                                            }
                                        }
                                    }

                                    // Slider and +/- Buttons
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        IconButton(
                                            onClick = { 
                                                onPlaySound()
                                                betSliderValue = maxOf(minRaiseTotal.toFloat(), betSliderValue - state.bigBlind) 
                                            },
                                            enabled = isMyTurn,
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.Remove, contentDescription = null, tint = if (isMyTurn) Gold else Gold.copy(alpha = 0.3f))
                                        }
                                        
                                        Column(modifier = Modifier.weight(1f)) {
                                            Slider(
                                                value = betSliderValue,
                                                onValueChange = { betSliderValue = it },
                                                enabled = isMyTurn,
                                                valueRange = minRaiseTotal.toFloat()..maxRaiseTotal.toFloat(),
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = SliderDefaults.colors(
                                                    thumbColor = Gold,
                                                    activeTrackColor = Gold,
                                                    inactiveTrackColor = Gold.copy(alpha = 0.24f),
                                                    disabledThumbColor = Gold.copy(alpha = 0.3f),
                                                    disabledActiveTrackColor = Gold.copy(alpha = 0.1f)
                                                )
                                            )
                                            Text(
                                                text = "${LocalizationService.getString("bet_amount", state.settings.language)}: ${betSliderValue.toLong()}",
                                                color = if (isMyTurn) Gold else Gold.copy(alpha = 0.5f),
                                                style = MaterialTheme.typography.labelMedium,
                                                modifier = Modifier.fillMaxWidth(),
                                                textAlign = TextAlign.Center,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        
                                        IconButton(
                                            onClick = { 
                                                onPlaySound()
                                                betSliderValue = minOf(maxRaiseTotal.toFloat(), betSliderValue + state.bigBlind) 
                                            },
                                            enabled = isMyTurn,
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = null, tint = if (isMyTurn) Gold else Gold.copy(alpha = 0.3f))
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { onAction(BettingAction.Fold) }, 
                                        enabled = isMyTurn,
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F), disabledContainerColor = Color(0xFFD32F2F).copy(alpha = 0.3f)),
                                        modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(horizontal = 8.dp)
                                    ) {
                                        Text(LocalizationService.getString("fold", state.settings.language), color = if (isMyTurn) Color.White else Color.White.copy(alpha = 0.5f))
                                    }
                                    
                                    val isChecking = myPlayer.currentBet >= state.currentMaxBet
                                    Button(
                                        onClick = { if (isChecking) onAction(BettingAction.Check) else onAction(BettingAction.Call) },
                                        enabled = isMyTurn,
                                        modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(horizontal = 8.dp)
                                    ) {
                                        if (isChecking) {
                                            Text(LocalizationService.getString("check", state.settings.language))
                                        } else {
                                            val toCall = state.currentMaxBet - myPlayer.currentBet
                                            Text("${LocalizationService.getString("call", state.settings.language)} $toCall")
                                        }
                                    }
                                    
                                    if (myPlayer.chips >= (minRaiseTotal - myPlayer.currentBet) && maxRaiseTotal >= minRaiseTotal) {
                                        Button(
                                            onClick = { onAction(BettingAction.Raise(betSliderValue.toLong())) },
                                            enabled = isMyTurn,
                                            modifier = Modifier.weight(1.2f),
                                            contentPadding = PaddingValues(horizontal = 8.dp)
                                        ) {
                                            Text("${LocalizationService.getString("raise_to", state.settings.language)} ${betSliderValue.toLong()}")
                                        }
                                    }
                                    
                                    Button(
                                        onClick = { onAction(BettingAction.AllIn) },
                                        enabled = isMyTurn,
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32), disabledContainerColor = Color(0xFF2E7D32).copy(alpha = 0.3f)),
                                        modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(horizontal = 8.dp)
                                    ) {
                                        Text("ALL-IN", color = if (isMyTurn) Color.White else Color.White.copy(alpha = 0.5f))
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
                    
                    state.nextHandAt?.let { nextHandAt ->
                        val remaining = maxOf(0, nextHandAt - currentTime)
                        Text(
                            text = "Next hand starting in ${(remaining / 1000) + 1}s...",
                            style = MaterialTheme.typography.labelMedium,
                            color = Gold,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    } ?: run {
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
}