package com.koog.studio.tools

import ai.koog.agents.core.tools.SimpleTool
import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.serialization.typeToken
import kotlinx.serialization.Serializable
import java.io.File
import java.util.concurrent.TimeUnit

object ExecuteCommandTool : SimpleTool<ExecuteCommandTool.Args>(
    argsType = typeToken<Args>(),
    name = "__execute_command__",
    description = """
        Executes a shell command in the specified directory and returns its output.
        
        Use this to:
        - Run build commands (gradle, npm, cargo, etc.)
        - Execute git operations
        - Run tests
        - Check system state or installed tools
        - Perform any terminal operation
        
        The command runs with a 30-second timeout.
        Returns stdout, stderr, and exit code.
    """.trimIndent()
) {
    @Serializable
    data class Args(
        @property:LLMDescription("The shell command to execute (e.g., 'ls -la', 'git status', 'npm test')")
        val command: String,
        @property:LLMDescription("Working directory for the command. Defaults to user home directory if not specified.")
        val workingDirectory: String = System.getProperty("user.home"),
    )

    override suspend fun execute(args: Args): String {
        val dir = File(args.workingDirectory)

        if (!dir.exists()) {
            return "Error: Directory not found: ${args.workingDirectory}"
        }

        if (!dir.isDirectory) {
            return "Error: Not a directory: ${args.workingDirectory}"
        }

        return try {
            val process = ProcessBuilder("sh", "-c", args.command)
                .directory(dir)
                .redirectErrorStream(true)
                .start()

            val completed = process.waitFor(30, TimeUnit.SECONDS)

            val output = process.inputStream.bufferedReader().readText().trim()

            if (!completed) {
                process.destroyForcibly()
                "Timeout after 30s:\n$output"
            } else {
                val exitCode = process.exitValue()
                if (exitCode == 0) {
                    output
                } else {
                    "Exit code $exitCode:\n$output"
                }
            }
        } catch (e: Exception) {
            "Error executing command: ${e.message}"
        }
    }
}
