package com.koog.studio.tools

import ai.koog.agents.core.tools.SimpleTool
import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.serialization.typeToken
import kotlinx.serialization.Serializable
import java.io.File

object EditFileTool : SimpleTool<EditFileTool.Args>(
    argsType = typeToken<Args>(),
    name = "__edit_file__",
    description = """
        Makes a single, targeted text replacement in a file; can also create new files or fully replace contents.
        
        Use this to:
        - Modify specific functions or code blocks in existing files
        - Add new imports, methods, or classes to source files
        - Create new configuration files with complete content
        - Update documentation or README files
        - Fix bugs by replacing problematic code segments
        
        The 'original' text must match text in the file exactly.
        Use empty string for 'original' when creating new files or performing complete rewrites.
    """.trimIndent()
) {
    @Serializable
    data class Args(
        @property:LLMDescription("Absolute path to the target file that will be modified or created")
        val path: String,
        @property:LLMDescription("The exact text block that will be located and replaced. Use empty string for new files")
        val original: String,
        @property:LLMDescription("The new text content that will replace the original text block")
        val replacement: String
    )

    override suspend fun execute(args: Args): String {
        val file = File(args.path)
        
        return try {
            if (args.original.isEmpty()) {
                // Create new file or completely replace content
                file.parentFile?.mkdirs()
                file.writeText(args.replacement)
                "Successfully created/overwrote file: ${args.path}"
            } else {
                // Replace existing content
                if (!file.exists()) {
                    return "Error: File not found: ${args.path}"
                }
                
                if (!file.canRead()) {
                    return "Error: Cannot read file: ${args.path}"
                }
                
                if (!file.canWrite()) {
                    return "Error: Cannot write to file: ${args.path}"
                }
                
                val content = file.readText()
                
                if (!content.contains(args.original)) {
                    return "Error: Original text not found in file. Make sure the text matches exactly."
                }
                
                val updatedContent = content.replace(args.original, args.replacement, ignoreCase = false)
                file.writeText(updatedContent)
                "Successfully edited file: ${args.path}"
            }
        } catch (e: Exception) {
            "Error editing file: ${e.message}"
        }
    }
}