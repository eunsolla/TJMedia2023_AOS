package com.verse.app.ui.mypage.activity

import android.content.Context
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.databinding.library.baseAdapters.BR
import com.verse.app.R
import com.verse.app.base.activity.BaseActivity
import com.verse.app.base.adapter.BaseFragmentPagerAdapter
import com.verse.app.base.viewmodel.BaseViewModel
import com.verse.app.contants.ExtraCode
import com.verse.app.contants.FragmentType
import com.verse.app.databinding.ActivityMypageFollowerListBinding
import com.verse.app.extension.initFragment
import com.verse.app.extension.startAct
import com.verse.app.model.mypage.MyPageIntentModel
import com.verse.app.ui.mypage.fragment.FollowerFragment
import com.verse.app.ui.mypage.fragment.FollowingFragment
import com.verse.app.ui.mypage.viewmodel.MypageFollowListViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MypageFollowActivity :
    BaseActivity<ActivityMypageFollowerListBinding, MypageFollowListViewModel>() {
    override val layoutId: Int = R.layout.activity_mypage_follower_list
    override val viewModel: MypageFollowListViewModel by viewModels()
    override val bindingVariable: Int = BR.viewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        with(viewModel) {
            binding.vpTab.isUserInputEnabled = false

            isLoadingShow.value = true

            goToUserMypage.observe(this@MypageFollowActivity) {
                startAct<MyPageRootActivity> {
                    putExtra(ExtraCode.MY_PAGE_DATA, MyPageIntentModel(it.second.memCd))
                }
            }

            startFinishEvent.observe(this@MypageFollowActivity) {
                finish()
            }


            start()
            initFollowingList()
            initMypageFollowingList()
        }
    }

    /**
     * ViewPager Fragment
     */
    class FollowTapFragmentPagerAdapter(val ctx: Context, private val viewModel: BaseViewModel?) :
        BaseFragmentPagerAdapter<String>(ctx) {

        override fun onCreateFragment(pos: Int) = when (pos) {
            0 -> {
                initFragment<FollowerFragment>()
            }

            else -> {
                initFragment<FollowingFragment>()
            }
        }

        override fun containsItem(itemId: Long): Boolean {
            val tmpCount = itemCount.plus(FragmentType.FOLLOWING_INFO.uniqueId)
            return itemId < tmpCount && itemId >= FragmentType.FOLLOWING_INFO.uniqueId
        }

        override fun getItemId(pos: Int) = (FragmentType.FOLLOWING_INFO.uniqueId + pos).toLong()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        viewModel.back()
    }

    override fun finish() {
        super.finish()
    }

}