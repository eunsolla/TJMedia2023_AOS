package com.verse.app.ui.mypage.my

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
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
import com.verse.app.contants.ExtraCode
import com.verse.app.databinding.FMyPageBinding
import com.verse.app.extension.clearChildFragment
import com.verse.app.extension.startAct
import com.verse.app.gallery.ui.GalleryImageDetailBottomSheetDialog
import com.verse.app.model.feed.FeedContentsData
import com.verse.app.ui.dialogfragment.FilterDialogFragment
import com.verse.app.ui.main.MainActivity
import com.verse.app.ui.main.fragment.MainFragment
import com.verse.app.ui.main.fragment.MainRightFragment
import com.verse.app.ui.main.viewmodel.MainViewModel
import com.verse.app.ui.mypage.activity.MyPageRootActivity
import com.verse.app.ui.mypage.activity.MypagePrivateSongBoxActivity
import com.verse.app.ui.mypage.activity.MypageSettingActivity
import com.verse.app.ui.mypage.bookmark.MyPageBookmarkTabFragment
import com.verse.app.ui.mypage.like.MyPageLikeTabFragment
import com.verse.app.ui.mypage.upload.MyPageUploadTabFragment
import com.verse.app.utility.DLogger
import com.verse.app.utility.provider.ResourceProvider
import com.verse.app.utility.RxBus
import com.verse.app.utility.RxBusEvent
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import javax.inject.Inject

/**
 * Description : 회원 마이페이지
 *
 * Created by juhongmin on 2023/05/30
 */
@AndroidEntryPoint
class MyPageFragment : BaseFragment<FMyPageBinding, MyPageFragmentViewModel>() {
    override val layoutId: Int = R.layout.f_my_page
    override val viewModel: MyPageFragmentViewModel by viewModels()
    override val bindingVariable: Int = BR.viewModel
    private lateinit var profileDisposable: Disposable
    private val activityViewModel: MainViewModel by activityViewModels()

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

            /**
             * 프로필 사진 Refresh
             * 내부에 있는 경로 가져오는 로직
             */
            profileDisposable = RxBus.listen(RxBusEvent.ProfileRefreshEvent::class.java)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    DLogger.d("profileImg : ${it.profileImg}")
                    DLogger.d("bgprofileImg : ${it.bgProfileImg}")

                    if (it.profileType) {
                        bindingProfileImage(it.profileImg)
                    }
                    if (it.bgProfileType) {
                        bindingBgProfileImage(it.bgProfileImg)
                    }

                }

            profileImageUrl.observe(viewLifecycleOwner) {
                bindingProfileImage(it)
            }

            profileBgImageUrl.observe(viewLifecycleOwner) {
                bindingBgProfileImage(it)
            }

            showInvalidUrl.observe(viewLifecycleOwner) {
                if (outLink.value.isNullOrEmpty()) {
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

            startSettingsPageEvent.observe(viewLifecycleOwner) {
                FilterDialogFragment()
                    .setBtnFourName(requireActivity().getString(R.string.filter_order_by_mypage_setting))
                    .setBtnFiveName(requireActivity().getString(R.string.filter_order_by_my_private_song))
                    .setListener(object : FilterDialogFragment.Listener {
                        override fun onClick(which: Int) {
                            if (which == 4) {
                                requireActivity().startAct<MypageSettingActivity>()
                            } else if (which == 5) {
                                requireActivity().startAct<MypagePrivateSongBoxActivity>()
                            }
                        }
                    }).show(childFragmentManager, null)
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
            initMyPageRefresh()
        }
        activityViewModel.setEnableRefresh(false)
    }

    override fun onDestroyView() {
        binding.vp.adapter = null
        clearChildFragment()
        super.onDestroyView()
    }

    /**
     * 마이페이지 피드 상세로 이동하는 최종 함수
     * @param pair MovePosition, 피드 페이징 데이터
     */
    fun moveToRootFeedDetail(triple: Triple<Int, String, PagingData<FeedContentsData>>) {
        val rootFragment = parentFragment
        DLogger.d("마이페이지 피드 상세 이동 처리 공통 함수 $rootFragment")
        // 메인탭 > 오른쪽 스와이프시 나오는 Fragment
        if (rootFragment is MainRightFragment) {
            rootFragment.moveToFeedDetail(triple.first, triple.second, triple.third)
        } else {
            val activity = requireActivity()
            if (activity is MainActivity) {
                val mainFragment = activity.supportFragmentManager
                    .fragments
                    .find { it is MainFragment } as? MainFragment
                DLogger.d("Fragment $mainFragment")
                mainFragment?.viewModel?.moveToFeedDetail(triple.first, triple.second, triple.third)
            } else if (activity is MyPageRootActivity) {
                DLogger.d("마이페이지 RootActivity 입니다.")
                activity.moveToFeedDetail(triple.first, triple.second, triple.third)
            }
        }
    }

    /**
     * MainRightFragment 에서 스와이프시
     * 좀더 디테일하게 데이터 호출하는 함수
     */
    fun handleMainRightApiCall() {
        viewModel.reqRefreshMyProfile()
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

    companion object {
        fun newInstance(bundle: Bundle? = null): Fragment {
            return MyPageFragment().apply {
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
                AppData.PREFIX_FEED
            )
        ) {

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
                AppData.PREFIX_FEED
            )
        ) {

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

    override fun onDetach() {
        super.onDetach()
        viewModel.onLoadingDismiss()
    }

    override fun onDestroy() {
        viewModel.onLoadingDismiss()
        setFragmentResult(ExtraCode.FRAGMENT_RESULT, bundleOf(ExtraCode.FRAGMENT_RESULT_CALL_BACK to false))
        super.onDestroy()
    }
}
