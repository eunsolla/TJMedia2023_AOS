package com.verse.app.ui.intro.activity


import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.databinding.library.baseAdapters.BR
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.verse.app.R
import com.verse.app.base.activity.BaseActivity
import com.verse.app.databinding.ActivityTutorialBinding
import com.verse.app.extension.getActivity
import com.verse.app.ui.intro.viewmodel.TutorialViewModel
import com.verse.app.ui.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint


/**
 * Description : Tutorial Activity Class
 *
 * Created by esna on 2023-03-17
 */

@AndroidEntryPoint
class TutorialActivity :
    BaseActivity<ActivityTutorialBinding, TutorialViewModel>() {
    override val layoutId = R.layout.activity_tutorial
    override val viewModel: TutorialViewModel by viewModels()
    override val bindingVariable = BR.viewModel
    private var isUserGuidePage: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        viewModel.deviceProvider.setDeviceStatusBarColor(getActivity()!!.window, R.color.white)

        val i = intent
        isUserGuidePage = i.getIntExtra("isUserGuide",0)

        with(viewModel) {

            // 튜토리얼인지 설정->유저가이드인지 판단 후 상단 아이콘 변경
            // true 유저가이드 false 튜토리얼
            // 유저가이드인경우 < 버튼 -> 뒤로가기
            if (isUserGuidePage == 1){
                isUserGuide.postValue(true)
            } else {
                isUserGuide.postValue(false)
            }

            // 뒤로가기
            backpress.observe(this@TutorialActivity) {
                finish()
            }

            // 버튼 클릭 시 로그인 팝업으로 이동
            startMain.observe(this@TutorialActivity) {
                val currentPosition: Int = binding.tutorialViewPager.currentItem

                if (!isUserGuide.value) {
                    if (currentPosition == viewModel.getImageList().size - 1) {
                        super.finish()

                        // 최초 국가 및 언어 설정 완료
                        accountPref.setIntroSettingPage(true)
                        // 메인 화면으로 이동
                        Intent(this@TutorialActivity,MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                            this@TutorialActivity.startActivity(this)
                            this@TutorialActivity.overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out)
                        }
                    } else {
                        binding.tutorialViewPager.setCurrentItem(currentPosition + 1, true)
                        return@observe
                    }

                } else {
                    if (currentPosition == viewModel.getImageList().size - 1) {
                        super.finish()
                    } else {
                        binding.tutorialViewPager.setCurrentItem(currentPosition + 1, true)
                        return@observe
                    }
                }
            }
        }

        setOnPageChangeListener()
    }

    private fun setOnPageChangeListener() {
        binding.tutorialViewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                setIndicator(position)
            }
        })
        binding.tutorialViewPager.currentItem = 0
        setIndicator(0)
    }

    private fun setIndicator(position: Int) {
        when (position) {
            0 -> {
                viewModel.tvSelected(false)
            }
            1 -> {
                viewModel.tvSelected(false)
            }
            2 -> {
                viewModel.tvSelected(false)
            }
            3 -> {
                viewModel.tvSelected(false)
            }
            4 -> {
                viewModel.tvSelected(true)
            }
            else -> {
                viewModel.tvSelected(false)
            }
        }
    }


    // 뒤로가기 버튼 막기
    override fun onBackPressed() {
        if (isUserGuidePage==1) super.onBackPressed()
    }

}