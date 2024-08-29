package com.verse.app.ui.search.activity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.databinding.library.baseAdapters.BR
import com.verse.app.R
import com.verse.app.base.activity.BaseActivity
import com.verse.app.contants.AppData
import com.verse.app.databinding.ActivitySearchMainBinding
import com.verse.app.ui.dialog.CommonDialog
import com.verse.app.ui.search.viewmodel.SearchMainViewModel
import com.verse.app.utility.provider.DeviceProvider
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SearchMainActivity : BaseActivity<ActivitySearchMainBinding, SearchMainViewModel>() {

    override val layoutId = R.layout.activity_search_main
    override val viewModel: SearchMainViewModel by viewModels()
    override val bindingVariable = BR.viewModel

    @Inject
    lateinit var deviceProvider: DeviceProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        with(viewModel) {

            startFinishEvent.observe(this@SearchMainActivity) {
                finish()
            }

            showOneButtonDialogPopup.observe(this@SearchMainActivity) {
                showRecentKeywordsAllClear(it)
            }

            start()
        }
    }

    private fun showRecentKeywordsAllClear(title: String) {
        CommonDialog(this)
            .setContents(title)
            .setIcon(AppData.POPUP_HELP)
            .setNegativeButton(getString(R.string.str_cancel))
            .setPositiveButton(getString(R.string.str_confirm))
            .setListener(object : CommonDialog.Listener {
                override fun onClick(which: Int) {
                    if (which == CommonDialog.POSITIVE) {
                        viewModel.runCatching {
                            overallRecentKeywordList.clear()
                            saveRecentKeywords(overallRecentKeywordList.data())
                        }
                    }
                }
            })
            .show()
    }
}