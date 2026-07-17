package com.koog.studio

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource

import koogstudio.shared.generated.resources.Res
import koogstudio.shared.generated.resources.send

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    val viewModel = remember { ChatViewModel() }
    val messages by viewModel.messages.collectAsState()
    val models by viewModel.availableModels.collectAsState()
    val selectedModel by viewModel.selectedModel.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isOllamaConnected by viewModel.isOllamaConnected.collectAsState()
    val threads by viewModel.threads.collectAsState()
    
    var inputText by remember { mutableStateOf("") }
    var showModelDropdown by remember { mutableStateOf(false) }

    MaterialTheme {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Left Sidebar - Chat
            Column(
                modifier = Modifier
                    .width(350.dp)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                // Chat Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Koog Agent",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = if (isOllamaConnected) "● Connected" else "● Disconnected",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isOllamaConnected) MaterialTheme.colorScheme.primary 
                                    else MaterialTheme.colorScheme.error
                        )
                    }
                }

                // Chat Messages
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(messages) { message ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
                        ) {
                            Surface(
                                shape = if (message.isUser) 
                                    RoundedCornerShape(12.dp, 12.dp, 4.dp, 12.dp)
                                else 
                                    RoundedCornerShape(12.dp, 12.dp, 12.dp, 4.dp),
                                color = if (message.isUser) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.widthIn(max = 280.dp)
                            ) {
                                Text(
                                    text = message.content,
                                    modifier = Modifier.padding(12.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (message.isUser) MaterialTheme.colorScheme.onPrimary
                                            else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                // Chat Input
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    tonalElevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        // Input Row - Prompt box + Send button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = inputText,
                                onValueChange = { inputText = it },
                                modifier = Modifier
                                    .weight(1f)
                                    .onPreviewKeyEvent { event ->
                                        if (event.type == KeyEventType.KeyDown && 
                                            event.key == Key.Enter &&
                                            !event.isShiftPressed) {
                                            if (inputText.isNotBlank() && !isLoading) {
                                                viewModel.sendMessage(inputText)
                                                inputText = ""
                                            }
                                            true
                                        } else {
                                            false
                                        }
                                    },
                                placeholder = {
                                    Text(
                                        text = "Tell me what to generate...",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                    focusedBorderColor = MaterialTheme.colorScheme.primary
                                ),
                                singleLine = true,
                                enabled = !isLoading
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            FilledIconButton(
                                onClick = {
                                    if (inputText.isNotBlank() && !isLoading) {
                                        viewModel.sendMessage(inputText)
                                        inputText = ""
                                    }
                                },
                                modifier = Modifier.size(44.dp),
                                shape = RoundedCornerShape(10.dp),
                                enabled = inputText.isNotBlank() && !isLoading
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        painter = painterResource(Res.drawable.send),
                                        contentDescription = "Send",
                                        tint = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Model Selector Row
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { showModelDropdown = true },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = selectedModel,
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            
                            DropdownMenu(
                                expanded = showModelDropdown,
                                onDismissRequest = { showModelDropdown = false }
                            ) {
                                models.forEach { model ->
                                    DropdownMenuItem(
                                        text = { Text(model) },
                                        onClick = {
                                            viewModel.selectModel(model)
                                            showModelDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Divider
            HorizontalDivider(
                modifier = Modifier.fillMaxHeight(),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )

            // Right Panel - Empty Content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // Empty for now
            }
        }
    }
}