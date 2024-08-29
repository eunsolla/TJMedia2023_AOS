package com.verse.app.ui.mypage.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.billingclient.api.Purchase
import com.verse.app.R
import com.verse.app.base.viewmodel.ActivityViewModel
import com.verse.app.contants.AppData
import com.verse.app.contants.EtcTermsType
import com.verse.app.contants.HttpStatusType
import com.verse.app.extension.applyApiScheduler
import com.verse.app.livedata.ListLiveData
import com.verse.app.livedata.NonNullLiveData
import com.verse.app.livedata.SingleLiveEvent
import com.verse.app.model.mypage.GetDetailTermsInfoResponseData
import com.verse.app.model.mypage.SubscData
import com.verse.app.model.mypage.SubscribeList
import com.verse.app.model.param.PurchaseInfoBody
import com.verse.app.repository.http.ApiService
import com.verse.app.repository.preferences.AccountPref
import com.verse.app.utility.DLogger
import com.verse.app.utility.provider.DeviceProvider
import com.verse.app.utility.manager.LoginManager
import com.verse.app.utility.provider.ResourceProvider
import com.verse.app.widget.pagertablayout.PagerTabItem
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.kotlin.addTo
import javax.inject.Inject

@HiltViewModel
class MemberShipViewModel @Inject constructor(
    private val accountPref: AccountPref,
    val deviceProvider: DeviceProvider,
    val resourceProvider: ResourceProvider,
    val loginManager: LoginManager,
    val apiService: ApiService,

    ) : ActivityViewModel() {
    private val LOG_TAG: String = "Purchase"
    val finish = SingleLiveEvent<Boolean>()
    val backpress: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }
    val isSwipeEnable = false
    val tabList: ListLiveData<PagerTabItem> by lazy {
        ListLiveData<PagerTabItem>().apply {
            add(PagerTabItem(title = resourceProvider.getString(R.string.membership_tab_period)))
            add(PagerTabItem(title = resourceProvider.getString(R.string.membership_tab_song)))
        }
    }
    val tabPosition: MutableLiveData<Int> by lazy { MutableLiveData() }

    // 멤버십 이용권 목록
    private val _subscribeList: MutableLiveData<SubscribeList> by lazy { MutableLiveData() }
    val subscribeList: MutableLiveData<SubscribeList> get() = _subscribeList

    val requestStartSetView: MutableLiveData<SubscribeList> by lazy { MutableLiveData() }
    val requestPurchase: MutableLiveData<SubscData> by lazy { MutableLiveData() }
    val handlePurchase: MutableLiveData<Purchase> by lazy { MutableLiveData() }
    val refreshMemberShipInfo: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }

    val refreshSubscItemView: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }

    val startLoadEtcPage: SingleLiveEvent<GetDetailTermsInfoResponseData> by lazy { SingleLiveEvent() }

    val startBilling: SingleLiveEvent<MutableList<String>> by lazy { SingleLiveEvent() }

    private val _startFailPopup: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }
    val startFailPopup: LiveData<Unit> get() = _startFailPopup

    private val _isBillingState: NonNullLiveData<Boolean> by lazy { NonNullLiveData(false) }
    val isBillingState: LiveData<Boolean> get() = _isBillingState

    private val _startPurchasePopup: SingleLiveEvent<SubscData> by lazy { SingleLiveEvent() }
    val startPurchasePopup: LiveData<SubscData> get() = _startPurchasePopup



    fun start() {
        requestSubscribeList()
        DLogger.d("loginManager user nick : ${loginManager.getUserLoginData().memNk}")
    }

    /**
     * 뒤로 선택
     */
    fun back() {
        backpress.call()
    }

    /**
     * API
     */
    private fun requestSubscribeList() {
        apiService.getSubscribeList()
            .doOnSubscribe { onLoadingShow() }
            .applyApiScheduler()
            .map { setRefreshUserMemberShipInfo(it.result) }
            .doOnSuccess { res ->

                requestStartSetView.value = res

                if (_isBillingState.value) {
                    startBilling()
                }

            }.doOnError {
                _startFailPopup.call()
            }.subscribe().addTo(compositeDisposable)
    }

    /**
     * 유저 멤버십 정보 갱신
     */
    private fun setRefreshUserMemberShipInfo(subscribeList: SubscribeList): SubscribeList {
        loginManager.getUserLoginData().subscTpCd = subscribeList.subscTpCd
        loginManager.getUserLoginData().purchPrdId = subscribeList.purchPrdId
        loginManager.getUserLoginData().purchToken = subscribeList.purchToken
        loginManager.getUserLoginData().subscMngCd = subscribeList.subscMngCd
        loginManager.getUserLoginData().subscSongCount = subscribeList.subscSongCount
        loginManager.getUserLoginData().subscPeriodDt = subscribeList.subscPeriodDt
        return subscribeList
    }

    fun checkCurrentSubscribe(data: SubscData) {
        DLogger.d("moveToPurchase : ${data.subscPurcId}")
        DLogger.d("moveToPurchase : ${data.productDetails!!.productId}")
        DLogger.d("moveToPurchase subscTpCd : ${loginManager.getUserLoginData().subscTpCd}")

        requestStartSetView.value?.let {
            val isPeriod = data in it.periodSubsList
            DLogger.d("moveToPurchase isPeriod : ${isPeriod}")

            // isPeriod 기간 이용권 : true , 곡 이용권 : false
            if (isPeriod) {

                val mySubcTpCd = loginManager.getUserLoginData().subscTpCd

                if (!mySubcTpCd.isNullOrEmpty()) {

                    val result = find(it.periodSubsList,mySubcTpCd)

                    if(result){
                        //기간 보유중
                        _startPurchasePopup.value = data
                    }else{
                        //곡 보유중
                        moveToPurchase(data)
                    }

                } else {
                    //보유 중인 기간 이용권이 없으면 바로 구매
                    moveToPurchase(data)
                }

            } else {
                //곡 이용권은 바로 구매
                moveToPurchase(data)
            }
        }
    }

    fun find(values: List<SubscData>, value: String): Boolean {
        for (e in values) {
            if (e.subscTpCd == value) {
                return true
            }
        }
        return false
    }

    fun moveToPurchase(data: SubscData) {
        DLogger.d("moveToPurchase : ${data.subscPurcId}")
        DLogger.d("moveToPurchase : ${data.productDetails!!.productId}")
        if (data.isVisibility) {
            requestPurchase.value = data
        }
    }

    fun requestValidatePurchase(
        subscTpCd: String,
        purchTpCd: String,
        purchPrdId: String,
        purchToken: String,
        purchPrice: String,
        purchUnit: String,
        purchase: Purchase,
    ) {
        apiService.validatePaymentToken(
            PurchaseInfoBody(
                subscTpCd = subscTpCd,
                purchTpCd = purchTpCd,
                purchPrdId = purchPrdId,
                purchToken = purchToken,
                purchPrice = purchPrice,
                purchUnit = purchUnit,
            )
        )
            .doLoading()
            .applyApiScheduler()
            .request({
                if (it.status == HttpStatusType.SUCCESS.status) {
                    DLogger.d(LOG_TAG, "Success validatePaymentToken => ${it.message}")
                    DLogger.d(LOG_TAG, "Success validatePaymentToken => ${purchase.purchaseToken}")
                    handlePurchase.value = purchase
                    showToastStringMsg.value = it.message
                } else {
                    showToastStringMsg.value = it.message
                    DLogger.d(LOG_TAG, "Fail validatePaymentToken => ${it.message}")
                    DLogger.d(LOG_TAG, "Fail validatePaymentToken => ${purchase.purchaseToken}")
                }
            }, {
                showToastStringMsg.value = it.message
                DLogger.d(LOG_TAG, "Error validatePaymentToken => ${it.message}")
                DLogger.d(LOG_TAG, "Error validatePaymentToken => ${purchase.purchaseToken}")
            })
    }

    fun requestPurchaseCompleted(
        subscTpCd: String,
        purchTpCd: String,
        purchPrdId: String,
        purchToken: String,
        purchPrice: String,
        purchUnit: String
    ) {
        apiService.requestPurchaseCompleted(
            PurchaseInfoBody(
                subscTpCd = subscTpCd,
                purchTpCd = purchTpCd,
                purchPrdId = purchPrdId,
                purchToken = purchToken,
                purchPrice = purchPrice,
                purchUnit = purchUnit,
            )
        )
            .doLoading()
            .applyApiScheduler()
            .request({
                if (it.status == HttpStatusType.SUCCESS.status) {
                    DLogger.d(LOG_TAG, "Success requestPurchaseCompleted => ${it.message}")
                    loginManager.setUserLoginData(it.result)
                    refreshMemberShipInfo.call()
                    showToastStringMsg.value = it.message
                    start()
                } else {
                    showToastStringMsg.value = it.message
                    DLogger.d(LOG_TAG, "Fail requestPurchaseCompleted => ${it.message}")
                }
            }, {
                showToastStringMsg.value = it.message
                DLogger.d(LOG_TAG, "Error requestPurchaseCompleted => ${it.message}")
            })
    }

    /**
     * 정책 페이시 상세 정보 조회
     */
    fun requestDetailTerms(termsTpCd: EtcTermsType) {
        apiService.getDetailTermsInfo(
            ctgCode = "",
            termsTpCd = termsTpCd.code
        )
            .doLoading()
            .applyApiScheduler()
            .request({ res ->
                DLogger.d("Success Terms Detail-> ${res}")
                if (res.status == HttpStatusType.SUCCESS.status) {
                    startLoadEtcPage.value = res.result
                }
            }, {
                DLogger.d("Error requestDetailTerms Report-> ${it.message}")
            })
    }

    private fun startBilling() {
        startBilling.value = requestStartSetView.value?.purchaseProductIdList
    }

    fun setBillingState(state: Boolean) {
        _isBillingState.value = state

        requestStartSetView.value?.let {
            startBilling()
        }
    }
}