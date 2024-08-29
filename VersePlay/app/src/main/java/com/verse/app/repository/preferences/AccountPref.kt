package com.verse.app.repository.preferences

import android.content.Context
import android.os.Build
import android.text.TextUtils
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.verse.app.contants.AppData
import com.verse.app.contants.NationLanType
import com.verse.app.model.encode.EncodeData
import com.verse.app.model.mypage.GetSettingInfoData
import com.verse.app.utility.DLogger
import com.verse.app.utility.manager.UserSettingManager
import java.util.Locale
import javax.inject.Inject

class AccountPref @Inject constructor(
    private val pref: BasePref,
) {
    private val PREF_FCM_PUSH_TOKEN = "PREF_FCM_PUSH_TOKEN"                  // FCM PUSH TOKEN
    private val PREF_PERMISSIONS = "PREF_PERMISSIONS"                      // 권한 고지 팝업 노출 여부
    private val PREF_INTRO_SETTING = "PREF_INTRO_SETTING"                        // 인트로 설정 노출 여부
    private val PREFERENCE_APP_LOCALE_COUNTRY = "PREFERENCE_APP_LOCALE_COUNTRY"                // 앱 국가 설정
    private val PREFERENCE_APP_LOCALE_LANGUAGE = "PREFERENCE_APP_LOCALE_LANGUAGE"              // 앱 언어 설정
    private val PREF_JWT_TOKEN = "PREF_JWT_TOKEN"                                // jwt 토큰
    private val PREF_AUTH_TYPE_CD = "PREF_AUTH_TYPE_CD"                              // 회원계정인증유형코드(AU001 : 구글  / AU002 : 페이스북 / AU003 : 애플 / AU004 : 카카오 / AU005 : 네이버 / AU006 : 트위터)
    private val PREF_SNS_PROVIDER = "PREF_SNS_PROVIDER"                              // sns name
    private val PREF_SNS_ACCESS_TOKEN = "PREF_SNS_ACCESS_TOKEN"                      // sns 인증 토큰
    private val PREF_CUR_FRAGMENT = "PREF_CUR_FRAGMENT"                           // 프래그먼트 하위 탭 전용 분기 코드
    private val PREF_RECENT_MAIN_POPUP_MNG_CD = "PREF_RECENT_MAIN_POPUP_MNG_CD"  // 가장 최근 노출 메인 공지 팝업 관리코드
    private val PREF_NO_SHOW_MAIN_POPUP = "PREF_NO_SHOW_MAIN_POPUP"              // 메인 공지 팝업 노출 여부
    private val PREF_RECENT_SEARCH_WORD = "PREF_RECENT_SEARCH_WORD"              // 최근 검색어
    private val PREF_ENCODE_SONG_DATA = "PREF_ENCODE_SONG_DATA"                  // 인코딩 곡 정보
    private val PREF_SETTING_VOLUME_MUTE = "PREF_SETTING_VOLUME_MUTE"       // 설정 -> 볼륨 -> 음소거
    private val PREF_SETTING_VOLUME_AUTO = "PREF_SETTING_VOLUME_AUTO"       // 설정 -> 자동볼륨설정
    private val PREF_SETTING_VOLUME_TYPE = "PREF_SETTING_VOLUME_TYPE"       // 설정 -> 볼륨타입
    private val PREF_SECTION_GUIDE = "PREF_SECTION_GUIDE"                      // 구간 설정 가이드 노출 여부
    private val PREF_FIRST_SHOW_NIGHT_TIME_AGREE_PUSH = "PREF_FIRST_SHOW_NIGHT_TIME_AGREE_PUSH"              // 야간 푸쉬 알림 동의 팝업 최초 노출 처리 여부

    fun setPermissionsPage(isDone: Boolean = true) {
        pref.setValue(PREF_PERMISSIONS, isDone)
    }

    fun isPermissionsPageShow() = pref.getValue(PREF_PERMISSIONS, false)

    fun setIntroSettingPage(isDone: Boolean = true) {
        pref.setValue(PREF_INTRO_SETTING, isDone)
    }

    fun isIntroSettingPageShow() = pref.getValue(PREF_INTRO_SETTING, false)

    /**
     * 국가 언어 설정
     */
    // 국가 설정
    fun setPreferenceLocaleCountry(localeCountry: String) {
        DLogger.d("setPreferenceLocaleCountry : $localeCountry")
        UserSettingManager.getSettingInfo().svcNtCd = localeCountry
        pref.setValue(PREFERENCE_APP_LOCALE_COUNTRY, localeCountry)
    }

    fun getPreferenceLocaleCountry(): String {
        DLogger.d("getPreferenceLocaleCountry : ${pref.getValue(PREFERENCE_APP_LOCALE_COUNTRY, NationLanType.KR.code)}")
        return pref.getValue(PREFERENCE_APP_LOCALE_COUNTRY, NationLanType.KR.code)
    }

    fun isPreferenceLocaleCountry() = pref.getValue(PREFERENCE_APP_LOCALE_COUNTRY, Locale.getDefault().country)

    fun getLocaleCountry(context: Context): String {
        if (isPreferenceLocaleCountry().isNotEmpty()) {
            return isPreferenceLocaleCountry()
        } else {
            val defaultLocale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                 context.resources.configuration.locales.get(0).country
            } else {
                @Suppress("DEPRECATION")
                 context.resources.configuration.locale.country
            }
            return defaultLocale
        }
    }

    // 언어 설정
    fun setPreferenceLocaleLanguage(localeLanguage: String) {
        DLogger.d("setPreferenceLocaleLanguage : $localeLanguage")
        UserSettingManager.getSettingInfo().svcLangCd = localeLanguage
        pref.setValue(PREFERENCE_APP_LOCALE_LANGUAGE, localeLanguage)
    }

    fun getPreferenceLocaleLanguage(): String {
        DLogger.d("getPreferenceLocaleLanguage : ${pref.getValue(PREFERENCE_APP_LOCALE_LANGUAGE, NationLanType.KO.code)}")
        return pref.getValue(PREFERENCE_APP_LOCALE_LANGUAGE, NationLanType.KO.code)
    }

    fun isPreferenceLocaleLanguage() =
        pref.getValue(PREFERENCE_APP_LOCALE_LANGUAGE, Locale.getDefault().language)

    /**
     * JWT TOKEN Set
     */
    fun setJWTToken(jwtToken: String) {
        pref.setValue(PREF_JWT_TOKEN, jwtToken)
    }

    /**
     * JWT TOKEN Get
     */
    fun getJWTToken(): String {
        return pref.getValue(PREF_JWT_TOKEN, "")
    }

    /**
     * AuthTypeCd Set
     */
    fun setAuthTypeCd(authTpCd: String) {
        pref.setValue(PREF_AUTH_TYPE_CD, authTpCd)
    }

    /**
     * JWT TOKEN Get
     */
    fun getAuthTypeCd(): String {
        return pref.getValue(PREF_AUTH_TYPE_CD, "")
    }

    /**
     * SNS 유형 Set
     */
    fun setSnsProvider(snsProvider: String) {
        if (snsProvider.isNotEmpty()) {
            pref.setValue(PREF_SNS_PROVIDER, snsProvider)
        }
    }

    /**
     * SNS 유형 Get
     */
    fun getSnsProvider(): String {
        return pref.getValue(PREF_SNS_PROVIDER, "")
    }

    /**
     * SNS 인증 토큰 Set
     */
    fun setSnsAccessToken(snsAccessToken: String) {
        if (snsAccessToken.isNotEmpty()) {
            pref.setValue(PREF_SNS_ACCESS_TOKEN, snsAccessToken)
        }
    }

    /**
     * SNS 인증 토큰 Get
     */
    fun getSnsAccessToken(): String {
        return pref.getValue(PREF_SNS_ACCESS_TOKEN, "")
    }


    /**
     * 현재 fragment 저장 프리퍼런스
     * 0 -> 마이페이지
     * 1 -> 메인 -> 유저페이지
     * 2 -> 마이페이지 -> 비공개 보관함
     */
    fun setCurrentFragment(fragment: Int) {
        if (TextUtils.isEmpty(fragment.toString())) {
            pref.setValue(PREF_CUR_FRAGMENT, fragment.toString())
        }
    }

    fun getCurrentFragment(): String {
        return pref.getValue(PREF_CUR_FRAGMENT, "1")
    }

    fun setRecentMainPopupMngCd(manageCode: String = "") {
        pref.setValue(PREF_RECENT_MAIN_POPUP_MNG_CD, manageCode)
    }

    fun getRecentMainPopupMngCd() = pref.getValue(PREF_RECENT_MAIN_POPUP_MNG_CD, "")

    fun setShowMainPopup(isShow: Boolean = true) {
        pref.setValue(PREF_NO_SHOW_MAIN_POPUP, isShow)
    }

    fun getShowMainPopup() = pref.getValue(PREF_NO_SHOW_MAIN_POPUP, true)

    /**
     * 최근 검색어 Set
     */
    fun setRecnetSearchWord(recentSearchWord: String) {
        pref.setValue(PREF_RECENT_SEARCH_WORD, recentSearchWord)
    }

    /**
     * 최근 검색어 Get
     */
    fun getRecnetSearchWord(): String {
        return pref.getValue(PREF_RECENT_SEARCH_WORD, "")
    }


    /**
     * 인코딩 데이터 Set
     */
    fun setEncodedData(list: MutableList<EncodeData>) {
        val jsonData = Gson().toJson(list)
        pref.setValue(PREF_ENCODE_SONG_DATA, jsonData)
    }

    /**
     * 인코딩 데이터 모두 삭제
     */
    fun clearEncodedData() {
        val jsonData = Gson().toJson(mutableListOf<EncodeData>())
        pref.setValue(PREF_ENCODE_SONG_DATA, jsonData)
    }

    /**
     * 인코딩 데이터 Get
     */
    fun getEncodedData(): MutableList<EncodeData> {
        return getList(PREF_ENCODE_SONG_DATA, EncodeData::class.java)
    }

    /**
     *  Pref Get List
     */
    fun <T> getList(key: String, cls: Class<T>): MutableList<T> {
        val gson = Gson()
        val list = mutableListOf<T>()
        val gsonStr = pref.getValue(key, "")
        return if (gsonStr.isEmpty()) {
            list
        } else {
            try {
                val array = JsonParser.parseString(gsonStr).asJsonArray
                for (jsonElement in array) {
                    list.add(gson.fromJson(jsonElement, cls))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            list
        }
    }

    /**
     * 설정 -> 볼륨값 저장
     * 다음 앱 실행 시 반영
     */
    fun setVolumeMute(isMute: Boolean = false) {
        pref.setValue(PREF_SETTING_VOLUME_MUTE, isMute)
    }

    fun getVolumeMute(context: Context) = pref.getValue(PREF_SETTING_VOLUME_MUTE, false)

    fun setVolumeAuto(isAuto: Boolean = false) {
        pref.setValue(PREF_SETTING_VOLUME_AUTO, isAuto)
    }

    fun getVolumeAuto(context: Context) = pref.getValue(PREF_SETTING_VOLUME_AUTO, false)

    fun setCurVolume(curType: String) {
        pref.setValue(PREF_SETTING_VOLUME_TYPE, curType)
    }

    fun getCurVolume(context: Context): String {
        return pref.getValue(PREF_SETTING_VOLUME_TYPE, "0")
    }

    fun setFcmPushToken(pushToken: String) {
        pref.setValue(PREF_FCM_PUSH_TOKEN, pushToken)
    }

    fun getFcmPushToken(): String {
        return pref.getValue(PREF_FCM_PUSH_TOKEN, "")
    }

    fun setSectionGuidePage(isDone: Boolean = true) {
        pref.setValue(PREF_SECTION_GUIDE, isDone)
    }

    fun istSectionGuidePage() = pref.getValue(PREF_SECTION_GUIDE, false)

    fun setNightTimeAgreePushPopup(isShow: Boolean = false) {
        pref.setValue(PREF_FIRST_SHOW_NIGHT_TIME_AGREE_PUSH, isShow)
    }

    fun getNightTimeAgreePushPopup(): Boolean {
        return pref.getValue(PREF_FIRST_SHOW_NIGHT_TIME_AGREE_PUSH, false)
    }

    fun logout() {
        setJWTToken("")
        setAuthTypeCd("")
        setSnsAccessToken("")
        setSnsProvider("")
    }
}