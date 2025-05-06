package com.huanli233.biliterminal2.utils.network

class Cookies(cookieString: String = "") : MutableMap<String, String> by mutableMapOf() {

    init {
        parseCookieString(cookieString)
    }

    private fun parseCookieString(cookieString: String) {
        clear()
        if (cookieString.isNotBlank()) {
            cookieString.split(";")
                .map { it.trim() }
                .filter { it.contains("=") }
                .map { it.split("=", limit = 2) }
                .forEach { parts ->
                    val key = parts[0].trim()
                    val value = parts[1].trim()
                    if (key.isNotEmpty()) {
                        put(key, value)
                    }
                }
        }
    }

    fun parse(cookieString: String) {
        parseCookieString(cookieString)
    }

    override fun toString(): String {
        return entries.joinToString(separator = "; ") { "${it.key}=${it.value}" }
    }
}