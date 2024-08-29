package com.verse.app.base.model

/**
 * Description : 페이징 처리 관련 데이터 모델
 * @param isLoading 로드 API 중인지 유무 (API 로드중: true, API 로드가 끝남: false)
 * @param isLast 더이상 페이징처리를 할수 없는 상태인지 유무 Flag (마지막 페이지: true, 로드할 페이지가 더 있음: false)
 *
 * Created by juhongmin on 2023/05/11
 */
data class PagingModel(
    var isLoading: Boolean = true,
    var isLast: Boolean = false
) {

    /**
     * 초기에 API 호출 방지를 위한 파라미터 초기화 함수
     */
    fun initParams() {
        isLoading = true
        isLast = false
    }
}