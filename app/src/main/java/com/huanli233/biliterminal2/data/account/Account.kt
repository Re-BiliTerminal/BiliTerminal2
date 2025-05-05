package com.huanli233.biliterminal2.data.account

data class Account(
    val accountId: Long,
    val username: String?,
    val avatarUrl: String?,
    val lastActiveTime: Long
)

data class AccountTokenData(
    val cookies: String? = null,
    val refreshToken: String? = null,
    val appKey: String? = null
)