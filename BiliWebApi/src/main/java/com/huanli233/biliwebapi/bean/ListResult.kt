package com.huanli233.biliwebapi.bean

import com.google.gson.annotations.SerializedName

data class ListResult<T>(
    @SerializedName("item", alternate = ["items", "list"]) val items: List<T>
)
