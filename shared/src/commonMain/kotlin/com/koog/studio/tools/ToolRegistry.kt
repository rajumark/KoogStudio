package com.koog.studio.tools

import ai.koog.agents.core.tools.ToolRegistry

fun createToolRegistry(): ToolRegistry {
    return ToolRegistry {
        tool(SayToUser)
        tool(AskUser)
        tool(ExitTool)
        tool(ReadFileTool)
        tool(EditFileTool)
        tool(ListDirectoryTool)
        tool(WriteFileTool)
    }
}