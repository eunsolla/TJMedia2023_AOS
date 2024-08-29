package com.verse.app.ui.vote

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.fragment.app.commitNow
import com.verse.app.BR
import com.verse.app.R
import com.verse.app.base.activity.BaseActivity
import com.verse.app.databinding.ActivityVoteRootBinding
import com.verse.app.model.vote.VoteIntentModel
import com.verse.app.ui.vote.fragment.VoteEndFragment
import com.verse.app.ui.vote.fragment.VoteParticipationFragment
import com.verse.app.ui.vote.viewmodel.VoteRootActivityViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Description :
 *
 * Created by juhongmin on 2023/05/19
 */
@AndroidEntryPoint
class VoteRootActivity : BaseActivity<ActivityVoteRootBinding, VoteRootActivityViewModel>() {
    override val layoutId: Int = R.layout.activity_vote_root
    override val viewModel: VoteRootActivityViewModel by viewModels()
    override val bindingVariable: Int = BR.viewModel

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        with(viewModel) {

            startFinishEvent.observe(this@VoteRootActivity) {
                finish()
            }

            startMoveVotePage.observe(this@VoteRootActivity) {
                moveToFragment(it)
            }

            start()
        }

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    private fun moveToFragment(data: VoteIntentModel) {
        val fragment = if (data.type == VoteIntentModel.Type.PARTICIPATION) {
            VoteParticipationFragment().apply {
                arguments = intent.extras
            }
        } else {
            VoteEndFragment().apply {
                arguments = intent.extras
            }
        }
        supportFragmentManager.commitNow {
            replace(R.id.container, fragment)
        }
    }
}