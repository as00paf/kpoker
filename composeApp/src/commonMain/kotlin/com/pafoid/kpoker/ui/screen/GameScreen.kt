package com.pafoid.kpoker.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.pafoid.kpoker.domain.model.BettingAction
import com.pafoid.kpoker.domain.model.GameState
import com.pafoid.kpoker.ui.component.PokerCard
import kpoker.composeapp.generated.resources.Res
import kpoker.composeapp.generated.resources.game_screen_bg
import org.jetbrains.compose.resources.painterResource

@Composable
fun GameScreen(
    state: GameState,
    playerId: String,
    onAction: (BettingAction) -> Unit,
    onLeave: () -> Unit,
    onStartGame: () -> Unit
) {
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
                
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        "Pot: ${state.pot}",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.headlineMedium
                    )
                }

                if (state.stage == com.pafoid.kpoker.domain.model.GameStage.WAITING && state.players.size >= 2) {
                    Button(onClick = onStartGame) {
                        Text("Start Hand")
                    }
                } else {
                    Spacer(modifier = Modifier.width(100.dp))
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
                    // Show placeholders or backs
                    Surface(
                        modifier = Modifier.size(80.dp, 120.dp).padding(4.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        shape = MaterialTheme.shapes.small,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    ) {}
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Controls
            val myPlayer = state.players.find { it.id == playerId }
            val isMyTurn = state.activePlayer?.id == playerId

            if (myPlayer != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Your Hand: ", style = MaterialTheme.typography.titleLarge)
                            myPlayer.holeCards.forEach { card ->
                                PokerCard(card = card, modifier = Modifier.padding(4.dp).size(60.dp, 90.dp))
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            Text("Chips: ${myPlayer.chips}", style = MaterialTheme.typography.titleLarge)
                        }

                        if (isMyTurn) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(onClick = { onAction(BettingAction.Fold) }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                                    Text("Fold")
                                }
                                if (myPlayer.currentBet >= state.currentMaxBet) {
                                    Button(onClick = { onAction(BettingAction.Check) }) {
                                        Text("Check")
                                    }
                                } else {
                                    Button(onClick = { onAction(BettingAction.Call) }) {
                                        Text("Call ${state.currentMaxBet - myPlayer.currentBet}")
                                    }
                                }
                                Button(onClick = { onAction(BettingAction.Raise(state.currentMaxBet + state.minRaise)) }) {
                                    Text("Raise ${state.currentMaxBet + state.minRaise}")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
