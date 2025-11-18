package com.huanli233.bilizepam.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.huanli233.biliwebapi.bean.video.VideoInfo
import com.huanli233.bilizepam.data.repository.SeriesRepository

class SeriesPagingSource(
    private val repository: SeriesRepository,
    private val type: String,
    private val mid: Long,
    private val id: Long
) : PagingSource<Int, VideoInfo>() {
    
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, VideoInfo> {
        return try {
            val page = params.key ?: 1
            
            val result = when (type) {
                "series" -> repository.getSeriesVideos(mid, id, page, params.loadSize)
                "season" -> repository.getSeasonVideos(mid, id, page, params.loadSize)
                else -> return LoadResult.Error(IllegalArgumentException("Unknown type: $type"))
            }
            
            result.fold(
                onSuccess = { seriesInfo ->
                    val videos = seriesInfo.archives
                    val pageInfo = seriesInfo.page
                    
                    val hasMore = when (type) {
                        "series" -> pageInfo?.let { 
                            videos.size >= (it.size ?: 0) && page * (it.size ?: 0) < (it.total ?: 0)
                        } ?: false
                        "season" -> pageInfo?.let {
                            videos.size >= (it.pageSize ?: 0) && page * (it.pageSize ?: 0) < (it.total ?: 0)
                        } ?: false
                        else -> false
                    }
                    
                    LoadResult.Page(
                        data = videos,
                        prevKey = if (page == 1) null else page - 1,
                        nextKey = if (hasMore) page + 1 else null
                    )
                },
                onFailure = { exception ->
                    LoadResult.Error(exception)
                }
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
    
    override fun getRefreshKey(state: PagingState<Int, VideoInfo>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}
