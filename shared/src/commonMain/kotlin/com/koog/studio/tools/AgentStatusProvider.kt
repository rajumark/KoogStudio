package com.koog.studio.tools

object AgentStatusProvider {
    var onStatusChange: ((String?) -> Unit)? = null

    fun notify(status: String?) {
        onStatusChange?.invoke(status)
    }
}
