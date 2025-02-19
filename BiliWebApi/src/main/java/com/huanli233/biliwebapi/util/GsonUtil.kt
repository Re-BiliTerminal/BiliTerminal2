package com.huanli233.biliwebapi.util

import com.google.gson.GsonBuilder

internal val gson = GsonBuilder()
    .excludeFieldsWithoutExposeAnnotation()
    .setLenient()
    .create()