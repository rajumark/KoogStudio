package com.koog.studio.tools

import ai.koog.agents.core.tools.SimpleTool
import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.serialization.typeToken
import kotlinx.serialization.Serializable

object SayToUser : SimpleTool<SayToUser.Args>(
    argsType = typeToken<Args>(),
    name = "__say_to_user__",
    description = "Service tool, used by the agent to talk."
) {
    @Serializable
    data class Args(
        @property:LLMDescription("Message from the agent")
        val message: String
    )

    override suspend fun execute(args: Args): String {
        AgentStatusProvider.notify("Speaking...")
        println("Agent says: ${args.message}")
        return "DONE"
    }
}