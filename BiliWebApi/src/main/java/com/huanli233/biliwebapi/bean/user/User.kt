package com.huanli233.biliwebapi.bean.user

import com.google.gson.annotations.Expose

data class User(
    @Expose val mid: Long,
    @Expose val name: String,
    @Expose val face: String
)
