package com.huanli233.biliwebapi.bean.login

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Country(
    @Expose val id: Int,
    @Expose val cname: String,
    @SerializedName("country_id") @Expose val countryId: String
)
