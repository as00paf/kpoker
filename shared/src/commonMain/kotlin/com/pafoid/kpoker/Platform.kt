package com.pafoid.kpoker

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform