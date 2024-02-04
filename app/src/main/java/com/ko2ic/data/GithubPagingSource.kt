package com.ko2ic.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.ko2ic.api.GithubService
import com.ko2ic.model.Repo

class GithubPagingSource(
    private val apiQuery: String,
    private val service: GithubService,
) : PagingSource<Int, Repo>() {

    override suspend fun load(
        params: LoadParams<Int>
    ): LoadResult<Int, Repo> {
        return try {
            // Start refresh at page 1 if undefined.
            val nextPageNumber = params.key ?: 1
            val apiResponse = service.searchRepos(apiQuery, nextPageNumber, GithubRepository.NETWORK_PAGE_SIZE)
            val repos = apiResponse.items
            val endOfPaginationReached = repos.isEmpty()
            val nextKey = if (endOfPaginationReached) null else nextPageNumber + 1
            LoadResult.Page(
                data = apiResponse.items,
                prevKey = null, // Only paging forward.
                nextKey = nextKey
            )
        } catch (e: Exception) {
            // Handle errors in this block and return LoadResult.Error for
            // expected errors (such as a network failure).
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Repo>): Int? {
        // Try to find the page key of the closest page to anchorPosition from
        // either the prevKey or the nextKey; you need to handle nullability
        // here.
        //  * prevKey == null -> anchorPage is the first page.
        //  * nextKey == null -> anchorPage is the last page.
        //  * both prevKey and nextKey are null -> anchorPage is the
        //    initial page, so return null.
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

}