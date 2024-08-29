package com.verse.app.ui.dialogfragment

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.databinding.library.baseAdapters.BR
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import com.verse.app.R
import com.verse.app.base.fragment.BaseBottomSheetDialogFragment
import com.verse.app.contants.Config
import com.verse.app.contants.DeleteRefreshFeedList
import com.verse.app.contants.DynamicLinkPathType
import com.verse.app.contants.ExtraCode
import com.verse.app.contants.LinkMenuTypeCode
import com.verse.app.contants.MediaType
import com.verse.app.databinding.DialogFeedMoreBinding
import com.verse.app.extension.shareDynamicLink
import com.verse.app.extension.startAct
import com.verse.app.model.feed.FeedContentsData
import com.verse.app.ui.dialog.ChangeContentsDialog
import com.verse.app.ui.dialog.CommonDialog
import com.verse.app.ui.feed.viewmodel.FeedMoreBottomSheetDialogViewModel
import com.verse.app.ui.main.MainActivity
import com.verse.app.ui.mypage.activity.MypagePrivateSongBoxActivity
import com.verse.app.ui.report.ReportActivity
import com.verse.app.utility.DLogger
import com.verse.app.utility.RxBus
import com.verse.app.utility.RxBusEvent
import com.verse.app.utility.moveToLoginAct
import dagger.hilt.android.AndroidEntryPoint

/**
 * Description :  메인 -> ... 더보기 Dialog
 *
 * Created by jhlee on 2023-06-04
 */
@AndroidEntryPoint
class FeedMoreDialog : BaseBottomSheetDialogFragment<DialogFeedMoreBinding, FeedMoreBottomSheetDialogViewModel>() {

    override val layoutId: Int = R.layout.dialog_feed_more
    override val viewModel: FeedMoreBottomSheetDialogViewModel by viewModels()
    override val bindingVariable: Int = BR.viewModel
    private var feedMoreListener: Listener? = null

    interface Listener {
        fun onSuccess(pair: Pair<Int,Boolean>)
        fun onDeleted()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        DLogger.d("FeedMoreDialog  =>  ${requireActivity().javaClass.simpleName}")

        with(viewModel) {

            startCloseEvent.observe(viewLifecycleOwner) {
                dismiss()
            }

            startShare.observe(viewLifecycleOwner) {
                DLogger.d("more share =>  ${it}")
                val imgUrl = Config.BASE_FILE_URL + if (it.mdTpCd == MediaType.AUDIO.code) {
                    it.albImgPath
                } else {
                    it.thumbPicPath
                }

                DLogger.d("more share =>  ${imgUrl}")
                requireActivity().shareDynamicLink(
                    path = DynamicLinkPathType.MAIN.name,
                    targetPageCode = LinkMenuTypeCode.LINK_FEED_CONTENTS.code,
                    mngCd = it.feedMngCd,
                    title = it.songNm,
                    description = it.feedNote,
                    imgUrl = imgUrl)
                dismiss()
            }

            startReport.observe(viewLifecycleOwner) {
                requireActivity().startAct<ReportActivity>() {
                    putExtra(ExtraCode.REPORT_CODE, it)
                }
                dismiss()
            }

            startLoginPage.observe(viewLifecycleOwner) {
                requireActivity().moveToLoginAct()
            }

            startChangeContents.observe(viewLifecycleOwner) {
                ChangeContentsDialog(requireContext())
                    .setCancelable(true)
                    .setCurrentType(feedContentsData.exposCd)
                    .setPositiveButton(R.string.str_confirm)
                    .setNegativeButton(R.string.str_cancel)
                    .setListener(
                        object : ChangeContentsDialog.Listener {
                            override fun onClick(which: Int, type: String) {
                                if (which == 1) {
                                    if (type.isNotEmpty() && type != feedContentsData.exposCd) {
                                        requestFeedVisibility(type)
                                    }
                                }
                            }
                        })
                    .show()
            }


            startBlockPopup.observe(viewLifecycleOwner) {
                if (it.second) {
                    CommonDialog(requireContext())
                        .setContents(resources.getString(R.string.str_block_feed))
                        .setCancelable(true)
                        .setPositiveButton(R.string.str_confirm)
                        .setNegativeButton(R.string.str_cancel)
                        .setListener(
                            object : CommonDialog.Listener {
                                override fun onClick(which: Int) {
                                    if (which == 1) {
                                        requestBlock()
                                    }
                                }
                            })
                        .show()
                    /*.setIcon(AppData.POPUP_BLOCK)*/
                } else {
                    requestBlock()
                }
            }

            callBack.observe(viewLifecycleOwner) {

                if (it.second) {
                    val msg = if (it.first == 0) {
                        resources.getString(R.string.str_block_complete)
                    } else {
                        resources.getString(R.string.str_uninterested_feed)
                    }

                    CommonDialog(requireContext())
                        .setContents(msg)
                        .setCancelable(true)
                        .setPositiveButton(R.string.str_confirm)
                        .setListener(object :CommonDialog.Listener{
                            override fun onClick(which: Int) {
                                dismiss()
                                feedMoreListener?.onSuccess(it)
                            }
                        })
                        .show()
                }else{
                    dismiss()
                    feedMoreListener?.onSuccess(it)
                }
            }


            startCommentPopup.observe(viewLifecycleOwner) {
                val msg = if (it) {
                    resources.getString(R.string.popup_disallow_comments)
                } else {
                    resources.getString(R.string.popup_allow_comments)
                }

                CommonDialog(requireContext())
                    .setContents(msg)
                    .setCancelable(true)
                    .setPositiveButton(R.string.str_confirm)
                    .setNegativeButton(R.string.str_cancel)
                    .setListener(
                        object : CommonDialog.Listener {
                            override fun onClick(which: Int) {
                                if (which == 1) {
                                    requestComment()
                                }
                            }
                        })
                    .show()
            }

            startDeletePopup.observe(viewLifecycleOwner) {

                if (requireActivity() !is MainActivity && requireActivity() !is MypagePrivateSongBoxActivity) {
                    Toast.makeText(requireActivity(), getString(R.string.str_can_remove_my_page), Toast.LENGTH_SHORT).show()
                    return@observe
                }

                CommonDialog(requireContext())
                    .setContents(getString(R.string.popup_feed_delete))
                    .setCancelable(true)
                    .setPositiveButton(R.string.str_delete)
                    .setNegativeButton(R.string.str_cancel)
                    .setListener(
                        object : CommonDialog.Listener {
                            override fun onClick(which: Int) {
                                if (which == 1) {
                                    requestDeleteFeed()
                                }
                            }
                        })
                    .show()
            }

            //삭제 갱신
            startDeleteRefresh.observe(viewLifecycleOwner) {
                feedMoreListener?.onDeleted()
                refreshFeed(it.first, it.second)
                dismiss()
            }

            startMessage.observe(viewLifecycleOwner) {
                Toast.makeText(requireActivity(), getString(it), Toast.LENGTH_SHORT).show()
            }

            interestedPopup.observe(viewLifecycleOwner) {
                CommonDialog(requireContext())
                    .setContents(getString(R.string.str_already_no_interested))
                    .setCancelable(true)
                    .setPositiveButton(R.string.str_confirm)
                    .setListener(
                        object : CommonDialog.Listener {
                            override fun onClick(which: Int) {
                                dismiss()
                            }
                        })
                    .show()
            }

            onStart()
        }
    }

    fun setFeedInfo(data: FeedContentsData, isDetail: Boolean): FeedMoreDialog {
        if (arguments == null) {
            arguments = Bundle().apply {
                putParcelable(FeedMoreBottomSheetDialogViewModel.FEED_DATA, data)
                putBoolean(FeedMoreBottomSheetDialogViewModel.IS_DETAIL, isDetail)
            }
        } else {
            arguments?.putParcelable(FeedMoreBottomSheetDialogViewModel.FEED_DATA, FeedContentsData())
        }
        return this
    }

    fun setListener(listener: Listener): FeedMoreDialog {
        feedMoreListener = listener
        return this
    }

    fun show(fm: FragmentManager) {
        try {
            // 이미 보여지고 있는 Dialog 인경우 스킵
            if (!isAdded) {
                super.show(fm, "GalleryBottomSheetDialog")
            }
        } catch (ex: Exception) {
            // ignore
        }
    }

    /**
     * 메인 피드 탭 갱신
     */
    private fun refreshFeed(data: FeedContentsData, isDetail: Boolean) {
        if (!isDetail) {
            DLogger.d("FeedRefreshEvent dialog main ")
            RxBus.publish(RxBusEvent.FeedRefreshEvent(feedContentData = data, deleteRefreshFeedList = DeleteRefreshFeedList.MAIN))
        } else {
            DLogger.d("FeedRefreshEvent dialog detail ")
            RxBus.publish(RxBusEvent.FeedRefreshEvent(feedContentData = data, deleteRefreshFeedList = DeleteRefreshFeedList.DETAIL))
        }
    }

    override fun onClick(p0: View?) {
    }
}