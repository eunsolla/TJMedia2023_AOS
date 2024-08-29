package com.verse.app.tracking.ui.viewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import com.verse.app.tracking.ui.models.BaseTrackingUiModel

internal abstract class BaseTrackingViewHolder(
    parent: ViewGroup,
    @LayoutRes layoutId: Int
) : RecyclerView.ViewHolder(
    LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
) {

    @Throws(Exception::class)
    abstract fun onBindView(model: BaseTrackingUiModel)
}
