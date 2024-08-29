package com.verse.app.gallery.core

/**
 * Description : 갤러리에서 추출한 이미지 타입 EnumClass
 * @see <a href="https://github.com/sieunju/gallery">참고</a>
 * Created by juhongmin on 2023/05/12
 */
enum class ImageType {
    UN_KNOWN, // 이미지 타입 체크하던중 에러 발생시
    CAMERA, // 카메라로 찍은 이미지
    SCREENSHOT, // 스크린샷
    ETC // 외부에서 다운로드 하거나, 다른 앱에서 다운로드 받은 경우
}