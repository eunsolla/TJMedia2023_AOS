package com.verse.app.model.sort

import com.verse.app.model.enums.SortEnum

/**
 * Description : 정렬 관련 데이터 모델
 *
 * Created by juhongmin on 2023/05/12
 */
data class SortCacheEntity(
    var defSortTxt: String = "", // Optional
    var defSort: SortEnum = SortEnum.DESC,
    var selectSortTxt: String = "",
    var selectSort: SortEnum = SortEnum.DESC
) {
    /**
     * 선태한 정렬 값 셋팅 처리하는 함수
     * @param txt Sort Resource String
     * @param enum SortEnum
     */
    fun setSelectSort(txt: String, enum: SortEnum) {
        selectSortTxt = txt
        selectSort = enum
    }

    /**
     * 기본 정렬값 셋팅 처리하는 함수
     * @param txt Sort Resource String
     * @param enum SortEnum
     */
    fun setDefSelectSort(txt: String, enum: SortEnum) {
        defSortTxt = txt
        defSort = enum
    }

    /**
     * 정렬 상태가 변경 되었는지 Boolean 으로 리턴하는 함수
     */
    fun isSortChange(): Boolean {
        return defSort != selectSort
    }

    /**
     * 정렬 초기화 처리
     */
    fun clearSelected() {
        selectSortTxt = defSortTxt
        selectSort = defSort
    }
}
