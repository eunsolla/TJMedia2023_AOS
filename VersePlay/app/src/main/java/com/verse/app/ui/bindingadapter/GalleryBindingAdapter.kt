package com.verse.app.ui.bindingadapter

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory
import com.verse.app.R

/**
 * Description : 갤러리 전용 BindingAdapter
 *
 * Created by juhongmin on 2023/05/13
 */
object GalleryBindingAdapter {

    private val crossFadeFactory: DrawableCrossFadeFactory by lazy {
        DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build()
    }

    private val crossFadeTransition: DrawableTransitionOptions by lazy {
        DrawableTransitionOptions.withCrossFade(crossFadeFactory)
    }

    private val gray4PlaceHolder: ColorDrawable by lazy { ColorDrawable(Color.rgb(229, 229, 229)) }

    @JvmStatic
    @BindingAdapter(value = ["requestManager", "localUrl", "overrideSize"], requireAll = false)
    fun setLoadLocalImageUrl(
        imgView: AppCompatImageView,
        requestManager: RequestManager?,
        localUrl: String?,
        resize: Int?
    ) {
        if (requestManager == null || localUrl.isNullOrEmpty()) return

        requestManager.load(localUrl)
            .diskCacheStrategy(DiskCacheStrategy.RESOURCE) // 해상도를 줄인 이미지만 캐싱
            .override(resize ?: -1)
            .placeholder(gray4PlaceHolder)
            .transition(crossFadeTransition)
            .into(imgView)
    }
}
