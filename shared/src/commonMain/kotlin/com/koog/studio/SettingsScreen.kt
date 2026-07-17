package com.koog.studio

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class ApiKeyState(
    val key: String = "",
    val saved: Boolean = false,
)

@Composable
fun SettingsScreen(
    initialKeys: Map<String, String>,
    onSaveKeys: (Map<String, String>) -> Unit,
    onClose: () -> Unit,
) {
    val providers = listOf("OpenAI", "Anthropic", "Google")
    var keyStates by remember {
        mutableStateOf(
            providers.associateWith { provider ->
                ApiKeyState(key = initialKeys[provider] ?: "", saved = initialKeys[provider]?.isNotBlank() == true)
            }
        )
    }
    var showKeys by remember { mutableStateOf<Map<String, Boolean>>(emptyMap()) }

    Box(
        modifier = Modifier.fillMaxSize().background(Color.White),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Settings",
                    fontSize = 18.sp,
                    color = Color(0xFF171717),
                )

                Spacer(modifier = Modifier.weight(1f))

                Box(
                    modifier = Modifier
                        .background(Color(0xFFF0F0F0))
                        .clickable { onClose() }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                ) {
                    Text(
                        text = "Close",
                        fontSize = 12.sp,
                        color = Color(0xFF666666),
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "API Keys",
                fontSize = 14.sp,
                color = Color(0xFF171717),
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Add your API keys to use cloud AI providers. Keys are stored locally.",
                fontSize = 12.sp,
                color = Color(0xFF999999),
            )

            Spacer(modifier = Modifier.height(20.dp))

            providers.forEach { provider ->
                val currentKey = keyStates[provider]?.key ?: ""
                val isShowing = showKeys[provider] == true

                Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = provider,
                            fontSize = 13.sp,
                            color = Color(0xFF171717),
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        if (keyStates[provider]?.saved == true && currentKey.isBlank()) {
                            Text(
                                text = "Key saved",
                                fontSize = 10.sp,
                                color = Color(0xFF4CAF50),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp)
                                .background(Color(0xFFF5F5F5)),
                        ) {
                            BasicTextField(
                                value = currentKey,
                                onValueChange = { newKey ->
                                    keyStates = keyStates.toMutableMap().apply {
                                        put(provider, ApiKeyState(key = newKey, saved = false))
                                    }
                                },
                                modifier = Modifier.fillMaxSize(),
                                textStyle = TextStyle(
                                    fontSize = 12.sp,
                                    color = Color(0xFF171717),
                                ),
                                singleLine = true,
                                cursorBrush = SolidColor(Color(0xFF171717)),
                                visualTransformation = if (isShowing) VisualTransformation.None else PasswordVisualTransformation(),
                                decorationBox = { innerTextField ->
                                    Box(
                                        modifier = Modifier.padding(horizontal = 8.dp),
                                        contentAlignment = Alignment.CenterStart,
                                    ) {
                                        if (currentKey.isEmpty()) {
                                            Text(
                                                text = "${provider} API key",
                                                fontSize = 12.sp,
                                                color = Color(0xFFBBBBBB),
                                            )
                                        }
                                        innerTextField()
                                    }
                                },
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        Box(
                            modifier = Modifier
                                .height(32.dp)
                                .background(Color(0xFFF0F0F0))
                                .clickable { showKeys = showKeys.toMutableMap().apply { put(provider, !isShowing) } }
                                .padding(horizontal = 8.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = if (isShowing) "Hide" else "Show",
                                fontSize = 10.sp,
                                color = Color(0xFF666666),
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color(0xFFF0F0F0))
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .background(Color(0xFF171717))
                    .clickable {
                        val keysToSave = providers.associateWith { provider ->
                            keyStates[provider]?.key ?: ""
                        }
                        onSaveKeys(keysToSave)
                        onClose()
                    }
                    .padding(horizontal = 20.dp, vertical = 8.dp),
            ) {
                Text(
                    text = "Save & Close",
                    fontSize = 12.sp,
                    color = Color.White,
                )
            }
        }
    }
}
