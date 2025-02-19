package com.huanli233.biliwebapi.bean.user

import com.google.gson.annotations.Expose

data class Pendant(
    @Expose val pid: Int,
    @Expose val name: String,
    @Expose val image: String,
    @Expose val expire: Int
)
