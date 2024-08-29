package com.verse.app.utility.manager

import com.verse.app.contants.AppData
import com.verse.app.model.mypage.GetSettingInfoData
import com.verse.app.utility.DLogger

/**
 * Description : 사용자 설정 데이터 관리 클래스
 *
 * Created by jhlee on 2023/08/17
 */
object UserSettingManager {

    private lateinit var settingInfoData: GetSettingInfoData

    /**
     * 설정 Set
     */
    fun setSettingInfo(data: GetSettingInfoData) {
        settingInfoData = data
    }

    /**
     * 설정 Get
     */
    fun getSettingInfo(): GetSettingInfoData {
        if (!::settingInfoData.isInitialized || settingInfoData == null) {
            settingInfoData = GetSettingInfoData()
        }
        return settingInfoData
    }

    /**
     * 공개/비공개 Set
     * @param strYn Y:비공개 , N : 공개
     */
    fun setPrivateYn(strYn: String) {
        getSettingInfo().prvAccYn = strYn
    }

    /**
     * 현재 상태가 공개/비공개인지 체크
     * @return true 비공개 , false 공개
     */
    fun isPrivateUser(): Boolean {
        return getSettingInfo().prvAccYn == AppData.Y_VALUE
    }

    /**
     * 로그아웃시 클리어
     */
    fun clear() {
        settingInfoData = GetSettingInfoData()
    }

}
