package com.verse.app.widget.views

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.text.TextPaint
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import com.verse.app.R
import com.verse.app.model.xtf.XTF_LYRICE_DTO
import com.verse.app.utility.DLogger


/**
 * Description : TJTextView View Class
 * Created by jhlee on 2023-04-14
 */
class TJTextView @JvmOverloads constructor(
    ctx: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : AppCompatTextView(ctx, attrs, defStyleAttr) {

    private lateinit var linePaint: TextPaint
    private var fTextTop = 0f
    private var fTextsBottom = 0f
    private var fLineHEight = 0f
    private var realFontHeight = 0f
    private var positionGap = 0f
    private var mXTF_LYRICE_DTO: XTF_LYRICE_DTO? = null
    private var curPosition = 0
    private var isTjSong = true


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (mXTF_LYRICE_DTO == null || !::linePaint.isInitialized) {
            return
        }

        mXTF_LYRICE_DTO?.let {


            val tempString = text.toString()

            val currentWidth = it.currentWidth
            val maxWidth = it.textWidth

            if (currentWidth > 0) {

                val textBounds = Rect()

                if (currentWidth == maxWidth) {
                    setTextColor(ContextCompat.getColor(context, R.color.color_707070))
                } else {

                    linePaint.getTextBounds(text.toString(), 0, text.toString().length, textBounds)

                    val mTextWidth = linePaint.measureText(text.toString()).toInt()

                    val bounds = canvas.clipBounds

                    val currentBitmap = run {
                        if (isTjSong) {
                            Bitmap.createBitmap(currentWidth, fLineHEight.toInt(), Bitmap.Config.ARGB_8888)
                        } else {
                            Bitmap.createBitmap(maxWidth, fLineHEight.toInt(), Bitmap.Config.ARGB_8888)
                        }
                    }

                    val mCanvas = Canvas(currentBitmap)

                    mCanvas.drawText(tempString, 0f, -fTextTop, linePaint)

                    canvas.drawBitmap(currentBitmap, bounds.centerX() - mTextWidth / 2f, realFontHeight, null)

                    currentBitmap.recycle()
                }
            }
        }
    }

    fun setConfigInfo(data: XTF_LYRICE_DTO, position: Int, isTj: Boolean) {
        mXTF_LYRICE_DTO = data
        isTjSong = isTj

        val textPaint = this.paint

        if (data.lineColor > -1) {
            linePaint = TextPaint().apply {
                set(textPaint)
                color = ContextCompat.getColor(context, data.lineColor)
                strokeWidth = 3f
            }
        }

        textPaint.fontMetrics?.let {
            fTextTop = it.top
            fTextsBottom = it.bottom
            fLineHEight = fTextsBottom - fTextTop
            positionGap = fLineHEight - realFontHeight
        }
        realFontHeight = ((fLineHEight - (textPaint.getFontMetricsInt(null).toFloat()  + fTextsBottom)) * -1) * 2
        curPosition = position
    }
}