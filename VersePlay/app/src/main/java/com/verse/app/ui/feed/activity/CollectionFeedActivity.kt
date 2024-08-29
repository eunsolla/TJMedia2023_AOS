package com.verse.app.ui.feed.activity

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.databinding.library.baseAdapters.BR
import androidx.fragment.app.Fragment
import androidx.paging.PagingData
import com.verse.app.R
import com.verse.app.base.activity.BaseActivity
import com.verse.app.databinding.ActivityCollectionBinding
import com.verse.app.extension.getActivity
import com.verse.app.extension.initFragment
import com.verse.app.extension.replaceFragment
import com.verse.app.model.feed.FeedContentsData
import com.verse.app.ui.dialog.CommonDialog
import com.verse.app.ui.feed.fragment.CollectionFragment
import com.verse.app.ui.feed.fragment.FeedDetailFragment
import com.verse.app.ui.feed.viewmodel.CollectionFeedViewModel
import com.verse.app.utility.moveToLoginAct
import com.verse.app.utility.moveToSingAct
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CollectionFeedActivity : BaseActivity<ActivityCollectionBinding, CollectionFeedViewModel>() {
    override val layoutId = R.layout.activity_collection
    override val viewModel: CollectionFeedViewModel by viewModels()
    override val bindingVariable = BR.viewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        viewModel.deviceProvider.setDeviceStatusBarColor(getActivity()!!.window, R.color.black)

        with(viewModel) {

            startFinish.observe(this@CollectionFeedActivity) {
                finish()
            }

            //필수인자 전달  X show Popup
            showErrorDialog.observe(this@CollectionFeedActivity) {
                showPopup()
            }

            startFeedDetailEvent.observe(this@CollectionFeedActivity) {
                vpCurPosition.value = it.first
                moveToChildFragment(it.first, it.second, it.third)
            }

            // 파트선택 & 부르기 이동
            moveToSing.observe(this@CollectionFeedActivity) {
                if (!viewModel.loginManager.isLogin()) {
                    moveToLoginAct()
                } else {
                    moveToSingAct(it.first, it.second, it.third)
                }
            }

            startCheckPrivateAccount.observe(this@CollectionFeedActivity) {
                CommonDialog(this@CollectionFeedActivity)
                    .setContents(getString(R.string.comment_private_account_popup))
//                    .setIcon(AppData.POPUP_WARNING)
                    .setPositiveButton(getString(R.string.str_confirm))
                    .show()
            }

            start()
        }

        //start fragment
        startFragment(binding.flRoot.id, initFragment<CollectionFragment>())
    }

    /**
     * default
     */
    private fun startFragment(flId: Int, fragment: Fragment) {
        replaceFragment(flId, fragment)
    }

    /**
     * show child
     */
    private fun moveToChildFragment() {
        /*  replaceFragment(
              enterAni = R.anim.in_right_to_left_medium,
              exitAni = R.anim.out_right_to_left_medium,
              popEnterAni = R.anim.out_left_to_right_medium,
              popExitAni = R.anim.in_left_to_right_medium,
              flId, fragment
          )*/
    }

    /**
     * show child
     */
    private fun moveToChildFragment(pos: Int, mngCd: String, dataList: PagingData<FeedContentsData>) {
        val fragment = FeedDetailFragment()
            .setTargetPos(pos)
            .setTargetFeedMngCd(mngCd)
            .setPagingData(dataList)
            .setListener(object : FeedDetailFragment.Listener {
                override fun onCurPosition(pos: Int) {
                    viewModel.vpCurPosition.value = pos
                }
            })
        replaceFragment(
            containerId = binding.flRoot.id,
            fragment = fragment,
            enterAni = R.anim.in_right_to_left_short,
            exitAni = R.anim.out_right_to_left_short,
            popEnterAni = R.anim.out_left_to_right_short,
            popExitAni = R.anim.in_left_to_right_short
        )
    }


    /**
     * showing popup
     */
    private fun showPopup() {
        CommonDialog(this@CollectionFeedActivity)
            .setContents(getString(R.string.network_status_rs004))
            .setCancelable(true)
            .setPositiveButton(getString(R.string.str_confirm))
            .setListener(object : CommonDialog.Listener {
                override fun onClick(which: Int) {
                    if (which == CommonDialog.POSITIVE) {
                        finish()
                    }
                }
            }).show()
    }

    override fun finish() {
        super.finish()
    }
}