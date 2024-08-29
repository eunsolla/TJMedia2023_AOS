package com.verse.app.widget.views


import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.util.TypedValue
import androidx.annotation.ColorInt
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.verse.app.R

/**
 * Description : CustomConstraintLayout Class
 *
 * Created by jhlee on 2023-01-01
 */
class CustomConstraintLayout @JvmOverloads constructor(
    ctx: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ConstraintLayout(ctx, attrs, defStyleAttr) {


    private var enableDrawable: GradientDrawable? = null
    private var disableDrawable: GradientDrawable? = null
    private var isClicked: Boolean = true

    init {

        context.obtainStyledAttributes(attrs, R.styleable.CustomConstraintLayout).run {
            try {

                val defState = getBoolean(R.styleable.CustomConstraintLayout_layoutDefState, true)
                val corner = getDimension(R.styleable.CustomConstraintLayout_layoutCorner, -1F)
                var color = getColor(R.styleable.CustomConstraintLayout_layoutBgColor, Color.WHITE)
                var strokeWidth = getDimensionPixelSize(R.styleable.CustomConstraintLayout_layoutBorder, NO_ID)
                var strokeColor = getColor(R.styleable.CustomConstraintLayout_layoutBorderColor, NO_ID)
                var clickEffect = getBoolean(R.styleable.CustomConstraintLayout_layoutClickEffect, true)

                enableDrawable = GradientDrawable(
                    GradientDrawable.Orientation.BL_TR,
                    intArrayOf(color, color)
                ).apply {
                    if (corner != -1F) {
                        cornerRadius = corner
                    }
                    if (strokeWidth != NO_ID && strokeColor != NO_ID) {
                        setStroke(strokeWidth, strokeColor)
                    }
                }

                color = getColor(R.styleable.CustomConstraintLayout_layoutDisableBgColor, Color.WHITE)
                strokeWidth = getDimensionPixelSize(R.styleable.CustomConstraintLayout_layoutDisableBorder, NO_ID)
                strokeColor = getColor(R.styleable.CustomConstraintLayout_layoutDisableBorderColor, NO_ID)

                disableDrawable = GradientDrawable(
                    GradientDrawable.Orientation.BL_TR,
                    intArrayOf(color, color)
                ).apply {
                    if (corner != -1F) {
                        cornerRadius = corner
                    }
                    if (strokeWidth != NO_ID && strokeColor != NO_ID) {
                        setStroke(strokeWidth, strokeColor)
                    }
                }

                // Default Setting
                background = if (defState) {
                    enableDrawable
                } else {
                    disableDrawable
                }

//                if(clickEffect){
//                    with(TypedValue()) {
//                        ctx.theme.resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, this, true)
//                        foreground = ContextCompat.getDrawable(ctx, this.resourceId)
//                    }
//                }

            } catch (_: Exception) {
            }

            recycle()
        }

        clipToOutline = true
    }

    override fun setSelected(selected: Boolean) {
        isClicked = selected
        background = if (selected) {
            enableDrawable
        } else {
            disableDrawable
        }
        super.setSelected(selected)
    }

    override fun setEnabled(enabled: Boolean) {
        isClicked = enabled
        background = if (enabled) {
            enableDrawable
        } else {
            disableDrawable
        }
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
        if (enableDrawable != null) {
            enableDrawable = null
        }
        enableDrawable =
            GradientDrawable(GradientDrawable.Orientation.BL_TR, intArrayOf(color, color)).apply {
                if (corner != -1F) {
                    cornerRadius = corner
                }
                if (strokeWidth != NO_ID && strokeColor != NO_ID) {
                    setStroke(strokeWidth, strokeColor)
                }
            }
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
        if (disableDrawable != null) {
            disableDrawable = null
        }
        disableDrawable =
            GradientDrawable(GradientDrawable.Orientation.BL_TR, intArrayOf(color, color)).apply {
                if (corner != -1F) {
                    cornerRadius = corner
                }
                if (strokeWidth != NO_ID && strokeColor != NO_ID) {
                    setStroke(strokeWidth, strokeColor)
                }
            }
    }

}