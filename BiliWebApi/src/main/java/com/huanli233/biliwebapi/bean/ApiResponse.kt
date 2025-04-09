package com.huanli233.biliwebapi.bean

data class ApiResponse<T>(
    val code: Int,
    val message: String,
    val data: T?
)
