package com.verse.app.contants

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.verse.app.R
import com.verse.app.model.mypage.GetSettingInfoData
import com.verse.app.model.user.UserData
import com.verse.app.utility.RxBus
import com.verse.app.utility.RxBusEvent


/**
 * Description : 앱  기본 설정
 *
 * Created by jhlee on 2023-01-01
 */
object Config {
    private const val PROFILE_DEV = "PROFILE_DEV"
    private const val PROFILE_STG = "PROFILE_STG"
    private const val PROFILE_PRD = "PROFILE_PRD"

    // [S]버전 생성 시 조정 상수값
    const val IS_DEBUG = true // 개발모드 여부
    const val IS_SSL = true // SSL 적용 여부
    private const val ACTIVE_PROFILE = PROFILE_PRD // 서버 인프라 환경
    // [E]버전 생성 시 조정 상수값

    private const val VERSION_CODE = "v2"
    private const val VERSION = VERSION_CODE
    const val API_VERSION = "/api/$VERSION/"

    const val DYNAMIC_LINK_PREFIX = "https://verseplay.page.link"         // 다이나믹 링크
    const val DYNAMIC_DOMAIN_PREFIX = "https://verseplay.com"         // 다이나믹 딥링크
    const val GOOGLE_PLAY_STORE_VERSE_PLAY_URL = "https://play.google.com/store/apps/details?id=com.verse.app"
    const val OPEN_SOURCE_LICENSE_PATH = "file:///android_asset/licenses/OpenSourceLicense.html" //오픈 소스 라이선스

    // 개발
    private const val DEV_API = "https://dev-api.verseplay.com${API_VERSION}"
    private const val DEV_WEB = "https://dev-web.verseplay.com"
    private const val DEV_API_IP = "http://20.221.202.148:8080${API_VERSION}"
    private const val DEV_WEB_IP = "http://20.221.202.148:8080"
    private const val DEV_CHAT = "dev-chat.verseplay.com"

    // 스테이징
    private const val STG_API = "https://stg-api.verseplay.com${API_VERSION}"
    private const val STG_WEB = "https://stg.verseplay.com"
    private const val STG_API_IP = "http://20.221.228.171:801${API_VERSION}"
    private const val STG_WEB_IP = "http://20.221.228.171:80"
    private const val STG_CHAT = "stg-chat.verseplay.com"

    // 운영
    private const val PRD_API = "https://prd-api.verseplay.com${API_VERSION}"
    private const val PRD_WEB = "https://prd.verseplay.com"
    private const val PRD_API_IP = "http://52.159.85.9:801${API_VERSION}"
    private const val PRD_WEB_IP = "http://52.159.85.9:80"
    private const val PRD_CHAT = "prd-chat.verseplay.com"

    // AZURE CDN 정보
    private const val DEV_AZUREEDGE_HOST = "https://tjus-dev.azureedge.net"      // 개발
    private const val STG_AZUREEDGE_HOST = "https://tjus-stg.azureedge.net"         // 스테이지
    private const val PRD_AZUREEDGE_HOST = "https://tjus-prd.azureedge.net"     // 운영

    val BASE_API_URL =
        when (ACTIVE_PROFILE) {
            PROFILE_DEV -> if (IS_SSL) DEV_API else DEV_API_IP
            PROFILE_STG -> if (IS_SSL) STG_API else STG_API_IP
            PROFILE_PRD -> if (IS_SSL) PRD_API else PRD_API_IP
            else -> if (IS_SSL) DEV_API else DEV_API_IP
        }

    val BASE_WEB_URL =
        when (ACTIVE_PROFILE) {
            PROFILE_DEV -> if (IS_SSL) DEV_WEB else DEV_WEB_IP
            PROFILE_STG -> if (IS_SSL) STG_WEB else STG_WEB_IP
            PROFILE_PRD -> if (IS_SSL) PRD_WEB else PRD_WEB_IP
            else -> if (IS_SSL) DEV_WEB else DEV_WEB_IP
        }

    val BASE_CHAT_URL =
        when (ACTIVE_PROFILE) {
            PROFILE_DEV -> DEV_CHAT
            PROFILE_STG -> STG_CHAT
            PROFILE_PRD -> PRD_CHAT
            else -> DEV_CHAT
        }

    val BASE_CHAT_PORT =
        when (ACTIVE_PROFILE) {
            PROFILE_DEV -> 7001
            PROFILE_STG -> 802
            PROFILE_PRD -> 802
            else -> 7001
        }

    val BASE_FILE_URL =
        when (ACTIVE_PROFILE) {
            PROFILE_DEV -> DEV_AZUREEDGE_HOST
            PROFILE_STG -> STG_AZUREEDGE_HOST
            PROFILE_PRD -> PRD_AZUREEDGE_HOST
            else -> DEV_AZUREEDGE_HOST
        }
}

object AppData {

    const val OS = "AOS"
    const val TCP_OS = "ANDROID"
    const val Y_VALUE = "Y"                                 //y value
    const val N_VALUE = "N"                                 //n value
    const val POPUP_WARNING = "warning"             // 팝업 경고 아이콘
    const val POPUP_HELP = "help"                        // 팝업 물음표 아이콘
    const val POPUP_COMPLETE = "complete"         // 팝업 확인 아이콘
    const val POPUP_CHANGE_PROFILE = "changeProfile"       // 팝업 확인 아이콘
    const val POPUP_BLOCK = "block"       // 팝업 차단 아이콘
    const val POPUP_UNINTEREST = "uninterest"       // 팝업 관심없음 아이콘
    const val PREFIX_TJ_SOUND = "/tj-sound/"   // url prefix tj-sound
    const val PREFIX_PROFILE = "/profile/"         // url prefix profile
    const val PREFIX_FEED = "/feed/"         // url prefix profile
    const val CHAT_RES = "/chat-res/"


    var isOnApp: Boolean = false
    var IS_MYPAGE_EDIT: Boolean = false
    var IS_SING_ING: Boolean = false
    var IS_ENCODE_ING: Boolean = false      //인코딩중 여부
    var IS_REUPLOAD_ING: Boolean = false      //재업로드 팝업 노출 여부
}

object Encoded {
    val SONG_ENCODE_START_SERVICE = "song_encode_start_service" // 노래 인코딩 서비스 start action
}


/**
 * ActivityType
 */
enum class MainEntryType {
    VERSION, //인트로
    PERMISSION, //퍼미션
    APP_MAIN, //메인
    SETTING, // 앱설치 후 국가.언어 세팅
}

/**
 * 메인 PagerType
 */
enum class MainStructureType {
    MAIN_FEED, // Base Page (Feed/Recommend)
    USER_FEED // Right Page (UserDetail)
}

/**
 * LoadingDialog
 */
enum class LoadingDialogState {
    NONE,
    SHOW,
    DISMISS
}

/**
 * Server Check Dialog
 */
enum class ServerCheckState {
    NONE,
    SHOW,
    DISMISS
}


/**
 * Intent Extra Code 값 Example
 */
object ExtraCode {
    const val FRAGMENT_RESULT = "FRAGMENT_RESULT"
    const val FRAGMENT_RESULT_CALL_BACK = "FRAGMENT_RESULT_CALL_BACK"                                                 // 메인 프레그먼트 종료 결과 값 키
    const val FRAGMENT_RESULT_DETAIL_CALL_BACK = "FRAGMENT_RESULT_DETAIL_CALL_BACK"                                                 // 메인 프레그먼트 종료 결과 값 키
    const val TAB_TYPE =
        "t"                                                                              // 마이페이지 탭 구분 키
    const val SUB_TAB_TYPE =
        "stt"                                                                     // 마이페이지 하위 탭 구분 키
    const val SUB_TAB_POSITION =
        "stp"                                                              // 마이페이지 하위 탭 포지션 구분 키
    const val SONG_MAIN_ITEM =
        "s"                                                                    // 노래 콘텐츠 업로드 -> 인기곡/신곡 키
    const val SONG_MORE_TYPE =
        "m"                                                                   // 노래 콘텐츠 업로드 -> 더보기(최근불렀던노래 or 인기곡 or 신곡) 구분 키
    const val SONG_INFO =
        "si"                                                                            // 노래 object
    const val SING_TYPE =
        "st"                                                                            // 솔로,듀엣,배틀 구분값
    const val MYPAGE_SETTING_CODE =
        "ms"                                                          // 마이페이지 세팅
    const val MYPAGE_PRIVATE_BOX =
        "pb"                                                             // 마이페이지 비공개 보관함
    const val REPORT_CODE =
        "rp"                                                                          // 솔로,듀엣,배틀 구분값
    const val COMMENT_TYPE =
        "CC"                                                                      // 코멘트 mng 값 콘텐츠 유형(피드 /커뮤니티라운지 /커뮤니티투표)
    const val SEARCH_INFO =
        "sf"                                                                          // 검색
    const val SEARCH_POPULAR_INFO =
        "sf"                                                             // 검색
    const val SING_STAR_POINT =
        "SING_STAR_POINT"                                              // 부르기 별점
    const val TEMP_NICK_NAME =
        "TEMP_NICK_NAME"                                                // 최초 회원 가입 시 사용 닉네임
    const val TEMP_EMAIL =
        "TEMP_EMAIL"                                                              // 최초 회원 가입 시 사용 이메일
    const val FOLLOWING =
        "f"                                                                               // 팔로잉,팔로우
    const val SING_PASS_USER_INFO =
        "SING_PASS_USER_INFO"                                // 씽패스 유저 회원코드
    const val SING_PASS_GENRE_INFO =
        "SING_PASS_GENRE_INFO"                             // 씽패스 유저 장르코드
    const val SING_PASS_SEASON_INFO = "SING_PASS_SEASON_INFO"                        // 씽패스 유저 시즌코드
    const val SING_ENCODE_ITEM =
        "SING_ENCODE"                                                 // 인코딩 object
    const val ALBUM_SELECTED_ITEM =
        "ALBUM_SELECTED_ITEM"                               // 갤러리 선택된 이미지
    const val UPLOAD_PAGE_TYPE =
        "UPLOAD_PAGE_TYPE"                                         // 컨텐츠 업로드 페이지 타입 구분값
    const val SEARCH_KEYWORD = "SEARCH_KEYWORD"
    const val SEARCH_RESULT_POS = "SEARCH_RESULT_POS"
    const val COLLECTION_TYPE =
        "COLLECTION_TYPE"                                             // 모아보기 요청 타입(F:피드 / T:태그)
    const val COLLECTION_PARAM =
        "COLLECTION_PARAM"                                        // 모아보기 요청 인자(반주음관리코드 OR 태그명)
    const val COLLECTION_FEED_PARAM =
        "COLLECTION_FEED_PARAM"                        // 모아보기 요청 인자(반주음관리코드 OR 태그명)
    const val FEED_MNG_CD =
        "FEED_MNG_CD"                                                         // 피드관리코드
    const val SINGING_SING_DATA = "SINGING_SING_DATA"                      // 부르기 타입 SOLO(PA001), DUET(PA002) ,BATTLE(PA004)
    const val SING_INTENT_MODEL = "SING_INTENT_MODEL"                      // 부르기 타입 SOLO(PA001), DUET(PA002) ,BATTLE(PA004)
    const val SINGING_SING_TYPE_CODE = "SINGING_SING_TYPE_CODE"                      // 부르기 타입 SOLO(PA001), DUET(PA002) ,BATTLE(PA004)
    const val SINGING_SONG_MNG_CD = "SINGING_SONG_MNG_CD"                             // 서비스음원관리코드
    const val SINGING_FEED_MNG_CD = "SINGING_FEED_MNG_CD"                               // 피드관리코드
    const val SINGING_FEED_MDTP_CD = "SINGING_FEED_MDTP_CD"                            // 듀엣 녹화/녹음
    const val FEED_DETAIL_TYPE =
        "FEED_DETAIL_TYPE"                                           // 피드 상세 유형
    const val FEED_DETAIL_MAIN_PARAM =
        "FEED_DETAIL_MAIN_PARAM"                      // 피드 상세 메인 파라미터
    const val FEED_DETAIL_SUB_PARAM =
        "FEED_DETAIL_SUB_PARAM"                         // 피드 상세 서브 파라미터
    const val FEED_DETAIL_SORT_PARAM = "FEED_DETAIL_SORT_PARAM"                      // 피드 상세 정렬 유형
    const val FEED_DETAIL_ITEM_INDEX =
        "FEED_DETAIL_ITEM_INDEX"                        // 피드 상세 아이템 위치
    const val PUSH_MSG_TITLE =
        "TITLE"                                                               // 푸쉬 메시지 제목
    const val PUSH_MSG_MESSAGE =
        "DESCRIPTION"                                                // 푸쉬 메시지 내용
    const val PUSH_MSG_LINK_TYPE =
        "LINK_CD"                                                     // 푸쉬 메시지 이동 처리 유형
    const val PUSH_MSG_LINK_DATA =
        "LINK_DATA"                                                  // 푸쉬 메시지 이동 처리 코드
    const val PUSH_MSG_ATT_IMAGE =
        "ATT_IMAGE_PATH"                                        // 푸쉬 메시지 첨부 이미지
    const val PUSH_MSG_SHOW_TYPE =
        "SHOW_TYPE"                                              // 푸쉬 메시지 노출 유형
    const val WRITE_LOUNGE_DATA = "WRITE_LOUNGE_DATA"
    const val USER_MEMCD =
        "USER_MEMCD"                                                             // 유저 마이페이지 진입 시 필요한 memcd
    const val MYPAGE_VIDEO_PLAY_USER =
        "MYPAGE_VIDEO_PLAY_USER"                      // 유저 마이페이지 진입 시 필요한 컨텐츠 정보
    const val MYPAGE_ENTER_TYPE =
        "MYPAGE_ENTER_TYPE"                                      // 유저 마이페이지 진입 시 필요한 컨텐츠 정보
    const val EVENT_DETAIL_CODE = "EVENT_DETAIL_CODE"
    const val VOTE_DETAIL_CODE = "VOTE_DETAIL_CODE"
    const val EXO_PAGE_TYPE =
        "EXO_PAGE_TYPE"                                                     //피드 페이지 타입
    const val SORT_TYPE =
        "SORT_TYPE"                                                                  // 정렬 타입
    const val SECTION_DTO_DATA =
        "SECTION_DTO"                                                  // 구간부르기
    const val SECTION_INDEX_INFO =
        "SECTION_INDEX_INFO"                                      // 구간부르기
    const val SECTION_SONG_INFO = "SECTION_SONG_INFO"                                       // 구간부르기
    const val SECTION_SONG_BG =
        "SECTION_SONG_BG"                                             // 구간부르기
    const val FOLLOW_DATA = "FOLLOW_DATA"
    const val COMMUNITY_ENTER_TYPE =
        "COMMUNITY_ENTER_TYPE"                            // 커뮤니티 진입 시 TAB PAGE 구분하기 위함
    const val MY_PAGE_DATA =
        "MY_PAGE_DATA"                                                       // 마이페이지, 유저페이지 진입시 필요한 데이터 모델
    const val CHAT_MESSAGE_ROOM_DATA = "CHAT_MESSAGE_ROOM_DATA"
}

/**
 * Glide Code
 */
object GlideCode {
    const val GLIDE_BLUR_RADIUS = 11
    const val GLIDE_BLUR_SAMPLING = 12
}

/**
 * 회원 상태 Type
 *  비회원 : US000 / 정상 : US001 / 휴면 : US002 / 탈퇴 : US003 / 영구정지 : US004
 *  1일정지 : US005 / 1개월정지 : US006 / 3개월정지 : US007 / 6개월정지 : US008 / 12개월정지 : US009
 */
enum class UserStateType(val code: String = "") {
    NON_MEMBER(code = "US000"),
    MEMBER(code = "US001"),
    DORMANT(code = "US002"),
    WITHDRAWAL(code = "US003"),
    SUSPEND(code = "US004"),
    ONE_DAY_SUSPEND(code = "US005"),
    ONE_MONTH_SUSPEND(code = "US006"),
    THREE_MONTH_SUSPEND(code = "US007"),
    SIX_MONTH_SUSPEND(code = "US008"),
    ONE_YEAR_SUSPEND(code = "US009"),
}

/**
 * 회원 유형 Type
 *  회원유형코드(MT001 : 신규/ MT002 : 무료/ MT003 : 유료/ MT004 : TJ대리점/ MT005 : 협력사/ MT006 : BP파트너/ MT007 :인플루언서)
 */
enum class UserType(val code: String = "") {
    NEW_MEMBER(code = "MT001"),
    FREE_MEMBER(code = "MT002"),
    PURCHASE_MEMBER(code = "MT003"),
    TJ_MEMBER(code = "MT004"),
    PARTNER_MEMBER(code = "MT005"),
    BP_MEMBER(code = "MT006"),
    INFLUENCER_MEMBER(code = "MT007"),
}

/**
 * 회원 구매 등급
 *  구매등급코드(PU001 : 유저 / PU002 : 일반 / PU003 : 친구 / PU004 : 가족 / PU005 : 빛)
 */
enum class UserPurchaseLevel(val code: String = "") {
    USER(code = "PU001"),
    NORMAL(code = "PU002"),
    FRIEND(code = "PU003"),
    FAMILY(code = "PU004"),
    SHINE(code = "PU005"),
}

/**
 * 회원 구매 등급
 *  구독이용권유형코드(SC001:3곡이용권 / SC002:30곡이용권 / SC003:100곡이용권 / SC004:월간패스 / SC005:1주일VIP이용권 / SC006:1개월VIP이용권 / SC007:6개월VIP이용권 / SC008:12개월VIP이용권 / SC009:VP쿠폰)
 */
enum class MemberShipType(val code: String = "", val grade: Int = -1) {
    SC001(code = "SC001", grade = -1),
    SC002(code = "SC002", grade = -1),
    SC003(code = "SC003", grade = -1),
    SC004(code = "SC004", grade = 1),
    SC005(code = "SC005", grade = 2),
    SC006(code = "SC006", grade = 3),
    SC007(code = "SC007", grade = 4),
    SC008(code = "SC008", grade = 5),
    SC009(code = "SC009", grade = -1),
}

/**
 * 네트워크 에러 메세지
 * @param code 상태 Code 값 없는 경우도 있음
 * @param fromResErrMsg 로컬 Res String 메세지 값
 * @param fromServerErrMsg 서버 메세지 값
 */
enum class HttpStatusType(
    val code: String = "",
    val status: String = "",
    @StringRes val fromResErrMsg: Int = -1,
    var fromServerErrMsg: String = ""
) {
    DEFAULT(fromResErrMsg = R.string.network_popup_default),
    SUCCESS(code = "200", status = "RS001", fromResErrMsg = R.string.network_status_rs001),
    FAIL(code = "200", status = "RS002", fromResErrMsg = R.string.network_status_rs002),
    NO_AUTHENTICATION(
        code = "401",
        status = "RS003",
        fromResErrMsg = R.string.network_status_rs003
    ),
    NO_ESSENTIAL_DATA(
        code = "200",
        status = "RS004",
        fromResErrMsg = R.string.network_status_rs004
    ),
    BAD_GATEWAY(code = "502", status = "RS005", fromResErrMsg = R.string.network_status_rs005),
    DUPLICATED_LOGIN(code = "402", status = "RS006", fromResErrMsg = R.string.network_status_rs003),
}


enum class SingingErrorType(
    @StringRes val fromResErrMsg: Int = -1,
    var fromServerErrMsg: String = ""
) {
    SINGING_EXCEPTION,
    FAIL_SONG_DATA(fromResErrMsg = R.string.network_popup_undefined),
    FAIL_MR_DOWNLOAD(fromResErrMsg = R.string.download_fail_mr),
    FAIL_XTF_DOWNLOAD(fromResErrMsg = R.string.download_fail_xtf),
    UNDEFINED_DOWNLOAD(fromResErrMsg = R.string.download_fail_song_info)
}

/**
 * 비디오 현재 페이지
 */
enum class ExoPageType(@StringRes val pageName: Int = -1) {
    NONE,
    MAIN_RECOMMEND(pageName = R.string.str_tab_recommend),     //메인 추천
    MAIN_FOLLOWING(pageName = R.string.str_tab_feed),                //메인 피드
    MAIN_SING_PASS,        //메인->씽패스
    SING_ING,               //부르기
    SING_SYNC,               //부르기 -> 프리뷰
    FEED_DETAIL              //피드 상세 화면
}

/**
 * 하단 네비
 */
enum class NaviType {
    NONE,           //default
    MAIN,
    SING_PASS,
    SING,
    COMMUNITY,
    MY,
}

/**
 * 페이지 탭 타입
 */
enum class TabPageType {
    MAIN_RECOMMEND,
    MAIN_FEED,
    MY_PAGE_UPLOAD,            //마이페이지 업로드
    MY_PAGE_LIKE,              //마이페이지 좋아요
    MY_PAGE_FAVORIT,           //마이페이지 즐겨찾기
    SONG_POPULAR,              //노래 콘텐츠 업로드 -> 노래 메인 -> 인기곡
    SONG_RECENT,               //노래 콘텐츠 업로드 -> 노래 메인 -> 신곡
    SONG_RECENTLY,             //노래 콘텐츠 업로드 -> 노래 메인 -> 최근 불렀던 노래
    SONG_GENRE,                //노래 콘텐츠 업로드 -> 노래 메인 -> 장르
    MY_PAGE_PRIVATE,           // 마이페이지 비공개 보관함
    SEARCH_POPULAR,            // 검색 인기
    SEARCH_VIDEO,              // 검색 동영상
    SEARCH_MR,                 // 검색 반주음
    SEARCH_TAG,                // 검색 태그
    SEARCH_USER,               // 검색 사용자
    FOLLOWING,                 // 팔로잉
    FOLLOWER,                  // 팔로워
    DAILY_MISSION,              // 일일미션
    PERIOD_MISSION,             // 기간미션
    SEASON_MISSION,             // 시즌미션
    MAIN_USER,                 // 메인 -> 유저
}


/**
 * Sing Type
 */
enum class SingType(
    val code: String = "",
    @StringRes val typeName: Int = -1,
    @DrawableRes val icon: Int = -1
) {
    SOLO(
        code = "PA001",
        typeName = R.string.str_solo,
        icon = R.drawable.ic_solo_s
    ),             //솔로
    DUET(
        code = "PA002",
        typeName = R.string.str_duet,
        icon = R.drawable.ic_duet
    ),                //듀엣
    BATTLE(code = "PA004", typeName = R.string.str_battle, icon = R.drawable.ic_battle_s),     //배틀
    GROUP(
        code = "PA003",
        typeName = R.string.str_group,
        icon = R.drawable.ic_battle_s
    ),      //그룹 (현재 지원 X)
    NORMAL(
        code = "PA005",
        typeName = R.string.str_normal
    ),                                               // 일반
    AD(
        code = "PA006",
        typeName = R.string.str_ad
    ),                                               // 광고
    NONE,   // default
}


/**
 * 씽패스 대시보드 미션 타입
 */
enum class MissionType(val code: String = "") {
    DAILY_MISSION(code = "RT001"),          // 데일리미션
    PERIOD_MISSION(code = "RT002"),          //기간미션
    SEASON_MISSION(code = "RT003")           //시즌미션
}


/**
 * 피드미디어유형코드(MD001 : 녹화 / MD002 : 녹음)
 */
enum class MediaType(val code: String = "") {
    VIDEO(code = "MD001"),          //녹화 (비디오)
    AUDIO(code = "MD002"),          //녹음 (오디오)
    NONE              //default
}

/**
 * Volume Type
 */
enum class VolumeType(val code: String = "") {
    VOLUME1(code = "1"),          // 1
    VOLUME2(code = "2"),          // 2
    VOLUME3(code = "3")           // 3
}

/**
 * 국가 언어 설정 Type
 */
enum class NationLanType(val code: String = "") {
    KR(code = "KR"),          // 한국
    EN(code = "EN"),          // 미국
    KO(code = "KO"),          // 한국어
    US(code = "US");           // 영어

    companion object {
        fun from(code: String): NationLanType {
            return values().find { it.code == code } ?: KO
        }
    }
}


/**
 * Sing 페이지 타입
 */
enum class SingEffectType {
    NONE, //SP sound filter
    SOUND, //SP sound filter
    VOLUME, //sound volume
    SYNC, // Video Sync
    SECTION,// 구간
    PREVIEW // 프리뷰
}

/**
 * 부르기 유형
 */
enum class SingingType(val code: String = "") {
    ALL(code = "SI001"),        //전체
    SECTION(code = "SI002") //구간
}


/**
 * 부르기 파트
 */
enum class PartType(
    val code: String = "",
    @ColorRes val sColor: Int = -1,
    @ColorRes val aColor: Int = -1,
    @ColorRes val bColor: Int = -1,
    @ColorRes val tColor: Int = -1
) {
    PART_A(code = "SP001"),
    PART_B(code = "SP002"),
}


/**
 * ChallengeType Type
 */
enum class ChallengeType {
    SECTION,                       //구간
    ALL,                              //전곡
    PART_A,                         //A 파트
    PART_B,                         //B 파트
    CHALLENGE_SING_PASS,    //씽패스로 도전
    CHALLENGE_SING_PASS_A, //씽패스 A 파트로 도전
    CHALLENGE_SING_PASS_B, //씽패스 B 파트로 도전
    NONE, //default
}


/**
 * Sing 페이지 타입
 */
enum class SingPageType {
    PREPARE, //파트,유형 선택
    SECTION, //구간부르기
    SING_ING, // 부르기
    SYNC_SING, // 싱크
    UPLOAD_FEED, //업로드중
    UPLOAD_FEED_COMPLETE,  //업로드 완료
    OFF_FEED_COMPLETE  // OFF 부르기  완료
}

/**
 * 배틀 진행 상태 코드
 */
enum class BattleStatusType {
    BS001,  //배틀진행
    BS002,  //배틀완료
    BS003,  //배틀만료
    BS004,  //배틀취소
}

/**
 * 콘텐츠노출설정코드
 * SH001 : 전체허용 ,SH002 : 나만허용 , SH003 : 친구허용
 */
enum class ShowContentsType(val code: String = "") {
    ALLOW_ALL(code = "SH001"),
    PRIVATE(code = "SH002"),
    ALLOW_FRIENDS(code = "SH003"),
    NONE("NONE"),
}

/**
 *  XTF 정보 타입
 */
enum class SingingCommandType(@DrawableRes val res: Int = -1) {
    LYRICS_START_EVENT,                 // 1개의 가사 이벤트 발생 시점
    LYRICS_END_EVENT,                     // 상위의 발생 이벤트 종료 시점
    INFO_SHOW,                                // 작사/작곡/노래정보 표출
    INFO_CLOSE,                               // 제목, 작사/작곡/노래 정보 지움
    START_LYRICS,                            // 가사 시퀀스 시작 시점 - 가사 2줄을 미리 표출
    END_LYRICS,                                // 가사 시컨스 종료 시점 - 가사 시컨싱이 완전히 정료되는 시점
    COUNT_4(res = R.drawable.sing_dot_4),                                    // 카운트 4
    COUNT_3(res = R.drawable.sing_dot_3),                                    // 카운트 3
    COUNT_2(res = R.drawable.sing_dot_2),                                    // 카운트 2
    COUNT_1(res = R.drawable.sing_dot_1),                                    // 카운트 1
    COUNT_0,                                    // 카운트 1
    SING_FEMALE_START,                   // 여자 부르기 시작
    SING_FEMALE_END,                       // 여자 부르기 끝
    SING_MALE_START,                       // 남자 부르기 시작
    SING_MALE_END,                          // 남자 부르기 끝
    SING_TOGETHER_START,               // 함깨 부르기 끝
    SING_TOGETHER_END,                  // 함꼐 부르기 끝
    NONE // 언논
}

enum class SingingPartType(@ColorRes val color: Int = -1) {
    DEFAULT(color = R.color.color_ffa8ff),    // 기본 part
    FEMALE_PART(color = R.color.color_00e7ff),  // 여자, A Part
    MALE_PART(color = R.color.color_ffa8ff),  // 남자, B Part
    T_PART(color = R.color.color_23ceb8), // together part
    USER(color = R.color.color_03ff20) // user song
}

/**
 * 좋아요 타입
 * 좋아요 유형(F:일반피드 / FC:피드댓글 / FR:피드답글  / L:라운지게시글 / LC:라운지댓글 / LR:라운지답글  / VC:투표댓글 / VR:투표답글)
 */
enum class LikeType(val code: String = "") {
    FEED(code = "F"),
    FEED_COMMENT(code = "FC"),
    FEED_RE_COMMENT(code = "FR"),
    LOUNGE(code = "L"),
    LOUNGE_COMMENT(code = "LC"),
    LOUNGE_RE_COMMENT(code = "LR"),
    VOTE_COMMENT(code = "VC"),
    VOTE_RE_COMMENT(code = "VR"),
}

/**
 * 즐겨찾기(북마크) 타입
 * 북마크 유형(F:피드 / S:반주음)
 */
enum class BookMarkType(val code: String = "") {
    FEED(code = "F"),
    SONG(code = "S"),
}

/**
 * 신고  타입
 * RP001:사용자신고 / RP002:피드콘텐츠신고 / RP003:댓글신고 / RP004:답글신고 / RP005:라운지
 * RP006:커뮤니티라운지댓글신고 / RP007:커뮤니티라운지답글신고 / RP008:커뮤니티투표하기댓글신고 / RP009:커뮤니티투표하기답글신고
 */
enum class ReportType(val code: String = "") {
    USER(code = "RP001"),
    FEED_CONTENTS(code = "RP002"),
    FEED_COMMENT(code = "RP003"),
    FEED_RE_COMMENT(code = "RP004"),
    LOUNGE(code = "RP005"),
    LOUNGE_COMMENT(code = "RP006"),
    LOUNGE_RE_COMMENT(code = "RP007"),
    VOTE_COMMENT(code = "RP008"),
    VOTE_RE_COMMENT(code = "RP009")
}

/**
 * 코멘트 타입
 * 콘텐츠 유형(F:피드 / L:커뮤니티라운지 / V:커뮤니티투표)
 */
enum class CommentType(val code: String = "") {
    FEED(code = "F"),   //피드
    LOUNGE(code = "L"), //커뮤니티 라운지
    COMMUNITY_VOTE(code = "V") // 커뮤니티 투표
}

/**
 * 피드 모아보기 타입
 * 피드 모아보기 유형(F:피드 / T:태그명)
 */
enum class CollectionType(val code: String = "") {
    FEED(code = "F"),
    TAG(code = "T"),
}

/**
 * 피드 상세 타입
 * 북마크 유형(F:피드 / T:태그명)
 */
enum class FeedDetailType(val code: String = "") {
    MY_UPLOAD_CONTENTS(code = "MY_UPLOAD_CONTENTS"),
    OTHER_UPLOAD_CONTENTS(code = "OTHER_UPLOAD_CONTENTS"),
    MY_LIKE_CONTENTS(code = "MY_LIKE_CONTENTS"),
    OTHER_LIKE_CONTENTS(code = "OTHER_LIKE_CONTENTS"),
    MY_BOOKMARK_CONTENTS(code = "MY_BOOKMARK_CONTENTS"),
    OTHER_BOOKMARK_CONTENTS(code = "OTHER_BOOKMARK_CONTENTS"),
    MY_PRIVATE_CONTENTS(code = "MY_PRIVATE_CONTENTS"),
    COLLECTION_SONG_CONTENTS(code = "COLLECTION_SONG_CONTENTS"),
    COLLECTION_TAG_CONTENTS(code = "COLLECTION_TAG_CONTENTS"),
    SEARCH_RESULT_CONTENTS(code = "SEARCH_RESULT_CONTENTS"),
    SINGLE_FEED_CONTENTS(code = "SINGLE_FEED_CONTENTS"),
}

/**
 * ImageAndVideoPicker Type
 */
enum class ImageVideoPickerType {
    albumVideoupload, gakkeryBothImageVideo, gakkeryOnlyImage, gakkeryOnlyVideo, none
}

enum class VideoUploadPageType {
    NONE,
    ALBUM,                      //앨범에서 올린 영상
    SING_CONTENTS,        //부르기에서 올린 영상
}

/**
 * LD001 : URL / LD002 : 부르기메인 / LD003 : 피드메인 / LD004 : 씽패스 / LD005 : 마이페이지 / LD006 : 커뮤니티투표 / LD007 : 이벤트 / LD008 : 라운지 /  LD009 : 멤버쉽 / LD010 피드 상세 / LD011 메세지함
 *
 */
enum class LinkMenuTypeCode(val code: String = "") {
    LINK_URL(code = "LD001"),
    LINK_SONG(code = "LD002"),
    LINK_FEED_MAIN(code = "LD003"),
    LINK_SING_PASS(code = "LD004"),
    LINK_MY_PAGE(code = "LD005"),
    LINK_COMMUNITY_VOTE(code = "LD006"),
    LINK_COMMUNITY_EVENT(code = "LD007"),
    LINK_COMMUNITY_LOUNGE(code = "LD008"),
    LINK_MEMBERSHIP(code = "LD009"),
    LINK_FEED_CONTENTS(code = "LD010"),
    LINK_MESSAGE_ROOM(code = "LD011"),
    LINK_USER_MYPAGE(code = "LD012");

    companion object {
        fun from(code: String): LinkMenuTypeCode? {
            return values().find { it.code == code }
        }
    }
}

enum class SortType { ASC, DESC, NONE }

/**
 * 유형별반주음 목록 조회
 * 반주음 조회 유형코드(G:장르별 / P:인기곡 / N:신곡 / R:연관음원곡 / M:최근 불렀던 노래)
 */
enum class ReqTypeCd { G, P, N, R, UR, M }

/**
 * 리소스타입(P : 프로필이미지 / I : 1대1문의첨부파일 / L : 라운지첨부파일 / F : 피드업로드파일)
 */
enum class ResourcePathType(val code: String = "") {
    PROFILE(code = "P"),
    QNA(code = "I"),
    LOUNGE(code = "L"),
    FEED(code = "F")
}

/**
 * 반주음원유형코드(SIT01:TJ반주음원 / SIT02:외부반주음원 / SIT03:사용자반주음원)
 */
enum class SITType(val code: String = "") {
    TJ_SOUND_SOURCE(code = "SIT01"),
    EXTERNAL_SOUND_SOURCE(code = "SIT02"),
    USER_SOUND_SOURCE(code = "SIT03")
}

/**
 * 자동볼륨조절 ON -> 볼륨 1,2,3 선택 가능
 * 앱 실행시 소리 OFF ON -> 자동볼륨조절 및 볼륨값 OFF
 *
 */
enum class PlayStatus {
    SETTING_AUTO, SETTING_MUTE
}

/**
 * PUSH 타입
 */
enum class PushType {
    PUSH_ALLOW_ALL,
    PUSH_UPLOADING,
    PUSH_UPLOAD_FAIL,
    PUSH_UPLOAD_COMPLETE,
    PUSH_UPLOAD_RELOAD,
    PUSH_DORMANT_USER,
    PUSH_STOP_USER,
    PUSH_MARKETING_ALLOW,
    PUSH_NEW_EVENT,
    PUSH_VOTE,
    PUSH_SEASON,
    PUSH_FOLLOW_ME,
    PUSH_LIKE_MY_CONTENTS,
    PUSH_LIKE_MY_POSTS,
    PUSH_LIKE_MY_COMMENT,
    PUSH_COMPLETE_MY_DUET,
    PUSH_COMPLETE_MY_BATTLE,
    PUSH_MY_FOLLOWERS_NEW_CONTENTS,
    PUSH_MY_FOLLOWERS_NEW_POSTS,
    PUSH_RECEIVE_MY_FOLLOWER,
    PUSH_GET_DM,
    PUSH_MANNER_MODE_ALLOW
}

/**
 * SUBTABFEED 타입
 */
enum class FeedSubDataType(val code: String = "") {
    C(code = "C"),  // 공연
    F(code = "F"),  // 즐겨찾기 피드
    P(code = "P"),  // 개인영상
    S(code = "S"),  // 반주음
}

/**
 * 차단 유형 코드 (BK001 : 사용자차단 / BK002 : 피드콘텐츠차단 / BK003 : 커뮤니티콘텐츠차단)
 */
enum class BlockType(val code: String = "") {
    USER(code = "BK001"),
    FEED(code = "BK002"),
    COMMUNITY(code = "BK003")
}

enum class DeleteRefreshFeedList {
    MAIN,
    DETAIL,
}


/**
 * 기타 약관 혹은 정책 조회 코드
 */
enum class EtcTermsType(val code: String = "") {
    LOGIN_AGREE(code = "TM001"),
    LOGIN_PERSONAL(code = "TM002"),
    MEMBERSHIP(code = "TM003"),
    SINGPASS(code = "TM004"),
    WITHDRAW(code = "TM005"),
}

/**
 * DynamicLink Segment
 */
enum class DynamicLinkPathType {
    MAIN
}

/**
 * DynamicLink Param
 */
enum class DynamicLinkKeyType {
    ID,
    MNGCD
}

/**
 * 팔로우 팔로잉 타입
 */
enum class FollowType(val code: String = "") {
    FOLLOWER(code = "W"),
    FOLLOWING(code = "I"),
}

/**
 * TCP 통신시 필요한 식별자
 */
enum class TcpHeaderType(val key: String) {
    SEND_MSG("KM"),
    SEND_SESSION("KR"),
    SEND_CONNECTION("KI"),
    SEND_UPLOAD_IMG("KP"),
    SEND_JOIN_USER("KJ"),

    RECV_MSG("SL"),
    RECV_SESSION("SK"),
    RECV_CONNECTION("SR"),
    RECV_CHAT("SE"),
    RECV_CHAT_MSG("SM"),
    RECV_CHAT_READ("SG"),
    RECV_IMAGE_RESOURCE("SP")
}

/**
 * 레벨
 */
enum class LevelType(val code: String) {
    LEVEL_1("LEVEL 01"),
    LEVEL_2("LEVEL 02"),
    LEVEL_3("LEVEL 03"),
    LEVEL_4("LEVEL 04"),
    LEVEL_5("LEVEL 05"),
}

/**
 * 이미지 상세 보기 타입
 */
enum class ShowImageDetailType(val code: String = "") {
    NORMAL(code = "NORMAL"),
    EDIT_FR_PROFILE(code = "EDIT_FR_PROFILE"),
    EDIT_BG_PROFILE(code = "EDIT_BG_PROFILE"),
    EDIT_ATTACH_IMAGE(code = "EDIT_ATTACH_IMAGE"),
}

enum class ChatMsgType(val code: String) {
    TEXT("CT001"),
    PHOTO("CT002");

    companion object {
        fun getType(code: String): ChatMsgType {
            return if (code == TEXT.code) {
                TEXT
            } else {
                PHOTO
            }
        }
    }
}

/**
 * 씽패스 스킵권 사용
 */
enum class SingPassSkipMissionType {
    SK003,
    SK004
}

// namespace
typealias UploadProgressAudio = RxBusEvent.SingUploadProgressEvent.AudioType

typealias UploadProgressVideo = RxBusEvent.SingUploadProgressEvent.VideoType

typealias ProgressVideoType = RxBusEvent.SingUploadProgressEvent.VideoType.Type

typealias UploadProgressState = RxBusEvent.SingUploadProgressEvent.Type
