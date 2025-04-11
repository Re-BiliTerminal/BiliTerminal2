package com.huanli233.biliterminal2.event

class Event<out T>(private val content: T) {
    private var hasBeenHandled = false

    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) null else {
            hasBeenHandled = true
            content
        }
    }

    inline fun handle(
        handler: (data: T) -> Unit
    ) {
        getContentIfNotHandled()?.let {
            handler(it)
        }
    }
}

fun <T> T.event() = Event(this)

fun emptyEvent() = Event(Unit)