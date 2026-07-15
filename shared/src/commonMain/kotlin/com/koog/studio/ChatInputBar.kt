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

@Composable
fun ChatInputBar(
    inputText: String,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    onStop: () -> Unit,
    isLoading: Boolean,
    agentStatus: String?,
    selectedModel: String,
    projectDir: String?,
    onProjectDirSelected: (String?) -> Unit,
) {
    var textFieldValue by remember { mutableStateOf(TextFieldValue(inputText)) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(inputText) {
        if (textFieldValue.text != inputText) {
            textFieldValue = TextFieldValue(inputText, selection = androidx.compose.ui.text.TextRange(inputText.length))
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        if (isLoading && agentStatus != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(14.dp),
                    strokeWidth = 2.dp,
                )
                Text(
                    text = agentStatus,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

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

            if (isLoading) {
                Button(
                    onClick = onStop,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                    ),
                ) {
                    Text("Stop")
                }
            } else {
                Button(
                    onClick = onSend,
                    enabled = inputText.isNotBlank(),
                ) {
                    Text("Send")
                }
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
        }
    }
}
