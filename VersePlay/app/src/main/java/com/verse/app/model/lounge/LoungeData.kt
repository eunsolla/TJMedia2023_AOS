package com.verse.app.model.lounge

/**
 * Description : PostLoungeUseCase 전용 데이터 모델
 *
 * Created by juhongmin on 2023/05/20
 */
data class LoungeData(
    var code: String? = null,
    val contents: String = "",
    val imageList: List<LoungeGalleryData> = listOf(),
    val linkUri: String? = null
)