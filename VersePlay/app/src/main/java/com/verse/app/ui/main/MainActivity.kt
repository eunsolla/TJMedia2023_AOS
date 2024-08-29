package com.verse.app.ui.main

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.databinding.library.baseAdapters.BR
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.dynamiclinks.PendingDynamicLinkData
import com.verse.app.MainApplication
import com.verse.app.R
import com.verse.app.base.activity.BaseActivity
import com.verse.app.base.adapter.BaseFragmentPagerAdapter
import com.verse.app.base.viewmodel.BaseViewModel
import com.verse.app.contants.DynamicLinkKeyType
import com.verse.app.contants.DynamicLinkPathType
import com.verse.app.contants.ExtraCode
import com.verse.app.contants.NaviType
import com.verse.app.contants.SingType
import com.verse.app.contants.VideoUploadPageType
import com.verse.app.databinding.ActivityMainBinding
import com.verse.app.extension.applyApiScheduler
import com.verse.app.extension.exitApp
import com.verse.app.extension.getFragment
import com.verse.app.extension.initFragment
import com.verse.app.extension.onMain
import com.verse.app.extension.popBackStackParentFragment
import com.verse.app.extension.reduceDragSensitivity
import com.verse.app.extension.startAct
import com.verse.app.gallery.ui.GalleryBottomSheetDialog
import com.verse.app.permissions.SPermissions
import com.verse.app.repository.http.ApiService
import com.verse.app.repository.preferences.AccountPref
import com.verse.app.ui.dialog.CommonDialog
import com.verse.app.ui.feed.fragment.FeedDetailFragment
import com.verse.app.ui.intro.activity.IntroActivity
import com.verse.app.ui.main.fragment.MainFragment
import com.verse.app.ui.main.fragment.MainRightFragment
import com.verse.app.ui.main.viewmodel.MainViewModel
import com.verse.app.ui.mypage.activity.MypageAlertActivity
import com.verse.app.ui.permissions.PermissionsActivity
import com.verse.app.ui.videoupload.VideouploadActivity
import com.verse.app.utility.DLogger
import com.verse.app.utility.RxBus
import com.verse.app.utility.RxBusEvent
import com.verse.app.utility.moveToLinkPage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit
import javax.inject.Inject


/**
 * 메인 Activity
 *
 * Created by jhlee on 2023-01-01
 */
@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding, MainViewModel>() {

    override val layoutId = R.layout.activity_main
    override val viewModel: MainViewModel by viewModels()
    override val bindingVariable = BR.viewModel

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            DLogger.d("메인 네비>  handleOnBackPressed")
            handleBack()
        }
    }

    fun handleBack() {
        val mainFragment = supportFragmentManager.fragments.first().childFragmentManager

        if (mainFragment.fragments.size > 1) {
            if (mainFragment.fragments.last() is FeedDetailFragment) {
                mainFragment.getFragment<FeedDetailFragment>()?.parentFragmentManager?.popBackStack()
            } else {
                mainFragment.popBackStack()
                RxBus.publish(RxBusEvent.NaviEvent(NaviType.MAIN))
            }
        } else {
            if (viewModel.vpMainPosition.value == 1) {
                supportFragmentManager.getFragment<MainRightFragment>()?.let {
                    it.childFragmentManager.fragments?.let {
                        //메인 우측 유저 페이지 피드 디테일화면인경우
                        if(it.last() is FeedDetailFragment){
                            it.last().popBackStackParentFragment()
                        }else{
                            moveToVideoScreen()
                        }
                    }
                }?:run {
                    moveToVideoScreen()
                }
            } else {
                viewModel.onBackPressed()
            }
        }
    }

    @Inject
    lateinit var accountPref: AccountPref

    @Inject
    lateinit var apiService: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.vpMain.reduceDragSensitivity()
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        if (savedInstanceState == null) {
            // Main init Start
            // 권한 고지 페이지 노출 여부
            if (accountPref.isPermissionsPageShow()) {
                //Start Intro Act
                startAct<IntroActivity>(
                    enterAni = android.R.anim.fade_in, exitAni = android.R.anim.fade_out
                )
            } else {
                //Start Permission Act
                startAct<PermissionsActivity>(
                    enterAni = android.R.anim.fade_in, exitAni = android.R.anim.fade_out
                )
            }
        } else if (viewModel.isInitViewCreated()) {
            // 메인 메모리에서 해제되고 다시 재실행 경우
            mainStart()
        }

        with(viewModel) {

            startMain.observe(this@MainActivity) {
                val mainFragment = supportFragmentManager.getFragment<MainFragment>()
                mainFragment?.createDefaultFragment()
                handleDynamicLinks(intent)
            }

            //메인 현재 탭 갱신
            startRefresh.observe(this@MainActivity) {
                val mainFragment = supportFragmentManager.getFragment<MainFragment>()
                mainFragment?.refreshTab()
            }

            vpMainPosition.observe(this@MainActivity) {
                if (it == 0) {
                    setEnableRefresh(true)
                } else {
                    setEnableRefresh(false)
                    handleMainRightApiCall()
                }
            }

            finish.observe(this@MainActivity) {
                if (it) {
                    exitApp()
                } else {
                    this@MainActivity.showToast(R.string.str_back_press_info)
                }
            }

            // 앨범 영상 업로드
            showAlbumContentsUpload.observe(this@MainActivity) {
                handlePermissions()
            }

            startVideoUpload.observe(this@MainActivity) { dataList ->
                dataList?.let {
                    startAct<VideouploadActivity> {
                        putExtra(ExtraCode.UPLOAD_PAGE_TYPE, VideoUploadPageType.ALBUM)
                        putExtra(ExtraCode.ALBUM_SELECTED_ITEM, it[0])
                    }
                }
            }

            initStart()
        }

        handleIntroRxEvent()
        handleVideoUserRxEvent()
    }

    /**
     * 앨범 업로드 퍼미션 확인
     */
    private fun handlePermissions() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            listOf(Manifest.permission.READ_MEDIA_VIDEO)
        } else {
            listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        SPermissions(this)
            .requestPermissions(*permissions.toTypedArray())
            .build { b, _ ->
                if (b) {
                    showGalleryDialog()
                } else {
                    // 권한 거부 팝업
                    CommonDialog(this)
                        .setContents(resources.getString(R.string.str_permission_denied))
                        .setPositiveButton(R.string.str_confirm)
                        .show()
                }
            }
    }

    /**
     * 갤러리 showing
     */
    private fun showGalleryDialog() {
        GalleryBottomSheetDialog()
            .setMaxPicker(1)
            .setVideoType()
            .setMaxSelectText(getString(R.string.gallery_invalid_file_max_confirm_message))
            .setConfirmInvalidText(getString(R.string.gallery_invalid_file_confirm_message))
            .setListener(viewModel)
            .simpleShow(supportFragmentManager)
    }

    /**
     * 메인 우측 에서 포지션 이동
     */
    private fun moveToVideoScreen() {
        viewModel.deviceProvider.setDeviceStatusBarColor(this.window, R.color.black)
        binding.vpMain.setCurrentItem(0, true)
    }

    /**
     * 메인 시작
     */
    private fun mainStart() {
        if (application is MainApplication) {
            (application as MainApplication).introActivity?.get()?.finish()
        }

        viewModel.start()
    }

    override fun onNewIntent(intent: Intent?) {
        // 푸쉬 메시지 랜딩 요청 시 알림 목록으로 이동 처리
        intent.let {
            if (!intent?.getStringExtra(ExtraCode.PUSH_MSG_LINK_TYPE).isNullOrEmpty()) {
                onMain {
                    delay(500)
                    startAct<MypageAlertActivity>()
                }
            }
        }

        handleDynamicLinks(intent)

        super.onNewIntent(intent)
    }

    /**
     * 메인 Fragment
     * ViewPager Fragment
     */
    class MainFragmentPagerAdapter(ctx: Context, private val viewModel: BaseViewModel?) :
        BaseFragmentPagerAdapter<String>(ctx) {

        override fun onCreateFragment(pos: Int) = when (pos) {
            0 -> {
                initFragment<MainFragment>()
            }

            else -> {
                initFragment<MainRightFragment>()
            }
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, 0)
    }

    override fun onResume() {
        super.onResume()
        viewModel.deviceProvider.setDeviceStatusBarColor(this.window, R.color.black)
    }

    private fun handleMainRightApiCall() {
        val fragment = getFragment(binding.vpMain) as? MainRightFragment
        fragment?.requestMyPage()
    }

    /** DynamicLink 수신 */
    private fun handleDynamicLinks(intent: Intent?) {
        if (intent == null) return

        FirebaseDynamicLinks.getInstance()
            .getDynamicLink(intent)
            .addOnSuccessListener(this) { pendingDynamicLinkData: PendingDynamicLinkData? ->
                if (pendingDynamicLinkData != null) {
                    val dynamicLink = pendingDynamicLinkData.link // deeplink로 app 넘어 왔을 경우
                    DLogger.d("dynamicLink=> $dynamicLink")
                    val segment = dynamicLink?.lastPathSegment?.uppercase()
                    DLogger.d(" dynamic segment=> $segment ")
                    when (segment) {
                        DynamicLinkPathType.MAIN.name -> {
                            if (dynamicLink.queryParameterNames.contains(DynamicLinkKeyType.ID.name)) {
                                dynamicLink.getQueryParameter(DynamicLinkKeyType.ID.name)?.let { code ->
                                    moveToLinkPage(
                                        code, dynamicLink.getQueryParameter(DynamicLinkKeyType.MNGCD.name) ?: ""
                                    )
                                }
                            }
                        }

                        else -> {
                            DLogger.d(" dynamic undefined DynamicLinkPathType")
                        }
                    }
                } else {
                    // app으로 실행 했을 경우 (deeplink 없는 경우)
                    DLogger.d("No have dynamic link")
                    return@addOnSuccessListener
                }
            }
            /** 딥링크 데이터 수신실패 */
            .addOnFailureListener { e ->
                DLogger.d("getDynamicLink : onFailure ${e}")
            }
    }

    private fun handleIntroRxEvent() {
        val mainEnterDisposable = RxBus.listen(RxBusEvent.MainEnterEvent::class.java)
            .debounce(200, TimeUnit.MILLISECONDS)
            .applyApiScheduler()
            .doOnNext {
                DLogger.d("Intro Finish onCompleted ${it.type}")
                mainStart()
                // 푸쉬 메시지 랜딩 요청 시 알림 목록으로 이동 처리
                if (!intent?.getStringExtra(ExtraCode.PUSH_MSG_LINK_TYPE).isNullOrEmpty()) {
                    startAct<MypageAlertActivity>()
                }
            }
            .doOnError { DLogger.d("오잉? $it") }
            .subscribe()
        viewModel.addDisposable(mainEnterDisposable)
    }

    private fun handleVideoUserRxEvent() {
        val videoUserDisposable = RxBus.listen(RxBusEvent.VideoUserInfoEvent::class.java)
            .debounce(100, TimeUnit.MILLISECONDS)
            .applyApiScheduler()
            .doOnNext {
                if (it.currentFeedData.feedMngCd.isNotEmpty()) {
                    viewModel.setIsAd(it.currentFeedData.paTpCd == SingType.AD.code)
                    if (it.currentFeedData.paTpCd == SingType.AD.code) {
                        viewModel.setEnableMainViewpager(false)
                    } else {
                        viewModel.setEnableMainViewpager(true)
                    }
                } else {
                    viewModel.setEnableMainViewpager(false)
                }
            }
            .subscribe()
        viewModel.addDisposable(videoUserDisposable)
    }
}