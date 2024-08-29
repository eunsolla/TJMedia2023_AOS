package com.verse.app.ui.mypage.activity

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.core.os.bundleOf
import androidx.paging.PagingData
import com.verse.app.BR
import com.verse.app.R
import com.verse.app.base.viewmodel.ActivityViewModel
import com.verse.app.base.activity.BaseActivity
import com.verse.app.contants.ExtraCode
import com.verse.app.databinding.AMyPageRootBinding
import com.verse.app.extension.parcelable
import com.verse.app.extension.replaceFragment
import com.verse.app.model.feed.FeedContentsData
import com.verse.app.model.mypage.MyPageIntentModel
import com.verse.app.ui.feed.fragment.FeedDetailFragment
import com.verse.app.ui.mypage.my.MyPageFragment
import com.verse.app.ui.mypage.user.UserPageFragment
import com.verse.app.utility.DLogger
import com.verse.app.utility.manager.LoginManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Description : 마이페이지 > 사용자 or 다른 사용자 페이지 전달 하는 Root Activity
 *
 * Created by juhongmin on 2023/06/02
 */
@AndroidEntryPoint
class MyPageRootActivity : BaseActivity<AMyPageRootBinding, ActivityViewModel>() {
    override val layoutId: Int = R.layout.a_my_page_root
    override val viewModel: ActivityViewModel by viewModels()
    override val bindingVariable: Int = BR.viewModel

    @Inject
    lateinit var loginManager: LoginManager

    private val backPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            DLogger.d("~~~~~~~ MyPageRootActivity backPressedCallback ")
            if (supportFragmentManager.backStackEntryCount > 1) {
                supportFragmentManager.popBackStack()
            } else {
                finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleMyPageFragment()

        onBackPressedDispatcher.addCallback(this, backPressedCallback)
    }

    private fun handleMyPageFragment() {
        val intentModel = intent.parcelable<MyPageIntentModel>(ExtraCode.MY_PAGE_DATA)

        if (intentModel == null) {
            finish()
            return
        }

        val bundle = bundleOf(ExtraCode.MY_PAGE_DATA to MyPageIntentModel(intentModel.memberCode))
        val fragment = if (loginManager.getUserLoginData().memCd == intentModel.memberCode) {
            MyPageFragment.newInstance(bundle)
        } else {
            UserPageFragment.newInstance(bundle)
        }

        replaceFragment(
            R.anim.in_right_to_left_short,
            R.anim.out_right_to_left_short,
            R.anim.out_left_to_right_short,
            R.anim.in_left_to_right_short,
            R.id.myPageRootContainer,
            fragment
        )
    }

    /**
     * 피드 상세 이동하는 함수
     * @param index 초기 이동할 위치값
     * @param dataList 피드 페이징 데이터
     */
    fun moveToFeedDetail(index: Int,mngCd:String, dataList: PagingData<FeedContentsData>) {
        val fragment = FeedDetailFragment()
            .setTargetPos(index)
            .setTargetFeedMngCd(mngCd)
            .setPagingData(dataList)
        DLogger.d("피드 상세 이동합니다. ")
        replaceFragment(
            R.anim.in_right_to_left_short,
            R.anim.out_right_to_left_short,
            R.anim.out_left_to_right_short,
            R.anim.in_left_to_right_short,
            R.id.myPageRootContainer,
            fragment,
        )
    }
}
