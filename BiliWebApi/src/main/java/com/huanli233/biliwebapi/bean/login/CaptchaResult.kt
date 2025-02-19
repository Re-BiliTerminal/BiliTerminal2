package com.huanli233.biliwebapi.bean.login

import com.google.gson.annotations.Expose

data class CaptchaResult(
    @Expose val validate: String,
    @Expose val seccode: String
)
