package com.koog.studio

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TypingIndicator() {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.CenterStart,
    ) {
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceVariant,
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text(
                    text = "Assistant",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    repeat(3) { index ->
                        val alpha by rememberInfiniteTransition(label = "dot$index").animateFloat(
                            initialValue = 0.3f,
                            targetValue = 1f,
                            animationSpec = infiniteRepeatable(
                                animation = androidx.compose.animation.core.tween(600, delayMillis = index * 200),
                                repeatMode = RepeatMode.Reverse,
                            ),
                            label = "dotAnim$index",
                        )
                        Surface(
                            modifier = Modifier.size(6.dp),
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha),
                        ) {}
                    }
                }
            }
        }
    }
}
