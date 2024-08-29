package com.verse.app.utility

import android.text.Layout
import android.text.Spannable
import android.text.method.BaseMovementMethod
import android.text.style.ClickableSpan
import android.view.MotionEvent
import android.widget.TextView


class CustomMovementUtils private constructor() : BaseMovementMethod() {
    override fun onTouchEvent(widget: TextView, buffer: Spannable, event: MotionEvent): Boolean {
        val action = event.action
        if (action == MotionEvent.ACTION_UP ||
            action == MotionEvent.ACTION_DOWN
        ) {
            var x = event.x.toFloat()
            var y = event.y.toInt()
            x -= widget.totalPaddingLeft
            y -= widget.totalPaddingTop
            x += widget.scrollX
            y += widget.scrollY
            val layout: Layout = widget.layout
            val line: Int = layout.getLineForVertical(y)
            val off: Int = layout.getOffsetForHorizontal(line, x)
            val link = buffer.getSpans(
                off, off,
                ClickableSpan::class.java
            )
            if (link.size != 0) {
                if (action == MotionEvent.ACTION_UP) {
                    link[0].onClick(widget)
                }
                return true
            }
        }
        return true
    }

    companion object {
        private var customMovementMethod: CustomMovementUtils? = null
        val instance: CustomMovementUtils?
            get() {
                if (customMovementMethod == null) {
                    synchronized(CustomMovementUtils::class.java) {
                        if (customMovementMethod == null) {
                            customMovementMethod =
                                CustomMovementUtils()
                        }
                    }
                }
                return customMovementMethod
            }
    }
}