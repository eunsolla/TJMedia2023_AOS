package com.verse.app.widget.views

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.util.TypedValue
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.StyleRes
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.widget.TextViewCompat
import com.verse.app.R


/**
 * Description : CustomTextView View Class
 * Created by jhlee on 2023-01-01
 */
class CustomTextView @JvmOverloads constructor(
    ctx: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : AppCompatTextView(ctx, attrs, defStyleAttr) {

    data class Item(
        @ColorInt var txtColor: Int? = null,
        var drawable: GradientDrawable? = null,
        @StyleRes var textStyle: Int = NO_ID,
    ) {
        var corner: Float = -1F
            set(value) {
                if (value != -1F) {
                    drawable?.cornerRadius = value
                }
                field = value
            }

        fun setStroke(@Dimension width: Int, @ColorInt color: Int) {
            if (width != -1 && color != NO_ID) {
                drawable?.setStroke(width, color)
            }
        }
    }

    private val enableItem = Item()
    private val disableItem = Item()
    private var isClicked: Boolean = true

    init {

        context.obtainStyledAttributes(attrs, R.styleable.CustomTextView).run {

            try {

                includeFontPadding = false

                isClicked = getBoolean(R.styleable.CustomTextView_textViewDefState, true)

                var txtColor: Int?

                txtColor = if (hasValue(R.styleable.CustomTextView_textViewTxtColor)) {
                    getColor(R.styleable.CustomTextView_textViewTxtColor, Color.BLACK)
                } else {
                    null
                }

                var bgColor = getColor(R.styleable.CustomTextView_textViewBgColor, Color.WHITE)
                var strokeWidth = getDimensionPixelSize(R.styleable.CustomTextView_textViewBorder, -1)
                var corner = getDimension(R.styleable.CustomTextView_textViewCorner, -1F)
                var strokeColor = getColor(R.styleable.CustomTextView_textViewBorderColor, NO_ID)
                var textStyle = getResourceId(R.styleable.CustomTextView_textViewTextStyle, NO_ID)
                var clickEffect = getBoolean(R.styleable.CustomTextView_textViewClickEffect, true)

                // TextStyle 를 우선순위로
                if (textStyle != NO_ID) {
                    txtColor = null
                }

                enableItem.apply {
                    this.txtColor = txtColor
                    this.drawable = GradientDrawable(
                        GradientDrawable.Orientation.BL_TR,
                        intArrayOf(bgColor, bgColor)
                    )
                    this.corner = corner
                    this.setStroke(strokeWidth, strokeColor)
                    this.textStyle = textStyle
                }

                txtColor = if (hasValue(R.styleable.CustomTextView_textViewDisableTxtColor)) {
                    getColor(R.styleable.CustomTextView_textViewDisableTxtColor, Color.BLACK)
                } else {
                    null
                }

                bgColor = getColor(R.styleable.CustomTextView_textViewDisableBgColor, Color.WHITE)
                strokeWidth = getDimensionPixelSize(R.styleable.CustomTextView_textViewDisableBorder, -1)
                strokeColor = getColor(R.styleable.CustomTextView_textViewDisableBorderColor, NO_ID)
                textStyle = getResourceId(R.styleable.CustomTextView_textViewDisableTextStyle, NO_ID)
                corner = getDimension(R.styleable.CustomTextView_textViewDisableCorner, -1F)

                // TextStyle 를 우선순위로
                if (textStyle != NO_ID) {
                    txtColor = null
                }

                disableItem.apply {
                    this.txtColor = txtColor
                    this.drawable = GradientDrawable(
                        GradientDrawable.Orientation.BL_TR,
                        intArrayOf(bgColor, bgColor)
                    )
                    this.corner = corner
                    this.setStroke(strokeWidth, strokeColor)
                    this.textStyle = textStyle
                }

//                if(clickEffect){
//                    with(TypedValue()) {
//                        ctx.theme.resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, this, true)
//                        foreground = ContextCompat.getDrawable(ctx, this.resourceId)
//                    }
//                }

            } catch (_: Exception) {
            } finally {
                recycle()
            }
        }

        // Default State
        performInvalidate()
    }

    /**
     * 텍스트 Background And TextStyle UI Update
     */
    private fun performInvalidate() {
        setCustomBackground(isClicked)
        setCustomTextStyle(isClicked)
    }

    /**
     * set Background
     * @param isEnable 활 / 비활성화
     */
    private fun setCustomBackground(isEnable: Boolean) {
        if (isEnable) {
            if (enableItem.drawable != null) {
                background = enableItem.drawable
            }
        } else {
            if (disableItem.drawable != null) {
                background = disableItem.drawable
            }
        }
    }

    /**
     * set TextStyle
     * @param isEnable 활 / 비활성화
     */
    private fun setCustomTextStyle(isEnable: Boolean) {
        if (isEnable) {
            // 텍스트 스타일이 정해져 있는 경우 -> TextColor 무시
            if (enableItem.textStyle != NO_ID) {
                TextViewCompat.setTextAppearance(this, enableItem.textStyle)
            } else if (enableItem.txtColor != null) {
                setTextColor(enableItem.txtColor!!)
            } else {
                // 기본값 블랙
                setTextColor(Color.BLACK)
            }
        } else {
            // 텍스트 스타일이 정해져 있는 경우 -> TextColor 무시
            if (disableItem.textStyle != NO_ID) {
                TextViewCompat.setTextAppearance(this, disableItem.textStyle)
            } else if (disableItem.txtColor != null) {
                setTextColor(disableItem.txtColor!!)
            } else {
                // 기본값 블랙
                setTextColor(Color.BLACK)
            }
        }
    }

    override fun setSelected(selected: Boolean) {
        isClicked = selected
        performInvalidate()
        super.setSelected(selected)
    }

    override fun setEnabled(enabled: Boolean) {
        isClicked = enabled
        performInvalidate()
        super.setEnabled(enabled)
    }

    /**
     * setEnable Drawable Code Type
     *
     * @param color Bg Color
     * @param strokeWidth Border Size
     * @param strokeColor Border Color
     *
     */
    fun setEnableDrawable(
        @ColorInt color: Int,
        corner: Float,
        strokeWidth: Int,
        @ColorInt strokeColor: Int,
    ) {
        enableItem.drawable = null
        enableItem.drawable = GradientDrawable(GradientDrawable.Orientation.BL_TR, intArrayOf(color, color)).apply {
            if (corner != -1F) {
                cornerRadius = corner
            }
            if (strokeWidth != NO_ID && strokeColor != NO_ID) {
                setStroke(strokeWidth, strokeColor)
            }
        }
        performInvalidate()
    }

    /**
     * setEnable Text Color Code Type
     * @param color TextColor
     */
    fun setEnableTxtColor(@ColorInt color: Int) {
        enableItem.txtColor = color
        performInvalidate()
    }

    /**
     * setEnable Text Color Code Type
     * @param color TextColor
     */
    fun setEnableTxtAndBgColor(@ColorInt color: Int) {
        enableItem.txtColor = color
        setEnableDrawable(color, enableItem.corner, 0, -1)
        performInvalidate()
    }

    /**
     * setDisable Drawable Code Type
     *
     * @param color Bg Color
     * @param strokeWidth Border Size
     * @param strokeColor Border Color
     *
     */
    fun setDisableDrawable(
        @ColorInt color: Int,
        corner: Float,
        strokeWidth: Int,
        @ColorInt strokeColor: Int,
    ) {
        disableItem.drawable = null
        disableItem.drawable =
            GradientDrawable(GradientDrawable.Orientation.BL_TR, intArrayOf(color, color)).apply {
                if (corner != -1F) {
                    cornerRadius = corner
                }
                if (strokeWidth != NO_ID && strokeColor != NO_ID) {
                    setStroke(strokeWidth, strokeColor)
                }
            }
        performInvalidate()
    }

    /**
     * setDisable Text Color Code Type
     * @param color TextColor
     */
    fun setDisableTxtColor(@ColorInt color: Int) {
        disableItem.txtColor = color
        performInvalidate()
    }

    private val Float.toSize: Float
        get() = this / Resources.getSystem().displayMetrics.density
}