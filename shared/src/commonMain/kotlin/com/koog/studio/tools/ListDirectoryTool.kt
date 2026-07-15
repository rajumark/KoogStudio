package com.koog.studio.tools

import ai.koog.agents.core.tools.SimpleTool
import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.serialization.typeToken
import kotlinx.serialization.Serializable
import java.io.File

object ListDirectoryTool : SimpleTool<ListDirectoryTool.Args>(
    argsType = typeToken<Args>(),
    name = "__list_directory__",
    description = """
        List a directory as a tree so an agent can orient itself in an unknown filesystem/repo and decide what to read next.
        
        This is usually the first tool to call:
        - Find where code, configs, and docs live
        - Confirm filenames and exact paths before reading/editing
        - Narrow the search space with a glob instead of dumping huge directory listings
        
        Recommended agent workflow:
        1) Call with a small `depth` (often `1` or `2`) to understand the top-level layout.
        2) If you need to locate specific files, add a `filter` (glob) and increase `depth` to '5' or more.
        3) Once you see the exact path(s), switch to other tools to work with content.
        
        This tool does NOT:
        - Return file contents
        - Search inside files (it only matches on paths via `filter`)
        - Modify the filesystem (read-only)
    """.trimIndent()
) {
    @Serializable
    data class Args(
        @property:LLMDescription("Absolute path to the directory to list")
        val absolutePath: String,
        @property:LLMDescription("Maximum traversal depth (> 0). Default is 1")
        val depth: Int = 1,
        @property:LLMDescription("Optional glob filter for narrowing results (e.g., '*.kt', '**/*.java')")
        val filter: String? = null
    )

    override suspend fun execute(args: Args): String {
        val dir = File(args.absolutePath)
        
        if (!dir.exists()) {
            return "Error: Path does not exist: ${args.absolutePath}"
        }
        
        if (!dir.isDirectory) {
            return "Error: Path is not a directory: ${args.absolutePath}"
        }
        
        if (args.depth < 1) {
            return "Error: Depth must be at least 1 (got ${args.depth})"
        }
        
        return try {
            val tree = buildString {
                appendLine("Directory: ${dir.name}")
                appendLine("Path: ${dir.absolutePath}")
                appendLine("Contents:")
                appendLine("---")
                listDirectory(dir, "", args.depth, 0, args.filter, this)
            }
            tree
        } catch (e: Exception) {
            "Error listing directory: ${e.message}"
        }
    }
    
    private fun listDirectory(
        dir: File,
        prefix: String,
        maxDepth: Int,
        currentDepth: Int,
        filter: String?,
        sb: StringBuilder
    ) {
        if (currentDepth >= maxDepth) return
        
        val files = dir.listFiles()?.sortedWith(compareBy<File> { !it.isDirectory }.thenBy { it.name }) ?: return
        
        for ((index, file) in files.withIndex()) {
            val isLast = index == files.size - 1
            val connector = if (isLast) "└── " else "├── "
            val childPrefix = if (isLast) "    " else "│   "
            
            val matchesFilter = filter == null || matchesGlob(file.name, filter) || 
                (file.isDirectory && hasMatchingChild(file, filter, maxDepth - currentDepth - 1))
            
            if (matchesFilter) {
                if (file.isDirectory) {
                    sb.appendLine("$prefix$connector📁 ${file.name}/")
                    listDirectory(file, prefix + childPrefix, maxDepth, currentDepth + 1, filter, sb)
                } else {
                    sb.appendLine("$prefix$connector📄 ${file.name}")
                }
            }
        }
    }
    
    private fun hasMatchingChild(dir: File, filter: String, maxDepth: Int): Boolean {
        if (maxDepth <= 0) return false
        
        val files = dir.listFiles() ?: return false
        
        for (file in files) {
            if (matchesGlob(file.name, filter)) return true
            if (file.isDirectory && hasMatchingChild(file, filter, maxDepth - 1)) return true
        }
        
        return false
    }
    
    private fun matchesGlob(filename: String, pattern: String): Boolean {
        val regex = pattern
            .replace(".", "\\.")
            .replace("*", ".*")
            .replace("?", ".")
            .toRegex(RegexOption.IGNORE_CASE)
        return regex.matches(filename)
    }
}