package com.koog.studio.tools

object AgentStatusProvider {
    var onStatusChange: ((String?) -> Unit)? = null
    var onLog: ((String) -> Unit)? = null

    fun notify(status: String?) {
        onStatusChange?.invoke(status)
    }

    fun log(entry: String) {
        onLog?.invoke(entry)
    }
}
