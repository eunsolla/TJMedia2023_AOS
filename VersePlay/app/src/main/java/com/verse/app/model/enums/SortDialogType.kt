package com.verse.app.model.enums

import com.verse.app.R
import com.verse.app.ui.dialogfragment.SortDialogFragment

/**
 * Description : 검색 Dialog Type EnumClass
 * @see SortDialogFragment
 * Created by juhongmin on 2023/05/12
 */
enum class SortDialogType(val list: List<Pair<Int, SortEnum>>) {
    DEFAULT(
        listOf(
            R.string.search_filter_order_by_latest to SortEnum.DESC,
            R.string.search_filter_order_by_oldest to SortEnum.ASC
        )
    ),
    USER_SEARCH(
        listOf(
            R.string.search_filter_order_by_contents to SortEnum.USER_CONTENTS,
            R.string.search_filter_order_by_official_partner to SortEnum.USER_PARTNER,
            R.string.search_filter_order_by_follower to SortEnum.USER_FOLLOWER,
        )
    )
}
