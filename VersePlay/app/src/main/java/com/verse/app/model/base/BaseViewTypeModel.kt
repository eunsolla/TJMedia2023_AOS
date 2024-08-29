package com.verse.app.model.base

import android.os.Parcelable
import com.verse.app.contants.ListPagedItemType
import kotlinx.serialization.Serializable

/**
 * Description : Base ViewType Model
 *
 * Created by jhlee on 2023-03-31
 */
@Serializable
abstract class BaseViewTypeModel(var itemViewType: ListPagedItemType = ListPagedItemType.NONE)
