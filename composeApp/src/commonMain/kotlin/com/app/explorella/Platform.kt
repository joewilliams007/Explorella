package com.app.explorella

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

expect fun isDesktop(): Boolean

