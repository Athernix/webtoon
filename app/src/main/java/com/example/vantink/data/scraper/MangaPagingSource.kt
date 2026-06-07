package com.example.vantink.data.scraper

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.vantink.domain.model.SearchFilter
import com.example.vantink.domain.model.Webtoon

// Paging source para soportar paginación en búsqueda de webtoons
class MangaPagingSource(
    private val source: Source,
    private val filter: SearchFilter,
) : PagingSource<Int, Webtoon>() {
    
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Webtoon> {
        val page = params.key ?: 1
        return try {
            val filterWithPage = filter.copy(page = page)
            val results = source.searchWebtoons(filterWithPage)
            
            LoadResult.Page(
                data = results,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (results.isEmpty()) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Webtoon>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val closestPage = state.closestPageToPosition(anchorPosition)
            closestPage?.prevKey?.plus(1) ?: closestPage?.nextKey?.minus(1)
        }
    }
}