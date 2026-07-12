package com.koog.studio.tools

import ai.koog.agents.core.tools.SimpleTool
import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.serialization.typeToken
import kotlinx.serialization.Serializable
import java.io.File

object ReadFileTool : SimpleTool<ReadFileTool.Args>(
    argsType = typeToken<Args>(),
    name = "__read_file__",
    description = """
        Reads a text file with optional line range selection.
        
        Use this to:
        - Read entire text files or specific line ranges
        - Get file content along with metadata
        - Extract portions of files using 0-based line indexing
        
        Returns file content and metadata (name, path, size).
    """.trimIndent()
) {
    @Serializable
    data class Args(
        @property:LLMDescription("Absolute path to the text file you want to read (e.g., /home/user/file.txt)")
        val path: String,
        @property:LLMDescription("First line to include (0-based, inclusive). Default is 0 to start from beginning")
        val startLine: Int = 0,
        @property:LLMDescription("First line to exclude (0-based, exclusive). Use -1 to read until end. Default is -1")
        val endLine: Int = -1,
    )

    override suspend fun execute(args: Args): String {
        val file = File(args.path)
        
        if (!file.exists()) {
            return "Error: File not found: ${args.path}"
        }
        
        if (!file.isFile) {
            return "Error: Not a file: ${args.path}"
        }
        
        if (!file.canRead()) {
            return "Error: Cannot read file: ${args.path}"
        }
        
        return try {
            val lines = file.readLines()
            val start = maxOf(0, args.startLine)
            val end = if (args.endLine == -1) lines.size else minOf(lines.size, args.endLine)
            
            if (start >= lines.size) {
                return "Error: startLine ${args.startLine} exceeds file length (${lines.size} lines)"
            }
            
            val selectedLines = lines.subList(start, end)
            val content = selectedLines.joinToString("\n")
            
            val metadata = buildString {
                appendLine("File: ${file.name}")
                appendLine("Path: ${file.absolutePath}")
                appendLine("Size: ${file.length()} bytes")
                appendLine("Lines: ${lines.size}")
                appendLine("Showing lines $start-${end - 1}:")
                appendLine("---")
                append(content)
            }
            
            metadata
        } catch (e: Exception) {
            "Error reading file: ${e.message}"
        }
    }
}