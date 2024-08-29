package com.verse.app.ui.bindingadapter

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.text.TextPaint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.LayoutParams
import android.webkit.URLUtil
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.widget.NestedScrollView
import androidx.databinding.BindingAdapter
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.apradanas.simplelinkabletext.Link
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.flexbox.FlexboxLayout
import com.verse.app.R
import com.verse.app.base.viewmodel.BaseViewModel
import com.verse.app.contants.Config
import com.verse.app.contants.ExtraCode
import com.verse.app.contants.ListPagedItemType
import com.verse.app.contants.MediaType
import com.verse.app.contants.SingType
import com.verse.app.contants.TabPageType
import com.verse.app.extension.changeVisible
import com.verse.app.extension.dp
import com.verse.app.extension.getFragmentActivity
import com.verse.app.extension.lifecycleOwner
import com.verse.app.extension.setReHeight
import com.verse.app.extension.startAct
import com.verse.app.model.base.BaseModel
import com.verse.app.model.feed.FeedContentsData
import com.verse.app.model.singpass.GenreRankingList
import com.verse.app.model.song.SongMainInfo
import com.verse.app.model.user.UserData
import com.verse.app.ui.adapter.CommonListAdapter
import com.verse.app.ui.adapter.CommonPagingAdapter
import com.verse.app.ui.search.activity.SearchResultActivity
import com.verse.app.ui.song.viewmodel.SongMainViewModel
import com.verse.app.utility.DLogger
import com.verse.app.utility.exo.ExoStyledPlayerView
import com.verse.app.widget.views.CustomLinkableTextView
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import java.util.regex.Pattern
import kotlin.math.abs


/**
 * Description : Feed BindingAdapter
 *
 * Created by jhlee on 2023-02-13
 */
object FeedBindingAdapter {


    /**
     * 비디오&오디오 녹화시 타이틀 텍스트 visible / gone 처리
     */
    @JvmStatic
    @BindingAdapter(value = ["curMediaType", "isRecording"], requireAll = false)
    fun setRecordingViewState(
        view: View,
        curMediaType: String,
        isRecording: Boolean,
    ) {
        if (curMediaType == MediaType.AUDIO.code) {
            view.visibility = if (!isRecording) View.VISIBLE else View.GONE
        } else {
            view.visibility = View.VISIBLE
        }
    }


    /**
     * set View inVisible
     */
    @JvmStatic
    @BindingAdapter(value = ["curCode", "targetCode", "isRecording"], requireAll = false)
    fun setCameraState(
        view: View,
        curCode: String,
        targetCode: String,
        isRecording: Boolean,
    ) {
        view.visibility =
            if ((curCode == targetCode) && !isRecording) View.VISIBLE else View.INVISIBLE
    }

    @JvmStatic
    @BindingAdapter(value = ["effectImageUrl", "requestManager"], requireAll = false)
    fun loadEffectImage(
        view: ImageView,
        url: String?,
        requestManager: RequestManager?
    ) {

        val reqManager = requestManager ?: Glide.with(view)

        if (url != null) {
            val previewUri = if (URLUtil.isNetworkUrl(url)) {
                url.toUri()
            } else {
                url.let {
                    File(it).toUri()
                }
            } ?: Uri.EMPTY

            if (previewUri == Uri.EMPTY) {
                view.setImageResource(R.drawable.bg_effect_normal)
            } else {
                val requestOptions = RequestOptions().apply {
                    transform(CenterCrop(), CircleCrop())
                    error(R.drawable.bg_effect_error)
                }
                reqManager
                    .load(previewUri).apply(requestOptions).into(view)
            }
        } else {
            reqManager
                .load(R.drawable.bg_effect_normal).into(view)
        }
    }

    @JvmStatic
    @BindingAdapter("isClear")
    fun clearViewPagerListAdapter(
        viewPager: ViewPager2,
        isClear: Boolean? = false,
    ) {
        if (viewPager.adapter != null) {
            val adapter = viewPager.adapter as CommonListAdapter<*>
            isClear?.let {
                if (it) {
//                    adapter.submitList(null)
                    viewPager.adapter = null
                    viewPager.visibility = View.GONE
                }
            }
        }
    }

    /**
     * 일반 MutableList ViewPager
     */
    @Suppress("UNCHECKED_CAST")
    @JvmStatic
    @BindingAdapter(
        value = ["itemType", "dataList", "viewModel", "emptyView"],
        requireAll = false
    )
    fun <T : BaseModel> setViewPagerAdapter(
        viewPager: ViewPager2,
        itemType: ListPagedItemType,
        dataList: MutableList<T>?,
        viewModel: BaseViewModel,
        emptyView: View?
    ) {

        if (dataList?.size == 0) {
            return
        }

        if (viewPager.adapter == null) {

            //팔로잉 ViewPager
            if (itemType == ListPagedItemType.MAIN_FOLLOWING) {
                viewPager.offscreenPageLimit = 3
                viewPager.post {
                    // 양 옆 설정 값
                    var compositePageTransformer = CompositePageTransformer().apply {
                        addTransformer(MarginPageTransformer(18.dp))
                        addTransformer { view: View, fl: Float ->
                            var v = 1 - abs(fl)
                            view.scaleY = 0.8f + v * 0.2f
                        }
                    }
                    viewPager.setPageTransformer(compositePageTransformer)
                }
            } else if (itemType == ListPagedItemType.FEED) {
                viewPager.offscreenPageLimit = 6
            } else if (itemType == ListPagedItemType.MAIN_SING_PASS) {
                viewPager.offscreenPageLimit = 3
            }

            // 씽패스 진행 현황 미션 ViewPager
            if (itemType == ListPagedItemType.MAIN_SING_PASS_DAILY_MISSION_LIST
                || itemType == ListPagedItemType.MAIN_SING_PASS_PERIOD_MISSION_LIST
                || itemType == ListPagedItemType.MAIN_SING_PASS_SEASON_MISSION_LIST
            ) {
                viewPager.offscreenPageLimit = 3
            }

            val listAdapter = CommonListAdapter<T>(viewModel, itemType)
            viewPager.apply {
                adapter = listAdapter
                getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER
            }
        }

        if (viewPager.adapter != null) {

            val adapter = viewPager.adapter as CommonListAdapter<T>

            if (dataList != null && dataList.size > 0) {
                adapter.submitList(dataList)
            }

            if (adapter.itemCount <= 0) {
                emptyView?.visibility = View.VISIBLE
                viewPager.visibility = View.GONE
            } else {
                emptyView?.visibility = View.GONE
                viewPager.visibility = View.VISIBLE
            }
        }
    }

    /**
     * 페이징 MutableList ViewPager
     */
    @Suppress("UNCHECKED_CAST")
    @JvmStatic
    @BindingAdapter(value = ["itemType", "pagedList", "viewModel", "emptyView"], requireAll = false)
    fun <T : BaseModel> setViewPagerPagingAdapter(
        viewPager: ViewPager2,
        itemType: ListPagedItemType,
        dataList: PagingData<T>?,
        viewModel: BaseViewModel,
        eptView: View?
    ) {

        if (dataList == null) {
            return
        }

        if (viewPager.adapter == null) {

            val commonAdapter = CommonPagingAdapter<T>(viewModel, itemType)

            viewPager.apply {
                offscreenPageLimit = 1
                getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER
                adapter = commonAdapter
            }
        }

        //Set Data
        if (viewPager.adapter != null) {

            //Set Paging data
            viewPager.lifecycleOwner?.lifecycleScope?.launch {
                val adapter = viewPager.adapter as CommonPagingAdapter<T>
                adapter.submitData(dataList)
            }
            //LoadStates
            //prepend : 앞 페이지 LoadState
            //append : 뒷 페이지 LoadState
            //refresh : 새로고침 LoadState
            viewPager.lifecycleOwner?.lifecycleScope?.launch {
                val adapter = viewPager.adapter as CommonPagingAdapter<T>
                adapter.loadStateFlow.collectLatest { loadStates ->

                    when (loadStates.refresh) {
                        is LoadState.Loading -> {
                            viewModel.onLoadingShow()
                        }

                        is LoadState.NotLoading -> {
                            viewModel.onLoadingDismiss()
                            //Empty View..
                            if (loadStates.append.endOfPaginationReached && adapter.itemCount < 1) {
                                eptView?.let {
                                    if (!it.isVisible) {
                                        it.visibility = View.VISIBLE
                                    }
                                }

                                if (viewPager.isVisible) {
                                    viewPager.visibility = View.GONE
                                }

                            } else {
                                eptView?.let {
                                    if (it.isVisible) {
                                        it.visibility = View.GONE
                                    }
                                }

                                if (!viewPager.isVisible) {
                                    viewPager.visibility = View.VISIBLE
                                }
                            }
                        }

                        is LoadState.Error -> {
                            viewModel.onLoadingDismiss()
                            viewModel.showNetworkErrorDialog(errorMsg = (loadStates.refresh as LoadState.Error).error.localizedMessage)
                            eptView?.let {
                                if (!it.isVisible) {
                                    it.visibility = View.VISIBLE
                                }
                            }

                            if (viewPager.isVisible) {
                                viewPager.visibility = View.GONE
                            }
                        }

                        else -> {
                            viewModel.onLoadingDismiss()
                        }
                    }
                }
            }
        }
    }

    @JvmStatic
    @BindingAdapter(value = ["thumbnail"], requireAll = false)
    fun loadImage(
        view: ImageView,
        thumbnailPath: String?,
    ) {

        if (!thumbnailPath.isNullOrEmpty() && URLUtil.isValidUrl(Config.BASE_FILE_URL + thumbnailPath)) {
            Glide.with(view.context)
                .setDefaultRequestOptions(
                    RequestOptions()
                        .placeholder(R.drawable.ic_album_default)
                        .error(R.drawable.ic_album_default)
                )
                .load(Config.BASE_FILE_URL + thumbnailPath)
                .thumbnail(
                    Glide.with(view)
                        .load(Config.BASE_FILE_URL + thumbnailPath)
                        .sizeMultiplier(0.25f)
                        .override(60, 60)
                )
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(view)
        } else {
            Glide.with(view.context)
                .load(R.drawable.ic_album_default)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(view)
        }
    }

    @JvmStatic
    @BindingAdapter(value = ["profileImage", "requestManager"], requireAll = false)
    fun loadProfileImage(
        view: ImageView,
        thumbnailPath: String?,
        requestManager: RequestManager?
    ) {

        val reqManager = requestManager ?: Glide.with(view)

        if (!thumbnailPath.isNullOrEmpty() && URLUtil.isValidUrl(Config.BASE_FILE_URL + thumbnailPath)) {
            reqManager
                .setDefaultRequestOptions(
                    RequestOptions()
                        .placeholder(R.drawable.ic_profile_default)
                        .error(R.drawable.ic_profile_default)
                )
                .load(Config.BASE_FILE_URL + thumbnailPath)
                .thumbnail(
                    reqManager
                        .load(Config.BASE_FILE_URL + thumbnailPath)
                        .sizeMultiplier(0.25f)
                        .override(60, 60)
                )
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(view)
        } else {
            reqManager
                .load(R.drawable.ic_profile_default)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(view)
        }
    }

    @JvmStatic
    @BindingAdapter(value = ["backgroundProfileImage"], requireAll = false)
    fun loadBgProfileImage(
        view: ImageView,
        thumbnailPath: String?
    ) {

        if (!thumbnailPath.isNullOrEmpty() && URLUtil.isValidUrl(Config.BASE_FILE_URL + thumbnailPath)) {
            Glide.with(view.context)
                .setDefaultRequestOptions(
                    RequestOptions()
                        .placeholder(R.drawable.bg_gradient_profile)
                        .error(R.drawable.bg_gradient_profile)
                )
                .load(Config.BASE_FILE_URL + thumbnailPath)
                .thumbnail(
                    Glide.with(view)
                        .load(Config.BASE_FILE_URL + thumbnailPath)
                        .sizeMultiplier(0.25f)
                        .override(60, 60)
                )
                .placeholder(R.drawable.bg_gradient_profile)
                .error(R.drawable.bg_gradient_profile)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(view)
        } else {
            Glide.with(view.context)
                .load(R.drawable.bg_gradient_profile)
                .placeholder(R.drawable.bg_gradient_profile)
                .error(R.drawable.bg_gradient_profile)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(view)
        }
    }

    @JvmStatic
    @BindingAdapter(value = ["thumbnailEx", "isInvisible", "isDashBoard"], requireAll = false)
    fun loadImageForGone(
        view: ImageView,
        thumbnailPath: String?,
        isInvisible: Boolean = false,
        isDashBoard: Boolean = false,
    ) {
        if (isDashBoard) {
            view.visibility = View.VISIBLE
            Glide.with(view.context)
                .setDefaultRequestOptions(
                    RequestOptions()
                        .placeholder(R.drawable.ic_album_default)
                        .error(R.drawable.ic_album_default)
                )
                .load(Config.BASE_FILE_URL + thumbnailPath)
                .thumbnail(
                    Glide.with(view)
                        .load(Config.BASE_FILE_URL + thumbnailPath)
                        .sizeMultiplier(0.25f)
                        .override(60, 60)
                )
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(view)

        } else if (!thumbnailPath.isNullOrEmpty() && URLUtil.isValidUrl(Config.BASE_FILE_URL + thumbnailPath)) {
            view.visibility = View.VISIBLE
            Glide.with(view.context)
                .setDefaultRequestOptions(
                    RequestOptions()
                        .placeholder(R.drawable.ic_album_default)
                        .error(R.drawable.ic_album_default)
                )
                .load(Config.BASE_FILE_URL + thumbnailPath)
                .thumbnail(
                    Glide.with(view)
                        .load(Config.BASE_FILE_URL + thumbnailPath)
                        .sizeMultiplier(0.25f)
                        .override(60, 60)
                )
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(view)
        } else {
            if (!isInvisible) {
                view.visibility = View.GONE
            } else {
                view.visibility = View.INVISIBLE
            }
        }
    }

    @JvmStatic
    @BindingAdapter(value = ["imageResource"], requireAll = false)
    fun loadImageResource(
        view: ImageView,
        imageResource: Int,
    ) {
        imageResource.let {
            view.setImageResource(imageResource)
        }
    }


    @JvmStatic
    @BindingAdapter(
        value = ["feedUrl", "thumbnailView", "albumView", "position", "feedContentsData", "btnPlayView", "btBtnPlayView", "btPlayingTime", "btSbBarView", "btTotalTimeView", "viewModel"],
        requireAll = false
    )
    fun loadVideo(
        playerView: ExoStyledPlayerView,
        uri: String,
        thumbnailView: ImageView,
        albumView: ImageView?,
        pos: Int,
        feedContentsData: FeedContentsData?,
        btnPlayView: ImageView?,
        btBtnPlayView: ImageView?,
        btPlayingTime: TextView?,
        btSbBar: SeekBar?,
        btTotalTime: TextView?,
        viewModel: BaseViewModel?
    ) {

        feedContentsData?.let { data ->
            if (data.isFeedContents) {
                if (!data.isDuetOrBattle) {
                    playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                } else {
                    if (data.partAmemCd.isNotEmpty() && data.partBmemCd.isNotEmpty()) {
                        playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
                    } else {
                        playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    }
                }
            } else {
                playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
            }
        }

        playerView.setPlayInfo(
            uri,
            thumbnailView,
            albumView,
            pos,
            feedContentsData,
            btnPlayView,
            btBtnPlayView,
            btPlayingTime,
            btSbBar,
            btTotalTime
        )

        viewModel?.let {
            viewModel.setPlayerView(playerView)
        }
    }

    @JvmStatic
    @BindingAdapter(
        value = ["genreRankingList", "thumbnailView", "position", "viewModel"], requireAll = false
    )
    fun loadSingPassVideoList(
        playerView: ExoStyledPlayerView,
        genreRankingList: MutableList<GenreRankingList>,
        thumbnailView: ImageView,
        pos: Int,
        viewModel: BaseViewModel,
    ) {
        playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
        playerView.setSingPassPlayInfo(genreRankingList, thumbnailView, pos)
        viewModel.setPlayerView(playerView)
    }

    @JvmStatic
    @BindingAdapter(
        value = ["feedTagData", "userData", "moreView", "rootView", "originTagView", "nestedScrollView"],
        requireAll = false
    )
    fun setMoreText(
        textView: CustomLinkableTextView,
        data: FeedContentsData?,
        userData: UserData?,
        moreView: TextView,
        rootView: ConstraintLayout,
        originTagTextView: CustomLinkableTextView,
        nsv: NestedScrollView
    ) {

        if (data == null || userData == null) {
            textView.changeVisible(false)
            moreView.changeVisible(false)
            rootView.changeVisible(true)
            return
        }

        val note = data.note ?: ""

        if (note.isEmpty()) {
            textView.changeVisible(false)
            moreView.changeVisible(false)
            rootView.changeVisible(true)
            return
        }

        nsv.setReHeight(LayoutParams.WRAP_CONTENT)
        rootView.changeVisible(true)

        val startIndex = note.indexOf("#")
        if (startIndex > -1) {
            /*   val builder = SpannableStringBuilder(note)
               val endIndex = note.length
               builder.setSpan(
                   StyleSpan(
                       Typeface.NORMAL
                   ), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
               )
   */
            val linkHashtag: Link = Link(Pattern.compile("(#\\w+)"))
                .setUnderlined(false)
                .setTextColor(Color.WHITE)
                .setTextStyle(Link.TextStyle.BOLD)
                .setClickListener(Link.OnClickListener { tag ->
                    val fragmentActivity = textView.getFragmentActivity() ?: return@OnClickListener
                    fragmentActivity.startAct<SearchResultActivity> {
                        putExtra(ExtraCode.SEARCH_KEYWORD, tag.replace("#", ""))
                        putExtra(ExtraCode.SEARCH_RESULT_POS, 3)
                    }
                })
            textView.setText(note.toString()).addLink(linkHashtag).build()
            originTagTextView.setText(note.toString()).addLink(linkHashtag).build()
        } else {
            textView.text = note
            originTagTextView.text = note
        }
        textView.setOriginalText(note)
    }

    @JvmStatic
    @BindingAdapter(value = ["genreDataList", "moreView", "viewModel"], requireAll = false)
    fun setGenreFlexbox(
        flexboxLayout: FlexboxLayout,
        songMainInfo: SongMainInfo,
        moreView: ImageView,
        viewModel: BaseViewModel,
    ) {

        songMainInfo.genreList?.let {

            if (flexboxLayout.childCount > 0) {
                flexboxLayout.removeAllViews()
            }

            for (genreData in it) {
                val view = LayoutInflater.from(flexboxLayout.context)
                    .inflate(R.layout.item_genre_list, null)
                val cl: ConstraintLayout = view.findViewById(R.id.cl_genre)
                val tvGenre: TextView = view.findViewById(R.id.tv_genre)
                tvGenre.text = genreData.genreNm
                cl.tag = genreData.genreNm + "_" + genreData.genreCd
                //장르 클릭
                cl.setOnClickListener {
                    val tag = it.tag as String
                    if (viewModel is SongMainViewModel) {
                        viewModel.onMore(TabPageType.SONG_GENRE, tag)
                    }
                }
                flexboxLayout.addView(view)
            }

            flexboxLayout.post {

                // line 2 맞춤
                if (flexboxLayout.childCount > 0) {
                    val llRoot = flexboxLayout.getChildAt(0) as LinearLayout
                    val cl = llRoot.getChildAt(0) as ConstraintLayout
                    val tvGenre: TextView = cl.getChildAt(0) as TextView
                    val tempPaint: TextPaint = tvGenre.paint
                    val mMetrics = tempPaint.fontMetrics
                    val fTextTop = mMetrics.top
                    val fTextsBottom = mMetrics.bottom
                    val fLineHEight = fTextsBottom - fTextTop

                    llRoot.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
                    flexboxLayout.measure(
                        View.MeasureSpec.UNSPECIFIED,
                        View.MeasureSpec.UNSPECIFIED
                    )

                    val llHeight = llRoot.height * 2
                    val flHeight = flexboxLayout.height

                    if (llHeight <= flHeight) {

                        moreView.visibility = View.VISIBLE

                        if (!songMainInfo.genreisOpened) {
                            flexboxLayout.updateLayoutParams<ConstraintLayout.LayoutParams> {
                                this.height = llHeight
                            }
                            moreView.setImageResource(R.drawable.ic_down)
                        } else {
                            flexboxLayout.updateLayoutParams<ConstraintLayout.LayoutParams> {
                                this.height = LinearLayout.LayoutParams.WRAP_CONTENT
                            }
                            songMainInfo.genreisOpened = true
                            moreView.setImageResource(R.drawable.ic_up)
                        }

                        //more arrow
                        moreView.setOnClickListener {
                            if (flexboxLayout.height == llHeight) {
                                flexboxLayout.updateLayoutParams<ConstraintLayout.LayoutParams> {
                                    this.height = LinearLayout.LayoutParams.WRAP_CONTENT
                                }
                                songMainInfo.genreisOpened = true
                                moreView.setImageResource(R.drawable.ic_up)
                            } else {
                                flexboxLayout.updateLayoutParams<ConstraintLayout.LayoutParams> {
                                    this.height = llHeight
                                }
                                songMainInfo.genreisOpened = false
                                moreView.setImageResource(R.drawable.ic_down)
                            }
                        }

                    } else {
                        moreView.setImageResource(R.drawable.ic_down)
                        moreView.visibility = View.GONE
                    }
                }
            }
        } ?: run {
            moreView.setImageResource(R.drawable.ic_down)
            moreView.visibility = View.GONE
        }
    }

    /**
     * solo , duet , battle icon,name Set
     */
    @JvmStatic
    @BindingAdapter(value = ["singType", "viewText"], requireAll = false)
    fun setSingingTypeView(
        view: ImageView,
        singType: SingType?,
        textView: TextView
    ) {

        if (singType == null) {
            return
        }

        view.setImageDrawable(
            ContextCompat.getDrawable(
                view.context, singType.icon
            )
        )
        textView.text = view.context.resources.getString(singType.typeName)
    }


    /**
     * 피드 컨텐츠 스케일 타입 지정
     */
    @JvmStatic
    @BindingAdapter(
        value = ["feedContentsData", "thumbUrl", "thumbPlaceHolder", "requestManager"],
        requireAll = false
    )
    fun setFeedThumbnailScaleType(
        imageView: AppCompatImageView,
        feedContentsData: FeedContentsData?,
        thumbUrl: String?,
        placeHolder: Drawable?,
        requestManager: RequestManager?
    ) {
        feedContentsData?.let { data ->
            if (data.isFeedContents) {
                if (!data.isDuetOrBattle) {
                    imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                } else {
                    if (data.partAmemCd.isNotEmpty() && data.partBmemCd.isNotEmpty()) {
                        imageView.scaleType = ImageView.ScaleType.FIT_CENTER
                    } else {
                        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                    }
                }
            } else {
                imageView.scaleType = ImageView.ScaleType.FIT_CENTER
            }
            GlideBindingAdapter.loadImage(
                imageView,
                thumbUrl,
                placeHolder,
                null,
                null,
                requestManager
            )
        }
    }


    /**
     * 피드 컨텐츠 스케일 타입 지정
     */
    @JvmStatic
    @BindingAdapter(
        value = ["spResId"],
        requireAll = false
    )
    fun setSpFilter(
        imageView: AppCompatImageView,
        resId: Int
    ) {
        imageView.setImageDrawable(ContextCompat.getDrawable(imageView.context, resId))
    }
}