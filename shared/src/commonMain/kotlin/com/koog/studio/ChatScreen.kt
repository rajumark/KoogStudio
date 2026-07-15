package com.koog.studio

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.compose.viewmodel.koinViewModel
import java.awt.datatransfer.StringSelection
import java.awt.Toolkit

@Composable
fun ChatScreen(viewModel: ChatViewModel = koinViewModel()) {
    val threads by viewModel.threads.collectAsState()
    val activeThreadId by viewModel.activeThreadId.collectAsState()
    val messages = viewModel.activeMessages
    val isLoading by viewModel.isLoading.collectAsState()
    val agentStatus by viewModel.agentStatus.collectAsState()
    val agentLog by viewModel.agentLog.collectAsState()
    val selectedModel by viewModel.selectedModel.collectAsState()
    val availableModels by viewModel.availableModels.collectAsState()
    val projectDir by viewModel.projectDir.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var inputText by remember { mutableStateOf("") }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    Row(modifier = Modifier.fillMaxSize()) {
        Sidebar(
            threads = threads,
            activeThreadId = activeThreadId,
            projectDir = projectDir,
            onNewThread = viewModel::createNewThread,
            onSelectThread = viewModel::selectThread,
            onDeleteThread = viewModel::deleteThread,
            onProjectDirSelected = { viewModel.setProjectDir(it) },
        )

        Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(Color(0xFFE5E5E5)))

        Box(
            modifier = Modifier.weight(1f).fillMaxHeight().background(Color.White),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(36.dp).background(Color.White),
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "KoogStudio",
                            fontSize = 13.sp,
                            color = Color(0xFF171717),
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        if (messages.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFF0F0F0))
                                    .clickable {
                                        val text = messages.joinToString("\n\n") { msg ->
                                            val role = if (msg.isUser) "User" else "AI"
                                            "$role: ${msg.content}"
                                        }
                                        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
                                        clipboard.setContents(StringSelection(text), null)
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                            ) {
                                Text(
                                    text = "Copy All",
                                    fontSize = 11.sp,
                                    color = Color(0xFF666666),
                                )
                            }
                        }
                    }
                }

                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFE5E5E5)))

                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                ) {
                    if (messages.isEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = "KoogStudio",
                                fontSize = 16.sp,
                                color = Color(0xFFCCCCCC),
                            )
                        }
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            items(messages, key = { it.id }) { message ->
                                ChatBubble(message)
                            }

                            if (isLoading) {
                                item {
                                    Column(
                                        modifier = Modifier.padding(start = 4.dp, top = 4.dp),
                                    ) {
                                        if (agentLog.isEmpty()) {
                                            Text(
                                                text = "Thinking...",
                                                fontSize = 11.sp,
                                                color = Color(0xFFBBBBBB),
                                            )
                                        } else {
                                            agentLog.forEach { entry ->
                                                Text(
                                                    text = entry,
                                                    fontSize = 10.sp,
                                                    color = Color(0xFFBBBBBB),
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFE5E5E5)))

                ChatInputBar(
                    inputText = inputText,
                    onInputChange = { inputText = it },
                    onSend = {
                        if (inputText.isNotBlank()) {
                            viewModel.onInputChange(inputText)
                            viewModel.sendMessage()
                            inputText = ""
                        }
                    },
                    onStop = { viewModel.stopAgent() },
                    isLoading = isLoading,
                    agentStatus = agentStatus,
                    selectedModel = selectedModel,
                    availableModels = availableModels,
                    onModelSelected = { viewModel.selectModel(it) },
                )
            }
        }
    }
}
