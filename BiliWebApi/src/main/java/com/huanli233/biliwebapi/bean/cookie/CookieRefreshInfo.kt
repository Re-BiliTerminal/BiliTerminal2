package com.huanli233.biliwebapi.bean.cookie

import android.os.Parcelable
import com.huanli233.biliwebapi.util.LowerCaseUnderScore
import kotlinx.parcelize.Parcelize

@Parcelize
data class CookieRefreshInfo(
    @LowerCaseUnderScore val refreshToken: String
): Parcelable