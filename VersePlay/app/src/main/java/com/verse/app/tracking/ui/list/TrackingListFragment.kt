package com.verse.app.tracking.ui.list

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.coroutineScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.verse.app.R
import com.verse.app.tracking.interceptor.TrackingDataManager
import com.verse.app.tracking.interceptor.model.BaseTrackingEntity
import com.verse.app.tracking.interceptor.model.TrackingHttpEntity
import com.verse.app.tracking.ui.models.BaseTrackingUiModel
import com.verse.app.tracking.ui.adapter.TrackingAdapter
import com.verse.app.tracking.ui.models.TrackingListUiModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.launch

/**
 * Description : HTTP Tracking List Fragment
 *
 * Created by juhongmin on 2023/01/06
 */
internal class TrackingListFragment : Fragment(R.layout.f_tracking_list) {

    private lateinit var rvContents: RecyclerView

    private val adapter: TrackingAdapter by lazy { TrackingAdapter(this) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rvContents = view.findViewById(R.id.rvContents)
        rvContents.layoutManager = LinearLayoutManager(view.context)
        rvContents.adapter = adapter

        setTrackingData(TrackingDataManager.getInstance().getTrackingList())

        TrackingDataManager.getInstance().setListener(object : TrackingDataManager.Listener {
            override fun onNotificationTrackingEntity() {
                setTrackingData(TrackingDataManager.getInstance().getTrackingList())
            }
        })
    }

    /**
     * 데이터 업데이트 처리 함수
     */
    private fun setTrackingData(newList: List<BaseTrackingEntity>) {
        lifecycle.coroutineScope.launch(Dispatchers.Main) {
            val uiList = flowOf(newList)
                .map { it.toChildTrackingModel() }
                .flowOn(Dispatchers.IO)
                .singleOrNull() ?: listOf()

            adapter.submitList(uiList)
        }
    }

    /**
     * Converter BaseTrackingEntity to TrackingListUiModel
     */
    private inline fun <reified T : List<BaseTrackingEntity>> T.toChildTrackingModel(): List<BaseTrackingUiModel> {
        val uiList = mutableListOf<BaseTrackingUiModel>()
        this.forEach {
            if (it is TrackingHttpEntity) {
                uiList.add(TrackingListUiModel(it))
            }
        }
        return uiList
    }

    companion object {
        fun newInstance(): TrackingListFragment = TrackingListFragment()
    }
}
