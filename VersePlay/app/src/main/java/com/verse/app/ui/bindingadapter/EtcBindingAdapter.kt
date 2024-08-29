package com.verse.app.ui.bindingadapter

import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.databinding.BindingAdapter
import com.google.android.material.appbar.AppBarLayout


/**
 * Description : 공통 BindingAdapter
 *
 * Created by jhlee on 2023-01-13
 */
object EtcBindingAdapter {

    /**
     * AppBar BottomLine 보이는 이슈 처리
     */
    @JvmStatic
    @BindingAdapter("disableBottomLine")
    fun AppBarLayout.setDisableBottomLine(
        ignore: Boolean
    ) {
        elevation = 0F
        outlineProvider = null
    }

    /**
     * View 회전 애니메이션 BindingAdapter 처리 함수
     */
    @JvmStatic
    @BindingAdapter(value = ["aniRotation", "duration"], requireAll = false)
    fun setAniRotate(
        v: View,
        targetRotation: Float?,
        targetDuration: Long?
    ) {
        if (targetRotation == null) return

        ObjectAnimator.ofFloat(
            v, View.ROTATION,
            v.rotation,
            targetRotation
        ).apply {
            interpolator = AccelerateDecelerateInterpolator()
            duration = targetDuration ?: 200
            start()
        }
    }
}