package com.verse.app.utility

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.ConsumeResponseListener
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryProductDetailsParams.Product
import com.android.billingclient.api.QueryPurchaseHistoryParams
import com.android.billingclient.api.queryPurchaseHistory
import com.verse.app.base.activity.BaseActivity
import com.verse.app.databinding.ActivityMembershipBinding
import com.verse.app.ui.mypage.viewmodel.MemberShipViewModel

/**
 * 결제 Callback 인터페이스
 */
interface BillingCallBack {
    fun onBillingConnected()
    fun onBillingFailConnected(billingResult: BillingResult)
    fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?)
    fun onProductDetails(billingResult: BillingResult, productDetails: List<ProductDetails>)
    fun onValidatePurchase(purchases: List<Purchase>?)
    fun onPurchaseCompleted(billingResult: BillingResult, purchaseToken: String)
}

class BillingManager(
    private val activity: BaseActivity<ActivityMembershipBinding, MemberShipViewModel>,
    private val billingCallBack: BillingCallBack
) : PurchasesUpdatedListener, BillingClientStateListener, ConsumeResponseListener {
    private val LOG_TAG: String = "Purchase"
    val maxTries = 3
    var tries = 1
    var isConnectionEstablished = false

    private val billingClient = BillingClient.newBuilder(activity)
        .setListener(this)
        .enablePendingPurchases()
        .build()

    init {
        billingClient.startConnection(this)
    }

    private fun retryBillingServiceConnection() {
        do {
            try {
                billingClient.startConnection(this)
            } catch (e: Exception) {
                e.message?.let { DLogger.d(it) }
                tries++
            }
        } while (tries <= maxTries && !isConnectionEstablished)
    }

    override fun onBillingServiceDisconnected() {
        DLogger.d(LOG_TAG, "onBillingServiceDisconnected")
        retryBillingServiceConnection()
    }

    override fun onBillingSetupFinished(billingResult: BillingResult) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            DLogger.d(LOG_TAG, "onBillingSetupFinished BillingResponseCode.OK")
            isConnectionEstablished = true
            billingCallBack.onBillingConnected()
        } else {
            DLogger.d(LOG_TAG, "onBillingSetupFinished BillingResponseCode.Fail")
            billingCallBack.onBillingFailConnected(billingResult)
//            retryBillingServiceConnection()
        }
    }

    /**
     * 등록 된 상품 정보 조회
     * @param productId 상품 ID
     */
    fun getProductDetails(productIdList: List<String>) {
        val productIds: ArrayList<Product> = ArrayList()

        for (i in productIdList.indices) {
            if (productIdList[i] != "vp_coupon") {
                productIds.add(
                    Product.newBuilder()
                        .setProductId(productIdList[i])
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build())
            }
        }

        val queryProductDetailsParams = QueryProductDetailsParams.newBuilder()
            .setProductList(productIds).build()

        billingClient.queryProductDetailsAsync(queryProductDetailsParams) { billingResult,  productDetailsList ->
            // check billingResult
            // process returned productDetailsList
            billingCallBack.onProductDetails(billingResult, productDetailsList)
        }
    }

    /**
     * 구매 시도
     * @param productDetail ProductDetails 구매 할 상품
     */
    fun purchaseProduct(productDetail: ProductDetails) {
        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                // retrieve a value for "productDetails" by calling queryProductDetailsAsync()
                .setProductDetails(productDetail)
                // to get an offer token, call ProductDetails.subscriptionOfferDetails()
                // for a list of offers that are available to the user
                //.setOfferToken(productDetail.zzb.getString("skuDetailsToken"))
                .build()
        )

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        // Launch the billing flow
        val billingResult = billingClient.launchBillingFlow(activity, billingFlowParams)
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            billingCallBack.onValidatePurchase(purchases)
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {

        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            // Handle an error caused by a user cancelling the purchase flow.
        } else {
            // Handle any other error codes.
        }
    }

    /**
     * 구매 확정 처리(상품 소비 처리)
     * @param purchase Purchase 결제 정보
     */
    fun handlePurchase(purchase: Purchase) {
        // Purchase retrieved from BillingClient#queryPurchasesAsync or your PurchasesUpdatedListener.
        //val purchase : Purchase = ...
        // Verify the purchase.
        // Ensure entitlement was not already granted for this purchaseToken.
        // Grant entitlement to the user.

        val consumeParams =
            ConsumeParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()

        billingClient.consumeAsync(consumeParams, this)
    }

    override fun onConsumeResponse(p0: BillingResult, p1: String) {
        DLogger.d(LOG_TAG, "consumeResult.billingResult.responseCode : ${p0.responseCode}")
        DLogger.d(LOG_TAG, "consumeResult.billingResult.p1 : ${p1}")
        billingCallBack.onPurchaseCompleted(p0, p1)
    }

    suspend fun validPurchaseInfo() {
        val params = QueryPurchaseHistoryParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        // uses queryPurchasesAsync Kotlin extension function
        val purchasesResult = billingClient.queryPurchaseHistory(params)

        if (purchasesResult.purchaseHistoryRecordList != null) {
            purchasesResult.purchaseHistoryRecordList!!.forEach {
                DLogger.d(LOG_TAG, "validPurchaseInfo : ${it.products} / ${it.purchaseToken} / ${it.purchaseTime}")
            }
        }
        // check purchasesResult.billingResult
        // process returned purchasesResult.purchasesList, e.g. display the plans user owns
    }
}