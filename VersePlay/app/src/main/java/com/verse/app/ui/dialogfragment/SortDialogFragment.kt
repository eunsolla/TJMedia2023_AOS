package com.verse.app.ui.dialogfragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.verse.app.BR
import com.verse.app.R
import com.verse.app.base.fragment.BaseBottomSheetDialogFragment
import com.verse.app.base.viewmodel.FragmentViewModel
import com.verse.app.databinding.DialogFragmentSortBinding
import com.verse.app.databinding.ItemSortBinding
import com.verse.app.extension.dp
import com.verse.app.model.enums.SortDialogType
import com.verse.app.model.enums.SortEnum
import com.verse.app.model.sort.SortCacheEntity
import com.verse.app.ui.decoration.VerticalDecorator
import com.verse.app.utility.provider.ResourceProvider
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Description : 정렬 DialogFragment
 *
 * Created by juhongmin on 2023/05/12
 */
@AndroidEntryPoint
class SortDialogFragment : BaseBottomSheetDialogFragment<DialogFragmentSortBinding, FragmentViewModel>() {

    override val layoutId: Int get() = R.layout.dialog_fragment_sort
    override val viewModel: FragmentViewModel by viewModels()
    override val bindingVariable: Int = BR.viewModel

    @Inject
    lateinit var resProvider: ResourceProvider

    interface Listener {
        fun onSortConfirm(selectSort: SortEnum, cacheEntity: SortCacheEntity)
    }

    private val adapter: Adapter by lazy { Adapter() }
    private var listener: Listener? = null
    private var type: SortDialogType = SortDialogType.DEFAULT
    private var cacheData: SortCacheEntity? = null

    override fun onStart() {
        super.onStart()
        if (dialog is BottomSheetDialog) {
            (dialog as BottomSheetDialog).runCatching {
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.skipCollapsed = true
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvContents.adapter = adapter
        binding.rvContents.addItemDecoration(VerticalDecorator(12.dp))
        // SortCacheEntity NonNull
        cacheData?.let { cache ->
            adapter.setData(getSortList(type, cache))
        } ?: run { dismiss() }
    }

    private fun getSortList(type: SortDialogType, cache: SortCacheEntity): List<SortUiModel> {
        val list = mutableListOf<SortUiModel>()
        type.list.forEach { pair ->
            list.add(SortUiModel(resProvider, pair, cache))
        }
        return list
    }

    /**
     * setListener
     */
    fun setListener(listener: Listener): SortDialogFragment {
        this.listener = listener
        return this
    }

    /**
     * 표시할 Dialog List Type
     * @param type 정렬에 표시할 리스트 Enum Type
     */
    fun setType(type: SortDialogType): SortDialogFragment {
        this.type = type
        return this
    }

    /**
     * 이전에 정렬 Show 한경우 해당 함수를 호출해야 한다.
     * Required
     * @param data Cache Data
     */
    fun setCacheData(data: SortCacheEntity?): SortDialogFragment {
        cacheData = data
        return this
    }

    fun simpleShow(fm: FragmentManager) {
        try {
            // 이미 보여지고 있는 Dialog 인경우 스킵
            if (!isAdded) {
                super.show(fm, "SortDialogFragment")
            }
        } catch (ex: Exception) {
        }
    }

    override fun onClick(v: View?) {
        // ignore
    }

    data class SortUiModel(
        val title: String,
        val enum: SortEnum,
        val isSelected: Boolean = false
    ) {
        constructor(
            resProvider: ResourceProvider,
            pair: Pair<Int, SortEnum>,
            cache: SortCacheEntity
        ) : this(
            title = resProvider.getString(pair.first),
            enum = pair.second,
            isSelected = pair.second == cache.selectSort
        )
    }

    /**
     * 정렬 전용 Adapter 이건 공통으로 하지 않고 단순하기 때문에 따로 빼서 처리함
     */
    inner class Adapter : RecyclerView.Adapter<Adapter.ViewHolder>() {

        private val dataList: MutableList<SortUiModel> by lazy { mutableListOf() }

        inner class SortDiffUtil(
            private val oldList: List<SortUiModel>,
            private val newList: List<SortUiModel>
        ) : DiffUtil.Callback() {
            override fun getOldListSize(): Int {
                return oldList.size
            }

            override fun getNewListSize(): Int {
                return newList.size
            }

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                val oldItem = oldList[oldItemPosition]
                val newItem = newList[newItemPosition]
                return oldItem.title == newItem.title
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                val oldItem = oldList[oldItemPosition]
                val newItem = newList[newItemPosition]
                return oldItem == newItem
            }
        }

        fun setData(list: List<SortUiModel>) {
            val diffUtil = DiffUtil.calculateDiff(SortDiffUtil(dataList, list))
            dataList.clear()
            dataList.addAll(list)
            diffUtil.dispatchUpdatesTo(this)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(parent)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            if (dataList.size > position) {
                holder.onBindView(dataList[position])
            }
        }

        override fun getItemCount(): Int {
            return dataList.size
        }

        inner class ViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_sort, parent, false)
        ) {

            val binding: ItemSortBinding by lazy { ItemSortBinding.bind(itemView) }

            init {
                itemView.setOnClickListener {
                    val data = binding.data ?: return@setOnClickListener
                    val cacheEntity = cacheData ?: return@setOnClickListener

                    // 같은 정렬 클릭시 스킵 처리
                    if (data.enum != cacheEntity.selectSort) {
                        cacheEntity.setSelectSort(data.title, data.enum)
                        listener?.onSortConfirm(data.enum, cacheEntity)
                        dismiss()
                    }
                }
            }

            fun onBindView(data: SortUiModel) {
                binding.setVariable(BR.data, data)
            }
        }
    }
}
