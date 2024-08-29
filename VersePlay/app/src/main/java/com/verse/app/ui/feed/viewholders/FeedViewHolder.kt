package com.verse.app.ui.feed.viewholders

import android.animation.ObjectAnimator
import android.app.ActionBar.LayoutParams
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.view.doOnAttach
import androidx.core.view.doOnDetach
import androidx.core.view.updateLayoutParams
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.verse.app.BR
import com.verse.app.R
import com.verse.app.base.viewholder.BaseFeedViewHolder
import com.verse.app.base.viewmodel.BaseViewModel
import com.verse.app.contants.AppData
import com.verse.app.databinding.ItemFeedBinding
import com.verse.app.extension.changeInVisible
import com.verse.app.extension.changeVisible
import com.verse.app.extension.onMain
import com.verse.app.extension.setReHeight
import com.verse.app.model.feed.FeedContentsData
import com.verse.app.ui.feed.viewmodel.FeedDetailFragmentViewModel
import com.verse.app.utility.DLogger
import com.verse.app.utility.manager.CommentCountManager
import com.verse.app.utility.manager.UserFeedBlockManager
import com.verse.app.utility.manager.UserFeedBookmarkManager
import com.verse.app.utility.manager.UserFeedDeleteManager
import com.verse.app.utility.manager.UserFeedInterestManager
import com.verse.app.utility.manager.UserFeedLikeManager
import com.verse.app.utility.manager.UserFollowManager
import com.verse.app.widget.views.CustomLinkableTextView
import kotlinx.coroutines.delay

/**
 * Description :
 *
 * Created by jhlee on 2023/06/04
 */
class FeedViewHolder(
    parent: ViewGroup,
    private val viewModel: BaseViewModel,
    private val itemBinding: ItemFeedBinding = createBinding(parent, R.layout.item_feed, viewModel)
) : BaseFeedViewHolder<FeedContentsData>(itemBinding), LifecycleEventObserver {

    companion object {
        const val MAX_TAG_LINE_CNT = 2
        const val MAX_TAG_EXP_LINE_CNT = 15
    }

    init {

        if (viewModel is FeedDetailFragmentViewModel) {
            itemBinding.setVariable(BR.isDetail, true)
        }

        itemView.doOnAttach { v ->
            val owner = getLifecycleOwner(v) ?: return@doOnAttach
            owner.lifecycle.addObserver(this)
        }

        itemView.doOnDetach { v ->
            val owner = getLifecycleOwner(v) ?: return@doOnDetach
            owner.lifecycle.removeObserver(this)
            closeDisposable()
        }

        itemBinding.ivLike.setOnClickListener {
            val model = itemBinding.data ?: return@setOnClickListener
            handleLike(model)
        }

        itemBinding.ivBookMark.setOnClickListener {
            val model = itemBinding.data ?: return@setOnClickListener
            handleBookMark(model)
        }

        itemBinding.ivComment.setOnClickListener {
            val model = itemBinding.data ?: return@setOnClickListener
            moveToComment(model)
        }

        itemBinding.ivMore.setOnClickListener {
            val model = itemBinding.data ?: return@setOnClickListener
            val isDetail = itemBinding.isDetail ?: false
            showMore(model, isDetail, viewModel)
        }

        itemBinding.ivAlbum.setOnClickListener {
            val model = itemBinding.data ?: return@setOnClickListener
            moveToCollection(model)
        }

        itemBinding.ivMike.setOnClickListener {
            val model = itemBinding.data ?: return@setOnClickListener
            moveToSing(model)
        }
        itemBinding.tvJoin.setOnClickListener {
            val model = itemBinding.data ?: return@setOnClickListener
            moveToSing(model)
        }

        itemBinding.tvAdDetail.setOnClickListener {
            val model = itemBinding.data ?: return@setOnClickListener
            moveToADWabView(model)
        }

        itemBinding.ivPartA.setOnClickListener {
            val model = itemBinding.data ?: return@setOnClickListener
            handleDuetFeedFollow(it, true, model)
        }

        itemBinding.ivPartB.setOnClickListener {
            val model = itemBinding.data ?: return@setOnClickListener
            handleDuetFeedFollow(it, false, model)
        }

        itemBinding.ivSoloUserF.setOnClickListener {
            val model = itemBinding.data ?: return@setOnClickListener
            handleFeedFollow(it, model)
        }

        itemBinding.ivSoloUser.setOnClickListener {
            val model = itemBinding.data ?: return@setOnClickListener
            var userMemCd: String

            if (model.ownerMemCd.isNotEmpty()) {
                userMemCd = model.ownerMemCd
            } else if (model.partAmemCd.isNotEmpty()) {
                userMemCd = model.partAmemCd
            } else if (model.partBmemCd.isNotEmpty()) {
                userMemCd = model.partBmemCd
            } else {
                return@setOnClickListener
            }

            handleUserDetail(it, userMemCd)
        }

        itemBinding.userAImageView.setOnClickListener {
            val model = itemBinding.data ?: return@setOnClickListener
            var userMemCd: String

            if (model.partAmemCd.isEmpty()) {
                userMemCd = model.ownerMemCd
            } else {
                userMemCd = model.partAmemCd
            }

            handleUserDetail(it, userMemCd)
        }

        itemBinding.userBImageView.setOnClickListener {
            val model = itemBinding.data ?: return@setOnClickListener
            var userMemCd: String

            if (model.partBmemCd.isEmpty()) {
                userMemCd = model.ownerMemCd
            } else {
                userMemCd = model.partBmemCd
            }

            handleUserDetail(it, userMemCd)
        }

        itemBinding.playerView.setOnClickListener {
            togglePlayAndPause(viewModel)
        }
        itemBinding.clAudio.setOnClickListener {
            togglePlayAndPause(viewModel)
        }
        itemBinding.tvBtnSongDel.setOnClickListener {
            val model = itemBinding.data ?: return@setOnClickListener
            val isDetail = itemBinding.isDetail ?: false
            requestDeleteFeed(model, isDetail)
        }
        itemBinding.ivBtPlay.setOnClickListener {
            if (viewModel is FeedDetailFragmentViewModel) {
                togglePlayAndPause(viewModel)
            }
        }

        itemBinding.tvTag.setLineCountCallback(object : CustomLinkableTextView.Listener {
            override fun onLineCountCallback(str: CharSequence, cnt: Int) {
                val data = itemBinding.data ?: return
                if (data.noteLineCount > -1) return
                if (data.note == str) {
                    data.noteLineCount = cnt
                    if (cnt > MAX_TAG_LINE_CNT) {
                        itemBinding.nsv.setOnTouchListener { _: View?, _: MotionEvent? -> false }
                        itemBinding.moreTextView.changeVisible(true)
                    } else {
                        itemBinding.nsv.setOnTouchListener { _: View?, _: MotionEvent? -> true }
                        itemBinding.moreTextView.changeVisible(false)
                    }
                }
            }
        })

        itemBinding.moreTextView.setOnClickListener {

            val data = itemBinding.data ?: return@setOnClickListener

            if (data.isAniStarting) return@setOnClickListener

            data.isAniStarting = true
            if (data.isMore) {
                handleShowAndHideContents(false)
            } else {
                handleShowAndHideContents(true)
            }

            data.isMore = !data.isMore
        }
    }

    override fun bind(data: FeedContentsData) {
        super.bind(data)
        itemBinding.ivPlay.visibility = View.GONE
        viewModel.setPlayerView(itemBinding.playerView)
    }


    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_PAUSE) {
            closeDisposable()
        } else if (event == Lifecycle.Event.ON_RESUME) {
            onRefreshFeed()
        }
    }

    /**
     * 내용 및 테그 더보기 Or 숨기기 애니메이션 처리하는 함수
     * 텍스트 LineHeight 값을 기준으로 애니메이션의 가중치값을 정해서 처리한다.
     * MaxLines이 고정되어있는 텍스트뷰와 그렇지 않은 텍스트뷰를 번갈아 가면서 노출 / 미노출 처리하는 방식
     * 애니메이션이 끝난 이후 [CustomLinkableTextView] 의 RootLayout [LayoutParams] 를
     * 다시 [LayoutParams.WRAP_CONTENT] 감싸면서 재사용 가능하도록 처리
     * @param isShow true: 더보기, false 숨기기 처리
     */
    private fun handleShowAndHideContents(isShow: Boolean) {

        val data = itemBinding.data ?: return

        val diffLineCount = (data.noteLineCount - MAX_TAG_LINE_CNT).coerceAtLeast(0)

        if (isShow) {
            val toHeight = itemBinding.clTag.height

            val fromHeight = if (data.noteLineCount < MAX_TAG_EXP_LINE_CNT) {
                itemBinding.nsv.isNestedScrollingEnabled = false
                data.noteLineCount * itemBinding.tvTag.lineHeight
            } else {
                itemBinding.nsv.isNestedScrollingEnabled = true
                MAX_TAG_EXP_LINE_CNT * itemBinding.tvTag.lineHeight
            }

            ObjectAnimator.ofInt(fromHeight - toHeight).apply {
                duration = 300
                interpolator = FastOutSlowInInterpolator()
                doOnStart {
                    itemBinding.tvOriginalTag.changeVisible(true)
                    itemBinding.tvTag.changeVisible(false)
                }
                doOnEnd {
                    itemBinding.clTag.setReHeight(fromHeight)
                    itemBinding.clTag.updateLayoutParams { height = LayoutParams.WRAP_CONTENT }
                    itemBinding.moreTextView.setText(R.string.fmt_feed_contents_hide)
                    onMain {
                        delay(300)
                        data.isAniStarting = false
                    }
                }
                addUpdateListener {
                    val value = it.animatedValue as Int
                    itemBinding.nsv.setReHeight(toHeight.plus(value))
                }
                start()
            }

        } else {

            val toHeight = itemBinding.nsv.height
            val fromHeight = MAX_TAG_LINE_CNT * itemBinding.tvTag.lineHeight

            ObjectAnimator.ofInt(fromHeight - toHeight).apply {
                duration = 300
                interpolator = FastOutSlowInInterpolator()
                doOnStart {
                    itemBinding.nsv.scrollY = 0
//                    itemBinding.tvTag.changeVisible(true)
//                    itemBinding.tvOriginalTag.changeVisible(false)
                }
                doOnEnd {
                    itemBinding.clTag.setReHeight(fromHeight)
                    itemBinding.tvTag.changeVisible(true)
                    itemBinding.tvOriginalTag.changeVisible(false)
                    itemBinding.nsv.updateLayoutParams { height = LayoutParams.WRAP_CONTENT }
                    itemBinding.clTag.updateLayoutParams { height = LayoutParams.WRAP_CONTENT }
                    itemBinding.moreTextView.setText(R.string.fmt_feed_contents_more)
                    onMain {
                        delay(300)
                        data.isAniStarting = false
                    }
                }
                addUpdateListener {
                    val value = it.animatedValue as Int
                    itemBinding.nsv.setReHeight(toHeight.plus(value))
                }
                start()
            }
        }
    }

    /**
     * handleUiUpdate 함수를 곧 onRefreshUi
     */
    override fun onRefreshFeed() {
        handleUiUpdate()
    }

    /**
     * 자동으로 갱신 해줘야 하는 UI들 처리하는 함수
     * @see [UserFollowManager]
     * @see [UserFeedLikeManager]
     */
    private fun handleUiUpdate() {
        val data = itemBinding.data ?: return

        DLogger.d("handleUiUpdate 업데이트 합니다. ${data}  / ${hashCode()}")

        // 팔로잉
        val ownerFollow = UserFollowManager.isFollow(data.ownerMemCd)
        if (ownerFollow != null && data.isOwerFollow != ownerFollow) {
            data.fgFollowYn = if (ownerFollow) AppData.Y_VALUE else AppData.N_VALUE
            data.isOwerFollow = ownerFollow
            itemBinding.ivSoloUserF.changeInVisible(!data.isOwerFollow && !isMyFeed(data.ownerMemCd))
        }

        val aPartFollow = UserFollowManager.isFollow(data.partAmemCd)
        if (aPartFollow != null && data.followApartYn != aPartFollow) {
            data.partAfgFollowYn = if (aPartFollow) AppData.Y_VALUE else AppData.N_VALUE
            data.followApartYn = aPartFollow
            itemBinding.ivPartA.changeInVisible(!data.followApartYn && !isMyFeed(data.partAmemCd))
        }

        val bPartFollow = UserFollowManager.isFollow(data.partBmemCd)
        if (bPartFollow != null && data.followBpartYn != bPartFollow) {
            data.partBfgFollowYn = if (bPartFollow) AppData.Y_VALUE else AppData.N_VALUE
            data.followBpartYn = bPartFollow
            itemBinding.ivPartB.changeInVisible(!data.followBpartYn && !isMyFeed(data.partBmemCd))
        }

        // 좋아요 처리
        onRefreshLike()
        // 북마크
        onRefreshBookMark()

        // 댓글 개수 업데이트
        val feedCommentCount = CommentCountManager.getFeedCount(data.feedMngCd)
        if (feedCommentCount != null) {
            data.replyCount = feedCommentCount
            itemBinding.tvReplyCount.setText("${data.replyCount}")
            DLogger.d("댓글 개수 업데이트 합니다. ${data.replyCount}")
        }

        //차단
        val isBlock = UserFeedBlockManager.isBlock(data.feedMngCd)
        if (isBlock != null && data.isBlock != isBlock) {
            data.isBlock = isBlock
            DLogger.d("차단 업데이트 ${data.isBlock}")
        }

        //관심없음
        val isNotInterested = UserFeedInterestManager.isNotInterested(data.feedMngCd)
        if (isNotInterested != null && data.isNotInterested != isNotInterested) {
            data.isNotInterested = isNotInterested
            DLogger.d("관심없음 업데이트 ${data.isNotInterested}")
        }

        //삭제
        val isDeleted = UserFeedDeleteManager.isDeleted(data.feedMngCd)

        if (isDeleted != null && data.isDeleted != isDeleted) {
            data.fgDelYn = AppData.Y_VALUE
            data.isDeleted = isDeleted
            DLogger.d("삭제 업데이트 ${data.isDeleted}")
        }

        data.isAniStarting = false
    }

    override fun onRefreshLike() {
        val data = itemBinding.data ?: return
        // 좋아요 처리
        val likeInfo = UserFeedLikeManager.getLikeInfo(data.feedMngCd)
        if (likeInfo != null && data.isLike != likeInfo.first) {
            data.fgLikeYn = if (likeInfo.first) AppData.Y_VALUE else AppData.N_VALUE
            data.isLike = likeInfo.first
            data.likeCnt = likeInfo.second
            itemBinding.tvLikeCount.text = "${data.likeCnt}"
            itemBinding.ivLike.isSelected = data.isLike
            DLogger.d("좋아요 업데이트 합니다. ${data.isLike}")
        }
    }

    override fun onRefreshBookMark() {
        val data = itemBinding.data ?: return
        val isBookmark = UserFeedBookmarkManager.isBookmark(data.feedMngCd)
        if (isBookmark != null && data.isBookMark != isBookmark) {
            data.fgBookMarkYn = if (isBookmark) AppData.Y_VALUE else AppData.N_VALUE
            data.isBookMark = isBookmark
            itemBinding.ivBookMark.isSelected = data.isBookMark
            DLogger.d("북마크 업데이트 합니다. ${data.isBookMark} / ${UserFeedBookmarkManager.hashCode()}")
        }
    }

    override fun onRefreshDeleted(state: Boolean) {
        itemBinding.data ?: return
        itemBinding.data?.isDeleted = true
        itemBinding.clFeedStop.changeVisible(false)
        itemBinding.tvFeedDeleted.changeVisible(true)
    }
}
