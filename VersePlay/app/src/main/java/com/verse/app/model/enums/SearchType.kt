package com.verse.app.model.enums

/**
 * Description :
 *
 * Created by juhongmin on 2023/05/11
 */
enum class SearchType(val query: String) {
    POPULAR("P"),
    VIDEO("V"),
    MR("S"),
    TAG("T"),
    USER("U")
}