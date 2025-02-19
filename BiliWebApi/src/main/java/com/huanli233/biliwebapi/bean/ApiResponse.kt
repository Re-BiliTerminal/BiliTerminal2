package com.huanli233.biliwebapi.bean

import com.google.gson.annotations.Expose

data class ApiResponse<T>(
    @field:Expose val code: Int,
    @field:Expose val message: String,
    @field:Expose val data: T?
)
