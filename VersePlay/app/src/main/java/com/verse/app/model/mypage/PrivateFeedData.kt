package com.verse.app.model.mypage

import androidx.paging.PagingData
import com.verse.app.model.base.BaseViewTypeModel
import com.verse.app.model.feed.FeedContentsData
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class PrivateFeedData(
     @Contextual
     var dataList: PagingData<FeedContentsData>? = null,
) : BaseViewTypeModel()