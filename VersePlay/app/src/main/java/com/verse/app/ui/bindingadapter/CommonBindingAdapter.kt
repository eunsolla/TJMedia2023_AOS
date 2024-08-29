package com.verse.app.ui.bindingadapter

import com.verse.app.base.adapter.PagingLoadStateAdapter
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.Spannable
import android.text.TextUtils
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.verse.app.R
import com.verse.app.base.adapter.BaseChildFragmentPagerAdapter
import com.verse.app.base.adapter.BaseFragmentPagerAdapter
import com.verse.app.base.viewmodel.BaseViewModel
import com.verse.app.contants.*
import com.verse.app.extension.*
import com.verse.app.model.base.BaseModel
import com.verse.app.model.base.BaseViewTypeModel
import com.verse.app.model.mypage.TutorialItemData
import com.verse.app.ui.adapter.*
import com.verse.app.ui.decoration.*
import com.verse.app.ui.main.MainActivity
import com.verse.app.ui.main.fragment.MainTabFragment
import com.verse.app.ui.mypage.activity.MypageFollowActivity
import com.verse.app.ui.sing.fragment.PrepareSingFragment
import com.verse.app.ui.singpass.acivity.SingPassDashBoardActivity
import com.verse.app.ui.song.activity.SongMainActivity
import com.verse.app.utility.DLogger
import com.verse.app.widget.indicator.BaseFragmentIndicatorView
import com.verse.app.widget.indicator.BaseIndicatorView
import com.verse.app.widget.pagertablayout.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.serialization.StringFormat
import java.text.DecimalFormat


/**
 * Description : 공통 BindingAdapter
 *
 * Created by jhlee on 2023-01-13
 */
object CommonBindingAdapter {

    // [s] Common View =============================================================================

    /**
     * set View Visible
     */
    @JvmStatic
    @BindingAdapter("android:visibility")
    fun setVisibility(
        view: View,
        visible: Boolean?
    ) {
        val changeVisible: Int = if (visible == true) View.VISIBLE else View.GONE
        // 뷰가 변경할때만 처리하도록 -> setVisibility -> requestLayout 계속함 (Resource 비용 증가)
        if (view.visibility != changeVisible) {
            view.visibility = changeVisible
        }
    }

    /**
     * set View inVisible
     */
    @JvmStatic
    @BindingAdapter("inVisibility")
    fun setInVisibility(
        view: View,
        inVisible: Boolean?,
    ) {
        view.visibility = if (inVisible == true) View.VISIBLE else View.INVISIBLE
    }

    @JvmStatic
    @BindingAdapter("turtleClick")
    fun setTurtleClick(
        view: View,
        listener: View.OnClickListener,
    ) {
        view.setOnClickListener(OnSingleClickListener {
            listener.onClick(it)
        })
    }

    @JvmStatic
    @BindingAdapter("naviClick")
    fun setNaviClick(
        view: View,
        listener: View.OnClickListener,
    ) {
        view.setOnClickListener(OnNaviClickListener {
            listener.onClick(it)
        })
    }

    // [s] 중복 클릭 방지 리스너
    class OnSingleClickListener(private val onSingleCLick: (View) -> Unit) : View.OnClickListener {
        companion object {
            const val CLICK_INTERVAL = 500
        }

        private var lastClickedTime: Long = 0L

        override fun onClick(v: View?) {
            v?.let {
                if (isSafe()) {
                    onSingleCLick(it)
                }
                lastClickedTime = System.currentTimeMillis()
            }
        }

        private fun isSafe() = System.currentTimeMillis() - lastClickedTime > CLICK_INTERVAL
    }
    // [e] 중복 클릭 방지 리스너

    // [s] 중복 클릭 방지 리스너
    class OnNaviClickListener(private val onSingleCLick: (View) -> Unit) : View.OnClickListener {
        companion object {
            const val CLICK_INTERVAL = 500
        }

        private var lastClickedTime: Long = 0L

        override fun onClick(v: View?) {
            v?.let {
                if (isSafe()) {
                    onSingleCLick(it)
                }
                lastClickedTime = System.currentTimeMillis()
            }
        }

        private fun isSafe() = System.currentTimeMillis() - lastClickedTime > CLICK_INTERVAL
    }
    // [e] 중복 클릭 방지 리스너

    /**
     * set Typeface TextView
     */
    @JvmStatic
    @BindingAdapter("android:textStyle")
    fun setTextViewTypeFace(
        textView: AppCompatTextView,
        style: String?,
    ) {
        when (style) {
            "bold" -> {
                textView.setTypeface(
                    ResourcesCompat.getFont(
                        textView.context,
                        R.font.font_noto_sans_bold
                    ), Typeface.BOLD
                )
            }

            "normal" -> {
                textView.setTypeface(
                    ResourcesCompat.getFont(
                        textView.context,
                        R.font.font_noto_sans_medium
                    ), Typeface.NORMAL
                )
            }

            "italic" -> {
                textView.setTypeface(
                    ResourcesCompat.getFont(
                        textView.context,
                        R.font.font_noto_sans_regular
                    ), Typeface.ITALIC
                )
            }

            else -> {
            }
        }
    }

    /**
     * set TextView
     * default Type
     */
    @JvmStatic
    @BindingAdapter("android:text")
    fun setText(
        textView: TextView,
        text: String?,
    ) {
        if (!text.isNullOrEmpty()) {
            val oldText = textView.text.toString()
            if (oldText == text) {
                return
            }

            textView.text = text
        }
    }

    /**
     * set TextView
     * Resource Id Type
     */
    @JvmStatic
    @BindingAdapter("resourceText")
    fun setResourceText(
        textView: AppCompatTextView,
        resourceId: Int?, //Default Argument 0
    ) {
        if (resourceId != null) {
            if (resourceId != 0 && resourceId != -1) {
                textView.setText(resourceId)
            }
        }
    }

    /**
     * set TextView
     * Int Type
     */
    @JvmStatic
    @BindingAdapter("intText")
    fun setIntText(
        textView: TextView,
        value: Int?,
    ) {
        textView.text = "${value ?: ""}"
    }

    /**
     * set TextView
     * 콤마
     */
    @JvmStatic
    @BindingAdapter("commaText")
    fun setCommaText(
        textView: AppCompatTextView,
        value: Int,
    ) {
        val decimalFormat = DecimalFormat("###,###")
        textView.text = decimalFormat.format(value)
    }

    /**
     * set TextView
     * htmlText Type
     */
    @JvmStatic
    @BindingAdapter("htmlText")
    fun setHtmlText(
        textView: AppCompatTextView,
        text: String?,
    ) {
        if (text != null && text.trim().isNotEmpty()) {
            textView.text = HtmlCompat.fromHtml(text, HtmlCompat.FROM_HTML_MODE_LEGACY)
        }
    }


    /**
     * set TextView
     * String Format Type
     * 초기에 null 보이는 이슈 처리용.
     */
    @JvmStatic
    @BindingAdapter(value = ["fmtText", "data"], requireAll = true)
    fun setFmtTextData(
        textView: AppCompatTextView,
        fmtText: String,
        data: Any?,
    ) {
        if (data != null) {
            textView.text = fmtText
            textView.visibility = View.VISIBLE
        } else {
            textView.visibility = View.GONE
        }
    }

    @JvmStatic
    @Suppress("DEPRECATION")
    @BindingAdapter(value = ["fmtHtmlText", "data"], requireAll = true)
    fun setFmtHtmlTextData(
        textView: AppCompatTextView,
        fmtText: String,
        data: Any?,
    ) {
        if (data != null) {
            textView.text = HtmlCompat.fromHtml(String.format(fmtText,data), HtmlCompat.FROM_HTML_MODE_LEGACY)
            textView.visibility = View.VISIBLE
        } else {
            textView.visibility = View.GONE
        }
    }


    /**
     * EditText
     * Action Next Done.
     */
    @JvmStatic
    @BindingAdapter("editNextDone")
    fun setEditTextListener(
        editText: EditText,
        listener: View.OnClickListener,
    ) {
        editText.setOnEditorActionListener { v, actionId, event ->
            if (event?.keyCode == KeyEvent.KEYCODE_ENTER || actionId == EditorInfo.IME_ACTION_DONE) {
                listener.onClick(v)
                v.context.hideKeyboard(v)
            }
            return@setOnEditorActionListener false
        }
    }


    @JvmStatic
    @BindingAdapter("android:enabled")
    fun setEnabled(
        view: View,
        isEnabled: Boolean,
    ) {
        view.isEnabled = isEnabled
    }

    /**
     * set Layout Width or Height
     */
    @JvmStatic
    @BindingAdapter(value = ["layout_width", "layout_height"], requireAll = false)
    fun setLayoutWidthAndHeight(
        view: View,
        @Dimension width: Int?,
        @Dimension height: Int?,
    ) {
        val layoutParams = view.layoutParams
        width?.run {
            layoutParams.width = when (this) {
                -1 -> ViewGroup.LayoutParams.MATCH_PARENT
                -2 -> ViewGroup.LayoutParams.WRAP_CONTENT
                else -> this
            }
        }

        height?.run {
            layoutParams.height = when (this) {
                -1 -> ViewGroup.LayoutParams.MATCH_PARENT
                -2 -> ViewGroup.LayoutParams.WRAP_CONTENT
                else -> this
            }
        }

        view.layoutParams = layoutParams
    }

    /**
     * set View Selected
     */
    @JvmStatic
    @BindingAdapter("isSelected")
    fun setSelected(
        view: View,
        isSelect: Boolean?,
    ) {
        if (view is TextView) {
            view.isSelected = isSelect ?: false
        } else {
            view.isSelected = isSelect ?: false
        }
    }

    @JvmStatic
    @BindingAdapter("isEnabled")
    fun setEnabled(
        view: View,
        isEnabled: Boolean?
    ) {
        view.isEnabled = isEnabled ?: false
    }

    @JvmStatic
    @BindingAdapter("android:textColor")
    fun setTextColor(
        view: TextView,
        color: Int,
    ) {
        try {
            view.setTextColor(ContextCompat.getColor(view.context, color))
        } catch (ex: Exception) {
            view.setTextColor(color)
        }
    }


    @JvmStatic
    @BindingAdapter("android:src")
    fun setImageDrawable(
        view: ImageView,
        drawable: Drawable?,
    ) {
        view.setImageDrawable(drawable)
    }

    @JvmStatic
    @BindingAdapter("imgBitmap")
    fun setImageViewBitmap(
        view: ImageView,
        bitmap: Bitmap?,
    ) {
        if (bitmap == null) return
        view.setImageBitmap(bitmap)
    }

    @JvmStatic
    @BindingAdapter(value = ["viewPager", "indicatorCnt"], requireAll = false)
    fun setIndicatorViewPager(
        indicatorView: BaseFragmentIndicatorView,
        viewPager: ViewPager2?,
        dataCnt: Int?,
    ) {
        indicatorView.viewPager = viewPager
        indicatorView.dataCnt = dataCnt ?: 0
    }

    @JvmStatic
    @BindingAdapter(value = ["viewPager", "indicatorCnt", "dotSize"], requireAll = false)
    fun setIndicatorViewPager(
        indicatorView: BaseFragmentIndicatorView,
        viewPager: ViewPager2?,
        dataCnt: Int?,
        dotSize: Int,
    ) {
        indicatorView.viewPager = viewPager
        indicatorView.dataCnt = dataCnt ?: 0
        indicatorView.dotSize = dotSize.dp
    }

    @JvmStatic
    @BindingAdapter(value = ["viewPager", "indicatorCnt"], requireAll = false)
    fun setIndicatorViewPager(
        indicatorView: BaseIndicatorView,
        viewPager: ViewPager2?,
        dataCnt: Int?,
    ) {
        indicatorView.viewPager = viewPager
        indicatorView.dataCnt = dataCnt ?: 0
    }

    /**
     * set Line Tab Layout
     */
    @JvmStatic
    @BindingAdapter(value = ["viewPager", "menuList", "fixedSize"], requireAll = false)
    fun setLineTabDataList(
        view: LinePagerTabLayout,
        viewPager: ViewPager2,
        dataList: MutableList<PagerTabItem>?,
        fixedSize: Int?,
    ) {
        DLogger.d("[setLineTabDataList]")
        view.viewPager = viewPager
        view.fixedSize = fixedSize ?: -1
        if (!dataList.isNullOrEmpty()) {
            view.dataList = dataList
        }
    }


    /**
     * LinePagerTabLayout Set DataList 처리 함수
     * @param viewPager CurrentViewPager
     * @param type 탭 레이아웃 타입
     * @param dataList 탭 리스트
     * @param fixedSize 고정으로 가득 채울 사이즈
     */
    @JvmStatic
    @BindingAdapter(value = ["viewPager", "type", "menuList", "fixedSize"], requireAll = false)
    fun setLineTabDataListV2(
        view: LinePagerTabLayoutV2,
        viewPager: ViewPager2?,
        type: PagerTabType?,
        dataList: List<PagerTabItemV2>?,
        fixedSize: Int?
    ) {
        if (type == null || dataList == null) return

        view.viewPager = viewPager
        view.fixedSize = fixedSize ?: -1
        view.setDataList(type, dataList)
    }

    // rv line
    // ex)
    //app:dividerColor="@{@color/color_007eff}"
    //app:dividerHeight="@{@dimen/size_1}"
    //app:dividerPadding="@{@dimen/size_10}"
    @JvmStatic
    @BindingAdapter(value = ["dividerHeight", "dividerPadding", "dividerColor"], requireAll = false)
    fun setRecyclerViewDivider(
        recyclerView: RecyclerView,
        dividerHeight: Float?,
        dividerPadding: Float?,
        @ColorInt dividerColor: Int?,
    ) {
        val decoration = CustomDecoration(
            height = dividerHeight ?: 0f,
            padding = dividerPadding ?: 0f,
            color = dividerColor ?: Color.TRANSPARENT
        )
        recyclerView.addItemDecoration(decoration)
    }


    //[s] EditText InverseBindingAdapter
    //Set
    @JvmStatic
    @BindingAdapter("android:text")
    fun setTextString(
        view: EditText,
        contents: String?
    ) {
        var old: String = view.text.toString()
        if (old != contents) view.setText(contents)
    }

    // Get
    @JvmStatic
    @InverseBindingAdapter(attribute = "android:text", event = "textAttrChanged")
    fun getTextString(
        view: EditText
    ): String? {
        return view.text.toString()
    }

    //Run
    @JvmStatic
    @BindingAdapter("textAttrChanged")
    fun setTextWatcher(view: EditText, textAttrChagned: InverseBindingListener) {
        view.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                textAttrChagned?.onChange();
            }
        })
    }
    //[e] EditText InverseBindingAdapter


    //[s] EditText InverseBindingAdapter - hash
    //Set
    @JvmStatic
    @BindingAdapter("hashText")
    fun setHashTextString(
        view: EditText,
        contents: String
    ) {
        var old: String = view.text.toString()
        if (old != contents) view.setText(contents)
    }

    // Get
    @JvmStatic
    @InverseBindingAdapter(attribute = "hashText", event = "hashTextAttrChanged")
    fun getHashTextString(
        view: EditText
    ): String? {
        return view.text.toString()
    }


    //Run
    @JvmStatic
    @BindingAdapter("hashTextAttrChanged")
    fun onHashTextWatcher(view: EditText, textAttrChagned: InverseBindingListener) {
        view.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

                if (!s.isNullOrEmpty() && s.trim().isNotEmpty()) {

                    val regex =
                        "[^#?ㄱ-ㅎㅏ-ㅣ가-힣A-Za-z0-9\\·\\u318D\\u119E\\u11A2\\u2022\\u2025a\\u00B7\\uFE55]+".toRegex()

                    var formattedText = ("$s")
                        .replace(" ", " #")
                        .replace("##", "#")
                        .replace(regex, "")

                    if (formattedText.isNotEmpty()) {
                        if (formattedText.first().toString() != "#") {
                            formattedText = "#$formattedText"
                        }
                    }

                    if (s.toString() != formattedText) {
                        s?.replace(0, s.length, formattedText, 0, formattedText.length)
                    }
                    setHashTagColorText(view, s.toString())
                }
            }

            override fun beforeTextChanged(p0: CharSequence, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(
                s: CharSequence,
                start: Int,
                lengthBefore: Int,
                lengthAfter: Int
            ) {
                textAttrChagned?.onChange()
            }
        })
    }

    private fun setHashTagColorText(view: EditText, text: String) {

//        CoroutineScope(Dispatchers.Main).launch {

        if (!TextUtils.isEmpty(text)) {

            val regex = "(#[ㄱ-ㅎㅏ-ㅣ가-힣A-Za-z0-9-_]+)(?:#[ㄱ-ㅎㅏ-ㅣ가-힣A-Za-z0-9-_]+)*".toRegex()

            val matchResult = regex.findAll(text)

            matchResult?.forEachIndexed { index, matchResult ->
                val targetStr = matchResult.value
                if (!TextUtils.isEmpty(targetStr)) {
                    val startPosition = text.indexOf(targetStr)
                    val endPosition = targetStr.length
                    colorSpannable(view, startPosition, (startPosition + endPosition))
                }
            }
        }
//        }
    }

    /**
     * Set Color
     */
    private fun colorSpannable(view: EditText, start: Int, end: Int) {
        var endPos: Int = end

        // 해쉬태그 최대 글자 수 제한 방어 코드 적용
        if (endPos > 150) {
            endPos = 150
        }

        view.text?.setSpan(
            ForegroundColorSpan(
                ContextCompat.getColor(
                    view.context,
                    R.color.color_2fc2ff
                )
            ), start, endPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }
    //[e] EditText InverseBindingAdapter

    /**
     * TextView Flags 셋팅 하는 함수
     * @param paintFlags ex.) Paint.UNDERLINE_TEXT_FLAG
     */
    @JvmStatic
    @BindingAdapter("textViewFlags")
    fun setTextViewFlags(tv: AppCompatTextView, paintFlags: Int) {
        try {
            tv.paintFlags = tv.paintFlags or paintFlags
        } catch (ex: Exception) {
        }
    }
    // [e] Common View =============================================================================

    /**
     * 공통 ViewPager fragment
     */
    @Suppress("UNCHECKED_CAST")
    @JvmStatic
    @BindingAdapter(value = ["itemType", "dataList", "viewModel"], requireAll = false)
    fun <T : Any> setFragmentViewPagerAdapter(
        viewPager: ViewPager2,
        type: FragmentType,
        dataList: MutableList<T>?,
        viewModel: BaseViewModel?,
    ) {

        if (viewPager.adapter == null) {
            viewPager.offscreenPageLimit = 1
            viewPager.adapter = when (type) {
                FragmentType.BASE_MAIN -> {
                    MainActivity.MainFragmentPagerAdapter(viewPager.context, viewModel)
                }

                //메인 feed,recommend
                FragmentType.BASE_MAIN_SUB -> {
                    viewPager.context.getFragmentAct()?.supportFragmentManager?.fragments?.first()
                        ?.let { mainFargment ->
                            mainFargment.childFragmentManager.findFragmentByTag(NaviType.MAIN.name)
                                ?.let { subFragment ->
                                    MainTabFragment.MainTabFragmentPagerAdapter(
                                        subFragment,
                                        viewModel
                                    )
                                }
                        }
                }

                FragmentType.POPULAR_RECENT -> { // 노래 리스트 메인 -> 인기곡/신곡
                    SongMainActivity.SongFragmentPagerAdapter(viewPager.context, viewModel)
                }

                FragmentType.PART_INFO_VIEW -> { // 노래 파트 선택
                    viewPager.context.getFragmentAct()?.supportFragmentManager?.fragments?.first()
                        ?.let { prepareSingFragment ->
                            PrepareSingFragment.PartInfoFragmentPagerAdapter(
                                prepareSingFragment,
                                viewModel
                            )
                        }
                }

                FragmentType.FOLLOWING_INFO -> { // 팔로우, 팔로잉
                    MypageFollowActivity.FollowTapFragmentPagerAdapter(viewPager.context, viewModel)
                }

                // 씽패스 미션 목록
                FragmentType.SING_PASS_MISSION -> {
                    DLogger.d("setFragmentViewPagerAdapter : FragmentType.SING_PASS_MISSION")
                    SingPassDashBoardActivity.MissionTabFragmentPagerAdapter(
                        viewPager.context,
                        viewModel
                    )
                }

            }
        }
        //[e] setAdapter

        if (dataList != null && dataList.size > 0) {
            //parent
            if (viewPager.adapter != null) {
                if (viewPager.adapter is BaseFragmentPagerAdapter<*>) {
                    (viewPager.adapter as BaseFragmentPagerAdapter<T>).run {
                        if (this.dataList !== dataList || this.dataList.size != dataList.size) {
                            setDataList(dataList)
                        }
                    }
                } else {
                    //child
                    (viewPager.adapter as BaseChildFragmentPagerAdapter<T>).run {
                        if (this.dataList !== dataList || this.dataList.size != dataList.size) {

                            setDataList(dataList)
                        }
                    }
                }
            }
        }
    }

    /**
     * 공통 RecyclerView : RecyclerView.Adapter
     */
    @Suppress("UNCHECKED_CAST")
    @JvmStatic
    @BindingAdapter(value = ["itemList", "viewModel"], requireAll = false)
    fun <T : BaseViewTypeModel> setRecyclerAdapter(
        recyclerView: RecyclerView,
        itemList: MutableList<T>?,
        viewModel: BaseViewModel,
    ) {

        if (recyclerView.adapter == null) {
            recyclerView.adapter = CommonAdapter<T>(viewModel)
        }

        if (itemList != null && itemList.size > 0) {
            (recyclerView.adapter as CommonAdapter<T>).run {
                this.addAll(itemList)
            }
        }
    }

    /**
     * 공통 ViewPager : CommonAdapter
     */
    @Suppress("UNCHECKED_CAST")
    @JvmStatic
    @BindingAdapter(value = ["vpDataList", "vpViewModel"], requireAll = false)
    fun <T : BaseViewTypeModel> setViewPagerAdapter(
        viewpager: ViewPager2,
        dataList: MutableList<T>?,
        viewModel: BaseViewModel
    ) {
        if (viewpager.adapter == null) {
            viewpager.adapter = CommonAdapter<T>(viewModel)
        }
        if (dataList != null && dataList.size > 0) {
            (viewpager.adapter as CommonAdapter<T>).run {
                this.addAll(dataList)
            }
        }
    }

    /**
     * 공통 RecyclerView  : ListAdapter
     */
    @Suppress("UNCHECKED_CAST")
    @JvmStatic
    @BindingAdapter(
        value = ["itemType", "dataList", "viewModel", "isAutoVisible","isScrollTop"],
        requireAll = false
    )
    fun <T : BaseModel> setRecyclerListAdapter(
        recyclerView: RecyclerView,
        itemType: ListPagedItemType,
        dataList: MutableList<T>?,
        viewModel: BaseViewModel,
        _isAutoVisible: Boolean?,
        _isScrollTop: Boolean?
    ) {
        val isAutoVisible = _isAutoVisible ?: true
        val isScrollTop = _isScrollTop ?: false
        if (recyclerView.adapter == null) {

            if (itemType == ListPagedItemType.MY_PAGE_RECOMMEND) {
                recyclerView.addItemDecoration(HorizontalDecorator(10.dp))
            } else if (itemType == ListPagedItemType.ITEM_SONG_HOT_INFO) {
                recyclerView.addItemDecoration(HorizontalDecorator(14.dp))
            } else if (itemType == ListPagedItemType.ITEM_SONG_POPULAR_RECENT) {
//                recyclerView.addItemDecoration(HorizontalDecorator(30.dp))
            } else if (itemType == ListPagedItemType.ITEM_COMMENT_RE) {
                recyclerView.itemAnimator = null
            } else if (itemType == ListPagedItemType.MAIN_SING_PASS_RANKING_LIST) {
                recyclerView.addItemDecoration(VerticalDecorator(5.dp))
            } else if (itemType == ListPagedItemType.ITEM_SEARCH_KEYWORD_POPULAR) {
                recyclerView.addItemDecoration(HorizontalDecorator(48.dp))
            } else if (itemType == ListPagedItemType.ITEM_SEARCH_LOVELY_SONG) {
                recyclerView.addItemDecoration(HorizontalDecorator(40.dp))
            } else if (itemType == ListPagedItemType.ITEM_SEARCH_POPULAR_SONG) {
                recyclerView.addItemDecoration(VerticalDecorator(20.dp))
            } else if (itemType == ListPagedItemType.ITEM_SEARCH_USER) {
                recyclerView.addItemDecoration(VerticalDecorator(20.dp))
            } else if (itemType == ListPagedItemType.ITEM_LYRICS || itemType == ListPagedItemType.ITEM_LYRICS_VIDEO) {
                recyclerView.setOnTouchListener { _: View?, _: MotionEvent? -> true }
                recyclerView.addItemDecoration(LyricsDecoration())
            } else if (itemType == ListPagedItemType.ITEM_LOUNGE_GALLERY) {
                recyclerView.addItemDecoration(HorizontalDecorator(2.dp))
            } else if (itemType == ListPagedItemType.MY_PAGE_SINGPASS) {
                recyclerView.addItemDecoration(HorizontalDecorator(20.dp))
            } else if (itemType == ListPagedItemType.ITEM_NATION) {
                recyclerView.addItemDecoration(VerticalDecorator(20.dp))
            } else if (itemType == ListPagedItemType.ITEM_LANGUAGE) {
                recyclerView.addItemDecoration(VerticalDecorator(20.dp))
            } else if (itemType == ListPagedItemType.MAIN_SING_PASS_SEASON_ITEM_LIST) {
                recyclerView.addItemDecoration(HorizontalDecorator(40.dp))
            }


            //setAdapter
            recyclerView.adapter = CommonListAdapter<T>(viewModel, itemType)
        }

        if (dataList != null && dataList.size > 0) {
            viewModel.viewModelScope.launch {
                val adapter = recyclerView.adapter as CommonListAdapter<T>
                dataList?.let {
                    if (itemType == ListPagedItemType.ITEM_SONG_RECENTLY){
                        if (dataList.size > 3){
                            adapter.submitList(dataList.subList(0,3).toList())
                        } else {
                            adapter.submitList(dataList.toList())
                        }
                    } else {
                        adapter.submitList(dataList.toList())
                    }
                }
                DLogger.d("isScrollTop=> ${isScrollTop}")
                if(isScrollTop){
                    delay(200)
                    recyclerView.scrollToPosition(0)
                }

            }

            if (isAutoVisible) {
                recyclerView.visibility = View.VISIBLE
            }
        } else {
            if (isAutoVisible) {
                recyclerView.visibility = View.GONE
            }
        }
    }

    @BindingAdapter("setAdapterWithIntList")
    @JvmStatic
    fun setAdapter(view: ViewPager2, item: ArrayList<TutorialItemData>) {
        val adapter = TutorialListAdapter(item)
        view.adapter = adapter
    }

    /**
     * 공통 RecyclerView (Paging) :  PagingDataAdapter
     */
    @Suppress("UNCHECKED_CAST")
    @JvmStatic
    @BindingAdapter(
        value = ["itemType", "pagedList", "viewModel", "emptyView"],
        requireAll = false
    )
    fun <T : BaseModel> setRecyclerPagingAdapter(
        recyclerView: RecyclerView,
        itemType: ListPagedItemType,
        dataList: PagingData<T>?,
        viewModel: BaseViewModel,
        emptyView: View?
    ) {

        emptyView?.visibility = View.GONE
//        recyclerView.visibility = View.GONE

        //Decoration 설정
        if (recyclerView.adapter == null) {

            val commonAdapter = CommonPagingAdapter<T>(viewModel, itemType)

            //add DecorationItem
            when (itemType) {
                ListPagedItemType.NONE -> recyclerView.addItemDecoration(VerticalDecorator(20.dp))
                ListPagedItemType.COLLECTION_FEED -> recyclerView.addItemDecoration(GridDecorator(3, 1.dp, false))
                ListPagedItemType.ITEM_RELATEDSOUND -> recyclerView.addItemDecoration(VerticalDecorator(20.dp))
                ListPagedItemType.ITEM_PRIVATE_FEED -> recyclerView.addItemDecoration(GridDecorator(3, 1.dp, false))
                ListPagedItemType.ITEM_SEARCH_VIDEO -> recyclerView.addItemDecoration(GridDecorator(3, 1.dp, false))
                ListPagedItemType.ITEM_SEARCH_CONTENT -> recyclerView.addItemDecoration(GridDecorator(3, 1.dp, false))
                ListPagedItemType.ITEM_SEARCH_USER,
                ListPagedItemType.ITEM_SEARCH_POPULAR_SONG -> recyclerView.addItemDecoration(VerticalDecorator(20.dp))
                ListPagedItemType.ITEM_COMMENT,ListPagedItemType.ITEM_COMMENT_RE -> recyclerView.itemAnimator =null
                ListPagedItemType.ITEM_MY_PAGE_FEED->  recyclerView.itemAnimator = null
                else -> {} // Default
            }
            //setAdapter
            recyclerView.adapter =  commonAdapter.withLoadStateFooter(PagingLoadStateAdapter(commonAdapter))
        }

        //Set Data
        if (recyclerView.adapter != null) {

            val adapter =  (recyclerView.adapter as ConcatAdapter).adapters[0] as CommonPagingAdapter<T>

            //Set Paging data
            recyclerView.lifecycleOwner?.lifecycleScope?.launch {
                dataList?.let {
                    adapter.submitData(it)
                }
            }

            //State
            recyclerView.lifecycleOwner?.lifecycleScope?.launch {
                adapter.loadStateFlow.collectLatest { loadStates ->

                    when (loadStates.refresh) {
//                        is LoadState.Loading -> viewModel.onLoadingShow()
                        is LoadState.NotLoading -> {
                            viewModel.onLoadingDismiss()
                            //Empty View..
                            if (loadStates.append.endOfPaginationReached && adapter.itemCount < 1) {

                                emptyView?.let {
                                    if (!it.isVisible) {
                                        it.visibility = View.VISIBLE
                                    }
                                }

                                if (recyclerView.isVisible) {
                                    recyclerView.visibility = View.GONE
                                }

                            } else {
                                emptyView?.let {
                                    if (it.isVisible) {
                                        it.visibility = View.GONE
                                    }
                                }

                                if (!recyclerView.isVisible) {
                                    recyclerView.visibility = View.VISIBLE
                                }
                            }
                        }

                        is LoadState.Error -> {
                            viewModel.onLoadingDismiss()
                            viewModel.showNetworkErrorDialog(errorMsg = (loadStates.refresh as LoadState.Error).error.localizedMessage)
                            emptyView?.let {
                                if (!it.isVisible) {
                                    it.visibility = View.VISIBLE
                                }
                            }

                            if (recyclerView.isVisible) {
                                recyclerView.visibility = View.GONE
                            }
                        }

                        else -> viewModel.onLoadingDismiss()
                    }
                }
            }
        }
    }
}