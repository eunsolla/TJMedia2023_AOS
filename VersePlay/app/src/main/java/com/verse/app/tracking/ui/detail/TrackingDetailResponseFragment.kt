package com.verse.app.tracking.ui.detail

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.verse.app.tracking.ui.Extensions
import com.verse.app.tracking.ui.TrackingBottomSheetDialog
import com.verse.app.R
import com.verse.app.tracking.interceptor.model.TrackingHttpEntity
import com.verse.app.tracking.ui.adapter.TrackingAdapter
import com.verse.app.tracking.ui.models.BaseTrackingUiModel
import com.verse.app.tracking.ui.models.TrackingTitleUiModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.launch

/**
 * Description : 상세 > Response Fragment
 *
 * Created by juhongmin on 2022/04/02
 */
internal class TrackingDetailResponseFragment : Fragment(R.layout.f_tracking_detail_response) {

    companion object {
        fun newInstance(): TrackingDetailResponseFragment = TrackingDetailResponseFragment()
    }

    private lateinit var rvContents: RecyclerView
    private val adapter: TrackingAdapter by lazy { TrackingAdapter(this) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rvContents = view.findViewById(R.id.rvContents)
        rvContents.layoutManager = LinearLayoutManager(view.context)
        rvContents.adapter = adapter
        handleRequestDetailEntity()
    }

    /**
     * Request Detail 처리
     */
    private fun handleRequestDetailEntity() {
        val dialogFragment = parentFragment?.parentFragment
        if (dialogFragment is TrackingBottomSheetDialog) {
            val detailEntity = dialogFragment.getTempDetailData()
            if (detailEntity != null) {
                lifecycleScope.launch(Dispatchers.Main) {
                    val uiList = flowOf(detailEntity)
                        .map { parseUiModel(it) }
                        .flowOn(Dispatchers.IO)
                        .singleOrNull()
                    adapter.submitList(uiList)
                }
            }
        }
    }

    private fun parseUiModel(entity: TrackingHttpEntity): List<BaseTrackingUiModel> {
        val uiList = mutableListOf<BaseTrackingUiModel>()
        val res = entity.res ?: return emptyList()

        if (res.headerMap.isNotEmpty()) {
            uiList.add(TrackingTitleUiModel("[header]"))
            uiList.addAll(Extensions.parseHeaderUiModel(res.headerMap))
        }

        res.body?.let { body ->
            if (body.isNotEmpty()) {
                uiList.add(TrackingTitleUiModel("[body]"))
                uiList.addAll(Extensions.parseResBodyUiModel(body))
            }
        }
        return uiList
    }
}
