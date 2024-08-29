package com.verse.app.ui.bindingadapter

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.verse.app.base.model.PagingModel

/**
 * Description : RecyclerView 전용 BindingAdapter
 *
 * Created by juhongmin on 2023/05/11
 */
object RecyclerViewBindingAdapter {

    interface RecyclerViewPagingListener {
        fun loadPage()
    }

    /**
     * DataBinding 으로 페이징 구현 처리하는 함수
     * @param model 페이징 처리 관련 모델
     * @param callback 페이징 콜백 처리 리스너
     * @sample
     * <androidx.recyclerview.widget.RecyclerView
     *  android:id="@+id/rvContents"
     *  app:onLoadNextPage="@{()->vm.onLoadNextPage()}"
     *  app:pagingModel="@{vm.pagingModel}"/>
     *
     */
    @JvmStatic
    @BindingAdapter(value = ["pagingModel", "onLoadNextPage"], requireAll = false)
    fun setPagingListener(
        rv: RecyclerView,
        model: PagingModel,
        callback: RecyclerViewPagingListener?
    ) {
        if (callback == null) return

        rv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                // 페이징 스킵 조건 (페이지를 더이상 로드할수 없을때, API 로드중일때 (중복 로딩), 어댑터가 널일때
                if (model.isLast || model.isLoading || recyclerView.adapter == null) {
                    return
                } else {
                    val itemCount = recyclerView.adapter?.itemCount ?: 0
                    var pos = 0
                    // GridLayoutManager 은 LinearLayoutManager 로직과 동일하게 처리 함
                    when (val lm = recyclerView.layoutManager) {
                        is LinearLayoutManager -> pos = lm.findLastVisibleItemPosition()
                    }
                    // 현재 포지션이 중간 이상 넘어간 경우 페이징 처리
                    val updatePosition = itemCount - pos / 2
                    if (pos >= updatePosition) {
                        callback.loadPage()
                    }
                }
            }
        })
    }
}
