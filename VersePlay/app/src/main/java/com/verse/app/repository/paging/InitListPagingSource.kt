package com.verse.app.repository.paging

import androidx.paging.PagingState
import androidx.paging.rxjava3.RxPagingSource
import com.verse.app.extension.applyApiScheduler
import com.verse.app.utility.DLogger
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers

/**
 * Description :
 *
 * Created by juhongmin on 2023/05/29
 */
class InitListPagingSource<T : Any>(
    private val initList: List<T>,
    private val initPageNo: Int,
    private val block: (Int) -> Single<LoadResult<Int, T>>
) : RxPagingSource<Int, T>() {

    override fun loadSingle(params: LoadParams<Int>): Single<LoadResult<Int, T>> {
        val pageNo = params.key ?: initPageNo
        DLogger.d("InitPage $initPageNo PageNo $pageNo")
        return if (initPageNo == pageNo) {
            Single.create<LoadResult<Int, T>> { emitter ->
                try {
                    val result = LoadResult.Page(
                        data = initList,
                        prevKey = pageNo.minus(1),
                        nextKey = pageNo.plus(1)
                    )
                    emitter.onSuccess(result)
                } catch (ex: Exception) {
                    emitter.onError(ex)
                }
            }.applyApiScheduler()
        } else {
            block(pageNo).applyApiScheduler()
        }
    }

    override fun getRefreshKey(state: PagingState<Int, T>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}