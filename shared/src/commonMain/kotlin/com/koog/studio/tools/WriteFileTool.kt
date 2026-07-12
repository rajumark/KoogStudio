package com.koog.studio.tools

import ai.koog.agents.core.tools.SimpleTool
import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.serialization.typeToken
import kotlinx.serialization.Serializable
import java.io.File

object WriteFileTool : SimpleTool<WriteFileTool.Args>(
    argsType = typeToken<Args>(),
    name = "__write_file__",
    description = """
        Writes text content to a file at an absolute path. Creates parent directories if needed and overwrites existing content.
        
        Use this to:
        - Create new text files with content
        - Replace entire content of existing files
        
        Returns file metadata (name, path, size).
    """.trimIndent()
) {
    @Serializable
    data class Args(
        @property:LLMDescription("Absolute path to the target file (e.g., /home/user/file.txt)")
        val path: String,
        @property:LLMDescription("Text content to write (must not be empty). Overwrites existing content completely")
        val content: String
    )

    override suspend fun execute(args: Args): String {
        if (args.content.isEmpty()) {
            return "Error: Content must not be empty"
        }
        
        val file = File(args.path)
        
        return try {
            file.parentFile?.mkdirs()
            file.writeText(args.content)
            
            val metadata = buildString {
                appendLine("Written: ${file.name}")
                appendLine("Path: ${file.absolutePath}")
                appendLine("Size: ${file.length()} bytes")
            }
            
            metadata
        } catch (e: Exception) {
            "Error writing file: ${e.message}"
        }
    }
}