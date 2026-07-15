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
        @property:LLMDescription("Working directory for the command. Use project root or appropriate subdirectory")
        val workingDirectory: String,
    )

    override suspend fun execute(args: Args): String {
        AgentStatusProvider.notify("Running: ${args.command.take(40)}")
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

            val output = process.inputStream.bufferedReader().readText()

            if (!completed) {
                process.destroyForcibly()
                "Error: Command timed out after 30 seconds\n\n$output"
            } else {
                val exitCode = process.exitValue()
                val prefix = if (exitCode == 0) "OK" else "FAILED (exit code: $exitCode)"
                "$prefix\n\n$output"
            }
        } catch (e: Exception) {
            "Error executing command: ${e.message}"
        }
    }
}
