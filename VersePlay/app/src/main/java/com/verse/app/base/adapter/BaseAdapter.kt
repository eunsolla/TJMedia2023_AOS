package com.verse.app.base.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.verse.app.base.viewholder.BaseViewHolder

/**
 * Description : Base ListAdapter Class
 *
 * Created by jhlee on 2023-01-01
 */
open class BaseAdapter<DATA : Any>(
    private val viewHolderFactory: (viewType: Int, parent: ViewGroup) -> BaseViewHolder<DATA>,
    private val viewTypeFactory: ((DATA) -> Int)?,
) : RecyclerView.Adapter<BaseViewHolder<DATA>>() {

    val dataList: MutableList<DATA> by lazy { mutableListOf() }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<DATA> {
        return viewHolderFactory.invoke(viewType, parent)
    }

    override fun getItemViewType(position: Int): Int {
        return dataList[position]?.let { viewTypeFactory?.invoke(it) } ?: super.getItemViewType(position)
    }

    override fun onBindViewHolder(holder: BaseViewHolder<DATA>, position: Int) {
        dataList[position]?.let { holder.bind(it) }
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

    override fun getItemCount(): Int {
        return dataList.size
    }

    fun addAll(dataList: MutableList<DATA>) {
        this.dataList.clear()
        this.dataList.addAll(dataList)
        this.notifyDataSetChanged()
    }

}