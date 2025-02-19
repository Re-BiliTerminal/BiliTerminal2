package com.huanli233.biliwebapi.bean.user

import com.google.gson.annotations.Expose

data class OfficialVerify(
    @Expose val type: Int,
    @Expose val desc: String
)
