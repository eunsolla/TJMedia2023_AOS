package com.verse.app.ui.mypage.activity

import android.os.Bundle
import android.view.WindowManager
import android.widget.LinearLayout
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.databinding.library.baseAdapters.BR
import com.verse.app.R
import com.verse.app.base.activity.BaseActivity
import com.verse.app.contants.VolumeType
import com.verse.app.databinding.ActivityMypageSettingPlayerBinding
import com.verse.app.extension.getActivity
import com.verse.app.ui.mypage.viewmodel.SettingPlayerViewModel
import com.verse.app.utility.DLogger
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingPlayerActivity :
    BaseActivity<ActivityMypageSettingPlayerBinding, SettingPlayerViewModel>() {
    override val layoutId = R.layout.activity_mypage_setting_player
    override val viewModel: SettingPlayerViewModel by viewModels()
    override val bindingVariable = BR.viewModel
    var isAuto: Boolean = false

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            finish()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        viewModel.deviceProvider.setDeviceStatusBarColor(getActivity()!!.window, R.color.white)
        this.onBackPressedDispatcher.addCallback(this, onBackPressedCallback) // 콜백 인스턴스

        with(viewModel) {

            mute.value = accountPref.getVolumeMute(this@SettingPlayerActivity)
            auto.value = accountPref.getVolumeAuto(this@SettingPlayerActivity)
            curVolume.value = accountPref.getCurVolume(this@SettingPlayerActivity)

            binding.apply {

                //자동볼륨조절 선택 ㅇ && 현재볼륨타입 선택 o
                if (auto.value && curVolume.value.isNotEmpty()) {
                    isAuto = true
                    if (curVolume.value == "1") {
                        curVolumeType.value = VolumeType.VOLUME1
                        settingVolumeLinearEnabled(binding.layoutVolume1, true)
                        settingVolumeTextEnabled(binding.tvVolume1, true)
                        settingVolumeImgEnabled(binding.iconVolume1, true)

                        settingVolumeLinearEnabled(binding.layoutVolume2, false)
                        settingVolumeTextEnabled(binding.tvVolume2, false)
                        settingVolumeImgEnabled(binding.iconVolume2, false)

                        settingVolumeLinearEnabled(binding.layoutVolume3, false)
                        settingVolumeTextEnabled(binding.tvVolume3, false)
                        settingVolumeImgEnabled(binding.iconVolume3, false)
                    }
                    if (curVolume.value == "2") {
                        curVolumeType.value = VolumeType.VOLUME2
                        settingVolumeLinearEnabled(binding.layoutVolume1, false)
                        settingVolumeTextEnabled(binding.tvVolume1, false)
                        settingVolumeImgEnabled(binding.iconVolume1, false)

                        settingVolumeLinearEnabled(binding.layoutVolume2, true)
                        settingVolumeTextEnabled(binding.tvVolume2, true)
                        settingVolumeImgEnabled(binding.iconVolume2, true)

                        settingVolumeLinearEnabled(binding.layoutVolume3, false)
                        settingVolumeTextEnabled(binding.tvVolume3, false)
                        settingVolumeImgEnabled(binding.iconVolume3, false)
                    }
                    if (curVolume.value == "3") {
                        curVolumeType.value = VolumeType.VOLUME3
                        settingVolumeLinearEnabled(binding.layoutVolume1, false)
                        settingVolumeTextEnabled(binding.tvVolume1, false)
                        settingVolumeImgEnabled(binding.iconVolume1, false)

                        settingVolumeLinearEnabled(binding.layoutVolume2, false)
                        settingVolumeTextEnabled(binding.tvVolume2, false)
                        settingVolumeImgEnabled(binding.iconVolume2, false)

                        settingVolumeLinearEnabled(binding.layoutVolume3, true)
                        settingVolumeTextEnabled(binding.tvVolume3, true)
                        settingVolumeImgEnabled(binding.iconVolume3, true)
                    }
                }

            }

            backpress.observe(this@SettingPlayerActivity) {
                finish()
            }

            saveData.observe(this@SettingPlayerActivity) {
                accountPref.setVolumeMute(mute.value)
                accountPref.setVolumeAuto(auto.value)
                accountPref.setCurVolume(curVolume.value)

                DLogger.d("saveData=> curSettingMute ${mute.value} /curAutoSettingState  ${auto.value} / curVolume ${curVolume.value}")

                if (mute.value != null && auto.value != null && curVolume.value != null) {
                    finish()
                }
            }

            // auto선택이 되어있을경우만
            startClickChecked.observe(this@SettingPlayerActivity) {
                curVolumeType.value = VolumeType.VOLUME2
            }

            // auto 선택 or mute선택일 경우
            // 자동 볼륨 조절 전부 select 해제
            typeBtnNotSelected.observe(this@SettingPlayerActivity) {
                curVolume.value = "0"
                settingVolumeLinearEnabled(binding.layoutVolume1, false)
                settingVolumeTextEnabled(binding.tvVolume1, false)
                settingVolumeImgEnabled(binding.iconVolume1, false)

                settingVolumeLinearEnabled(binding.layoutVolume2, false)
                settingVolumeTextEnabled(binding.tvVolume2, false)
                settingVolumeImgEnabled(binding.iconVolume2, false)

                settingVolumeLinearEnabled(binding.layoutVolume3, false)
                settingVolumeTextEnabled(binding.tvVolume3, false)
                settingVolumeImgEnabled(binding.iconVolume3, false)
            }


            // 볼륨값 선택 시 버튼 색상 변경
            curVolumeType.observe(this@SettingPlayerActivity) {
                if (!viewModel.auto.value) return@observe

                when (it) {
                    VolumeType.VOLUME1 -> {
                        curVolume.value = "1"
                        settingVolumeLinearEnabled(binding.layoutVolume1, true)
                        settingVolumeTextEnabled(binding.tvVolume1, true)
                        settingVolumeImgEnabled(binding.iconVolume1, true)

                        settingVolumeLinearEnabled(binding.layoutVolume2, false)
                        settingVolumeTextEnabled(binding.tvVolume2, false)
                        settingVolumeImgEnabled(binding.iconVolume2, false)

                        settingVolumeLinearEnabled(binding.layoutVolume3, false)
                        settingVolumeTextEnabled(binding.tvVolume3, false)
                        settingVolumeImgEnabled(binding.iconVolume3, false)
                    }

                    VolumeType.VOLUME2 -> {
                        curVolume.value = "2"
                        settingVolumeLinearEnabled(binding.layoutVolume1, false)
                        settingVolumeTextEnabled(binding.tvVolume1, false)
                        settingVolumeImgEnabled(binding.iconVolume1, false)

                        settingVolumeLinearEnabled(binding.layoutVolume2, true)
                        settingVolumeTextEnabled(binding.tvVolume2, true)
                        settingVolumeImgEnabled(binding.iconVolume2, true)

                        settingVolumeLinearEnabled(binding.layoutVolume3, false)
                        settingVolumeTextEnabled(binding.tvVolume3, false)
                        settingVolumeImgEnabled(binding.iconVolume3, false)
                    }

                    VolumeType.VOLUME3 -> {
                        curVolume.value = "3"
                        settingVolumeLinearEnabled(binding.layoutVolume1, false)
                        settingVolumeTextEnabled(binding.tvVolume1, false)
                        settingVolumeImgEnabled(binding.iconVolume1, false)

                        settingVolumeLinearEnabled(binding.layoutVolume2, false)
                        settingVolumeTextEnabled(binding.tvVolume2, false)
                        settingVolumeImgEnabled(binding.iconVolume2, false)

                        settingVolumeLinearEnabled(binding.layoutVolume3, true)
                        settingVolumeTextEnabled(binding.tvVolume3, true)
                        settingVolumeImgEnabled(binding.iconVolume3, true)
                    }
                }
            }
        }

    }

    private fun settingVolumeLinearEnabled(v: LinearLayout, isSelected: Boolean) {
        if (isSelected) {
            v.background = ContextCompat.getDrawable(
                this@SettingPlayerActivity,
                R.drawable.edit_complete_border
            )
        } else {
            v.background = ContextCompat.getDrawable(
                this@SettingPlayerActivity,
                R.drawable.edit_border
            )
        }
    }

    private fun settingVolumeTextEnabled(v: AppCompatTextView, isSelected: Boolean) {
        if (isSelected) {
            v.setTextColor(
                ContextCompat.getColor(
                    this@SettingPlayerActivity,
                    R.color.color_2fc2ff
                )
            )
        } else {
            v.setTextColor(
                ContextCompat.getColor(
                    this@SettingPlayerActivity,
                    R.color.color_adadad
                )
            )
        }
    }

    private fun settingVolumeImgEnabled(v: AppCompatImageView, isSelected: Boolean) {
        if (isSelected) {
            when (v) {
                binding.iconVolume1 -> {
                    v.setImageDrawable(
                        ContextCompat.getDrawable(
                            this@SettingPlayerActivity, R.drawable.volume_abled_1
                        )
                    )

                }
                binding.iconVolume2 -> {
                    v.setImageDrawable(
                        ContextCompat.getDrawable(
                            this@SettingPlayerActivity, R.drawable.volume_abled_2
                        )
                    )

                }
                binding.iconVolume3 -> {
                    v.setImageDrawable(
                        ContextCompat.getDrawable(
                            this@SettingPlayerActivity, R.drawable.volume_abled_3
                        )
                    )
                }
            }
        } else {
            when (v) {
                binding.iconVolume1 -> {
                    v.setImageDrawable(
                        ContextCompat.getDrawable(
                            this@SettingPlayerActivity, R.drawable.volume_disabled_1
                        )
                    )

                }
                binding.iconVolume2 -> {
                    v.setImageDrawable(
                        ContextCompat.getDrawable(
                            this@SettingPlayerActivity, R.drawable.volume_disabled_2
                        )
                    )

                }
                binding.iconVolume3 -> {
                    v.setImageDrawable(
                        ContextCompat.getDrawable(
                            this@SettingPlayerActivity, R.drawable.volume_disabled_3
                        )
                    )
                }
            }
        }
    }

}