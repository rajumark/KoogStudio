package com.koog.studio

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.halilibo.richtext.commonmark.Markdown
import com.halilibo.richtext.ui.material3.RichText

@Composable
fun ChatBubble(message: ChatMessage) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val clipboardManager = LocalClipboardManager.current

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (message.isUser) Alignment.CenterEnd else Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 520.dp)
                .hoverable(interactionSource)
                .background(
                    if (message.isUser) Color(0xFFF0F0F0) else Color(0xFFF8F8F8)
                )
                .padding(horizontal = 8.dp, vertical = 5.dp),
        ) {
            Box {
                if (message.isUser) {
                    Text(
                        text = message.content,
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        color = Color(0xFF171717),
                    )
                } else {
                    ProvideTextStyle(
                        TextStyle(fontSize = 11.sp, lineHeight = 15.sp, color = Color(0xFF171717))
                    ) {
                        RichText {
                            Markdown(message.content)
                        }
                    }
                }

                if (isHovered) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(2.dp)
                            .size(18.dp)
                            .background(Color(0xFFE0E0E0))
                            .clickable { clipboardManager.setText(AnnotatedString(message.content)) },
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "C",
                                fontSize = 8.sp,
                                color = Color(0xFF999999),
                            )
                        }
                    }
                }
            }
        }
    }
}
