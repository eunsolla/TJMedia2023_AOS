package com.verse.app.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.verse.app.databinding.ItemTutorialBinding
import com.verse.app.model.mypage.TutorialItemData

class TutorialListAdapter(itemList: ArrayList<TutorialItemData>) :
    RecyclerView.Adapter<TutorialListAdapter.TutorialViewHolder>() {
    var mItemList = itemList

    inner class TutorialViewHolder(val binding: ItemTutorialBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TutorialItemData) {
            with(binding) {
                imgDrawble.setImageResource(item.imageRes)
                tvTitle.text = item.title
                tvSubTitle.text = item.subTitle
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TutorialViewHolder {
        return TutorialViewHolder(
            ItemTutorialBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: TutorialViewHolder, position: Int) {
        holder.bind(mItemList[position])
    }

    override fun getItemCount(): Int = mItemList.size

}