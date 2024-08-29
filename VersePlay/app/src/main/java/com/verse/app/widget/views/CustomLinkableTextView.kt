package com.verse.app.widget.views

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import com.apradanas.simplelinkabletext.LinkableTextView


/**
 * Description : CustomTextView View Class
 * Created by jhlee on 2023-01-01
 */
class CustomLinkableTextView @JvmOverloads constructor(
    ctx: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : LinkableTextView(ctx, attrs, defStyleAttr) {

    interface Listener {
        /**
         * TextViewLineCount가 계산이 된 이후 콜백하는 함수
         */
        fun onLineCountCallback(str: CharSequence, cnt: Int)
    }

    private var originalText: CharSequence? = null
    private var listener: Listener? = null

    fun setOriginalText(text: CharSequence) {
        originalText = text
    }

    fun setLineCountCallback(listener: Listener) {
        this.listener = listener
    }

    override fun onDraw(canvas: Canvas?) {
        // 지정된 MaxLine 보다 큰경우 글자를 잘리도록 처리 특정 페이지에서만 사용하기 떄문에 말줄임 처리 X
        if (lineCount >= maxLines) {
            val charSequence = text
            val lineVisibleEnd = layout.getLineVisibleEnd(maxLines - 1)
            this.listener?.onLineCountCallback(originalText ?: "", lineCount)
            if (charSequence.length > lineVisibleEnd) {
                text = charSequence.subSequence(0, lineVisibleEnd)
            }
        } else {
            this.listener?.onLineCountCallback(originalText ?: "", lineCount)
        }

        super.onDraw(canvas)
    }
}