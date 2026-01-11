package com.pafoid.kpoker.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.pafoid.kpoker.domain.model.Card as CardModel
import com.pafoid.kpoker.domain.model.Rank
import com.pafoid.kpoker.domain.model.Suit
import kpoker.composeapp.generated.resources.*
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

import androidx.compose.ui.graphics.RectangleShape

@Composable
fun PokerCard(
    card: CardModel?,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.size(80.dp, 120.dp),
        shape = RectangleShape,
        color = Color.Transparent
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (card != null) {
                Image(
                    painter = painterResource(getCardResource(card)),
                    contentDescription = card.toString(),
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Image(
                    painter = painterResource(Res.drawable.red), // Default card back
                    contentDescription = "Hidden Card",
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

fun getCardResource(card: CardModel): DrawableResource {
    val suitPrefix = card.suit.name.lowercase()
    val rankSuffix = when (card.rank) {
        Rank.ACE -> "ace"
        Rank.JACK -> "jack"
        Rank.QUEEN -> "queen"
        Rank.KING -> "king"
        else -> card.rank.value.toString()
    }
    
    val resourceName = "${suitPrefix}_$rankSuffix"
    
    // This is a bit manual since we can't easily use reflection on Res.drawable
    // In a real production app, we might have a generated map or use a more dynamic approach
    return when (resourceName) {
        "clubs_2" -> Res.drawable.clubs_2
        "clubs_3" -> Res.drawable.clubs_3
        "clubs_4" -> Res.drawable.clubs_4
        "clubs_5" -> Res.drawable.clubs_5
        "clubs_6" -> Res.drawable.clubs_6
        "clubs_7" -> Res.drawable.clubs_7
        "clubs_8" -> Res.drawable.clubs_8
        "clubs_9" -> Res.drawable.clubs_9
        "clubs_10" -> Res.drawable.clubs_10
        "clubs_jack" -> Res.drawable.clubs_jack
        "clubs_queen" -> Res.drawable.clubs_queen
        "clubs_king" -> Res.drawable.clubs_king
        "clubs_ace" -> Res.drawable.clubs_ace
        
        "diamonds_2" -> Res.drawable.diamonds_2
        "diamonds_3" -> Res.drawable.diamonds_3
        "diamonds_4" -> Res.drawable.diamonds_4
        "diamonds_5" -> Res.drawable.diamonds_5
        "diamonds_6" -> Res.drawable.diamonds_6
        "diamonds_7" -> Res.drawable.diamonds_7
        "diamonds_8" -> Res.drawable.diamonds_8
        "diamonds_9" -> Res.drawable.diamonds_9
        "diamonds_10" -> Res.drawable.diamonds_10
        "diamonds_jack" -> Res.drawable.diamonds_jack
        "diamonds_queen" -> Res.drawable.diamonds_queen
        "diamonds_king" -> Res.drawable.diamonds_king
        "diamonds_ace" -> Res.drawable.diamonds_ace
        
        "hearts_2" -> Res.drawable.hearts_2
        "hearts_3" -> Res.drawable.hearts_3
        "hearts_4" -> Res.drawable.hearts_4
        "hearts_5" -> Res.drawable.hearts_5
        "hearts_6" -> Res.drawable.hearts_6
        "hearts_7" -> Res.drawable.hearts_7
        "hearts_8" -> Res.drawable.hearts_8
        "hearts_9" -> Res.drawable.hearts_9
        "hearts_10" -> Res.drawable.hearts_10
        "hearts_jack" -> Res.drawable.hearts_jack
        "hearts_queen" -> Res.drawable.hearts_queen
        "hearts_king" -> Res.drawable.hearts_king
        "hearts_ace" -> Res.drawable.hearts_ace
        
        "spades_2" -> Res.drawable.spades_2
        "spades_3" -> Res.drawable.spades_3
        "spades_4" -> Res.drawable.spades_4
        "spades_5" -> Res.drawable.spades_5
        "spades_6" -> Res.drawable.spades_6
        "spades_7" -> Res.drawable.spades_7
        "spades_8" -> Res.drawable.spades_8
        "spades_9" -> Res.drawable.spades_9
        "spades_10" -> Res.drawable.spades_10
        "spades_jack" -> Res.drawable.spades_jack
        "spades_queen" -> Res.drawable.spades_queen
        "spades_king" -> Res.drawable.spades_king
        "spades_ace" -> Res.drawable.spades_ace
        
        else -> Res.drawable.red // Fallback
    }
}
