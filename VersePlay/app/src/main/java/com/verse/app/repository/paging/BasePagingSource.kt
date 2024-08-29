package com.verse.app.repository.paging

import androidx.paging.PagingState
import androidx.paging.rxjava3.RxPagingSource
import com.verse.app.extension.applyApiScheduler
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.delay

/**
 * Description : Base Paging Source
 *
 * Created by jhlee on 2023-02-02
 */
class BasePagingSource<V : Any>(
    val startPageNo : Int,
    private val block: (Int) -> Single<LoadResult<Int, V>>,
) : RxPagingSource<Int, V>() {

    override fun loadSingle(params: LoadParams<Int>): Single<LoadResult<Int, V>> {
        val pageNo = params.key ?: startPageNo
        return block(pageNo)
            .applyApiScheduler()
    }

    override fun getRefreshKey(state: PagingState<Int, V>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}