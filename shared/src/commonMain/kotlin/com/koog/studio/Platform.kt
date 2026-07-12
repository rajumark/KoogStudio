package com.koog.studio

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform