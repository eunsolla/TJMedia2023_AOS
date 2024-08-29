package com.verse.app.ui.feed.activity

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.databinding.library.baseAdapters.BR
import com.verse.app.R
import com.verse.app.base.activity.BaseActivity
import com.verse.app.databinding.ActivityFeedDetailBinding
import com.verse.app.extension.replaceFragment
import com.verse.app.ui.feed.fragment.FeedDetailFragment
import com.verse.app.ui.feed.viewholders.FeedDetailViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FeedDetailActivity : BaseActivity<ActivityFeedDetailBinding, FeedDetailViewModel>() {
    override val layoutId = R.layout.activity_feed_detail
    override val viewModel: FeedDetailViewModel by viewModels()
    override val bindingVariable = BR.viewModel

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        with(viewModel) {

            startFinishEvent.observe(this@FeedDetailActivity) {
                finish()
            }

            startFeedDetailEvent.observe(this@FeedDetailActivity) {
                val fragment = FeedDetailFragment()
                    .setTargetPos(it.first)
                    .setTargetFeedMngCd(it.second)
                    .setPagingData(it.third)
                replaceFragment(
                    containerId = binding.flRoot.id,
                    fragment = fragment,
                    tagName = "FeedDetailFragment"
                )
            }

            start()
        }
    }

    override fun finish() {
        super.finish()
    }
}