package com.verse.app.model.base

import com.verse.app.contants.ListPagedItemType
import kotlinx.serialization.Serializable

/**
 * Description : Base Model
 *
 * Created by jhlee on 2023-01-01
 */
@Serializable
abstract class BaseModel() {
    var itemViewType: ListPagedItemType = ListPagedItemType.NONE
    abstract fun getViewType(): ListPagedItemType
    abstract fun getClassName(): String
    abstract fun areItemsTheSame(diffUtil: Any): Boolean
    abstract fun areContentsTheSame(diffUtil: Any): Boolean
}
