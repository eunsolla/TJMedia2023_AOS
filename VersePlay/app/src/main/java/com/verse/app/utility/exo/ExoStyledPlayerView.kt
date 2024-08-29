package com.verse.app.utility.exo

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.verse.app.contants.Config
import com.verse.app.di.ExoModule
import com.verse.app.extension.multiNullCheck
import com.verse.app.model.feed.FeedContentsData
import com.verse.app.model.singpass.GenreRankingList
import com.verse.app.utility.DLogger
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


/**
 * Custom StyledPlayerView
 * Created by jhlee on 2023-02-01
 */
@AndroidEntryPoint
class ExoStyledPlayerView @JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : StyledPlayerView(context, attrs, defStyleAttr) {

    companion object {
        private const val TAG = "[ExoStyledPlayerView]"
    }

    enum class ExoPlayType {
        OBTAIN,
        LIST
    }

    @Inject
    lateinit var simpleCache: SimpleCache

    @Inject
    lateinit var progFactory: ProgressiveMediaSource.Factory

    // 컨텐츠 재생 정보 Data
    data class Item(
        var feedContentsData: FeedContentsData? = null,
        var feedContentsDataList: MutableList<FeedContentsData> = mutableListOf(),
        var url: String = "",
        var urlList: MutableList<String> = mutableListOf(),
        var genreRankingList: MutableList<GenreRankingList> = mutableListOf(),
        var thumbnailView: View? = null,
        var albumView: View? = null,
        var position: Int = -1,
        var mediaSource: MediaSource? = null,
        var mediaSourceList: MutableList<MediaSource>? = null,
        var playType: ExoPlayType? = ExoPlayType.OBTAIN,
        var btnPlayView: View? = null,
        var btBtnPlayView: View? = null,
        var playingTime: TextView? = null,
        var sbBar: SeekBar? = null,
        var totalTime: TextView? = null,
    )

    private val mFeedItem = Item() //컨텐츠 재생 정보

    init {
        if (!isInEditMode) {
            //UI Controller 숨김
            this.useController = false
            //화면 맞춤
            this.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
        }
    }

    /**
     * 컨텐츠 정보 Set
     */
    fun setPlayInfo(
        targetUri: String,
        view: View,
        album: View?,
        pos: Int,
        feedContentsData: FeedContentsData?,
        btnPlayView: View?,
        btBtnPlayView: View?,
        btPlayingTime: TextView?,
        btSbBar: SeekBar?,
        btTotalTime: TextView?
    ) {
        multiNullCheck(targetUri, view, pos) { url, view, pos ->
            mFeedItem.apply {
                this.playType = ExoPlayType.OBTAIN
                this.position = pos
                this.url = targetUri
                this.thumbnailView = view
                this.albumView = album
                this.btnPlayView = btnPlayView
                this.btBtnPlayView = btBtnPlayView
                this.feedContentsData = feedContentsData
                this.playingTime = btPlayingTime
                this.sbBar = btSbBar
                this.totalTime = btTotalTime
                this.mediaSource = run {
                    if (targetUri.startsWith("/data")) {
                        buildLocalMediaSource(targetUri)
                    } else {
                        buildMediaSource(Config.BASE_FILE_URL + targetUri)
                    }
                }
            }
        }
    }

    /**
     * 컨텐츠 정보 List Set
     */
    fun setPlayInfo(targetUriList: MutableList<String>, view: View, pos: Int, feedContentsDataList: MutableList<FeedContentsData>) {

        multiNullCheck(targetUriList, view, pos) { urlList, view, pos ->
            mFeedItem.apply {
                this.playType = ExoPlayType.LIST
                this.thumbnailView = view
                this.position = pos
                this.urlList = urlList
                this.feedContentsDataList = feedContentsDataList
                this.mediaSourceList = run {
                    var tmpList = mutableListOf<MediaSource>()
                    urlList.forEach { url ->
                        tmpList.add(buildMediaSource(Config.BASE_FILE_URL + url))
                    }
                    tmpList
                }
            }
        }
    }

    /**
     * 씽패스 컨텐츠 정보 List Set
     */
    fun setSingPassPlayInfo(genreRankingList: MutableList<GenreRankingList>, view: View, pos: Int) {

        multiNullCheck(genreRankingList, view, pos) { genreList, view, pos ->
            mFeedItem.apply {
                this.playType = ExoPlayType.LIST
                this.thumbnailView = view
                this.position = pos
                this.genreRankingList = genreRankingList
                this.mediaSourceList = run {
                    var tmpList = mutableListOf<MediaSource>()
                    genreRankingList.forEach { genreRankingList ->
                        tmpList.add(buildMediaSource(Config.BASE_FILE_URL + genreRankingList.highConPath))
                    }
                    tmpList
                }
            }
        }
    }

    /**
     * 재생 아이템 CreateMediaSource
     */
    private fun buildMediaSource(url: String): MediaSource {
        val isCached = simpleCache.isCached(url, 0, ExoModule.CACHE_SIZE_EACH_VIDEO)
        DLogger.d(TAG, "buildMediaSource isCached=> ${isCached} / ${url}")
        val mediaItem = MediaItem.Builder()
            .setCustomCacheKey(url)
            .setUri(Uri.parse(url))
            .build()
        return progFactory.createMediaSource(mediaItem)
    }

    /**
     * 디바이스 내부 재생 아이템 CreateMediaSource
     */
    private fun buildLocalMediaSource(url: String): MediaSource {
        return ProgressiveMediaSource.Factory(DefaultDataSource.Factory(context))
            .createMediaSource(MediaItem.fromUri(url))
    }


    /**
     * 컨텐츠  정보 Get
     */
    fun getFeedItem(): Item {
        return mFeedItem
    }
}
