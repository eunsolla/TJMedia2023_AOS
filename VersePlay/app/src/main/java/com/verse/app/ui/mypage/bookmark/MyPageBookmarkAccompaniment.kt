package com.verse.app.ui.mypage.bookmark

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.verse.app.BR
import com.verse.app.R
import com.verse.app.base.fragment.BaseFragment
import com.verse.app.databinding.FMyPageBookMarkAccompanimentBinding
import com.verse.app.extension.dp
import com.verse.app.extension.onMain
import com.verse.app.ui.decoration.GridDecorator
import com.verse.app.utility.DLogger
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay

/**
 * Description : 마이페이지 > 즐겨찾기 > [반주음]
 *
 * Created by juhongmin on 2023/05/30
 */
@AndroidEntryPoint
class MyPageBookmarkAccompaniment :
    BaseFragment<FMyPageBookMarkAccompanimentBinding, MyPageBookmarkAccompanimentViewModel>() {
    override val layoutId: Int = R.layout.f_my_page_book_mark_accompaniment
    override val viewModel: MyPageBookmarkAccompanimentViewModel by viewModels()
    override val bindingVariable: Int = BR.viewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvContents.addItemDecoration(GridDecorator(3, 1.dp, false))
        with(viewModel) {

            isRefresh.observe(viewLifecycleOwner) {
                if(it){
                    binding.rvContents.removeAllViews()
                    binding.rvContents.adapter = null
                }
            }
            start()
        }
    }

    companion object {
        fun newInstance(data: Bundle?): Fragment {
            return MyPageBookmarkAccompaniment().apply {
                arguments = data
            }
        }
    }
}
