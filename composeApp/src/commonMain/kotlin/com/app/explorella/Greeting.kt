package com.meet.bottom_navigation_bar_navigation_rail

import com.app.explorella.getPlatform

class Greeting {
    private val platform = getPlatform()

    fun greet(): String {
        return "Hello, ${platform.name}!"
    }
}