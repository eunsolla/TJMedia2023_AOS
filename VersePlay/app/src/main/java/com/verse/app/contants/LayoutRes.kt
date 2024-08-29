package com.verse.app.contants

import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import com.verse.app.R

/**
 * 헤더 타입
 */
enum class HeaderType(@LayoutRes val id: Int) {
    NONE(R.layout.view_header_none),
    SEARCH(R.layout.view_header_search),                //노래 콘텐츠 업로드 -> 노래 메인 -> 검색
    SONG_LIST(R.layout.view_header_song_list),        //노래 콘텐츠 업로드 -> 노래 메인 -> 더보기 -> 목록
    MYPAGE_SETTING(R.layout.view_header_back_title), //마이페이지 -> 세팅
    HEADER_BACK_TITLE(R.layout.view_header_back_title), //back,title
    COMMENT(R.layout.view_header_comment),                   // 댓글
}


/**
 * HeaderType -> 헤더 타이틀
 */
enum class HeaderInfo(@StringRes val defineText: Int = -1, var dynamicText: String = "") {
    DEFAULT(defineText = R.string.app_name),                                       //노래 콘텐츠 업로드 -> 노래 메인 -> 인기곡
    SONG_POPULAR(defineText = R.string.str_song_popular),                                      //노래 콘텐츠 업로드 -> 노래 메인 -> 인기곡
    SONG_RECENT(defineText = R.string.str_song_recent),                                         //노래 콘텐츠 업로드 -> 노래 메인 -> 신곡
    SONG_RECENTLY(defineText = R.string.song_main_title_recently),                             //노래 콘텐츠 업로드 -> 노래 메인 -> 최근 불렀던 노래
    SONG_GENRE(defineText = R.string.song_main_title_genre)                  //노래 콘텐츠 업로드 -> 노래 메인 -> 장르
}


/**
 * Recycler Paging Type
 */
enum class ListPagedItemType(@LayoutRes val layoutId: Int) {
    NONE(-1),
    FEED(R.layout.item_feed),
    MAIN_FOLLOWING(R.layout.item_following_feed),
    MAIN_SING_PASS(R.layout.item_sing_pass),                                   //씽패스메인
    MAIN_SING_PASS_LIST(R.layout.item_sing_pass_list),                          //씽패스장르랭킹목록
    MAIN_SING_PASS_RANKING_LIST(R.layout.item_sing_pass_ranking_list),          //씽패스장르랭킹목록아이템
    MAIN_SING_PASS_SEASON_ITEM_LIST(R.layout.item_season_item_list),          //씽패스 획득 가능한 아이템
    MAIN_SING_PASS_DAILY_MISSION_LIST(R.layout.item_sing_pass_mission_daily_list),          //씽패스미션목록(일일)
    MAIN_SING_PASS_PERIOD_MISSION_LIST(R.layout.item_sing_pass_mission_period_list),          //씽패스미션목록(기간)
    MAIN_SING_PASS_SEASON_MISSION_LIST(R.layout.item_sing_pass_mission_season_list),          //씽패스미션목록(시즌)
    MY_PAGE_RECOMMEND(R.layout.item_recommend_user),            //마이페이지 -> 추천유저
    ACCOMPANIMENT(R.layout.item_accompaniment),                 //마이페이지 -> 즐겨찾기 -> 반주음
    SONG_HOT_INFO_VIEW(R.layout.item_song_main_hot_info),       //노래 콘텐츠 업로드 -> 노래 메인 -> 지금뜨는 노래
    SONG_RECENTLY_VIEW(R.layout.item_song_main_recently),       //노래 콘텐츠 업로드 -> 노래 메인 -> 최근 불렀던 노래
    SONG_GENRE_VIEW(R.layout.item_song_main_genre),             //노래 콘텐츠 업로드 -> 노래 메인 -> 장르
    SONG_CHART_VIEW(R.layout.item_song_main_chart),             //노래 콘텐츠 업로드 -> 노래 메인 -> 인기곡/신곡
    ITEM_SONG_HOT_INFO(R.layout.item_hot_info),                 //노래 콘텐츠 업로드 -> 노래 메인 -> 지금뜨는 노래 -> 하위 아이템
    ITEM_SONG_RECENTLY(R.layout.item_song_h),                   //노래 콘텐츠 업로드 -> 노래 메인 -> 최근 불렀던 노래 -> 하위 아이템
    ITEM_SONG_POPULAR_RECENT(R.layout.item_song_h),             //노래 콘텐츠 업로드 -> 노래 메인 -> 인기곡/신곡 -> 하위 아이템
    SONG_LIST(R.layout.item_song_list),                         //노래 콘텐츠 업로드 -> 더보기 -> 목록
    SOUND_FILTER(R.layout.item_sound_filter),                   //SuperPowered 사운드 필터
    ITEM_LYRICS(R.layout.item_lyrics),                          //부르기 가사 정보
    ITEM_LYRICS_VIDEO(R.layout.item_lyrics),                    //부르기 가사 정보
    TUTORIAL_USER_GUIDE(R.layout.item_tutorial),                //튜토리얼, 유저 가이드
    MYPAGE_ALERT(R.layout.item_mypage_alert),                   //마이페이지 알림 내역
    MYPAGE_SETTING_SECURITY(R.layout.item_login_device),        //로그인한 디바이스
    REPORT(R.layout.item_report),                               //신고 목록
    REPORT_SUB(R.layout.item_report_sub),                                           //신고 목록 서브
    ITEM_SEARCH_MAIN_RECENT_KEYWORD(R.layout.item_search_main_recent_keyword),      // 검색 최근 검색어
    ITEM_SEARCH_POPULARKEYWORD(R.layout.item_search_popularkeyword),                // 검색 인기검색어
    ITEM_SEARCH_LOVELY_SONG(R.layout.item_search_lovely_song),                      // 검색 지금 사랑 받는 노래
    ITEM_SEARCH_POPULAR_SONG(R.layout.item_search_popular_song),                    // 검색 인기 노래
    ITEM_SEARCH_CONTENT(R.layout.item_search_video),              //검색 인기 관련 컨텐츠
    ITEM_SEARCH_VIDEO(R.layout.item_search_video),                                  //검색 인기
    ITEM_SEARCH_USER(R.layout.item_search_user),                                    //검색 인기
    ITEM_SEARCH_TAG(R.layout.item_search_tag),                                      //검색 인기
    NOTICE(R.layout.item_noti),                                                     //마이페이지 -> 공지사항
    TERMS(R.layout.item_terms),                                                     //마이페이지 -> 정책
    BLOCK_LIST(R.layout.item_block_list),                                           //마이페이지 -> 차단유저
    FAQ_LIST(R.layout.item_faq_list),                                               //마이페이지 -> FAQ
    CATEGORY_LIST(R.layout.item_category_list),                                     //마이페이지 -> 카테고리
    CATEGORY_LIST_SUB(R.layout.item_category_sub),                                  //마이페이지 -> FAQ -> 서브
    CATEGORY_LIST_SUB_LAST(R.layout.item_category_sub_last),                                  //마이페이지 -> FAQ -> 서브
    ITEM_COMMENT(R.layout.item_comment),                                            //댓글
    ITEM_COMMENT_RE(R.layout.item_comment_re),
    ITEM_MY_QNA(R.layout.item_my_qna),                                              // 마이페이지 -> 나의 1:1문의
    ITEM_SEND_QNA(R.layout.item_select_box),                                        // 마이페이지 -> 의견보내기
    MY_PAGE_SINGPASS(R.layout.item_mypage_sing_pass),                               //마이페이지 -> 싱패스
    FOLLOW_LIST(R.layout.item_follow_list),                                         //마이페이지 -> 팔로우 리스트
    ITEM_SEARCH_KEYWORD_POPULAR(R.layout.item_search_keyword_popular),              // 검색어 입력 > 인기 검색어
    COLLECTION_FEED(R.layout.item_collection_feed),                                 // 피드모아보기 아이템
    ITEM_COMMUNITY_MAIN_BANNER(R.layout.item_community_main_banner), // 커뮤니티 탭 > 메인 배너
    ITEM_COMMUNITY_LOUNGE(R.layout.item_community_lounge),
    ITEM_COMMUNITY_EVENT(R.layout.item_community_event),
    ITEM_COMMUNITY_VOTE(R.layout.item_community_vote),
    ITEM_LOUNGE_GALLERY(R.layout.item_lounge_gallery),
    ITEM_LOUNGE_MODIFY_EDIT_TEXT(R.layout.item_lounge_edit_text),
    ITEM_LOUNGE_MODIFY_IMAGE(R.layout.item_lounge_modify_image),
    ITEM_LOUNGE_MODIFY_LINK_URL(R.layout.item_lounge_modify_linkurl),
    ITEM_MEMBERSHIP(R.layout.item_membership),
    ITEM_RELATEDSOUND(R.layout.item_related_sound),
    ITEM_MY_PAGE_FEED(R.layout.item_my_page_feed),
    ITEM_PRIVATE_SONG_BOX(R.layout.item_private_song_box),                  //마이페이지 -> 비공개 콘텐츠 보관함
    ITEM_PRIVATE_FEED(R.layout.item_my_page_feed),                              //마이페이지 -> 비공개 콘텐츠 보관함 -> 피드
    ITEM_EMPTY(R.layout.item_empty),
    ITEM_NATION(R.layout.item_nation),          // 국가 선택
    ITEM_LANGUAGE(R.layout.item_language),          // 언어 선택
    ITEM_CHAT_ROOM(R.layout.item_chat_room),    // 메시지함
    ITEM_CHAT_MESSAGE_DATE(R.layout.item_chat_message_date),
    ITEM_CHAT_MY_MESSAGE(R.layout.item_chat_my_message),
    ITEM_CHAT_MY_PHOTO(R.layout.item_chat_my_photo),
    ITEM_CHAT_OTHER_MESSAGE(R.layout.item_chat_other_message),
    ITEM_CHAT_OTHER_PHOTO(R.layout.item_chat_other_photo),
    ITEM_CHAT_INIT_OTHER_PROFILE(R.layout.item_chat_init_profile),
}

/**
 * ViewPager2 Fragment Adapter
 */
enum class FragmentType(val uniqueId: Int = 0) {
    BASE_MAIN(0),                                               // Base Main
    BASE_MAIN_SUB(20),                                          // Base Main -> 하위 탭 (feed,recommend)
    POPULAR_RECENT(50),                                         // 노래콘텐츠업로드 -> 인기곡/신곡
    PART_INFO_VIEW(60),                                         // 솔로/듀엣/배틀 파트 선택
    // SEARCH_INFO(80),            // 검색
    FOLLOWING_INFO(90),                                       // 마이페이지 => 팔로우/팔로잉 목록
    SING_PASS_MISSION(100),                                     // 씽패스 미션 목록
}

/**
 * Bottom Navi
 */
enum class BottomNaviType(@LayoutRes val id: Int) {
    DEFAULT(R.layout.view_navigation_bar),
    BOTTOM_B(-1)
}

/**
 * gif type
 */
enum class GIFType(val gifId: Int) {
    INTRO(gifId = R.raw.verse_intro),
    DOWN_LOADING_CONTENTS(gifId = R.raw.downloading_contents),
    UPLOADING_CONTENTS(gifId = R.raw.ic_uploading_song_content),
}

