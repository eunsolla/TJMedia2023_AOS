package com.verse.app.repository.paging

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.rxjava3.observable
import com.verse.app.base.viewmodel.BaseViewModel
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

/**
 * Description : Base Paging Class
 *
 * Created by jhlee on 2023-02-03
 */
open class BaseRepository {

    companion object {
        const val DEFAULT_PAGE_NO = 1
        const val DEFAULT_PAGE_SIZE = 10
    }

    /**
     *  pageSize(loadSIze*3) :  => 10으로 설정하면 처음 loadSize는 30, 이후부터 10으로
     * 페이징 아이템 생성
     */
    open fun <V : Any> createPager(
        startPageNo: Int = DEFAULT_PAGE_NO,
        pageSize: Int? = DEFAULT_PAGE_SIZE,
        prefetchDis: Int? = pageSize,
        enablePlaceholders: Boolean = false,
        block: (Int) -> Single<PagingSource.LoadResult<Int, V>>,
    ): Observable<PagingData<V>> = Pager(
        config = PagingConfig(enablePlaceholders = enablePlaceholders, pageSize = pageSize!!, prefetchDistance = prefetchDis!!),
        pagingSourceFactory = { BasePagingSource(startPageNo, block) }
    ).observable

    /**
     * 페이징 아이템 Set
     */
    open fun <T : Any> toLoadResultOne(
        page: Int,
        data: List<T>,
    ): PagingSource.LoadResult<Int, T> {
        return PagingSource.LoadResult.Page(
            data = data,
            prevKey = null,
            nextKey = if (data == null || data.isEmpty()) null else page
        )
    }

    /**
     * 페이징 아이템 Set
     */
    open fun <T : Any> toLoadResult(
        page: Int,
        totalPageCount: Int? = 0,
        data: List<T>,
        totalCnt: Int? = 0,
    ): PagingSource.LoadResult<Int, T> {
        return PagingSource.LoadResult.Page(
            data = data,
            prevKey = if (page == 1) null else page - 1,
            nextKey = if (data.isEmpty() || page == totalPageCount || 0 == totalPageCount) {
                if (data.isNotEmpty() && data.size < totalCnt!!) {
                    page + 1
                } else {
                    null
                }
            } else {
                page + 1
            }
        )
    }

    protected fun <T : Any, S : Single<T>> S.doLoading(pageNo: Int, viewModel: BaseViewModel): Single<T> =
        doOnSubscribe {
            if (pageNo == 1) {
                viewModel.onLoadingShow()
            }
        }

}