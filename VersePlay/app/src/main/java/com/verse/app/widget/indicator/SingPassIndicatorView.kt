package com.verse.app.widget.indicator

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.util.AttributeSet
import androidx.viewpager2.widget.ViewPager2
import com.verse.app.R
import com.verse.app.extension.dp
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.ActivityScoped

/**
 * Description : ViewPager2 Dot Indicator View.
 *
 * Created by jhlee on 2023-03-24
 */
class SingPassIndicatorView @JvmOverloads constructor(
    ctx: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BaseFragmentIndicatorView(ctx, attrs, defStyleAttr) {

    private val dotGap: Int = dotSize
    private var rootWidth: Int = 0
    private var rootHeight: Int = 0
    private var indicatorStartPoint: Float = 0F
    private var defaultRadius = 50F //


    override fun updateIndicator() {

    }

    override fun onPageSelect(pos: Int) {
        position = pos+1
        invalidate()
    }

    override fun onPageScroll(pos: Int, offset: Float) {}

    override fun onPageScrollState(state: Int) {}

    override fun onIndicatorDraw(canvas: Canvas) {
        // Indicator Setting
        backgroundRect.left = indicatorStartPoint
        backgroundRect.right = backgroundRect.left + dotSize
        val middlePoint: Float = rootHeight / 2F
        backgroundRect.top = middlePoint - (dotSize / 2F)
        backgroundRect.bottom = middlePoint + (dotSize / 2F)
        indicatorRect.top = backgroundRect.top
        indicatorRect.bottom = backgroundRect.bottom
        var selectNext: Boolean = false

        val indicatorIndex = computeFindPos()
        for (i in 0 until dataCnt) {

            // Indicator Draw
            if (i == indicatorIndex) {
                indicatorRect.left = backgroundRect.left
                indicatorRect.right = backgroundRect.right + dotSize

                selectNext = true

                canvas.drawRoundRect(indicatorRect, defaultRadius, defaultRadius, indicatorPaint)
            } else {
                // Background Draw
                canvas.drawRoundRect(backgroundRect, defaultRadius, defaultRadius, backgroundPaint)
            }

            if (selectNext) {
                backgroundRect.left = (indicatorRect.right + dotGap)
                backgroundRect.right = backgroundRect.left + dotSize
                selectNext = false
            } else {
                backgroundRect.left = (backgroundRect.right + dotGap)
                backgroundRect.right = backgroundRect.left + dotSize
            }
        }
    }

    private fun computeFindPos() = position.minus(1)

    override fun onIndicatorMeasure(measureWidth: Int, measureHeight: Int) {
        rootWidth = measureWidth
        rootHeight = measureHeight
        computeStartPoint()
    }

    /**
     * Gravity 상태에 따라서
     * Indicator Start Point 계산 하는 함수.
     */
    private fun computeStartPoint() {
        // 불필요한 타입인 경우 리턴.
        if (rootWidth == 0 || dataCnt == 0 || gravity == Gravity.LEFT) return

        val indicatorRootWidth =
            (dotSize * dataCnt.toFloat()) + (dotGap * (dataCnt.toFloat() - 1F).coerceAtLeast(0F))
        if (gravity == Gravity.CENTER) {
            indicatorStartPoint = (rootWidth.toFloat() / 2F) - (indicatorRootWidth / 2F)
        } else if (gravity == Gravity.RIGHT) {
            indicatorStartPoint = rootWidth - indicatorRootWidth
        }
    }
}