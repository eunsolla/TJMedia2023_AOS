package com.verse.app.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.verse.app.contants.Config
import com.verse.app.contants.LinkMenuTypeCode
import com.verse.app.databinding.ItemMainNoticePopupBinding
import com.verse.app.extension.dp
import com.verse.app.extension.getFragmentAct
import com.verse.app.extension.getFragmentActivity
import com.verse.app.extension.openBrowser
import com.verse.app.model.common.NoticeImageData
import com.verse.app.ui.bindingadapter.GlideBindingAdapter
import com.verse.app.utility.DLogger
import com.verse.app.utility.moveToLinkPage

class MainNoticePopupViewPagerAdapter(
    itemList: ArrayList<NoticeImageData>,
    clickListener: OnClickListener
) :
    RecyclerView.Adapter<MainNoticePopupViewPagerAdapter.MainNoticePopupViewHolder>() {
    var mItemList = itemList
    var mClickListener = clickListener

    inner class MainNoticePopupViewHolder(val binding: ItemMainNoticePopupBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: NoticeImageData) {
            DLogger.d("MainNoticePopupViewHolder : ${Config.BASE_FILE_URL + item.svcPopImgPath}")
            with(binding) {

                itemView.getFragmentActivity()?.let {
                    GlideBindingAdapter.loadImage(
                        popupImageView,
                        item.svcPopImgPath,
                        20.dp,
                        20.dp,
                        0,
                        0,
                        Glide.with(it)
                    )
                }

                popupImageView.setOnClickListener(
                    View.OnClickListener {
                        DLogger.d("Main Notice Popup Move Type : " + item.svcLdTpCd)
                        DLogger.d("Main Notice Popup Move URL : " + item.svcLdTpCd)
                        if (itemView.context.getFragmentAct() != null) {
                            if(item.svcLdTpCd != null){
                                if (item.svcLdTpCd == LinkMenuTypeCode.LINK_URL.code) {
                                    itemView.context.getFragmentAct()!!.openBrowser(item.svcPopUrl)
                                } else {
                                    itemView.context.getFragmentAct()!!.moveToLinkPage(item.svcLdTpCd, item.svcPopUrl)
                                }
                            }

                            mClickListener.onClick(null)
                        }
                    })

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainNoticePopupViewHolder {
        return MainNoticePopupViewHolder(
            ItemMainNoticePopupBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: MainNoticePopupViewHolder, position: Int) {
        holder.bind(mItemList.get(position))
    }

    override fun getItemCount(): Int = mItemList.size
}