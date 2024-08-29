package com.verse.app.ui.search.activity

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.paging.PagingData
import com.verse.app.BR
import com.verse.app.R
import com.verse.app.base.activity.BaseActivity
import com.verse.app.databinding.ActivitySearchResultBinding
import com.verse.app.extension.replaceFragment
import com.verse.app.model.feed.FeedContentsData
import com.verse.app.ui.feed.fragment.FeedDetailFragment
import com.verse.app.ui.search.fragment.SearchResultFragment
import com.verse.app.ui.search.viewmodel.SearchResultViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class SearchResultActivity : BaseActivity<ActivitySearchResultBinding, SearchResultViewModel>() {
    override val layoutId = R.layout.activity_search_result
    override val viewModel: SearchResultViewModel by viewModels()
    override val bindingVariable = BR.viewModel

    private val backPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (supportFragmentManager.backStackEntryCount > 1) {
                supportFragmentManager.popBackStack()
            } else {
                finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        moveToSearchResult()
        onBackPressedDispatcher.addCallback(this, backPressedCallback)
    }

    private fun moveToSearchResult() {
        val fragment = SearchResultFragment.newInstance(intent.extras)
        replaceFragment(
            R.anim.in_right_to_left_short,
            R.anim.out_right_to_left_short,
            R.anim.out_left_to_right_short,
            R.anim.in_left_to_right_short,
            R.id.container,
            fragment
        )
    }

    /**
     * 피드 상세 이동하는 함수
     * @param pair {초기 이동할 값}, {피드 페이징 데이터}
     */
    fun moveToFeedDetail(triple: Triple<Int, String,PagingData<FeedContentsData>>) {
        val fragment = FeedDetailFragment()
            .setTargetPos(triple.first)
            .setTargetFeedMngCd(triple.second)
            .setPagingData(triple.third)
        replaceFragment(
            R.anim.in_right_to_left_short,
            R.anim.out_right_to_left_short,
            R.anim.out_left_to_right_short,
            R.anim.in_left_to_right_short,
            R.id.container,
            fragment
        )
    }
}

