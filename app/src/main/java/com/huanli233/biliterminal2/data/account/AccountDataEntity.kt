package com.huanli233.biliterminal2.data.account

import androidx.room.Entity
import androidx.room.PrimaryKey
import okhttp3.Cookie

val emptyAccount = AccountEntity(
    accountId = 0,
    lastActiveTime = 0
)

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey val accountId: Long,
    val refreshToken: String? = null,
    val appKey: String? = null,
    val userName: String? = null,
    val avatarUrl: String? = null,
    val lastActiveTime: Long,
)

@Entity(tableName = "cookies")
data class CookieEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    var accountId: Long?,
    var name: String,
    var value: String,
    var expires: Long? = null,
    var domain: String? = null,
    var hostOnly: Boolean = false,
    var path: String? = null,
    var secure: Boolean = false,
    var httpOnly: Boolean = false
) {
    fun toOkHttpCookie(): Cookie {
        val builder = Cookie.Builder()
            .name(name)
            .value(value)
            .path(path.toString())
            .apply {
                if (hostOnly) {
                    hostOnlyDomain(domain.toString())
                } else {
                    domain(domain.toString())
                }
            }

        expires?.let { builder.expiresAt(it) }
        if (secure) {
            builder.secure()
        }
        if (httpOnly) {
            builder.httpOnly()
        }
        return builder.build()
    }
}

fun Cookie.toCookieEntity(accountId: Long? = null): CookieEntity {
    val expiresAt = if (expiresAt == -1L) null else expiresAt

    return CookieEntity(
        accountId = accountId,
        name = name,
        value = value,
        expires = expiresAt,
        domain = domain,
        path = path,
        secure = secure,
        httpOnly = httpOnly
    )
}