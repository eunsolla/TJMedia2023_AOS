package com.verse.app.ui.report

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import com.verse.app.base.viewmodel.ActivityViewModel
import com.verse.app.contants.AppData
import com.verse.app.contants.ExtraCode
import com.verse.app.contants.HttpStatusType
import com.verse.app.contants.ReportType
import com.verse.app.extension.applyApiScheduler
import com.verse.app.extension.multiNullCheck
import com.verse.app.livedata.ListLiveData
import com.verse.app.livedata.SingleLiveEvent
import com.verse.app.model.param.ReportParam
import com.verse.app.model.report.ReportData
import com.verse.app.model.report.ReportSubData
import com.verse.app.repository.http.ApiService
import com.verse.app.utility.DLogger
import com.verse.app.utility.provider.DeviceProvider
import com.verse.app.utility.manager.LoginManager
import com.verse.app.utility.provider.ResourceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Description : 신고하기 ViewModel Class
 *
 * Created by jhlee on 2023-04-21
 */
@HiltViewModel
class ReportViewModel @Inject constructor(
    val apiService: ApiService,
    val resourceProvider: ResourceProvider,
    val loginManager: LoginManager,
    val deviceProvider: DeviceProvider,
    savedStateHandle: SavedStateHandle,
) : ActivityViewModel() {

    // first = USER(code = "RP001") , second = target Cd
    // first = FEED(code = "RP002") , second = target Cd
    // first = REPLY(code = "RP003") , second = target Cd
    // first = RE_REPLY(code = "RP004") , second = target Cd
    // first = LOUNGE(code = "RP005" , second = target Cd
    val reportParam = savedStateHandle.getLiveData<Pair<ReportType, String>>(ExtraCode.REPORT_CODE)  //받은 값 .

    private val _reportData: ListLiveData<ReportData> by lazy { ListLiveData() }
    val reportData: ListLiveData<ReportData> get() = _reportData

    private val _reportSubData: ListLiveData<ReportSubData> by lazy { ListLiveData() }
    val reportSubData: ListLiveData<ReportSubData> get() = _reportSubData

    private val _subTitle: MutableLiveData<String> by lazy { MutableLiveData() }
    val subTitle: MutableLiveData<String> get() = _subTitle
    val resultString: SingleLiveEvent<String> by lazy { SingleLiveEvent() }

    val startOneButtonPopup: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }      // popup

    /**
     * API - 신고 목록
     */
    fun requestReportList() {

        apiService.fetchReportCategoryInfo()
            .doLoading()
            .applyApiScheduler()
            .request({ res ->
                _reportData.value = res.result.dataList
            }, {
                DLogger.d("Error ReportResponse ${it}")
            })
    }

    fun onMoveToSubReport(data: ReportData) {
        if (data.subDataList.isEmpty()){
            requestReport(data)
        } else {
            _subTitle.value = data.repCtgMngNm
            _reportSubData.value = data.subDataList
        }
    }

    /**
     * 신고하기
     */
    fun requestSubReport(reportSubData: ReportSubData) {

        multiNullCheck(reportParam.value, reportSubData) { param, data ->
            apiService.requestReport(ReportParam(
                repTpCd = param.first.code,                 //신고유형(RP001:사용자신고 / RP002:피드콘텐츠신고 / RP003:댓글신고 / RP004:답글신고 / RP005:라운지)
                repMngCd = data.repCtgMngCd,            //선택 신고하기 카테고리관리코드
                repConCd = param.second,                   // 사용자 고유값/콘텐츠 고유값/댓글 고유값/답글 고유값/라운지 고유값
                fgLoginYn = loginManager.isLoginYN()
            )).applyApiScheduler()
                .doLoading()
                .request({ res ->

                    if (res.status == HttpStatusType.SUCCESS.status) {
                        startOneButtonPopup.call()
                    }else{
                        resultString.value = res.message
                    }
                    DLogger.d("Success Report-> $res")
                }, {
                    DLogger.d("Error Report-> ${it.message}")
                })
        } ?: run {
        }
    }

    /**
     * 신고하기
     */
    fun requestReport(data:ReportData) {

        if (reportParam.value != null) {

            apiService.requestReport(ReportParam(
                repTpCd = reportParam.value!!.first.code,                 //신고유형(RP001:사용자신고 / RP002:피드콘텐츠신고 / RP003:댓글신고 / RP004:답글신고 / RP005:라운지)
                repMngCd = data.repCtgMngCd,            //선택 신고하기 카테고리관리코드
                repConCd = reportParam.value!!.second,                   // 사용자 고유값/콘텐츠 고유값/댓글 고유값/답글 고유값/라운지 고유값
                fgLoginYn = loginManager.isLoginYN()
            )).applyApiScheduler()
                .doLoading()
                .request({ res ->

                    if (res.status == HttpStatusType.SUCCESS.status) {
                        startOneButtonPopup.call()
                    }else{
                        resultString.value = res.message
                    }
                    DLogger.d("Success Report-> $res")
                }, {
                    DLogger.d("Error Report-> ${it.message}")
                })
        }
    }

}