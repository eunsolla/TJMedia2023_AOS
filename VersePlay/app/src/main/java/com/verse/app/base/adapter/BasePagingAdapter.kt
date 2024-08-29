package com.verse.app.base.adapter

import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.verse.app.base.viewholder.BaseViewHolder

/**
 * Description : Base ListAdapter Class
 *
 * Created by jhlee on 2023-01-01
 */
open class BasePagingAdapter<DATA : Any>(
    diffUtil: DiffUtil.ItemCallback<DATA>,
    private val viewHolderFactory: (viewType: Int, parent: ViewGroup) -> BaseViewHolder<DATA>,
    private val viewTypeFactory: ((DATA) -> Int)?,
) : PagingDataAdapter<DATA, BaseViewHolder<DATA>>(diffUtil) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<DATA> {
        return viewHolderFactory.invoke(viewType, parent)
    }

    override fun getItemViewType(position: Int): Int {
        return getItem(position)?.let { viewTypeFactory?.invoke(it) } ?: super.getItemViewType(position)
    }

    override fun onBindViewHolder(holder: BaseViewHolder<DATA>, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    override fun onViewAttachedToWindow(holder: BaseViewHolder<DATA>) {
        super.onViewAttachedToWindow(holder)
        holder.onViewAttachedToWindow(holder)
    }

    override fun onViewDetachedFromWindow(holder: BaseViewHolder<DATA>) {
        super.onViewDetachedFromWindow(holder)
        holder.onViewDetachedToWindow(holder)
    }

    override fun onViewRecycled(holder: BaseViewHolder<DATA>) {
        super.onViewRecycled(holder)
        holder.onViewRecycled()
    }

}