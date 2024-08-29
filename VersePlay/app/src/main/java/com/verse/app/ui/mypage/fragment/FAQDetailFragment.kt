package com.verse.app.ui.mypage.fragment

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import com.verse.app.BR
import com.verse.app.R
import com.verse.app.base.fragment.BaseFragment
import com.verse.app.databinding.FragmentFaqDetailBinding
import com.verse.app.extension.popBackStackParentFragment
import com.verse.app.ui.mypage.viewmodel.FAQViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Description : faq 디테일
 *
 * Created by esna on 2023-05-03
 */
@AndroidEntryPoint
class FAQDetailFragment : BaseFragment<FragmentFaqDetailBinding, FAQViewModel>() {
    override val layoutId: Int = R.layout.fragment_faq_detail
    override val viewModel: FAQViewModel by activityViewModels()
    override val bindingVariable: Int = BR.viewModel
    private lateinit var callback: OnBackPressedCallback

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                popBackStackParentFragment()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(viewModel) {
            binding.apply {
                vHeader.setHeaderTitle(detailFAQTitle.value)
            }

            subFAQContents.observe(viewLifecycleOwner) {
                val html = "<!DOCTYPE html>" +
                        "<html>" +
                        "<head>" +
                        "<meta name='viewport' content='width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no'/>" +
                        "<style>\n" +
                        "body { padding-left: 10px;padding-right: 10px; }\n" +
                        "img { max-width: 100%; }\n" +
                        "figure {margin-left: 0;margin-right: 0;}\n" +
                        "figure.table {margin: 0;}\n" +
                        "      table {\n" +
                        "        width: 100%; border-collapse: collapse;\n" +
                        "      }\n" +
                        "      table, th, td {\n" +
                        "        border: 1px solid #000;\n" +
                        "      }" +
                        "</style>\n" +
                        "</head>" +
                        "<body>" +
                        it + "</body>" + "</html>"
                binding.faqWebview.loadDataWithBaseURL(null, html, "text/html; charset=utf-8", "utf-8", null)
            }

        }
    }

    override fun onDetach() {
        super.onDetach()
        callback.remove()
    }

}