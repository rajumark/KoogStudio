package com.koog.studio

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.swing.JFileChooser

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Sidebar(
    threads: List<Thread>,
    activeThreadId: String?,
    projectDir: String?,
    onNewThread: () -> Unit,
    onSelectThread: (String) -> Unit,
    onDeleteThread: (String) -> Unit,
    onProjectDirSelected: (String?) -> Unit,
) {
    var contextMenuThreadId by remember { mutableStateOf<String?>(null) }
    var isPickingFolder by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    fun pickFolder() {
        if (isPickingFolder) return
        isPickingFolder = true
        coroutineScope.launch {
            try {
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
            } finally {
                isPickingFolder = false
            }
        }
    }

    Box(
        modifier = Modifier
            .width(200.dp)
            .fillMaxHeight()
            .background(Color(0xFFF7F7F7)),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .background(Color(0xFFEEEEEE))
                    .combinedClickable(enabled = !isPickingFolder) { pickFolder() }
                    .padding(horizontal = 8.dp, vertical = 5.dp),
            ) {
                if (projectDir != null) {
                    val file = java.io.File(projectDir)
                    Text(
                        text = file.name,
                        fontSize = 11.sp,
                        color = Color(0xFF171717),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                } else {
                    Text(
                        text = "Project Folder",
                        fontSize = 11.sp,
                        color = Color(0xFF666666),
                    )
                }
            }

            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFE0E0E0)))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .background(Color(0xFFEEEEEE))
                    .combinedClickable(onClick = { onNewThread() })
                    .padding(horizontal = 8.dp, vertical = 5.dp),
            ) {
                Text(
                    text = "+ New",
                    fontSize = 12.sp,
                    color = Color(0xFF666666),
                )
            }

            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFE0E0E0)))

            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
            ) {
                items(threads, key = { it.id }) { thread ->
                    val isActive = thread.id == activeThreadId
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (isActive) Color(0xFFEEEEEE) else Color.Transparent)
                            .combinedClickable(
                                onClick = { onSelectThread(thread.id) },
                                onLongClick = { contextMenuThreadId = thread.id },
                            )
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                    ) {
                        Text(
                            text = thread.title,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = if (isActive) Color(0xFF171717) else Color(0xFF999999),
                        )
                    }
                }
            }
        }

        threads.forEach { thread ->
            DropdownMenu(
                expanded = contextMenuThreadId == thread.id,
                onDismissRequest = { contextMenuThreadId = null },
            ) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "Delete",
                            fontSize = 12.sp,
                            color = Color(0xFFCC0000),
                        )
                    },
                    onClick = {
                        onDeleteThread(thread.id)
                        contextMenuThreadId = null
                    },
                )
            }
        }
    }
}
