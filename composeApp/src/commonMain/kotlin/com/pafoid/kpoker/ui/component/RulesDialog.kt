package com.pafoid.kpoker.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pafoid.kpoker.domain.model.*
import com.pafoid.kpoker.network.LocalizationService
import com.pafoid.kpoker.Gold

@Composable
fun RulesDialog(
    language: Language,
    onDismiss: () -> Unit,
    onPlaySound: () -> Unit = {}
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf(
        if (language == Language.FRENCH) "Règles" else "Rules",
        if (language == Language.FRENCH) "Classement" else "Rankings"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth(0.75f),
        title = {
            Text(
                text = LocalizationService.getString("poker_rules_title", language),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 600.dp)
            ) {
                PrimaryTabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { 
                                onPlaySound()
                                selectedTabIndex = index 
                            },
                            text = { Text(title, fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    if (selectedTabIndex == 0) {
                        Text(
                            text = LocalizationService.getString("poker_rules_content", language),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(8.dp)
                        )
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            HandRankingItem(
                                if (language == Language.FRENCH) "Quinte Flush Royale" else "Royal Flush",
                                if (language == Language.FRENCH) "10 au As de la même couleur" else "10 to Ace of the same suit",
                                listOf(
                                    Card(Rank.ACE, Suit.SPADES),
                                    Card(Rank.KING, Suit.SPADES),
                                    Card(Rank.QUEEN, Suit.SPADES),
                                    Card(Rank.JACK, Suit.SPADES),
                                    Card(Rank.TEN, Suit.SPADES)
                                ),
                                tag = if (language == Language.FRENCH) "MEILLEUR" else "BEST",
                                tagColor = Gold
                            )

                            HandRankingItem(
                                if (language == Language.FRENCH) "Quinte Flush" else "Straight Flush",
                                if (language == Language.FRENCH) "5 cartes consécutives de même couleur" else "5 consecutive cards of same suit",
                                listOf(
                                    Card(Rank.NINE, Suit.HEARTS),
                                    Card(Rank.EIGHT, Suit.HEARTS),
                                    Card(Rank.SEVEN, Suit.HEARTS),
                                    Card(Rank.SIX, Suit.HEARTS),
                                    Card(Rank.FIVE, Suit.HEARTS)
                                )
                            )

                            HandRankingItem(
                                if (language == Language.FRENCH) "Carré" else "Four of a Kind",
                                if (language == Language.FRENCH) "4 cartes de même valeur" else "4 cards of the same rank",
                                listOf(
                                    Card(Rank.ACE, Suit.SPADES),
                                    Card(Rank.ACE, Suit.HEARTS),
                                    Card(Rank.ACE, Suit.DIAMONDS),
                                    Card(Rank.ACE, Suit.CLUBS),
                                    Card(Rank.KING, Suit.SPADES)
                                )
                            )

                            HandRankingItem(
                                "Full House",
                                if (language == Language.FRENCH) "3 cartes identiques + 2 identiques" else "3 of a kind + a pair",
                                listOf(
                                    Card(Rank.KING, Suit.SPADES),
                                    Card(Rank.KING, Suit.HEARTS),
                                    Card(Rank.KING, Suit.DIAMONDS),
                                    Card(Rank.TEN, Suit.CLUBS),
                                    Card(Rank.TEN, Suit.SPADES)
                                )
                            )

                            HandRankingItem(
                                if (language == Language.FRENCH) "Couleur" else "Flush",
                                if (language == Language.FRENCH) "5 cartes de la même couleur" else "5 cards of the same suit",
                                listOf(
                                    Card(Rank.ACE, Suit.CLUBS),
                                    Card(Rank.TEN, Suit.CLUBS),
                                    Card(Rank.EIGHT, Suit.CLUBS),
                                    Card(Rank.SIX, Suit.CLUBS),
                                    Card(Rank.TWO, Suit.CLUBS)
                                )
                            )

                            HandRankingItem(
                                if (language == Language.FRENCH) "Quinte" else "Straight",
                                if (language == Language.FRENCH) "5 cartes de valeurs consécutives" else "5 cards of consecutive rank",
                                listOf(
                                    Card(Rank.NINE, Suit.SPADES),
                                    Card(Rank.EIGHT, Suit.HEARTS),
                                    Card(Rank.SEVEN, Suit.DIAMONDS),
                                    Card(Rank.SIX, Suit.CLUBS),
                                    Card(Rank.FIVE, Suit.SPADES)
                                )
                            )

                            HandRankingItem(
                                if (language == Language.FRENCH) "Brelan" else "Three of a Kind",
                                if (language == Language.FRENCH) "3 cartes de même valeur" else "3 cards of the same rank",
                                listOf(
                                    Card(Rank.QUEEN, Suit.SPADES),
                                    Card(Rank.QUEEN, Suit.HEARTS),
                                    Card(Rank.QUEEN, Suit.DIAMONDS),
                                    Card(Rank.ACE, Suit.CLUBS),
                                    Card(Rank.TWO, Suit.SPADES)
                                )
                            )

                            HandRankingItem(
                                if (language == Language.FRENCH) "Double Paire" else "Two Pair",
                                if (language == Language.FRENCH) "Deux paires différentes" else "Two different pairs",
                                listOf(
                                    Card(Rank.JACK, Suit.SPADES),
                                    Card(Rank.JACK, Suit.HEARTS),
                                    Card(Rank.FOUR, Suit.DIAMONDS),
                                    Card(Rank.FOUR, Suit.CLUBS),
                                    Card(Rank.ACE, Suit.SPADES)
                                )
                            )

                            HandRankingItem(
                                if (language == Language.FRENCH) "Paire" else "One Pair",
                                if (language == Language.FRENCH) "Deux cartes de même valeur" else "Two cards of the same rank",
                                listOf(
                                    Card(Rank.TEN, Suit.SPADES),
                                    Card(Rank.TEN, Suit.HEARTS),
                                    Card(Rank.ACE, Suit.DIAMONDS),
                                    Card(Rank.KING, Suit.CLUBS),
                                    Card(Rank.QUEEN, Suit.SPADES)
                                )
                            )

                            HandRankingItem(
                                if (language == Language.FRENCH) "Carte Haute" else "High Card",
                                if (language == Language.FRENCH) "La carte la plus forte" else "The highest card held",
                                listOf(
                                    Card(Rank.ACE, Suit.SPADES),
                                    Card(Rank.KING, Suit.HEARTS),
                                    Card(Rank.JACK, Suit.DIAMONDS),
                                    Card(Rank.SIX, Suit.CLUBS),
                                    Card(Rank.FOUR, Suit.SPADES)
                                ),
                                tag = if (language == Language.FRENCH) "PIRE" else "WORST",
                                tagColor = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = LocalizationService.getString("close", language),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        textContentColor = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
private fun HandRankingItem(
    name: String, 
    description: String, 
    cards: List<Card>,
    tag: String? = null,
    tagColor: Color = Color.Transparent
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            if (tag != null) {
                Spacer(Modifier.width(8.dp))
                Surface(
                    color = tagColor,
                    shape = MaterialTheme.shapes.extraSmall
                ) {
                    Text(
                        text = tag,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (tagColor == Gold) Color.Black else Color.White,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            cards.forEach { card ->
                PokerCard(card = card, modifier = Modifier.size(45.dp, 68.dp))
            }
        }
    }
}
