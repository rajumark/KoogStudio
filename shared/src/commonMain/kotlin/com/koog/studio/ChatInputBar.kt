package com.koog.studio

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.swing.JFileChooser
import javax.swing.filechooser.FileSystemView

@Composable
fun ChatInputBar(
    inputText: String,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    isLoading: Boolean,
    models: List<String>,
    selectedModel: String,
    onModelSelected: (String) -> Unit,
    selectedMode: String,
    onModeSelected: (String) -> Unit,
    projectDir: String?,
    onProjectDirSelected: (String?) -> Unit,
) {
    var textFieldValue by remember { mutableStateOf(TextFieldValue(inputText)) }
    var modelMenuExpanded by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(inputText) {
        if (textFieldValue.text != inputText) {
            textFieldValue = TextFieldValue(inputText, selection = androidx.compose.ui.text.TextRange(inputText.length))
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            OutlinedTextField(
                value = textFieldValue,
                onValueChange = { newValue ->
                    textFieldValue = newValue
                    onInputChange(newValue.text)
                },
                modifier = Modifier
                    .weight(1f)
                    .onPreviewKeyEvent { event ->
                        if (event.type == KeyEventType.KeyDown && event.key == Key.Enter) {
                            onSend()
                            true
                        } else {
                            false
                        }
                    },
                placeholder = { Text("Message KoogStudio...") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSend() }),
                minLines = 1,
                maxLines = 5,
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onSend,
                enabled = inputText.isNotBlank() && !isLoading,
            ) {
                Text(if (isLoading) "..." else "Send")
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedButton(
                onClick = {
                    coroutineScope.launch {
                        val dir = withContext(Dispatchers.IO) {
                            val chooser = JFileChooser().apply {
                                fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
                                dialogTitle = "Select Project Folder"
                                currentDirectory = projectDir?.let { java.io.File(it) }
                            }
                            val result = chooser.showOpenDialog(null)
                            if (result == JFileChooser.APPROVE_OPTION) {
                                chooser.selectedFile.absolutePath
                            } else {
                                null
                            }
                        }
                        if (dir != null) {
                            onProjectDirSelected(dir)
                        }
                    }
                },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            ) {
                val dirName = projectDir?.let { java.io.File(it).name } ?: "Project"
                Text(
                    text = dirName,
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                )
            }

            if (projectDir != null) {
                IconButton(
                    onClick = { onProjectDirSelected(null) },
                    modifier = Modifier.size(20.dp),
                ) {
                    Text("x", style = MaterialTheme.typography.labelSmall)
                }
            }

            Box {
                OutlinedButton(
                    onClick = { modelMenuExpanded = true },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = selectedModel.ifEmpty { "Select model" },
                        style = MaterialTheme.typography.labelMedium,
                    )
                }

                DropdownMenu(
                    expanded = modelMenuExpanded,
                    onDismissRequest = { modelMenuExpanded = false },
                ) {
                    if (models.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No models found") },
                            onClick = { modelMenuExpanded = false },
                        )
                    } else {
                        models.forEach { model ->
                            DropdownMenuItem(
                                text = { Text(model) },
                                onClick = {
                                    onModelSelected(model)
                                    modelMenuExpanded = false
                                },
                            )
                        }
                    }
                }
            }

            val modes = listOf("Chat Mode", "Agent Mode")
            modes.forEach { mode ->
                FilterChip(
                    selected = selectedMode == mode,
                    onClick = { onModeSelected(mode) },
                    label = { Text(mode, style = MaterialTheme.typography.labelSmall) },
                    enabled = mode == "Chat Mode",
                )
            }
        }
    }
}
