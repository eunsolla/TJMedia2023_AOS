package com.verse.app.tracking.ui.viewholder

import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import com.verse.app.R
import com.verse.app.tracking.ui.models.BaseTrackingUiModel
import com.verse.app.tracking.ui.models.TrackingPathUiModel

/**
 * Description : Http Path ViewHolder
 *
 * Created by juhongmin on 2022/04/03
 */
internal class TrackingPathViewHolder(
    parent: ViewGroup
) : BaseTrackingViewHolder(
    parent,
    R.layout.vh_tracking_path
) {

    private val tvPath: AppCompatTextView by lazy { itemView.findViewById(R.id.tvPath) }

    override fun onBindView(model: BaseTrackingUiModel) {
        if (model is TrackingPathUiModel) {
            tvPath.text = model.path
        }
    }
}
