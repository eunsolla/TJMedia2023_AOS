package com.verse.app.ui.comment

import android.os.Bundle
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.forEach
import androidx.databinding.library.baseAdapters.BR
import androidx.paging.PagingData
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.RecyclerView
import com.verse.app.R
import com.verse.app.base.activity.BaseActivity
import com.verse.app.base.viewholder.BaseViewHolder
import com.verse.app.contants.AppData
import com.verse.app.contants.CommentType
import com.verse.app.contants.ExtraCode
import com.verse.app.contants.ReportType
import com.verse.app.databinding.ActivityCommentBinding
import com.verse.app.extension.getActivity
import com.verse.app.extension.hideKeyboard
import com.verse.app.extension.initFragment
import com.verse.app.extension.multiNullCheck
import com.verse.app.extension.onMain
import com.verse.app.extension.popBackStackFragment
import com.verse.app.extension.recyclerViewNotifyPayload
import com.verse.app.extension.recyclerViewNotifyRemoved
import com.verse.app.extension.replaceBackStackFragment
import com.verse.app.extension.startAct
import com.verse.app.model.comment.CommentData
import com.verse.app.ui.adapter.CommonPagingAdapter
import com.verse.app.ui.dialog.CommonDialog
import com.verse.app.ui.report.ReportActivity
import com.verse.app.utility.DLogger
import com.verse.app.utility.RxBus
import com.verse.app.utility.RxBusEvent
import com.verse.app.utility.moveToUserPage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Description : 댓글 Activity
 *
 * Created by jhlee on 2023-04-20
 */
@AndroidEntryPoint
class CommentActivity : BaseActivity<ActivityCommentBinding, CommentViewModel>() {

    override val layoutId = R.layout.activity_comment
    override val viewModel: CommentViewModel by viewModels()
    override val bindingVariable = BR.viewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.deviceProvider.setDeviceStatusBarColor(getActivity()!!.window, R.color.white)

        with(viewModel) {

            //종료
            startFinish.observe(this@CommentActivity) {
                finish()
            }

            //헤더 영역 총 수
            commentRepCount.observe(this@CommentActivity) {
                binding.vHeader.apply {
                    setHeaderTitle(getString(R.string.str_comment))
                    setContentsCount(it)
                }

                commentParam.value?.let {
                    if (it.first == CommentType.LOUNGE) {
                        RxBus.publish(RxBusEvent.LoungeRefreshEvent(isTop = false))
                    } else if (it.first == CommentType.FEED) {
//                        commentRepCount.value?.let {count->
//                            RxBus.publish(RxBusEvent.FeedCommentRefreshEvent(it.second,count))
//                        }
                    }
                }
            }

            //all refresh
            refreshAll.observe(this@CommentActivity) { state ->

                binding.recyclerView.adapter?.let {
                    if (it is ConcatAdapter) {
                        it.adapters?.let { adapters ->
                            if (adapters.size > 0) {
                                val commonPagingAdapter =
                                    adapters[0] as CommonPagingAdapter<CommentData>
                                commonPagingAdapter?.let { commonAdapter ->
                                    CoroutineScope(Dispatchers.Main).launch {
                                        commonAdapter.submitData(lifecycle, PagingData.empty())
                                        binding.recyclerView.removeAllViews()
                                        binding.recyclerView.adapter = null
                                        requestComment()
                                    }
                                }
                            }
                        }
                    }
                }
            }

            //row refresh
            refresh.observe(this@CommentActivity) {

                if (!it.second.isShowReComments) {
                    val holder = (binding.recyclerView.findViewHolderForAdapterPosition(it.first) as BaseViewHolder<*>)
                    val view = holder.itemView as ConstraintLayout
                    view?.let { rootView ->
                        val childView = rootView.getChildAt(view.childCount - 1) as ConstraintLayout
                        childView?.let { view ->
                            if (view.childCount > -1) {
                                view.forEach { cView ->
                                    if (cView is RecyclerView) {
                                        cView.removeAllViews()
                                        cView.adapter = null
                                        binding.recyclerView.recyclerViewNotifyPayload(it.first,it.second)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    binding.recyclerView.recyclerViewNotifyPayload(it.first, it.second)
                }
            }

            //re comment refresh
            refreshReComment.observe(this@CommentActivity) {
                DLogger.d("refreshReComment 답글 갱신 : ${it.first}  ${it.second} / ${it.third}")
                val holder = (binding.recyclerView.findViewHolderForAdapterPosition(it.first) as BaseViewHolder<*>)
                val view = holder.itemView as ConstraintLayout
                view?.let { rootView ->
                    val childView = rootView.getChildAt(view.childCount - 1) as ConstraintLayout
                    childView?.let { view ->
                        if (view.childCount > -1) {
                            view.forEach { cView ->
                                if (cView is RecyclerView) {
                                    DLogger.d("refreshReComment 답글 갱신 goog : ${it.second} / ${it.third}")
                                    onMain { cView.recyclerViewNotifyPayload(it.second, null) }
                                }
                            }
                        }
                    }
                }
            }

            //re comment remove
            deleteReComment.observe(this@CommentActivity) {
                multiNullCheck(it.first, it.second, it.third) { parentPositon, startPos, endPos ->

                    DLogger.d("deleteReComment info : ${parentPositon}  / ${startPos} / ${endPos}")

                    val holder =
                        (binding.recyclerView.findViewHolderForAdapterPosition(parentPositon) as BaseViewHolder<*>)
                    val view = holder.itemView as ConstraintLayout
                    view?.let { rootView ->
                        val childView = rootView.getChildAt(view.childCount - 1) as ConstraintLayout
                        childView?.let { view ->
                            if (view.childCount > -1) {
                                view.forEach { cView ->
                                    if (cView is RecyclerView) {
                                        cView.recyclerViewNotifyRemoved(startPos, endPos)
                                    }
                                }
                            }
                        }
                    }
                }
            }


            //toast msg
            showToastStringMsg.observe(this@CommentActivity) {
                showToast(it)
            }

            showToastIntMsg.observe(this@CommentActivity) {
                showToast(it)
            }

            //댓글 /답글 작성 show/hide
            showEditDialog.observe(this@CommentActivity) { state ->
                DLogger.d("showEditDialog state :  ${state}")
                if (state.first) {
                    replaceBackStackFragment(
                        binding.commentContainerFrameLayout.id,
                        initFragment<CommentWriteFragment>()
                    )
                } else {
                    hideKeyboard()
                    popBackStackFragment()
                    clearCommentValue()
                }
            }

            //삭제/신고 Dialog
            showReportDialog.observe(this@CommentActivity) {
                it?.let {

                    if (it.isMine) { //내 댓글/답글 삭제

                        CommonDialog(this@CommentActivity)
                            .setTitle(R.string.str_dialog_comment_report_delete)
                            .setContents(getString(R.string.str_dialog_comment_report_delete_msg))
                            .setCancelable(true)
                            .setPositiveButton(R.string.str_cancel)
                            .setNegativeButton(R.string.str_delete)
                            .setListener(
                                object : CommonDialog.Listener {
                                    override fun onClick(which: Int) {
                                        if (which == CommonDialog.NEGATIVE) {
                                            if (it.reportType == ReportType.FEED_COMMENT
                                                || it.reportType == ReportType.LOUNGE_COMMENT
                                                || it.reportType == ReportType.VOTE_COMMENT
                                            ) {
                                                onDeleteComment(it)
                                            } else {
                                                onDeleteCommentRe(it)
                                            }
                                        }
                                    }
                                })
                            .show()

                    } else { //신고

                        CommonDialog(this@CommentActivity)
                            .setTitle(R.string.str_dialog_comment_report)
                            .setContents(getString(R.string.str_dialog_comment_report_msg))
                            .setCancelable(true)
                            .setPositiveButton(R.string.str_cancel)
                            .setNegativeButton(R.string.str_report)
                            .setListener(
                                object : CommonDialog.Listener {
                                    override fun onClick(which: Int) {
                                        if (which == CommonDialog.NEGATIVE) {
                                            DLogger.d("신고하기 =>  ${it.reportType} / ${it.mngCd}")
                                            startAct<ReportActivity>() {
                                                putExtra(
                                                    ExtraCode.REPORT_CODE,
                                                    Pair(it.reportType, it.mngCd)
                                                )
                                            }
                                        }
                                    }
                                })
                            .show()
                    }
                }
            }

            startCheckProhibit.observe(this@CommentActivity) {
                CommonDialog(this@CommentActivity)
                    .setContents(getString(R.string.str_prohibit_notice))
                    .setIcon(AppData.POPUP_WARNING)
                    .setPositiveButton(getString(R.string.str_confirm))
                    .setListener(object : CommonDialog.Listener {
                        override fun onClick(which: Int) {
                            if (which == CommonDialog.POSITIVE) {
                                viewModel.refreshUI()
                            }
                        }
                    })
                    .show()
            }

            startCheckPrivateAccount.observe(this@CommentActivity) {
                CommonDialog(this@CommentActivity)
                    .setContents(getString(R.string.comment_private_account_popup))
//                    .setIcon(AppData.POPUP_WARNING)
                    .setPositiveButton(getString(R.string.str_confirm))
                    .show()
            }

            // 상세이동
            startUserDetailPage.observe(this@CommentActivity) {
                moveToUserPage(it)
            }

            start()

        }
    }

}