package com.verse.app.base.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.verse.app.R
import com.verse.app.databinding.ItemLoadStateBinding
import com.verse.app.utility.DLogger

/**
 * Description : Paging 3 하단 로딩  상태 아답터
 *
 * Created by jhlee on 2023-03-01
 */
class PagingLoadStateAdapter<T : Any>(
    private val adapter: BasePagingAdapter<T>,
) : LoadStateAdapter<PagingLoadStateAdapter.LoadStateItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState) =
        LoadStateItemViewHolder(
            ItemLoadStateBinding.bind(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_load_state, parent, false)
            )
        ) { adapter.retry() }

    override fun onBindViewHolder(holder: LoadStateItemViewHolder, loadState: LoadState) =
        holder.bind(loadState)

    class LoadStateItemViewHolder(
        private val binding: ItemLoadStateBinding,
        private val retryCallback: () -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
//            binding.retryButton.setOnClickListener { retryCallback() }
        }

        fun bind(loadState: LoadState) {
            with(binding) {
                DLogger.d("LoadStateItemViewHolder LoadState=> $loadState")
                progressBar.isVisible = loadState is LoadState.Loading
//                retryButton.isVisible = loadState is LoadState.Error
//                errorMsg.isVisible = !(loadState as? LoadState.Error)?.error?.message.isNullOrBlank()
//                errorMsg.text = (loadState as? LoadState.Error)?.error?.message
            }
        }
    }

    fun getPagingAdapter() : BasePagingAdapter<*> {
        return adapter
    }
}