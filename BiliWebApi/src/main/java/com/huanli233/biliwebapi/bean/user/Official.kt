package com.huanli233.biliwebapi.bean.user

import com.google.gson.annotations.Expose

data class Official(
    @Expose val role: Int,
    @Expose val title: String,
    @Expose val desc: String,
    @Expose val type: Int
)
