package com.huanli233.biliwebapi.httplib

object HeaderNames {
    const val USER_AGENT: String = "User-Agent"
    const val COOKIES: String = "Cookie"
    const val SET_COOKIE: String = "Set-Cookie"
    const val CONTENT_TYPE: String = "Content-Type"
    const val SEC_CH_UA: String = "Sec-Ch-Ua"
    const val SEC_CH_UA_PLATFORM: String = "Sec-Ch-Ua-Platform"
    const val SEC_CH_UA_MOBILE: String = "Sec-Ch-Ua-Mobile"
}

object HeaderValues {
    const val USER_AGENT_VAL: String =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.5414.75 Safari/537.36"
    const val REFERER: String = "https://www.bilibili.com"
    const val SEC_CH_UA: String = "\"Chromium\";v=\"109\", \"Not_A Brand\";v=\"99\""
    const val SEC_CH_UA_PLATFORM: String = "\"Windows\""
    const val SEC_CH_UA_MOBILE: String = "?0"
}

object Protocols {
    const val HTTP: String = "http://"
    const val HTTPS: String = "https://"
}

object Domains {
    const val MAIN_URL: String = "www.bilibili.com"
    const val BASE_API_URL: String = "api.bilibili.com"
    const val PASSPORT_URL: String = "passport.bilibili.com"
    const val VC_API_URL: String = "api.vc.bilibili.com"
    const val LIVE_API_URL: String = "api.live.bilibili.com"
    const val ACCOUNT_URL: String = "account.bilibili.com"
}