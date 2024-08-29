package com.verse.app.ui.mypage.activity

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.core.text.HtmlCompat
import androidx.databinding.library.baseAdapters.BR
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.verse.app.R
import com.verse.app.base.activity.BaseActivity
import com.verse.app.contants.AppData
import com.verse.app.contants.MemberShipType
import com.verse.app.databinding.ActivityMembershipBinding
import com.verse.app.extension.getActivity
import com.verse.app.extension.onMain
import com.verse.app.extension.startAct
import com.verse.app.model.mypage.SubscData
import com.verse.app.model.mypage.SubscribeList
import com.verse.app.ui.dialog.CommonDialog
import com.verse.app.ui.mypage.fragment.MemberShipPeriodFragment
import com.verse.app.ui.mypage.fragment.MemberShipSongFragment
import com.verse.app.ui.mypage.viewmodel.MemberShipViewModel
import com.verse.app.ui.webview.WebViewActivity
import com.verse.app.utility.BillingCallBack
import com.verse.app.utility.BillingManager
import com.verse.app.utility.DLogger
import com.verse.app.utility.RxBus
import com.verse.app.utility.RxBusEvent
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MemberShipActivity :
    BaseActivity<ActivityMembershipBinding, MemberShipViewModel>() {
    override val layoutId = R.layout.activity_membership
    override val viewModel: MemberShipViewModel by viewModels()
    override val bindingVariable = BR.viewModel
    private val adapter: MemberShipActivity.PagerAdapter by lazy { PagerAdapter() }
    private var billingManager: BillingManager? = null
    private var productDetails: ProductDetails? = null

    // membership content
    private var memberShipText1 = ""
    private var memberShipText2 = ""

    private val LOG_TAG: String = "Purchase"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.deviceProvider.setDeviceStatusBarColor(getActivity()!!.window, R.color.white)

        billingManager = BillingManager(this, billingCallBack = billingCallBack)

        with(binding) {
            vp.adapter = adapter
        }

        with(viewModel) {

            requestStartSetView.observe(this@MemberShipActivity) {

                refreshMemberShipInfo(it)

                binding.vp.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                    override fun onPageSelected(position: Int) {
                        super.onPageSelected(position)
                        val vpFragment = supportFragmentManager.findFragmentByTag("f" + adapter.getItemId(position))
                        vpFragment?.view?.let {
                            updatePagerHeightForChild(it, binding.vp)
                        }
                    }
                })
            }

            startBilling.observe(this@MemberShipActivity) {
                DLogger.d("onBillingConnected aa")
                if(it.isEmpty()){
                    showFailPurchaseDialog()
                }else{
                    DLogger.d("onBillingConnected bb")
                    billingManager!!.getProductDetails(it)
                }
            }

            requestPurchase.observe(this@MemberShipActivity) {
                if (billingManager != null) {
                    it.productDetails!!.let {
                        billingManager!!.purchaseProduct(it)
                    }
                }
            }

            handlePurchase.observe(this@MemberShipActivity) {
                if (billingManager != null) {
                    billingManager!!.handlePurchase(it)
                }
            }

            startPurchasePopup.observe(this@MemberShipActivity){
                showPurchaseDialog(it)
            }

            refreshMemberShipInfo.observe(this@MemberShipActivity) {
//                refreshMemberShipInfo()
//                refreshSubscItemView.call()
                //부르기 화면 -> 이용권 구매 -> 갱신 call
                RxBus.publish(RxBusEvent.RefreshDataEvent())
            }

            backpress.observe(this@MemberShipActivity) {
                finish()
            }

            startLoadEtcPage.observe(this@MemberShipActivity) {
                startAct<WebViewActivity> {
                    putExtra(
                        WebViewActivity.WEB_VIEW_INTENT_DATA_TYPE,
                        WebViewActivity.WEB_VIEW_INTENT_VALUE_CONTENTS
                    )
                    putExtra(
                        WebViewActivity.WEB_VIEW_INTENT_TITLE,
                        it.bctgMngNm
                    )
                    putExtra(WebViewActivity.WEB_VIEW_INTENT_DATA, it.termsContent)
                }
            }

            startFailPopup.observe(this@MemberShipActivity) {
                showFailPurchaseDialog()
            }

            viewModel.start()
        }

    }

    private fun updatePagerHeightForChild(view: View, pager: ViewPager2) {
        view.post {
            val wMeasureSpec = View.MeasureSpec.makeMeasureSpec(view.width, View.MeasureSpec.EXACTLY)
            val hMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            view.measure(wMeasureSpec, hMeasureSpec)

            if (pager.layoutParams.height != view.measuredHeight) {
                pager.layoutParams = (pager.layoutParams)
                    .also { lp ->
                        lp.height = view.measuredHeight
                    }
            }

            pager.invalidate()
        }
    }

    inner class PagerAdapter : FragmentStateAdapter(this) {
        override fun getItemCount(): Int {
            return 2
        }

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> MemberShipPeriodFragment.newInstance()
                else -> MemberShipSongFragment.newInstance()
            }
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.out_left_to_right, R.anim.in_left_to_right)
    }

    private var billingCallBack = object : BillingCallBack {

        override fun onBillingConnected() {
            onMain {
                viewModel.setBillingState(true)
            }
        }

        override fun onBillingFailConnected(billingResult: BillingResult) {
            onMain {
                DLogger.d("onBillingConnected fail")
                showFailPurchaseDialog()
            }
        }

        override fun onPurchasesUpdated(
            billingResult: BillingResult,
            purchases: List<Purchase>?
        ) {
        }

        override fun onProductDetails(
            billingResult: BillingResult,
            productDetails: List<ProductDetails>
        ) {
            DLogger.d("onBillingConnected ff")
            if (productDetails.isEmpty()) {
                DLogger.d("onBillingConnected gg")
                showFailPurchaseDialog()
                return
            }

            productDetails.forEach { productDetails ->
                viewModel.requestStartSetView.value!!.songSubsList.forEach {
                    if (it.subscPurcId == productDetails.productId) {
                        var price = productDetails.oneTimePurchaseOfferDetails!!.formattedPrice
                        var priceFormat = price.substring(0, 1) + " " + price.substring(1)
                        it.subscPrice = priceFormat
                        it.productDetails = productDetails
                    }
                }

                viewModel.requestStartSetView.value!!.periodSubsList.forEach {
                    if (it.subscPurcId == productDetails.productId) {
                        var price = productDetails.oneTimePurchaseOfferDetails!!.formattedPrice
                        var priceFormat = price.substring(0, 1) + " " + price.substring(1)
                        it.subscPrice = priceFormat
                        it.productDetails = productDetails
                        it.isVisibility = isVisibilityProduct(it.subscTpCd)
                    }
                }

                DLogger.d("onBillingConnected hhh")
                viewModel.subscribeList.postValue(viewModel.requestStartSetView.value)
                viewModel.onLoadingDismiss()
            }
        }

        override fun onValidatePurchase(purchases: List<Purchase>?) {
//            DLogger.d(LOG_TAG, "onValidatePurchase")

            if (billingManager != null) {
                if (purchases != null) {
//                    DLogger.d(LOG_TAG, "onValidatePurchase purchases size : ${purchases!!.size}")

                    for (purchase in purchases) {
                        if (viewModel.requestPurchase.value != null) {
                            viewModel.requestValidatePurchase(
                                viewModel.requestPurchase.value!!.subscTpCd,
                                "PR001",
                                viewModel.requestPurchase.value!!.productDetails!!.productId,
                                purchase.purchaseToken,
                                (viewModel.requestPurchase.value!!.productDetails!!.oneTimePurchaseOfferDetails!!.priceAmountMicros / 1000000).toString(),
                                viewModel.requestPurchase.value!!.productDetails!!.oneTimePurchaseOfferDetails!!.priceCurrencyCode,
                                purchase
                            )
                        }
                    }
                }
            }
        }

        override fun onPurchaseCompleted(billingResult: BillingResult, purchaseToken: String) {

//            DLogger.d(LOG_TAG, "oPurchaseCompleted billingResult : ${billingResult.responseCode}")
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                if (viewModel.requestPurchase.value != null) {
                    viewModel.requestPurchaseCompleted(
                        viewModel.requestPurchase.value!!.subscTpCd,
                        "PR001",
                        viewModel.requestPurchase.value!!.productDetails!!.productId,
                        purchaseToken,
                        (viewModel.requestPurchase.value!!.productDetails!!.oneTimePurchaseOfferDetails!!.priceAmountMicros / 1000000).toString(),
                        viewModel.requestPurchase.value!!.productDetails!!.oneTimePurchaseOfferDetails!!.priceCurrencyCode
                    )
                }
            }
        }
    }

    fun showFailPurchaseDialog() {
        viewModel.onLoadingDismiss()
        CommonDialog(this@MemberShipActivity)
            .setContents(getString(R.string.membership_fail_load_purchase_info))
            .setPositiveButton(getString(R.string.str_confirm))
            .setIcon(AppData.POPUP_WARNING)
            .setListener(object : CommonDialog.Listener {
                override fun onClick(which: Int) {
                    if (which == CommonDialog.POSITIVE) {
                        finish()
                    }
                }
            })
            .show()
    }

    private fun showPurchaseDialog(subscData: SubscData) {

        CommonDialog(this@MemberShipActivity)
            .setContents(getString(R.string.membership_buy_ing_message,subscData.subscTpNm))
            .setNegativeButton(getString(R.string.str_cancel))
            .setPositiveButton(getString(R.string.membership_additional_purchases))
            .setListener(object : CommonDialog.Listener {
                override fun onClick(which: Int) {
                    if (which == CommonDialog.POSITIVE) {
                        viewModel.moveToPurchase(subscData)
                    }
                }
            })
            .show()
    }

    private fun refreshMemberShipInfo(subscribeList: SubscribeList) {

        if (subscribeList == null) {
            return
        }

        //viewModel.loginManager.getUserLoginData().subscTpCd = MemberShipType.SC009.code

        if (viewModel.loginManager.getUserLoginData().subscTpCd.isEmpty()) {
            binding.tvMembershipEmptyContents.visibility = View.VISIBLE
            binding.tvMembershipContents.visibility = View.GONE
            binding.tvMembershipInfo.visibility = View.GONE
        } else {
            binding.tvMembershipEmptyContents.visibility = View.GONE
            binding.tvMembershipContents.visibility = View.VISIBLE
            binding.tvMembershipInfo.visibility = View.VISIBLE

            if(subscribeList.subscTpCd == MemberShipType.SC009.code){
                setContenUseTicketType(subscribeList.subscTpCd)

                if(subscribeList.subscPeriodDt.isNotEmpty()){
                    binding.tvMembershipInfo.text = HtmlCompat.fromHtml(
                        java.lang.String.format(
                            getString(R.string.membership_period_date),
                            subscribeList.subscPeriodDt
                        ),
                        HtmlCompat.FROM_HTML_MODE_LEGACY
                    )
                }

            }else{
                subscribeList.songSubsList.forEachIndexed { index, subscData ->
                    if (subscribeList.subscTpCd == subscData.subscTpCd) {
                        // 이용권 문구 세팅
                        setContenUseTicketType(subscData.subscTpCd)

                        binding.tvMembershipInfo.text = HtmlCompat.fromHtml(
                            java.lang.String.format(
                                getString(R.string.membership_song_count),
                                subscribeList.subscSongCount
                            ),
                            HtmlCompat.FROM_HTML_MODE_LEGACY
                        )
                    }
                }
            }

            if (memberShipText1.isEmpty() && memberShipText2.isEmpty()) {

                subscribeList.periodSubsList.forEachIndexed { index, subscData ->
                    if (subscribeList.subscTpCd == subscData.subscTpCd) {
                        // 이용권 문구 세팅
                        setContenUseTicketType(subscData.subscTpCd)

                        binding.tvMembershipInfo.text = HtmlCompat.fromHtml(
                            java.lang.String.format(
                                getString(R.string.membership_period_date),
                                subscribeList.subscPeriodDt
                            ),
                            HtmlCompat.FROM_HTML_MODE_LEGACY
                        )

                        subscribeList.periodSubsList[index].isVisibility = isVisibilityProduct(subscData.subscTpCd)
                    }
                }
            }
        }

    }

    private fun isVisibilityProduct(subscTpCd: String): Boolean {
        return true
// 2023-07-21 무조건 true 로 변경        
//        var myGrade: Int = -1
//
//        if (viewModel.loginManager.getUserLoginData().subscTpCd.isNotEmpty()) {
//            myGrade = MemberShipType.valueOf(viewModel.loginManager.getUserLoginData().subscTpCd).grade
//        }
//
//        return MemberShipType.valueOf(subscTpCd).grade > myGrade
    }

    private fun setContenUseTicketType(contentType:String){

        when (contentType) {
            MemberShipType.SC001.code -> {
                memberShipText1 = ""
                memberShipText2 = getString(R.string.memberShip_contents_sc)
            }
            MemberShipType.SC002.code -> {
                memberShipText1 = ""
                memberShipText2 = getString(R.string.memberShip_contents_sc)
            }
            MemberShipType.SC003.code -> {
                memberShipText1 = ""
                memberShipText2 = getString(R.string.memberShip_contents_sc)
            }
            MemberShipType.SC004.code -> {
                memberShipText1 = getString(R.string.memberShip_contents_sc004)
                memberShipText2 = getString(R.string.memberShip_contents_monthly_pass)
            }
            MemberShipType.SC005.code -> {
                memberShipText1 = getString(R.string.memberShip_contents_sc005)
                memberShipText2 = getString(R.string.memberShip_contents_useVipTicket)
            }
            MemberShipType.SC006.code -> {
                memberShipText1 = getString(R.string.memberShip_contents_sc006)
                memberShipText2 = getString(R.string.memberShip_contents_useVipTicket)
            }
            MemberShipType.SC007.code -> {
                memberShipText1 = getString(R.string.memberShip_contents_sc007)
                memberShipText2 = getString(R.string.memberShip_contents_useVipTicket)
            }
            MemberShipType.SC008.code -> {
                memberShipText1 = getString(R.string.memberShip_contents_sc008)
                memberShipText2 = getString(R.string.memberShip_contents_useVipTicket)
            }
            MemberShipType.SC009.code -> {
                memberShipText1 = ""
                memberShipText2 = getString(R.string.memberShip_contents_sc009)
            }

        }

        binding.tvMembershipContents.text = HtmlCompat.fromHtml(
            java.lang.String.format(
                getString(R.string.membership_notice),
                memberShipText1, memberShipText2
            ),
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )

    }

    override fun onDestroy() {

        super.onDestroy()
    }
}