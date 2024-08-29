package com.verse.app.utility.manager

import android.app.*
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.user.UserApiClient
import com.navercorp.nid.NaverIdLoginSDK
import com.navercorp.nid.oauth.NidOAuthLogin
import com.navercorp.nid.oauth.OAuthLoginCallback
import com.verse.app.BuildConfig
import com.verse.app.R
import com.verse.app.contants.AppData
import com.verse.app.extension.getActivity
import com.verse.app.extension.isServiceRunning
import com.verse.app.extension.stopService
import com.verse.app.model.user.UserData
import com.verse.app.repository.preferences.AccountPref
import com.verse.app.ui.dialog.CommonDialog
import com.verse.app.ui.main.MainActivity
import com.verse.app.utility.DLogger
import com.verse.app.utility.SongEncodeService
import com.verse.app.utility.moveToSingAct
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Description : 사용자 이름이랑 토큰 등을 관리하는 클래스
 *
 * Created by jhlee on 2023-01-01
 */
interface LoginManager {
    fun setUserLoginData(userData: UserData)
    fun getUserLoginData(): UserData
    fun initializeGoogle(activity: Activity): GoogleSignInClient?
    fun initializeNaver()
    fun initializeKakao()
    fun isLogin(): Boolean
    fun isLoginYN(): String
    fun initLoginPreference()
    fun naverLogout()
    fun kakaoLogout()
    fun googleLogout()
    fun facebookLogout()
    fun twitterLogout()
    fun requestLogout(context: Context, loginType: String)
    fun logoutUser()
    fun logout(context: Context, isWithdraw: Boolean = false)

    /**
     * SNS Login Type
     */
    enum class LoginType(val code: String) {
        facebook(code = "AU002"),
        google(code = "AU001"),
        kakao(code = "AU004"),
        naver(code = "AU005"),
        snapchat(code = ""),
        twitter(code = "AU006"),
        none(code = "")
    }
}

class LoginManagerImpl @Inject constructor(
    @ApplicationContext
    val context: Context,
    private val accountPref: AccountPref
) : LoginManager {

    private var userData: UserData = UserData()

    override fun requestLogout(context: Context, loginType: String) {
        CommonDialog(context)
            .setContents(context.resources.getString(R.string.Would_like_to_log_out))
            .setNegativeButton(context.resources.getString(R.string.str_cancel))
            .setPositiveButton(context.resources.getString(R.string.str_confirm))
            .setListener(object : CommonDialog.Listener {
                override fun onClick(which: Int) {
                    if (which == CommonDialog.POSITIVE) {
                        logout(context)
                    }
                }

            })
            .show()
    }

    override fun facebookLogout() {
        com.facebook.login.LoginManager.getInstance().logOut()
    }

    override fun googleLogout() {
//            googleInitialize(context)
        Firebase.auth.signOut()
//        Auth.GoogleSignInApi.signOut(initializeGoogle()).setResultCallback(
//            object : ResultCallback<Status> {
//                override fun onResult(status: Status) {
//                    Toast.makeText(context, "구글 아이디 로그아웃 성공!", Toast.LENGTH_SHORT).show()
//                }
//            })
    }

    override fun kakaoLogout() {
        UserApiClient.instance.logout { error ->
            if (error != null) {
                DLogger.d("로그아웃 실패. SDK에서 토큰 삭제됨: ${error}")
            } else {
                DLogger.d("로그아웃 성공. SDK에서 토큰 삭제됨")
            }
        }
    }

    override fun naverLogout() {
        NaverIdLoginSDK.logout()
        DLogger.d("네이버 아이디 로그아웃 성공!")
    }

    override fun twitterLogout() {
        Firebase.auth.signOut()
        DLogger.d("트위터 로그아웃 성공!")
    }

    override fun initializeGoogle(activity: Activity): GoogleSignInClient? {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(BuildConfig.SNS_LOGIN_GOOGLE_REQUEST_TOKEN)
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(activity, gso)
        googleSignInClient.signOut()

        return GoogleSignIn.getClient(activity, gso)
    }

    private fun disconnectKakao() {
        UserApiClient.instance.unlink { error ->
            if (error != null) {
                DLogger.d("로그아웃 kakao->회원탈퇴 실패. SDK에서 토큰 삭제됨: ${error}")
            } else {
                DLogger.d("로그아웃 kakao->회원탈퇴 성공. SDK에서 토큰 삭제됨")
            }
        }
    }

    private fun disconnectNaver() {
        NidOAuthLogin().callDeleteTokenApi(context, object : OAuthLoginCallback {
            override fun onSuccess() {
                DLogger.d("로그아웃 naver->회원탈퇴 성공. SDK에서 토큰 삭제됨")
//                Toast.makeText(this@LoginActivity, "네이버 아이디 토큰삭제 성공!", Toast.LENGTH_SHORT).show()
            }

            override fun onFailure(httpStatus: Int, message: String) {
                Log.d("naver", "errorCode: ${NaverIdLoginSDK.getLastErrorCode().code}")
                Log.d("naver", "errorDesc: ${NaverIdLoginSDK.getLastErrorDescription()}")
            }

            override fun onError(errorCode: Int, message: String) {
                onFailure(errorCode, message)
            }
        })
    }

    /** Naver Login Module Initialize */
    override fun initializeNaver() {
        val naverClientId = BuildConfig.SNS_LOGIN_NAVER_CLIENT_ID
        val naverClientSecret = BuildConfig.SNS_LOGIN_NAVER_CLIENT_SECRET
        val naverClientName = context.getString(R.string.social_login_info_naver_client_name)
        NaverIdLoginSDK.initialize(context, naverClientId, naverClientSecret, naverClientName)
    }

    /** KakoSDK init */
    override fun initializeKakao() {
        KakaoSdk.init(context, BuildConfig.SNS_LOGIN_KAKAO_API_KEY)
    }

    override fun setUserLoginData(userData: UserData) {
        accountPref.setJWTToken(userData.jwtToken)
        accountPref.setAuthTypeCd(userData.authTpCd)
        this.userData = userData
    }

    override fun getUserLoginData(): UserData {
        return this.userData
    }

    /**
     * 사용자 정보 Get Boolean
     */
    override fun isLogin(): Boolean {
        var result: Boolean = false

        if (!userData.memNk.isEmpty() && !userData.memCd.isEmpty() && !accountPref.getJWTToken()
                .isEmpty()
        ) {
            result = true
        } else {
            initLoginPreference()
            result = false
        }

        return result
    }

    /**
     * 사용자 정보 Get Y/N
     */
    override fun isLoginYN(): String {
        return if (isLogin()) {
            AppData.Y_VALUE
        } else {
            AppData.N_VALUE
        }
    }

    override fun initLoginPreference() {
        accountPref.setJWTToken("")
        accountPref.setAuthTypeCd("")
    }

    override fun logout(context: Context, isWithdraw: Boolean) {
        when (accountPref.getAuthTypeCd()) {

            LoginManager.LoginType.facebook.code -> {
                facebookLogout()
            }

            LoginManager.LoginType.google.code -> {
                googleLogout()
            }

            LoginManager.LoginType.kakao.code -> {
                kakaoLogout()
                if (isWithdraw) {
                    disconnectKakao()
                }
            }

            LoginManager.LoginType.naver.code -> {
                naverLogout()
                if (isWithdraw) {
                    disconnectNaver()
                }
            }

            LoginManager.LoginType.twitter.code -> {
                twitterLogout()
            }
        }
        logoutUser()
        UserFollowManager.clear()
        UserFeedLikeManager.clear()
        UserFeedBookmarkManager.clear()
        UserFeedBlockManager.clear()
        UserFeedInterestManager.clear()
        UserFeedDeleteManager.clear()
        UserSettingManager.clear()
        FirebaseMessaging.getInstance().deleteToken()

        //인코딩 중이면 서비스 정지
        if (context.isServiceRunning(SongEncodeService::class.java)) {
            AppData.IS_ENCODE_ING = false
            AppData.IS_REUPLOAD_ING = false
            context.stopService(SongEncodeService::class.java)
        }

        moveToMain(context)
    }

    /**
     * 로그아웃 후 메인 이동 및 배지 count 삭제
     */
    private fun moveToMain(context: Context) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        }

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.cancelAll()

        context.startActivity(intent)
        context.getActivity()
            ?.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    override fun logoutUser() {
        initLoginPreference()
        this.userData = UserData()
    }
}