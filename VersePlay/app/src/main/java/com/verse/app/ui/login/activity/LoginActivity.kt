package com.verse.app.ui.login.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.GraphRequest
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.OAuthCredential
import com.google.firebase.auth.OAuthProvider
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.common.util.Utility
import com.kakao.sdk.user.UserApiClient
import com.navercorp.nid.NaverIdLoginSDK
import com.navercorp.nid.oauth.NidOAuthLogin
import com.navercorp.nid.oauth.OAuthLoginCallback
import com.navercorp.nid.profile.NidProfileCallback
import com.navercorp.nid.profile.data.NidProfileResponse
import com.verse.app.BR
import com.verse.app.R
import com.verse.app.base.activity.BaseActivity
import com.verse.app.contants.*
import com.verse.app.databinding.ActivityLoginBinding
import com.verse.app.extension.getActivity
import com.verse.app.extension.startAct
import com.verse.app.ui.dialog.CommonDialog
import com.verse.app.ui.login.viewmodel.LoginViewModel
import com.verse.app.ui.webview.WebViewActivity
import com.verse.app.utility.*
import com.verse.app.utility.manager.LoginManager
import dagger.hilt.android.AndroidEntryPoint
import java.util.*


@Suppress("DEPRECATION")
@AndroidEntryPoint
class LoginActivity : BaseActivity<ActivityLoginBinding, LoginViewModel>(),
    GoogleApiClient.OnConnectionFailedListener {

    override val layoutId = R.layout.activity_login
    override val viewModel: LoginViewModel by viewModels()
    override val bindingVariable = BR.viewModel

    private val RC_SIGN_IN = 9001

    //Facebook
    private lateinit var callbackManager: CallbackManager

    private var mGoogleSignInClient: GoogleSignInClient? = null
    private var mAuth: FirebaseAuth? = null

    val REQUEST_ACCOUNT_AUTHORIZATION = 12

    var tempNickName: String = ""
    var tempEmail: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.deviceProvider.setDeviceStatusBarColor(getActivity()!!.window, R.color.white)

        viewModel.loginManager.initLoginPreference()

        with(viewModel) {

            binding.apply {
                if (accountPref.getPreferenceLocaleLanguage() == NationLanType.KO.code) {
                    binding.tvPrivacyPolicy2.visibility = View.GONE
                    binding.tvPrivacyPolicy.visibility = View.VISIBLE
                } else {
                    binding.tvPrivacyPolicy2.visibility = View.VISIBLE
                    binding.tvPrivacyPolicy.visibility = View.GONE
                }
            }

            startMain.observe(this@LoginActivity) {
                super.finish()
                RxBus.publish(RxBusEvent.MainEnterEvent(MainEntryType.VERSION, true))
            }

            startMoveNickname.observe(this@LoginActivity) {
                super.finish()
                startAct<NicknameActivity>(
                    enterAni = android.R.anim.fade_in, exitAni = android.R.anim.fade_out
                ) {
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }

            //유저 상태에 따라 화면 이동 처리
            memStCd.observe(this@LoginActivity) {
                if (it.isNotEmpty()) {
                    moveToUserStAct(this@LoginActivity, viewModel.accountPref, viewModel.loginManager, false, it, tempNickName, tempEmail)
                }
            }

            showOneButtonDialogPopup.observe(this@LoginActivity) {
                CommonDialog(this@LoginActivity)
                    .setContents(it)
                    .setPositiveButton(getResources().getString(R.string.str_confirm))
                    .setIcon(AppData.POPUP_WARNING)
                    .setListener(object : CommonDialog.Listener {
                        override fun onClick(which: Int) {
                            if (which == CommonDialog.POSITIVE) {
                            }
                        }
                    })
                    .show()
            }

            backpress.observe(this@LoginActivity) {
                finish()
            }

            startLoadEtcPage.observe(this@LoginActivity) {
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
        }

        // 국가 설정에 따른 로그인 버튼 초기화
        initLoginUI()

    }

    override fun onDestroy() {
        super.onDestroy()
    }

    fun initLoginUI() {
        if (viewModel.accountPref.isPreferenceLocaleCountry() == NationLanType.KR.code) {
            binding.btnGoogle.visibility = View.VISIBLE
            binding.btnKakao.visibility = View.VISIBLE
            binding.btnNaver.visibility = View.VISIBLE
            binding.btnTwitter.visibility = View.GONE
            binding.btnFacebook.visibility = View.GONE

            /** Google init */
            mGoogleSignInClient = viewModel.loginManager.initializeGoogle(this)
            binding.btnGoogle.setOnClickListener {
                viewModel.accountPref.setAuthTypeCd(LoginManager.LoginType.google.code)
                googleSignIn()
            }

            /** Kako Login */
            binding.btnKakao.setOnClickListener {
                KakaoSdk.loggingEnabled
                viewModel.accountPref.setAuthTypeCd(LoginManager.LoginType.kakao.code)
                kakaoLogin()
            }

            /** Naver Login  */
            binding.btnNaver.setOnClickListener {
                viewModel.accountPref.setAuthTypeCd(LoginManager.LoginType.naver.code)
                startNaverLogin()
            }
        } else {
            binding.btnGoogle.visibility = View.VISIBLE
            binding.btnKakao.visibility = View.GONE
            binding.btnNaver.visibility = View.GONE
            binding.btnTwitter.visibility = View.VISIBLE
            binding.btnFacebook.visibility = View.VISIBLE

            /** Google init */
            mGoogleSignInClient = viewModel.loginManager.initializeGoogle(this)
            binding.btnGoogle.setOnClickListener {
                viewModel.accountPref.setAuthTypeCd(LoginManager.LoginType.google.code)
                googleSignIn()
            }

            /** Twitter Login Module Initialize */
            binding.btnTwitter.setOnClickListener {
                viewModel.accountPref.setAuthTypeCd(LoginManager.LoginType.twitter.code)
                twitterLogin()
            }


            /** Facebook-Login */
            callbackManager = CallbackManager.Factory.create() //로그인 응답 처리할 CallbackManager

            binding.btnFacebookLogin.setPermissions(
                Arrays.asList<String>(
                    "public_profile",
                    "email"
                )
            )

            binding.btnFacebook.setOnClickListener(View.OnClickListener {
                viewModel.accountPref.setAuthTypeCd(LoginManager.LoginType.facebook.code)

                val accessToken = AccessToken.getCurrentAccessToken()
                val isLoggedIn = accessToken != null && !accessToken.isExpired

                if (isLoggedIn) {
                    requestMe(accessToken!!)

                    viewModel.requestTokenLogin(
                        LoginManager.LoginType.facebook,
                        accessToken!!.token
                    )
                } else {
                    // facebook
                    binding.btnFacebookLogin.isSoundEffectsEnabled = false
                    binding.btnFacebookLogin.performClick()
                }
            })
        }

//        binding.tvTerms.setOnClickListener(View.OnClickListener {
//            viewModel.requestDetailTerms(EtcTermsType.LOGIN_AGREE.code)
//        })
//
//        binding.tvPrivacyPolicy.setOnClickListener(View.OnClickListener {
//            viewModel.requestDetailTerms(EtcTermsType.LOGIN_PERSONAL.code)
//        })
    }

    fun requestMe(accessToken: AccessToken) {
        val request = GraphRequest.newMeRequest(accessToken) { `object`, response ->
            try {
                `object`?.let {
                    tempEmail = it.getString("email")
                }
                DLogger.d("Facebook tempEmail : ${tempEmail}")

                `object`?.let {
                    tempNickName = it.getString("name")
                }
                DLogger.d("Facebook tempNickName : ${tempNickName}")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val parameters = Bundle()
        parameters.putString("fields", "name,email,picture")
        request.parameters = parameters
        request.executeAsync()

    }

    // Twitter SNS Login
    private fun twitterLogin() {
        val provider = OAuthProvider.newBuilder("twitter.com")
        val firebaseAuth = FirebaseAuth.getInstance()

        firebaseAuth
            .startActivityForSignInWithProvider(this, provider.build())
            .addOnSuccessListener { // User is signed in.
                val credential = it.credential as? OAuthCredential
                val accessToken = credential?.accessToken
                val secret = credential?.secret

                DLogger.d("Twitter NickName =>>>  ${it.user!!.displayName}")
                DLogger.d("Twitter Email =>>>  ${it.user!!.email}")

                it.user?.displayName?.let {
                    tempNickName = it
                }

                it.user?.email?.let {
                    tempEmail = it
                }

                DLogger.d("twitterLogin accessToken =>>>  ${accessToken}")
                viewModel.requestTokenLogin(LoginManager.LoginType.twitter, accessToken.toString())
            }
            .addOnFailureListener {
                DLogger.d("twitterLogin accessToken ff =>>>  " + it)
                // Handle failure.
            }
    }

    // Google SMS Login
    fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        val scope = "oauth2:" + Scopes.EMAIL + " " + Scopes.PROFILE
        GetTokenTask(
            this@LoginActivity,
            acct.account,
            scope,
            REQUEST_ACCOUNT_AUTHORIZATION
        ).execute()
    }

    var activityResultLauncher: ActivityResultLauncher<Intent?>? = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // google
        if (result.getResultCode() === RESULT_OK) {
            val task =
                GoogleSignIn.getSignedInAccountFromIntent(result.getData())
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account =
                    task.getResult(ApiException::class.java)
                if (account != null) {
                    DLogger.d("account.idToken ==> ${account.idToken}")
                    DLogger.d("account.idToken ==> ${account.email}")
                    DLogger.d("account.idToken ==> ${account.displayName}")

                    tempNickName = account.displayName!!
                    tempEmail = account.email!!

                    firebaseAuthWithGoogle(account)
                }
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
            }
        }
    }

    fun onTokenReceived(token: String) {
        DLogger.d("onTokenReceived > token ==> ${token}")
        viewModel.requestTokenLogin(LoginManager.LoginType.google, token)
    }

    fun googleSignIn() {
        val signInIntent = mGoogleSignInClient!!.signInIntent
        activityResultLauncher?.launch(signInIntent)
    }

    // kakao
    private fun kakaoLogin() {
        var keyHash = Utility.getKeyHash(this)
        DLogger.d("Hash Key : ${keyHash}")
        // 카카오계정으로 로그인 공통 callback 구성
        // 카카오톡으로 로그인 할 수 없어 카카오계정으로 로그인할 경우 사용됨
        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            if (error != null) {
                DLogger.d("카카오계정으로 로그인 실패 : ${error}")
            } else if (token != null) {
                //TODO: 최종적으로 카카오로그인 및 유저정보 가져온 결과
                UserApiClient.instance.me { user, error ->
                    DLogger.d("카카오계정으로 로그인 성공 \n\n " + "token: ${token.accessToken} \n\n " + "me: ${user}")
                    user?.kakaoAccount?.profile?.nickname?.let {
                        DLogger.d("nickname : ${it}")
                        tempNickName = it
                    }

                    user?.kakaoAccount?.email?.let {
                        DLogger.d("email : ${it}")
                        tempEmail = it
                    }
                }

                //viewModel.accountPref.setTempRegisterNickName(user.)
                viewModel.requestTokenLogin(LoginManager.LoginType.kakao, token.accessToken)
            }
        }

        // 카카오톡이 설치되어 있으면 카카오톡으로 로그인, 아니면 카카오계정으로 로그인
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
            UserApiClient.instance.loginWithKakaoTalk(this) { token, error ->
                if (error != null) {
                    DLogger.d("카카오 로그인 실패 : ${error}")
                    if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                        return@loginWithKakaoTalk
                    }
                } else if (token != null) {
                    UserApiClient.instance.me { user, error ->
                        DLogger.d("카카오계정으로 로그인 성공 \n\n " + "token: ${token.accessToken} \n\n " + "me: ${user}")
                        user?.kakaoAccount?.profile?.nickname?.let {
                            DLogger.d("nickname : ${it}")
                            tempNickName = it
                        }

                        user?.kakaoAccount?.email?.let {
                            DLogger.d("email : ${it}")
                            tempEmail = it
                        }
                    }

                    viewModel.requestTokenLogin(LoginManager.LoginType.kakao, token.accessToken)
                }
            }
        } else {
            UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
        }
    }

    /**
     * 로그인
     * authenticate() 메서드를 이용한 로그인 */
    private fun startNaverLogin() {
        var naverToken: String? = ""

        val profileCallback = object : NidProfileCallback<NidProfileResponse> {
            override fun onSuccess(response: NidProfileResponse) {
                response.profile?.nickname?.let {
                    tempNickName = it
                    DLogger.d("Naver Login NickName : ${it}")
                }

                response.profile?.email?.let {
                    tempEmail = it
                    DLogger.d("Naver Login Email : ${it}")
                }

                viewModel.requestTokenLogin(LoginManager.LoginType.naver, naverToken.toString())
            }

            override fun onFailure(httpStatus: Int, message: String) {
                val errorCode = NaverIdLoginSDK.getLastErrorCode().code
                val errorDescription = NaverIdLoginSDK.getLastErrorDescription()
                Toast.makeText(
                    this@LoginActivity, "errorCode: ${errorCode}\n" +
                            "errorDescription: ${errorDescription}", Toast.LENGTH_SHORT
                ).show()
            }

            override fun onError(errorCode: Int, message: String) {
                onFailure(errorCode, message)
            }
        }

        /** OAuthLoginCallback을 authenticate() 메서드 호출 시 파라미터로 전달하거나 NidOAuthLoginButton 객체에 등록하면 인증이 종료되는 것을 확인할 수 있습니다. */
        val oauthLoginCallback = object : OAuthLoginCallback {
            override fun onSuccess() {
                naverToken = NaverIdLoginSDK.getAccessToken()
                //로그인 유저 정보 가져오기
                NidOAuthLogin().callProfileApi(profileCallback)

            }

            override fun onFailure(httpStatus: Int, message: String) {
                val errorCode = NaverIdLoginSDK.getLastErrorCode().code
                val errorDescription = NaverIdLoginSDK.getLastErrorDescription()
                Toast.makeText(
                    this@LoginActivity, "errorCode: ${errorCode}\n" +
                            "errorDescription: ${errorDescription}", Toast.LENGTH_SHORT
                ).show()
            }

            override fun onError(errorCode: Int, message: String) {
                onFailure(errorCode, message)
            }
        }

        NaverIdLoginSDK.authenticate(this, oauthLoginCallback)
    }

    /**
     * 연동해제
     * 네이버 아이디와 애플리케이션의 연동을 해제하는 기능은 다음과 같이 NidOAuthLogin().callDeleteTokenApi() 메서드로 구현합니다.
    연동을 해제하면 클라이언트에 저장된 토큰과 서버에 저장된 토큰이 모두 삭제됩니다.
     */
    private fun startNaverDeleteToken() {
        NidOAuthLogin().callDeleteTokenApi(this, object : OAuthLoginCallback {
            override fun onSuccess() {
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

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_up, R.anim.slide_down)
    }

    override fun onConnectionFailed(p0: ConnectionResult) {

    }
}