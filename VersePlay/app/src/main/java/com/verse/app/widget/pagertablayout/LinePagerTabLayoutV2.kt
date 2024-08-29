package com.verse.app.widget.pagertablayout

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Space
import androidx.annotation.Dimension
import androidx.annotation.LayoutRes
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.postDelayed
import androidx.core.view.size
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.MutableLiveData
import androidx.viewpager2.widget.ViewPager2
import com.verse.app.BR
import com.verse.app.R
import com.verse.app.databinding.ItemLineTabLayoutV2Binding
import com.verse.app.databinding.VLineTabLayoutV2Binding
import com.verse.app.extension.currentItem
import com.verse.app.extension.dp
import com.verse.app.extension.getFragmentActivity
import com.verse.app.extension.initBinding
import com.verse.app.extension.multiNullCheck
import com.verse.app.utility.DLogger

/**
 * Description : Line Pager TabLayout V2
 *
 * Created by juhongmin on 2023/05/17
 */
class LinePagerTabLayoutV2 @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BaseTabLayoutV2(context, attrs, defStyleAttr) {

    private val tabListener = object : Listener {
        override fun onTabClick(pos: Int, view: View) {
            if (!isEnabled) {
                return
            }
            if (currentPos != pos) {
                currentPos = pos
                // ViewPager Animation 인경우
                val viewPager = viewPager
                if (viewPager != null) {
                    viewPager.currentItem(pos, false)
                    setCurrentIndicator(currentPos)
                    updateTab(currentPos)
                } else {
                    setCurrentIndicator(currentPos)
                    updateTab(currentPos)
                }

                tabClickListener?.onTabClick(pos)
            }
        }
    }

    // [s] Attribute Set Variable
    @Dimension
    private val indicatorHeight: Float

    @Dimension
    private val indicatorCorner: Float
    private var scrollingOffset = 0

    @Dimension
    private val indicatorPadding: Float
    // [e] Attribute Set Variable

    private val indicatorPaint: Paint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }
    }

    private val tabContainer: VLineTabLayoutV2Binding
    private val dataList: MutableList<PagerTabItemV2> by lazy { mutableListOf() }
    private val currentPosition: MutableLiveData<Int> by lazy { MutableLiveData() }

    var fixedSize: Int = -1

    private var tabCount: Int = 0
    private var currentPos: Int = 0 // 현재 위치값.
    private var posScrollOffset: Float = -1F // Scroll Offset.
    private var lastScrollX = 0
    private val indicatorRectF = RectF()
    private val bottomLineRectF = RectF()

    var tabClickListener: TabClickListener? = null

    init {
        setWillNotDraw(false)
        context.obtainStyledAttributes(attrs, R.styleable.LinePagerTabLayoutV2).run {
            val indicatorColor = getColor(
                R.styleable.LinePagerTabLayoutV2_tabIndicatorColor,
                NO_ID
            )
            if (indicatorColor != NO_ID) {
                indicatorPaint.color = indicatorColor
            }

            indicatorHeight = getDimension(R.styleable.LinePagerTabLayoutV2_tabIndicatorHeight, -1F)
            indicatorCorner = getDimension(R.styleable.LinePagerTabLayoutV2_tabIndicatorCorner, 0F)
            indicatorPadding =
                getDimension(R.styleable.LinePagerTabLayoutV2_tabIndicatorPadding, 0F)
            scrollingOffset =
                getDimensionPixelOffset(R.styleable.LinePagerTabLayoutV2_tabScrollOffset, 0)

            recycle()
        }

        tabContainer = initBinding(R.layout.v_line_tab_layout_v2, this)

        // Preview Mode
        if (isInEditMode) {
            val previewTabItems: String
            val previewTabPos: Int

            context.obtainStyledAttributes(attrs, R.styleable.LinePagerTabLayoutV2).run {
                previewTabItems = getString(R.styleable.LinePagerTabLayoutV2_tabPreviewItems) ?: ""
                previewTabPos = getInt(R.styleable.LinePagerTabLayoutV2_tabPreviewPos, 0)
                recycle()
            }
            addPreviewTabItems(previewTabItems, previewTabPos)
        } else {
            this.getFragmentActivity()?.lifecycle?.addObserver(this)
        }
    }

    // [s] Preview Mode Perform

    /**
     * 미리보기 지원 함수
     */
    private fun addPreviewTabItems(tabItems: String, pos: Int) {
        try {
            val split = tabItems.split(",")
            if (type == Type.FIXED) {
                tabContainer.linerLayout.weightSum = split.size.toFloat()
            }
            split.forEachIndexed { index, s ->
                tabContainer.linerLayout.addView(initPreview(s, index, pos))
            }
        } catch (ex: Exception) {
            // ignore
        }
    }

    /**
     * 미리보기 화면에 대한 표시 처리
     */
    private fun initPreview(title: String, currPos: Int, indicatorPos: Int): ViewGroup {
        return ConstraintLayout(context).apply {
            // Scrollable Type 에 따라서 Child View 처리
            layoutParams = if (type == Type.FIXED) {
                LinearLayoutCompat.LayoutParams(
                    0,
                    LinearLayoutCompat.LayoutParams.MATCH_PARENT
                ).also {
                    it.weight = 1F
                }
            } else {
                LinearLayoutCompat.LayoutParams(
                    LinearLayoutCompat.LayoutParams.WRAP_CONTENT,
                    LinearLayoutCompat.LayoutParams.MATCH_PARENT
                )
            }

            addView(initPreviewTextView(title, currPos == indicatorPos))
            if (indicatorHeight > 0F || bottomLineHeight > 0F) {
                addView(initPreviewIndicator(currPos == indicatorPos))
            }
        }
    }

    private fun initPreviewTextView(title: String, isIndicator: Boolean): AppCompatTextView {
        return AppCompatTextView(context).apply {
            layoutParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            ).also {
                it.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
                it.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
                it.leftToLeft = ConstraintLayout.LayoutParams.PARENT_ID
                it.rightToRight = ConstraintLayout.LayoutParams.PARENT_ID
                it.leftMargin = 10.dp
                it.rightMargin = 10.dp
            }
            gravity = Gravity.CENTER

            if (textAppearance != -1) {
                setTextAppearance(textAppearance)
            }

            if (isIndicator) {
                setTextViewTypeFace(this, enableStyle.style)
                setTextColor(enableStyle.color)
            } else {
                setTextViewTypeFace(this, disableStyle.style)
                setTextColor(disableStyle.color)
            }

            text = title
        }
    }

    private fun setTextViewTypeFace(tv: AppCompatTextView, style: String?) {
        if (style == null) return
        when (style) {
            "bold" -> {
                tv.typeface = ResourcesCompat.getFont(
                    context,
                    R.font.font_noto_sans_kr_bold
                )
            }

            "normal" -> {
                tv.typeface = ResourcesCompat.getFont(
                    context,
                    R.font.font_noto_sans_kr_light
                )
            }

            "medium" -> {
                tv.typeface = ResourcesCompat.getFont(
                    context,
                    R.font.font_noto_sans_kr_medium
                )
            }

            else -> {
            }
        }
    }

    private fun initPreviewIndicator(isIndicator: Boolean): View {
        return View(context).apply {
            layoutParams = ConstraintLayout.LayoutParams(
                0,
                if (isIndicator) indicatorHeight.toInt() else bottomLineHeight.toInt()
            ).also {
                it.leftToLeft = ConstraintLayout.LayoutParams.PARENT_ID
                it.rightToRight = ConstraintLayout.LayoutParams.PARENT_ID
                it.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
            }
            setBackgroundColor(if (isIndicator) indicatorPaint.color else bottomLinePaint.color)
        }
    }
    // [e] Preview Mode Perform

    override fun onCreate() {
        super.onCreate()
        currentPosition.observe(this) {
            updateTab(it)
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas == null) return
        // 아직 TabLayout 정의되지 않은 경우 밑줄선은 그리도록 처리
        if (tabCount == 0 && bottomLineHeight > 0F) {
            drawBottomLine(canvas)
            return
        }
        if (indicatorHeight == -1F) return

        drawBottomLine(canvas)
        drawLine(canvas, currentPos, posScrollOffset)
    }

    /**
     * Bottom Line 그리기
     * @param canvas Canvas
     */
    private fun drawBottomLine(canvas: Canvas) {
        // BottomLine Draw
        if (bottomLineHeight > 0F) {
            // 초기값 셋팅
            if (bottomLineRectF.height() == 0F) {
                bottomLineRectF.left = 0F
                bottomLineRectF.top = height - bottomLineHeight
                bottomLineRectF.bottom = height.toFloat()
            }
            bottomLineRectF.right = tabContainer.linerLayout.right.toFloat()
            canvas.drawRect(bottomLineRectF, bottomLinePaint)
            canvas.save()
        }
    }

    /**
     * DrawLine
     */
    private fun drawLine(canvas: Canvas, currPos: Int, scrollOffset: Float) {
        // 초기 Top or Bottom 셋팅
        if (indicatorRectF.top == 0F && height > 0) {
            indicatorRectF.top = height - indicatorHeight
            indicatorRectF.bottom = height.toFloat()
        }

        // Scroll 할때만 처리
        if (scrollOffset > 0F && currentPos < tabCount.minus(1)) {
            tabContainer.linerLayout.runCatching {
                multiNullCheck(
                    dataList[currPos].view,
                    dataList[currPos.plus(1)].view
                ) { currTab, nextTab ->
                    var left =
                        (scrollOffset * nextTab.left + (1F - scrollOffset) * currTab.left)
                    var right =
                        (scrollOffset * nextTab.right + (1F - scrollOffset) * currTab.right)
                    left -= indicatorPadding
                    right += indicatorPadding

                    indicatorRectF.left = left
                    indicatorRectF.right = right
                }
            }
        }

        // Indicator Draw
        if (indicatorCorner > 0) {
            canvas.drawRoundRect(
                indicatorRectF,
                indicatorCorner,
                indicatorCorner,
                indicatorPaint
            )
        } else {
            canvas.drawRect(
                indicatorRectF,
                indicatorPaint
            )
        }
    }

    /**
     * Set Data List
     * @param childLayoutType 페이저 타입 레이아웃
     * @param list 데이터 리스트
     */
    fun setDataList(childLayoutType: PagerTabType, list: List<PagerTabItemV2>?) {
        if (list == null) return

        // [s] 초기화 처리
        currentPos = 0
        currentPosition.value = 0
        tabCount = list.size
        tabContainer.linerLayout.removeAllViews()
        // [e] 초기화 처리
        val layoutParams = LinearLayoutCompat.LayoutParams(
            LinearLayoutCompat.LayoutParams.WRAP_CONTENT,
            LinearLayoutCompat.LayoutParams.MATCH_PARENT
        )

        if (type == Type.FIXED) {
            if (fixedSize != -1) {
                tabContainer.linerLayout.weightSum = fixedSize.toFloat()
            } else {
                tabContainer.linerLayout.weightSum = tabCount.toFloat()
            }
            layoutParams.width = 0
            layoutParams.weight = 1F
        }

        // 데이터 리스트에 맞게 아이템 Binding 한다.
        list.forEachIndexed { index, data ->
            data.pos = index
            data.isSelected?.value = index == currentPos
            data.txtColor = enableStyle.color
            data.txtStyle = enableStyle.style
            data.disableTxtColor = disableStyle.color
            data.disableTxtStyle = disableStyle.style

            // 각 타입별 분기 처리
            when (childLayoutType) {
                PagerTabType.COMMUNITY -> {
                    initCommonChildViewV2<ItemLineTabLayoutV2Binding>(
                        childLayoutType.layoutId,
                        layoutParams, data
                    ).apply {
                        // TextStyle Setting
                        if (textAppearance != -1) {
                            tvTitle.setTextAppearance(textAppearance)
                        }

                        if (index == 0) {
                            addSpace(tabContainer.linerLayout,  30.dp)
                        } else {
                            addSpace(tabContainer.linerLayout,  10.dp)
                        }
                        tabContainer.linerLayout.addView(this.root)

                        if (index == list.lastIndex) {
                            addSpace(tabContainer.linerLayout, 30.dp)
                        } else {
                            addSpace(tabContainer.linerLayout,  10.dp)
                        }

                        // 특정 탭 레이아웃들 인디게이터 사이즈 처리
                        if (index == 0) {
                            this.root.post {
                                indicatorRectF.left = this.root.left.minus(indicatorPadding)
                                indicatorRectF.right = this.root.right.plus(indicatorPadding)
                                this@LinePagerTabLayoutV2.invalidate()
                            }
                        }
                    }
                }

                PagerTabType.DEFAULT -> {
                    initCommonChildViewV2<ItemLineTabLayoutV2Binding>(
                        childLayoutType.layoutId,
                        layoutParams, data
                    ).apply {
                        // TextStyle Setting
                        if (textAppearance != -1) {
                            tvTitle.setTextAppearance(textAppearance)
                        }

                        // 스크롤 상태인경우에만 빈 공간 추가
                        if (type == Type.SCROLLABLE) {
                            // 공통 사이 간격 첫번째는 20, 나머지 양옆 10씩
                            addEmptyView(
                                tabContainer.linerLayout,
                                index,
                                if (index == 0) 16.dp else 10.dp
                            )
                        }

                        tabContainer.linerLayout.addView(this.root)

                        // 스크롤 상태인경우에만 빈 공간 추가
                        if (type == Type.SCROLLABLE) {
                            addEmptyView(tabContainer.linerLayout, index, 10.dp)
                        }

                        // 특정 탭 레이아웃들 인디게이터 사이즈 처리
                        if (index == 0) {
                            this.root.post {
                                indicatorRectF.left = this.root.left.minus(indicatorPadding)
                                indicatorRectF.right = this.root.right.plus(indicatorPadding)
                                this@LinePagerTabLayoutV2.invalidate()
                            }
                        }
                    }
                }
            }
        }

        dataList.clear()
        dataList.addAll(list)
    }

    /**
     * PagerTab 공통 바인딩 처리 함수
     */
    @Throws(NullPointerException::class)
    private inline fun <reified T : ViewDataBinding> initCommonChildViewV2(
        @LayoutRes layoutId: Int,
        layoutParams: LinearLayoutCompat.LayoutParams,
        data: PagerTabItemV2
    ): T {
        val viewRoot = LayoutInflater.from(context).inflate(layoutId, this, false)
        return DataBindingUtil.bind<T>(viewRoot)?.apply {
            this.lifecycleOwner = this@LinePagerTabLayoutV2
            setVariable(BR.listener, tabListener)
            setVariable(BR.item, data)
            data.view = this.root
            (this.root as ConstraintLayout).layoutParams = layoutParams
        } ?: run {
            throw NullPointerException("Invalid LayoutId")
        }
    }

    /**
     * 빈 뷰 추가 하기
     * 클릭 리스너는 추가한 인덱스 클릭 리스너 추라
     */
    private fun addEmptyView(rootView: LinearLayoutCompat, idx: Int, requestWidth: Int) {
        View(context).apply {
            layoutParams =
                LinearLayoutCompat.LayoutParams(requestWidth, ViewGroup.LayoutParams.MATCH_PARENT)
                    .apply {
                        if (requestWidth == 0) {
                            weight = 1F
                        }
                    }
            setOnClickListener { tabListener.onTabClick(idx, it) }
            rootView.addView(this)
        }
    }

    /**
     * add Space
     * @param rootView 추가 하고 싶은 ViewGroup Layout
     * @param requestWidth 스페이스 너비값
     */
    private fun addSpace(rootView: LinearLayoutCompat, requestWidth: Int) {
        Space(context).apply {
            layoutParams = LayoutParams(requestWidth, 0.dp)
            rootView.addView(this)
        }
    }

    /**
     * Update Tab Style.
     */
    private fun updateTab(pos: Int) {
        dataList.forEach { data ->
            data.isSelected?.value = data.pos == pos
        }
    }

    /**
     * 한 화면에 보여지는 Tab 이 Over 되는 경우
     * Scrolling 처리 함수.
     * @param pos Current Pos
     * @param offset Scrolling Offset
     */
    private fun scrollToChild(pos: Int, offset: Int) {
        if (tabCount == 0) return

        var newScrollX = tabContainer.linerLayout.getChildAt(pos).left + offset

        if (pos > 0 || offset > 0) {
            newScrollX -= scrollingOffset
        }

        if (newScrollX != lastScrollX) {
            lastScrollX = newScrollX
            scrollTo(newScrollX, 0)
        }
    }

    override fun onPageSelect(pos: Int) {}

    override fun onPageScroll(pos: Int, offset: Float) {
        currentPos = pos
        posScrollOffset = offset
        try {
            if (tabContainer.linerLayout.size > pos) {
                val scrollOffset = offset * (tabContainer.linerLayout).getChildAt(pos).width
                scrollToChild(pos, scrollOffset.toInt())
            }
        } catch (ex: Exception) {
            // ignore
        }

        // ReDraw
        invalidate()
    }

    override fun onPageScrollStated(state: Int) {
        if (ViewPager2.SCROLL_STATE_IDLE == state) {
            currentPosition.value = currentPos
            // 딥링크시 페이지 init 할때 인디게이터 안그리는 이슈 처리 로직
            setCurrentIndicator(currentPos)
        }
    }

    /**
     * 현재 포지션값에 따라서 인디게이터 처리
     * @param pos TargetPosition
     */
    private fun setCurrentIndicator(pos: Int) {
        runCatching {
            val childView = dataList[pos].view
            if (childView != null) {
                indicatorRectF.left = childView.left - indicatorPadding
                indicatorRectF.right = childView.right + indicatorPadding
                invalidate()
            }
        }
    }

    fun getCurrentPosition() = currentPos

    /**
     * Indicator Update
     */
    fun updateLineIndicator() {
        postDelayed(300) {
            posScrollOffset = 0.0000001F
            invalidate()
        }
    }

    fun moveTab(pos: Int) {
        currentPos = pos
        updateTab(pos)
        setCurrentIndicator(pos)
    }

    /**
     * 특정 위치에 있는 타이틀값 가져와서 수정하고 싶을때 사용하는 함수
     * @param pos 특정 위치값
     */
    fun getTabTextView(pos: Int): AppCompatTextView? {
        return if (dataList.size > pos) {
            val tabItem = dataList.get(pos)
            try {
                tabItem.view?.findViewById<AppCompatTextView>(R.id.tvTitle)
            } catch (ex: Exception) {
                null
            }
        } else {
            null
        }
    }


}
