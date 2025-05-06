package com.huanli233.biliterminal2.ui.fragment.recommend

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.huanli233.biliterminal2.api.apiResultNonNull
import com.huanli233.biliterminal2.api.bilibiliApi
import com.huanli233.biliterminal2.utils.api.uniqId
import com.huanli233.biliwebapi.api.interfaces.IRecommendApi
import com.huanli233.biliwebapi.bean.video.VideoInfo

private const val STARTING_PAGE_INDEX = 1

class RecommendPagingSource: PagingSource<Int, VideoInfo>() {

    override fun getRefreshKey(state: PagingState<Int, VideoInfo>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        } ?: STARTING_PAGE_INDEX
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, VideoInfo> {
        val position = params.key ?: STARTING_PAGE_INDEX
        bilibiliApi.api(IRecommendApi::class) {
            getRecommend(
                uniqId = uniqId.toString(),
                freshIndex = position,
                freshIndex1h = position,
                brush = position
            )
        }.apiResultNonNull().onSuccess {
            return LoadResult.Page(
                data = it.item,
                prevKey = null,
                nextKey = position + 1
            )
        }.onFailure {
            return LoadResult.Error(it)
        }
        return LoadResult.Error(IllegalStateException())
    }

}