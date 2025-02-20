package com.huanli233.biliwebapi.bean

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class ListResult<T>(
    @Expose @SerializedName("item", alternate = ["items", "list"]) val items: List<T>
)
