package com.verse.app.widget.views


import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.util.TypedValue
import android.widget.LinearLayout
import androidx.annotation.ColorInt
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.verse.app.R

/**
 * Description : CustomLinearLayout Class
 *
 * Created by jhlee on 2023-04-27
 */
class CustomLinearLayout @JvmOverloads constructor(
    ctx: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : LinearLayout(ctx, attrs, defStyleAttr) {

    private var enableDrawable: GradientDrawable? = null
    private var disableDrawable: GradientDrawable? = null
    private var isClicked: Boolean = true

    init {

        context.obtainStyledAttributes(attrs, R.styleable.CustomLinearLayout).run {
            try {

                val defState = getBoolean(R.styleable.CustomLinearLayout_linearLayoutDefState, true)
                val corner = getDimension(R.styleable.CustomLinearLayout_linearLayoutCorner, -1F)
                var color = getColor(R.styleable.CustomLinearLayout_linearLayoutBgColor, Color.WHITE)
                var strokeWidth = getDimensionPixelSize(R.styleable.CustomLinearLayout_linearLayoutBorder, NO_ID)
                var strokeColor = getColor(R.styleable.CustomLinearLayout_linearLayoutBorderColor, NO_ID)

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

                color = getColor(R.styleable.CustomLinearLayout_linearLayoutDisableBgColor, Color.WHITE)
                strokeWidth = getDimensionPixelSize(R.styleable.CustomLinearLayout_linearLayoutDisableBorder, NO_ID)
                strokeColor = getColor(R.styleable.CustomLinearLayout_linearLayoutDisableBorderColor, NO_ID)

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

                with(TypedValue()) {
                    ctx.theme.resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, this, true)
                    foreground = ContextCompat.getDrawable(ctx, this.resourceId)
                }

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