package com.verse.app.ui.bindingadapter

import androidx.databinding.BindingAdapter
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

/**
 * Description : Pull to Refresh BindingAdapter
 *
 * Created by juhongmin on 2023/05/20
 */
object SwipeRefreshBindingAdapter {

    interface SwipeRefreshListener {
        fun callback()
    }

    /**
     * SwipeRefreshLayout 갱신 처리 함수
     * ex.)
     * <androidx.swiperefreshlayout.widget.SwipeRefreshLayout
     *
     * app:onRefresh="@{()->vm.start()}
     * />
     * @param listener SwipeRefreshListener
     */
    @JvmStatic
    @BindingAdapter("onRefresh")
    fun SwipeRefreshLayout.setSwipeRefreshListener(
        listener: SwipeRefreshListener?
    ) {
        // SwipeRefreshLayout 민감도 설정
        setDistanceToTriggerSync(300)
        setOnRefreshListener {
            runCatching {
                listener?.callback()
            }

            postDelayed({
                isRefreshing = false
            }, 500)
        }
    }
}