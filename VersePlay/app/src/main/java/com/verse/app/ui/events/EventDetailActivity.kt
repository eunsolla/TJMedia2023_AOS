package com.verse.app.ui.events

import android.os.Bundle
import androidx.activity.viewModels
import com.bumptech.glide.Glide
import com.verse.app.BR
import com.verse.app.R
import com.verse.app.base.activity.BaseActivity
import com.verse.app.contants.Config
import com.verse.app.contants.DynamicLinkPathType
import com.verse.app.contants.LinkMenuTypeCode
import com.verse.app.databinding.ActivityEventDetailBinding
import com.verse.app.extension.openBrowser
import com.verse.app.extension.shareDynamicLink
import com.verse.app.utility.moveToLinkPage
import dagger.hilt.android.AndroidEntryPoint

/**
 * Description :
 *
 * Created by juhongmin on 2023/05/19
 */
@AndroidEntryPoint
class EventDetailActivity :
    BaseActivity<ActivityEventDetailBinding, EventDetailActivityViewModel>() {
    override val layoutId: Int = R.layout.activity_event_detail
    override val viewModel: EventDetailActivityViewModel by viewModels()
    override val bindingVariable: Int = BR.viewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.setVariable(BR.requestManager, Glide.with(this))
        with(viewModel) {

            startFinishEvent.observe(this@EventDetailActivity) {
                finish()
            }

            startShareEvent.observe(this@EventDetailActivity) {
                // share
                shareDynamicLink(
                    path = DynamicLinkPathType.MAIN.name,
                    targetPageCode = it.linkCode,
                    title = it.title,
                    description = getString(R.string.event_participate_title),
                    imgUrl = Config.BASE_FILE_URL + it.thumbnailImageUrl
                )
            }

            startParticipateEvent.observe(this@EventDetailActivity) {
                if(it.linkCode.isNotEmpty()){
                    if (it.linkCode == LinkMenuTypeCode.LINK_URL.code) {
                        openBrowser(it.linkUrl)
                    } else {
                        moveToLinkPage(it.linkCode)
                        finish()
                    }
                }
            }

            start()
        }
    }
}
