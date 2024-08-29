package com.verse.app.widget.views

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.ScrollView
import dagger.hilt.android.AndroidEntryPoint

/**
 * Description :
 *
 * Created by jhlee on 2023-05-28
 */
@AndroidEntryPoint
class SectionScrollView @JvmOverloads constructor(
    ctx: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ScrollView(ctx, attrs, defStyleAttr) {

    var isState: Boolean = false

    fun setShouldStopInterceptingTouchEvent(state: Boolean) {
        this.isState = state;
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return if (isState) {
            false
        } else {
            super.onInterceptTouchEvent(ev)
        }
    }
}