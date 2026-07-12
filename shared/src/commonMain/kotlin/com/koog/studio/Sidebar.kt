package com.koog.studio

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun Sidebar(
    threads: List<Thread>,
    activeThreadId: String?,
    onNewThread: () -> Unit,
    onSelectThread: (String) -> Unit,
) {
    Surface(
        modifier = Modifier.width(240.dp).fillMaxHeight(),
        tonalElevation = 3.dp,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
            ) {
                Button(
                    onClick = onNewThread,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("New Thread")
                }
            }

            HorizontalDivider()

            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
            ) {
                items(threads, key = { it.id }) { thread ->
                    val isActive = thread.id == activeThreadId
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectThread(thread.id) },
                        color = if (isActive) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surface,
                    ) {
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            text = thread.title,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        }
    }
}
