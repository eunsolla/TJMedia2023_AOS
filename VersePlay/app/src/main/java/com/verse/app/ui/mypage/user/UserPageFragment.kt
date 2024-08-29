package com.verse.app.ui.mypage.user

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.paging.PagingData
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.verse.app.BR
import com.verse.app.R
import com.verse.app.base.fragment.BaseFragment
import com.verse.app.contants.AppData
import com.verse.app.contants.Config
import com.verse.app.databinding.FUserPageBinding
import com.verse.app.extension.clearChildFragment
import com.verse.app.gallery.ui.GalleryImageDetailBottomSheetDialog
import com.verse.app.model.feed.FeedContentsData
import com.verse.app.ui.dialog.CommonDialog
import com.verse.app.ui.main.MainActivity
import com.verse.app.ui.main.fragment.MainFragment
import com.verse.app.ui.main.fragment.MainRightFragment
import com.verse.app.ui.mypage.activity.MyPageRootActivity
import com.verse.app.ui.mypage.bookmark.MyPageBookmarkTabFragment
import com.verse.app.ui.mypage.like.MyPageLikeTabFragment
import com.verse.app.ui.mypage.upload.MyPageUploadTabFragment
import com.verse.app.utility.DLogger
import com.verse.app.utility.provider.ResourceProvider
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Description : 사용자 마이 페이지
 *
 * Created by juhongmin on 2023/06/02
 */
@AndroidEntryPoint
class UserPageFragment : BaseFragment<FUserPageBinding, UserPageFragmentViewModel>() {
    override val layoutId: Int = R.layout.f_user_page
    override val viewModel: UserPageFragmentViewModel by viewModels()
    override val bindingVariable: Int = BR.viewModel

    private var adapter: PagerAdapter? = null

    @Inject
    lateinit var resProvider: ResourceProvider

    private val requestManager: RequestManager by lazy { Glide.with(requireView()) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (adapter == null) {
            binding.vp.adapter = PagerAdapter()
        }
        binding.setVariable(BR.requestManager, Glide.with(this))

        handleHeaderNavigator()

        with(viewModel) {

            startBackEvent.observe(viewLifecycleOwner) {
                handleOnBack()
            }

            startBlockPopup.observe(viewLifecycleOwner) {
                CommonDialog(requireContext())
                    .setContents(String.format(getString(R.string.str_block_user), it))
                    .setCancelable(true)
                    .setPositiveButton(R.string.str_confirm)
                    .setNegativeButton(R.string.str_cancel)
                    .setListener(
                        object : CommonDialog.Listener {
                            override fun onClick(which: Int) {
                                if (which == 1) {
                                    requestBlockUser()
                                }
                            }
                        })
                    .show()
            }

            startOneButtonPopup.observe(viewLifecycleOwner) {
                CommonDialog(requireContext())
                    .setContents(it)
                    .setIcon(AppData.POPUP_WARNING)
                    .setCancelable(true)
                    .setPositiveButton(R.string.str_confirm)
                    .show()
            }

            startCompleteButtonPopup.observe(viewLifecycleOwner) {
                CommonDialog(requireContext())
                    .setContents(it)
                    .setIcon(AppData.POPUP_COMPLETE)
                    .setCancelable(true)
                    .setPositiveButton(R.string.str_confirm)
                    .setListener(object : CommonDialog.Listener {
                        override fun onClick(which: Int) {
                            if (which == CommonDialog.POSITIVE) {
                                handleOnBack()
                            }
                        }
                    })
                    .show()
            }

            profileImageUrl.observe(viewLifecycleOwner) {
                bindingProfileImage(it)
            }

            profileBgImageUrl.observe(viewLifecycleOwner) {
                bindingBgProfileImage(it)
            }

            showInvalidUrl.observe(viewLifecycleOwner) {
                if (outLink.value.isNullOrEmpty()){
                    binding.linkIcon.visibility = View.GONE
                    return@observe

                } else {
                    binding.linkIcon.visibility = View.VISIBLE
                    requestManager.load(R.drawable.ic_link)
                        .centerCrop()
                        .error(R.drawable.ic_link)
                        .into(binding.linkIcon)
                }
            }
            startFocusingTabPositionEvent.observe(viewLifecycleOwner) {
                binding.abl.setExpanded(false, true)
                binding.vp.setCurrentItem(0, true)
            }

            moveToMyLink.observe(viewLifecycleOwner) {
                if (outLink.value?.isNotEmpty() == true) {
                    val uri = outLink.value
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("$uri"))
                    requireActivity().startActivity(intent)
                }
            }

            moveToProfileImageDetail.observe(viewLifecycleOwner) {
                showImageDetailDialog(it)
            }

            start()
        }
    }

    override fun onDestroyView() {
        binding.vp.adapter = null
        clearChildFragment()
        super.onDestroyView()
    }

    private fun handleHeaderNavigator() {
        binding.abl.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            if (appBarLayout.totalScrollRange == Math.abs(verticalOffset)) {
                if (binding.toolbar.alpha != 1.0F) {
                    binding.toolbar.alpha = 1.0F
                }
            } else {
                if (binding.toolbar.alpha != 0.0F) {
                    binding.toolbar.alpha = 0.0F
                }
            }
        }
    }

    /**
     * 마이페이지 피드 상세로 이동하는 최종 함수
     * @param pair MovePosition, 피드 페이징 데이터
     */
    fun moveToRootFeedDetail(triple: Triple<Int,String, PagingData<FeedContentsData>>) {
        val rootFragment = parentFragment
        DLogger.d("마이페이지 피드 상세 이동 처리 공통 함수 $rootFragment")
        // 메인탭 > 오른쪽 스와이프시 나오는 Fragment
        if (rootFragment is MainRightFragment) {
            rootFragment.moveToFeedDetail(triple.first, triple.second,triple.third)
        } else {
            val activity = requireActivity()
            if (activity is MainActivity) {
                val mainFragment = activity.supportFragmentManager
                    .fragments
                    .find { it is MainFragment } as? MainFragment
                mainFragment?.viewModel?.moveToFeedDetail(triple.first, triple.second,triple.third)
            } else if (activity is MyPageRootActivity) {
                activity.moveToFeedDetail(triple.first, triple.second,triple.third)
            }
        }
    }

    /**
     * MainRightFragment 에서 스와이프시
     * 좀더 디테일하게 데이터 호출하는 함수
     */
    fun handleMainRightApiCall() {
        viewModel.reqRefreshUserProfile()
    }

    private fun handleOnBack() {
        val parentFragment = parentFragment
        if (parentFragment is MainRightFragment) {
            parentFragment.moveToMainFeed()
        } else {
            val activity = requireActivity()
            if (activity is MyPageRootActivity) {
                activity.finish()
            }
        }
    }

    inner class PagerAdapter : FragmentStateAdapter(this) {
        override fun getItemCount(): Int {
            return 3
        }

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> MyPageUploadTabFragment.newInstance(arguments)
                1 -> MyPageLikeTabFragment.newInstance(arguments)
                else -> MyPageBookmarkTabFragment.newInstance(arguments)
            }
        }
    }

    companion object {
        fun newInstance(bundle: Bundle?): Fragment {
            return UserPageFragment().apply {
                arguments = bundle
            }
        }
    }

    private fun showImageDetailDialog(imagePath: String) {
        GalleryImageDetailBottomSheetDialog()
            .setImageUrl(imagePath)
            .simpleShow(requireActivity().supportFragmentManager)
    }

    fun bindingProfileImage(url: String) {

        if (!url.startsWith(AppData.PREFIX_TJ_SOUND) && !url.startsWith(AppData.PREFIX_PROFILE) && !url.startsWith(
                AppData.PREFIX_FEED))
        {
            requestManager.load(url)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .skipMemoryCache(false)
                .centerCrop()
                .placeholder(R.drawable.profile_empty_big)
                .error(R.drawable.profile_empty_big)
                .into(binding.ivProfile)

        } else {
            requestManager.load(Config.BASE_FILE_URL + url)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .skipMemoryCache(false)
                .centerCrop()
                .placeholder(R.drawable.profile_empty_big)
                .transition(DrawableTransitionOptions.withCrossFade())
                .error(R.drawable.profile_empty_big)
                .into(binding.ivProfile)
        }
    }

    fun bindingBgProfileImage(url: String) {

        if (!url.startsWith(AppData.PREFIX_TJ_SOUND) && !url.startsWith(AppData.PREFIX_PROFILE) && !url.startsWith(
                AppData.PREFIX_FEED))
        {
            requestManager.load(url)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .skipMemoryCache(false)
                .placeholder(R.drawable.bg_gradient_profile)
                .centerCrop()
                .error(R.drawable.bg_gradient_profile)
                .into(binding.ivBgProfile)

        } else {

            requestManager.load(Config.BASE_FILE_URL + url)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .skipMemoryCache(false)
                .centerCrop()
                .transition(DrawableTransitionOptions.withCrossFade())
                .placeholder(R.drawable.bg_gradient_profile)
                .error(R.drawable.bg_gradient_profile)
                .into(binding.ivBgProfile)
        }
    }
}
