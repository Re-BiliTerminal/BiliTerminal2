package com.huanli233.biliwebapi.bean.common

import android.os.Parcelable
import com.huanli233.biliwebapi.util.LowerCaseUnderScore
import kotlinx.parcelize.Parcelize

@Parcelize
data class SimpleAction(
    val action: Int,
    val entity: Entity,
    val meta: Meta = Meta("444.42.0.0", "unknown")
) : Parcelable {
    @Parcelize
    data class Meta(
        val spmid: String,
        val from: String
    ) : Parcelable

    @Parcelize
    data class Entity(
        @LowerCaseUnderScore val objectIdStr: String,
        val type: EntityType
    ) : Parcelable

    @Parcelize
    data class EntityType(
        val biz: Int
    ) : Parcelable
}
