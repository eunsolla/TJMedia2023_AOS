package com.verse.app.base.viewholder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.databinding.library.baseAdapters.BR
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewTreeLifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.verse.app.base.viewmodel.BaseViewModel
import com.verse.app.entrypoint.ViewHolderEntryPoint
import com.verse.app.extension.getFragmentActivity
import dagger.hilt.EntryPoints

/**
 * Description : Base ListAdapter Class
 *
 * Created by jhlee on 2023-01-01
 */
open class BaseViewHolder<DATA>(
    private val binding: ViewDataBinding,
) : RecyclerView.ViewHolder(binding.root) {

    protected val entryPoint: ViewHolderEntryPoint by lazy {
        EntryPoints.get(itemView.context.applicationContext, ViewHolderEntryPoint::class.java)
    }

    companion object {
        fun <DATA> create(
            parent: ViewGroup,
            @LayoutRes layout: Int,
            vm: BaseViewModel
        ): BaseViewHolder<DATA> {
            val binding = DataBindingUtil.inflate<ViewDataBinding>(
                LayoutInflater.from(parent.context),
                layout,
                parent,
                false
            ).apply {
                setVariable(BR.viewModel, vm)
            }
            return BaseViewHolder(binding)
        }

        fun <T : ViewDataBinding> createBinding(
            parent: ViewGroup,
            @LayoutRes layout: Int,
            vm: BaseViewModel
        ): T {
            val binding = DataBindingUtil.inflate<T>(
                LayoutInflater.from(parent.context),
                layout,
                parent,
                false
            ).apply {
                setVariable(BR.viewModel, vm)
            }
            return binding
        }
    }

    init {
        bindingRequestManager()
    }

    open fun bind(data: DATA) {
        binding.apply {
            setVariable(BR.data, data)
            setVariable(BR.position, absoluteAdapterPosition)
            entryPoint.loginManager()?.getUserLoginData()?.let {
                setVariable(BR.myInfo, entryPoint.loginManager().getUserLoginData())
            }
        }.run {
            executePendingBindings()
        }
    }

    open fun bindPayload() {}

    open fun onViewAttachedToWindow(holder: BaseViewHolder<DATA>) {}
    open fun onViewDetachedToWindow(holder: BaseViewHolder<DATA>) {}
    open fun onViewRecycled() {}

    /**
     * Getter LifecycleOwner
     */
    protected fun getLifecycleOwner(v: View): LifecycleOwner? {
        return ViewTreeLifecycleOwner.get(v) ?: v.getFragmentActivity()
    }

    /**
     * Glide.RequestManager 가 필요한 ViewHolder 에서 호출하는 함수
     */
    protected fun bindingRequestManager() {
        itemView.getFragmentActivity()?.let {
            binding.setVariable(BR.requestManager, Glide.with(it))
        }
    }
}