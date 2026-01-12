package com.pafoid.kpoker.ui.component

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import com.pafoid.kpoker.domain.model.Language
import com.pafoid.kpoker.network.LocalizationService

@Composable
fun RulesDialog(
    language: Language,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = LocalizationService.getString("poker_rules_title", language),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = LocalizationService.getString("poker_rules_content", language),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
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
