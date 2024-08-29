package com.verse.app.ui.feed.viewmodel

import androidx.lifecycle.LiveData
import androidx.paging.PagingData
import androidx.paging.filter
import com.verse.app.base.viewmodel.BaseFeedViewModel
import com.verse.app.contants.Config
import com.verse.app.contants.ExoPageType
import com.verse.app.extension.applyApiScheduler
import com.verse.app.extension.onMain
import com.verse.app.livedata.SingleLiveEvent
import com.verse.app.model.feed.FeedContentsData
import com.verse.app.utility.DLogger
import com.verse.app.utility.exo.ExoManager
import com.verse.app.utility.exo.ExoStyledPlayerView
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.kotlin.addTo
import kotlinx.coroutines.delay
import javax.inject.Inject

/**
 * Description :
 *
 * Created by juhongmin on 2023/05/29
 */
@HiltViewModel
class FeedDetailFragmentViewModel @Inject constructor(

) : BaseFeedViewModel(), ExoManager.ExoStateListener {

    private val _startBackEvent: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }
    val startBackEvent: LiveData<Unit> get() = _startBackEvent

    fun start(pos: Int, feedMngCd: String, pagingData: PagingData<FeedContentsData>) {
        _exoPageType.postValue(ExoPageType.FEED_DETAIL)
        exoManager.setExoStateListener(this)
        DLogger.d("FeedDetail Model $pos $feedMngCd $pagingData")
        Single.just(200)
            .applyApiScheduler()
            .doOnSuccess {
                _vpCurPosition.value = pos
                _vpPrePosition.value = -1
                _feedListPaging.value = pagingData.filter {
                    it.feedMngCd == feedMngCd
                }
                onMain {
                    delay(1000)
                    _vpAni.value = true
                    _feedListPaging.value = pagingData
                }
            }
            .subscribe().addTo(compositeDisposable)
    }

    fun onBack() {
        _startBackEvent.call()
    }

    override fun setPlayerView(playerView: ExoStyledPlayerView) {
        playerView?.let {
            it.getFeedItem()?.feedContentsData?.let {
                exoManager.goPreLoad(it.orgConPath)
            }

            if(it.getFeedItem().position == 0 && exoManager.getPlayerSize() <= 0){
                it.getFeedItem().position = _vpCurPosition.value
            }
            exoManager?.setPlayerView(it, _exoPageType.value, _vpCurPosition.value)
        }
    }


    override fun onStateReady() {
//        onLoadingShow()
    }

    override fun onStateBuffering() {
//        onLoadingDismiss()
    }

    override fun onStateError() {
//        onBack()
    }
}
