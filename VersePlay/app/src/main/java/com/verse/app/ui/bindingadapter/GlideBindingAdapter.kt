package com.verse.app.ui.bindingadapter

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory
import com.verse.app.R
import com.verse.app.contants.Config
import com.verse.app.contants.GIFType
import com.verse.app.utility.DLogger
import jp.wasabeef.glide.transformations.BlurTransformation
import jp.wasabeef.glide.transformations.RoundedCornersTransformation


/**
 * Description : 공통 어댑터에서 잦은 소스 충돌로 인해 이미지 로더 전용 BindingAdapter 클래스 생성
 *
 * Created by juhongmin on 2023/06/20
 */
object GlideBindingAdapter {
    @JvmStatic
    @BindingAdapter(
        value = ["gifSrc"], requireAll = false
    )
    fun loadGif(
        view: ImageView,
        gifType: GIFType,
    ) {
        Glide.with(view.context)
            .asGif()
            .load(gifType.gifId)
            .into(view)
    }

    @JvmStatic
    @BindingAdapter(
        value = ["imageUrl", "placeholder", "blurRadius", "blurSampling", "requestManager"],
        requireAll = false
    )
    fun loadImage(
        view: ImageView,
        imgUrl: String?,
        placeHolder: Drawable?,
        blurRadius: Int?,
        blurSampling: Int?,
        requestManager: RequestManager?
    ) {

        val url = imgUrl ?: ""
        val reqManager = requestManager ?: Glide.with(view)

        // 앞에 값이 http or ContentsProvider 로 오는 경우 그대로 나머지 FILE_URL 붙여서 처리
        val fullUrl = getImagePath(url)

        val placeHolderDrawable = placeHolder ?: ContextCompat.getDrawable(view.context, R.drawable.ic_album_default)

        if (blurRadius != null && blurSampling != null) {
            val options = RequestOptions()
                .placeholder(placeHolderDrawable)
                .error(placeHolderDrawable)

            val thumbnail = reqManager
                .load(fullUrl)
                .placeholder(placeHolderDrawable)
                .error(placeHolderDrawable)
                .sizeMultiplier(0.25f)

            val blurTransform = RequestOptions.bitmapTransform(BlurTransformation(blurRadius, blurSampling))

            reqManager
                .setDefaultRequestOptions(options)
                .load(fullUrl)
                .placeholder(placeHolderDrawable)
                .thumbnail(thumbnail)
                .apply(blurTransform)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(view)

        } else {
            if (!fullUrl.isNullOrEmpty()) {
                reqManager
                    .load(fullUrl)
                    .placeholder(placeHolderDrawable)
                    .error(placeHolderDrawable)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(view)
            } else {
                reqManager
                    .load(placeHolderDrawable)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(view)
            }
        }
    }

    @JvmStatic
    @BindingAdapter(
        value = ["imageUrl", "topLeft", "topRight", "bottomLeft", "bottomRight", "requestManager"],
        requireAll = false
    )
    fun loadImage(
        view: ImageView,
        url: String?,
        topLeft: Int,
        topRight: Int,
        bottomLeft: Int,
        bottomRight: Int,
        requestManager: RequestManager?
    ) {
        if (url.isNullOrEmpty()) return

        // 앞에 값이 http or ContentsProvider 로 오는 경우 그대로 나머지 FILE_URL 붙여서 처리
        val fullUrl = getImagePath(url)

        val multiLeftTopCorner: MultiTransformation<Bitmap> = MultiTransformation(
            RoundedCornersTransformation(
                topRight,
                0,
                RoundedCornersTransformation.CornerType.TOP_RIGHT
            ),
            RoundedCornersTransformation(
                topLeft,
                0,
                RoundedCornersTransformation.CornerType.TOP_LEFT
            ),
            RoundedCornersTransformation(
                bottomRight,
                0,
                RoundedCornersTransformation.CornerType.BOTTOM_RIGHT
            ),
            RoundedCornersTransformation(
                bottomLeft,
                0,
                RoundedCornersTransformation.CornerType.BOTTOM_LEFT
            ),
        )

        val reqManager = requestManager ?: Glide.with(view)

        if (!fullUrl.isNullOrEmpty()) {
            reqManager
                .load(fullUrl)
                .placeholder(R.drawable.ic_album_default)
                .error(R.drawable.ic_album_default)
                .apply(RequestOptions.bitmapTransform(multiLeftTopCorner))
                .into(view)
        } else {
            reqManager
                .load(R.drawable.ic_album_default)
                .placeholder(R.drawable.ic_album_default)
                .error(R.drawable.ic_album_default)
                .apply(RequestOptions.bitmapTransform(multiLeftTopCorner))
                .into(view)
        }
    }

    @JvmStatic
    @BindingAdapter(value = ["albumUrl", "placeholder", "requestManager"], requireAll = false)
    fun loadAlbumImage(
        view: ImageView,
        url: String,
        placeholder: Drawable,
        requestManager: RequestManager?
    ) {

        val reqManager = requestManager ?: Glide.with(view)

        // 앞에 값이 http or ContentsProvider 로 오는 경우 그대로 나머지 FILE_URL 붙여서 처리
        val fullUrl = getImagePath(url)

        if (fullUrl.isNotEmpty()) {
            reqManager
                .load(fullUrl)
                .placeholder(placeholder)
                .error(placeholder)
                .into(view)
        } else {
            reqManager
                .load(placeholder)
                .placeholder(placeholder)
                .error(placeholder)
                .into(view)
        }
    }

    // 앞에 값이 http or ContentsProvider 로 오는 경우 그대로 나머지 FILE_URL 붙여서 처리
    private fun getImagePath(url: String): String {
        return if (url.startsWith("http") ||
            url.startsWith("content://") ||
            url.startsWith("/data")
        ) {
            url
        } else {
            if (url.isNotEmpty() && url != "NO-DATA") {
                "${Config.BASE_FILE_URL}$url"
            } else {
                ""
            }
        }
    }
}
