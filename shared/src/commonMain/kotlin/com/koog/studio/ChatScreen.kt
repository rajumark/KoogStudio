package com.koog.studio

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(viewModel: ChatViewModel = koinViewModel()) {
    val threads by viewModel.threads.collectAsState()
    val activeThreadId by viewModel.activeThreadId.collectAsState()
    val messages = viewModel.activeMessages
    val isLoading by viewModel.isLoading.collectAsState()
    val models by viewModel.models.collectAsState()
    val selectedModel by viewModel.selectedModel.collectAsState()
    val selectedMode by viewModel.selectedMode.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val activeThread = threads.find { it.id == activeThreadId }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    Row(modifier = Modifier.fillMaxSize()) {
        Sidebar(
            threads = threads,
            activeThreadId = activeThreadId,
            onNewThread = viewModel::createNewThread,
            onSelectThread = viewModel::selectThread,
        )

        VerticalDivider()

        Surface(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                TopAppBar(
                    title = { Text("KoogStudio") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                )

                HorizontalDivider()

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    if (messages.isEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = "KoogStudio",
                                style = MaterialTheme.typography.headlineMedium,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = selectedModel.ifEmpty { "No model selected" },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            items(messages, key = { it.id }) { message ->
                                ChatBubble(message)
                            }

                            if (isLoading) {
                                item {
                                    TypingIndicator()
                                }
                            }
                        }
                    }
                }

                HorizontalDivider()

                ChatInputBar(
                    inputText = viewModel.inputText.collectAsState().value,
                    onInputChange = viewModel::onInputChange,
                    onSend = {
                        viewModel.sendMessage()
                        coroutineScope.launch {
                            listState.animateScrollToItem(messages.size)
                        }
                    },
                    isLoading = isLoading,
                    models = models,
                    selectedModel = selectedModel,
                    onModelSelected = viewModel::onModelSelected,
                    selectedMode = selectedMode,
                    onModeSelected = viewModel::onModeSelected,
                    projectDir = activeThread?.projectDir,
                    onProjectDirSelected = { viewModel.setProjectDir(it) },
                )
            }
        }
    }
}
