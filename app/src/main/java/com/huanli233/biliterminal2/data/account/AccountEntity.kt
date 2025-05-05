package com.huanli233.biliterminal2.data.account

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey val accountId: Long,
    val username: String?,
    val avatarUrl: String?,
    val lastActiveTime: Long,
)