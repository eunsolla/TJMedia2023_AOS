package com.verse.app.utility

import com.verse.app.model.weblink.WebLinkData
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import javax.inject.Inject

/**
 * Description :
 *
 * Created by juhongmin on 2023/05/18
 */
interface WebLinkProvider {
    fun getLinkMeta(linkUrl: String): Single<WebLinkData>
}

internal class WebLinkProviderImpl @Inject constructor(
) : WebLinkProvider {

    override fun getLinkMeta(linkUrl: String): Single<WebLinkData> {
        return Single.create { emitter ->
            try {
                val document = Jsoup.connect(linkUrl)
                    .timeout(10_000)
                    .get()
                val elements = document.select("meta[property^=og:]")

                var metaTitle: String? = null
                var metaImageUrl: String? = null
                var metaIconUrl: String? = null
                var fullUrl: String? = null
                // meta[og: 로 시작하는 것들
                elements.forEach { meta ->
                    when (meta.attr("property")) {
                        "og:url" -> {
                            fullUrl = meta.attr("content")
                        }

                        "og:title" -> {
                            metaTitle = meta.attr("content")
                        }

                        "og:image" -> {
                            metaImageUrl = meta.attr("content")
                        }
                    }
                }

                val faviconLinks = document.select("link[rel~=icon]")

                // 파비콘 URL 가져오기
                for (faviconLink: Element in faviconLinks) {
                    val faviconUrl: String = faviconLink.attr("href")
                    val faviconUrlSize: String = faviconLink.attr("sizes")

                    if (faviconUrl.startsWith("http") && faviconUrlSize.isNotEmpty()) {
                        metaIconUrl = faviconUrl
                    } else if (faviconLinks.size == 1) {
                        metaIconUrl = faviconUrl
                    }
                }

                val model = WebLinkData(linkUrl, metaTitle, metaImageUrl, metaIconUrl, fullUrl)
                emitter.onSuccess(model)

            } catch (ex: Exception) {
                emitter.onError(ex)
            }
        }.subscribeOn(Schedulers.io())
    }
}