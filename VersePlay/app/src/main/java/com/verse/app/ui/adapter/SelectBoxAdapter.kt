package com.verse.app.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.verse.app.databinding.ItemSelectBoxBinding
import com.verse.app.model.common.SelectBoxData
import com.verse.app.ui.dialog.SelectDialog

class SelectBoxAdapter(
    itemList: ArrayList<SelectBoxData>,
    clickListener: OnClickListener,
    selectCallback: SelectDialog.SelectCallback
) :
    RecyclerView.Adapter<SelectBoxAdapter.SelectBoxViewHolder>() {

    var mItemList = itemList
    var mSelectCallback = selectCallback
    var mClickListener = clickListener

    inner class SelectBoxViewHolder(val binding: ItemSelectBoxBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: String, bctgMngCd: String) {
            with(binding) {
                qnaSelectItem.text = item
                rvSelectItem.setOnClickListener(
                    View.OnClickListener {
                        mSelectCallback.selectedCallback(item, bctgMngCd)
                        mClickListener.onClick(null)
                    })

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectBoxViewHolder {
        return SelectBoxViewHolder(
            ItemSelectBoxBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = mItemList.size

    override fun onBindViewHolder(holder: SelectBoxViewHolder, position: Int) {
        holder.bind(
            mItemList[position].contents.toString(),
            mItemList[position].bctgMngCd.toString()
        )

    }
}