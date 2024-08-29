package com.verse.app.ui.lounge

import android.os.Bundle
import androidx.activity.viewModels
import androidx.fragment.app.commitNow
import com.verse.app.BR
import com.verse.app.R
import com.verse.app.base.activity.BaseActivity
import com.verse.app.contants.AppData
import com.verse.app.databinding.ActivityLoungeRootBinding
import com.verse.app.model.lounge.LoungeIntentModel
import com.verse.app.ui.dialog.CommonDialog
import com.verse.app.ui.lounge.detail.LoungeDetailFragment
import com.verse.app.ui.lounge.modify.LoungeModifyFragment
import com.verse.app.ui.lounge.write.LoungeWriteFragment
import dagger.hilt.android.AndroidEntryPoint

/**
 * Description : 라운지 글쓰기 or 수정 페이지
 *
 * Created by juhongmin on 2023/05/17
 */
@AndroidEntryPoint
class LoungeRootActivity :
    BaseActivity<ActivityLoungeRootBinding, LoungeRootActivityViewModel>() {

    override val layoutId: Int = R.layout.activity_lounge_root
    override val viewModel: LoungeRootActivityViewModel by viewModels()
    override val bindingVariable: Int = BR.viewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        with(viewModel) {

            startFinishEvent.observe(this@LoungeRootActivity) {
                finish()
            }

            startCheckPrivateAccount.observe(this@LoungeRootActivity) {
                CommonDialog(this@LoungeRootActivity)
                    .setContents(getString(R.string.community_private_account_popup))
                    .setIcon(AppData.POPUP_WARNING)
                    .setPositiveButton(getString(R.string.str_confirm))
                    .setListener(object : CommonDialog.Listener {
                        override fun onClick(which: Int) {
                            if (which == CommonDialog.POSITIVE) {
                                finish()
                            }
                        }
                    }).show()
            }

            startMoveLoungePage.observe(this@LoungeRootActivity) {
                moveToFragment(it)
            }

            start()
        }
    }

    private fun moveToFragment(data: LoungeIntentModel) {
        val fragment = when (data.type) {
            LoungeIntentModel.Type.WRITE -> {
                LoungeWriteFragment().apply { arguments = intent.extras}
            }
            LoungeIntentModel.Type.DETAIL -> {
                LoungeDetailFragment().apply { arguments = intent.extras }
            }
            else -> {
                LoungeModifyFragment().apply { arguments = intent.extras }
            }
        }
        supportFragmentManager.commitNow {
            replace(R.id.container, fragment)
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_up, R.anim.slide_down)
    }
}
