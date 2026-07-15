package com.koog.studio

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ChatInputBar(
    inputText: String,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    onStop: () -> Unit,
    isLoading: Boolean,
    agentStatus: String?,
    selectedModel: String,
    availableModels: List<String>,
    onModelSelected: (String) -> Unit,
) {
    var textFieldValue by remember { mutableStateOf(TextFieldValue(inputText)) }
    var showModelMenu by remember { mutableStateOf(false) }

    LaunchedEffect(inputText) {
        if (textFieldValue.text != inputText) {
            textFieldValue = TextFieldValue(inputText, selection = androidx.compose.ui.text.TextRange(inputText.length))
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp)
                    .background(Color(0xFFF5F5F5))
                    .onPreviewKeyEvent { event ->
                        if (event.type == KeyEventType.KeyDown && event.key == Key.Enter) {
                            onSend()
                            true
                        } else {
                            false
                        }
                    },
            ) {
                BasicTextField(
                    value = textFieldValue,
                    onValueChange = { newValue ->
                        textFieldValue = newValue
                        onInputChange(newValue.text)
                    },
                    modifier = Modifier.fillMaxSize(),
                    textStyle = TextStyle(
                        fontSize = 13.sp,
                        color = Color(0xFF171717),
                    ),
                    singleLine = true,
                    cursorBrush = SolidColor(Color(0xFF171717)),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { onSend() }),
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier.padding(horizontal = 10.dp),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            if (textFieldValue.text.isEmpty()) {
                                Text(
                                    text = "Message...",
                                    fontSize = 13.sp,
                                    color = Color(0xFFAAAAAA),
                                )
                            }
                            innerTextField()
                        }
                    },
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .background(Color(0xFFEEEEEE))
                        .clickable { onStop() }
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Stop",
                        fontSize = 12.sp,
                        color = Color(0xFF666666),
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .background(if (inputText.isNotBlank()) Color(0xFF171717) else Color(0xFFF0F0F0))
                        .clickable(enabled = inputText.isNotBlank()) { onSend() }
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Send",
                        fontSize = 12.sp,
                        color = if (inputText.isNotBlank()) Color.White else Color(0xFFBBBBBB),
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box {
                Box(
                    modifier = Modifier
                        .background(Color(0xFFF0F0F0))
                        .clickable { showModelMenu = true }
                        .padding(horizontal = 6.dp, vertical = 3.dp),
                ) {
                    Text(
                        text = selectedModel,
                        fontSize = 10.sp,
                        color = Color(0xFF666666),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                DropdownMenu(
                    expanded = showModelMenu,
                    onDismissRequest = { showModelMenu = false },
                ) {
                    availableModels.forEach { model ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = model,
                                    fontSize = 11.sp,
                                    color = if (model == selectedModel) Color(0xFF171717) else Color(0xFF666666),
                                )
                            },
                            onClick = {
                                onModelSelected(model)
                                showModelMenu = false
                            },
                        )
                    }
                }
            }
        }
    }
}
