package com.verse.app.model.enums

/**
 * Description : 검색 QueryParameter -> Sort
 *
 * Created by juhongmin on 2023/05/10
 */
enum class SortEnum(val query: String) {
    ASC("ASC"), // 오래된 순
    DESC("DESC"), // 최신순
    USER_CONTENTS("C"),
    USER_PARTNER("P"),
    USER_FOLLOWER("F")
}