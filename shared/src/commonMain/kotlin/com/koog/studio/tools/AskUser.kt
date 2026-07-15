package com.koog.studio.tools

import ai.koog.agents.core.tools.SimpleTool
import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.serialization.typeToken
import kotlinx.serialization.Serializable

object AskUser : SimpleTool<AskUser.Args>(
    argsType = typeToken<Args>(),
    name = "__ask_user__",
    description = "Service tool, used by the agent to talk with user"
) {
    @Serializable
    data class Args(
        @property:LLMDescription("Message from the agent")
        val message: String
    )

    override suspend fun execute(args: Args): String {
        AgentStatusProvider.notify("Asking user...")
        println(args.message)
        return readln()
    }
}