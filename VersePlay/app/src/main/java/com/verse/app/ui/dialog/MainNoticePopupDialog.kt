package com.verse.app.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import androidx.viewpager2.widget.ViewPager2
import com.verse.app.R
import com.verse.app.databinding.MainNoticePopupDialogBinding
import com.verse.app.extension.viewPagerRecyclerViewNotifyAll
import com.verse.app.model.common.NoticeImageData
import com.verse.app.model.common.NoticeResponse
import com.verse.app.repository.preferences.AccountPref
import com.verse.app.ui.adapter.MainNoticePopupViewPagerAdapter
import com.verse.app.utility.DLogger


/**
 * Description : 메인 공지 팝업
 *
 * Created by jhlee on 2023-02-14
 */
class MainNoticePopupDialog constructor(
    context: Context,
    private val noticePopupData: NoticeResponse?,
    private var preference: AccountPref,
    private val clickListener: View.OnClickListener,
) : Dialog(context, R.style.CommonOneBtnDialog) {

    private var isCloseOptionCheckBox: Boolean = false
    private var binding: MainNoticePopupDialogBinding? = null

    companion object {
        const val POSITIVE: Int = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        MainNoticePopupDialogBinding.inflate(LayoutInflater.from(context)).apply {
            binding = this

            noticePopupData?.let {
                it.result?.let {

                    val list=  it.imageList.filter { it.svcPopImgPath.isNotEmpty() }

                    list?.let {

                        val adapter = MainNoticePopupViewPagerAdapter(list as ArrayList<NoticeImageData>, mCloseClickListener)

                        // 페이지네이션 개수 지정
                        initPagination(it.size)

                        binding?.let {
                            it.noticeViewPager.adapter = adapter
                            it.noticeViewPager.viewPagerRecyclerViewNotifyAll()
                            setOnPageChangeListener()
                        }
                    }
                }
            }

            this.tvPositive.setOnClickListener(mCloseClickListener)
            this.noticeViewPager.setOnClickListener(mCloseClickListener)
            this.noticeCloseOptionTextView.setOnClickListener(mCheckBoxClickListener)
            this.roundCheckBoxImageView.setOnClickListener(mCheckBoxClickListener)

            // 팝업 닫기 옵션 유형 기본값
            initCheckBoxOption(1)

            setContentView(this.root)
        }
        setCanceledOnTouchOutside(false)
        setCancelable(false)
    }

    private fun setOnPageChangeListener() {
        binding?.noticeViewPager?.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                DLogger.d("onPageSelected : " + position)
                setPagination(position)
                super.onPageSelected(position)
            }
        })
    }

    override fun show() {
        super.show()
    }

    override fun dismiss() {
        // 체크 박스 선택 여부에 따라 팝업 노출 정보 저장
        if (isCloseOptionCheckBox) {
            preference.setRecentMainPopupMngCd(noticePopupData!!.result!!.svcPopMngCd)
            preference.setShowMainPopup(!isCloseOptionCheckBox)
        }

        super.dismiss()
    }

    fun initPagination(pageCount: Int) {
        binding?.let {
            when (pageCount) {
                1 -> {
                    it.paginationImageView1.setImageResource(R.drawable.on_page_dot)
                    it.paginationImageView2.setImageResource(R.drawable.off_page_dot)
                    it.paginationImageView3.setImageResource(R.drawable.off_page_dot)
                    it.paginationImageView4.setImageResource(R.drawable.off_page_dot)
                    it.paginationImageView5.setImageResource(R.drawable.off_page_dot)

                    it.paginationImageView1.visibility = View.VISIBLE
                    it.paginationImageView2.visibility = View.GONE
                    it.paginationImageView3.visibility = View.GONE
                    it.paginationImageView4.visibility = View.GONE
                    it.paginationImageView5.visibility = View.GONE
                }

                2 -> {
                    it.paginationImageView1.setImageResource(R.drawable.on_page_dot)
                    it.paginationImageView2.setImageResource(R.drawable.off_page_dot)
                    it.paginationImageView3.setImageResource(R.drawable.off_page_dot)
                    it.paginationImageView4.setImageResource(R.drawable.off_page_dot)
                    it.paginationImageView5.setImageResource(R.drawable.off_page_dot)

                    it.paginationImageView1.visibility = View.VISIBLE
                    it.paginationImageView2.visibility = View.VISIBLE
                    it.paginationImageView3.visibility = View.GONE
                    it.paginationImageView4.visibility = View.GONE
                    it.paginationImageView5.visibility = View.GONE
                }

                3 -> {
                    it.paginationImageView1.setImageResource(R.drawable.on_page_dot)
                    it.paginationImageView2.setImageResource(R.drawable.off_page_dot)
                    it.paginationImageView3.setImageResource(R.drawable.off_page_dot)
                    it.paginationImageView4.setImageResource(R.drawable.off_page_dot)
                    it.paginationImageView5.setImageResource(R.drawable.off_page_dot)

                    it.paginationImageView1.visibility = View.VISIBLE
                    it.paginationImageView2.visibility = View.VISIBLE
                    it.paginationImageView3.visibility = View.VISIBLE
                    it.paginationImageView4.visibility = View.GONE
                    it.paginationImageView5.visibility = View.GONE
                }

                4 -> {
                    it.paginationImageView1.setImageResource(R.drawable.on_page_dot)
                    it.paginationImageView2.setImageResource(R.drawable.off_page_dot)
                    it.paginationImageView3.setImageResource(R.drawable.off_page_dot)
                    it.paginationImageView4.setImageResource(R.drawable.off_page_dot)
                    it.paginationImageView5.setImageResource(R.drawable.off_page_dot)

                    it.paginationImageView1.visibility = View.VISIBLE
                    it.paginationImageView2.visibility = View.VISIBLE
                    it.paginationImageView3.visibility = View.VISIBLE
                    it.paginationImageView4.visibility = View.VISIBLE
                    it.paginationImageView5.visibility = View.GONE
                }

                5 -> {
                    it.paginationImageView1.setImageResource(R.drawable.on_page_dot)
                    it.paginationImageView2.setImageResource(R.drawable.off_page_dot)
                    it.paginationImageView3.setImageResource(R.drawable.off_page_dot)
                    it.paginationImageView4.setImageResource(R.drawable.off_page_dot)
                    it.paginationImageView5.setImageResource(R.drawable.off_page_dot)

                    it.paginationImageView1.visibility = View.VISIBLE
                    it.paginationImageView2.visibility = View.VISIBLE
                    it.paginationImageView3.visibility = View.VISIBLE
                    it.paginationImageView4.visibility = View.VISIBLE
                    it.paginationImageView5.visibility = View.VISIBLE
                }
            }
        }
    }

    fun setPagination(selectIndex: Int) {
        binding?.let {
            when (selectIndex) {
                0 -> {
                    it.paginationImageView1.setImageResource(R.drawable.on_page_dot)
                    it.paginationImageView2.setImageResource(R.drawable.off_page_dot)
                    it.paginationImageView3.setImageResource(R.drawable.off_page_dot)
                    it.paginationImageView4.setImageResource(R.drawable.off_page_dot)
                    it.paginationImageView5.setImageResource(R.drawable.off_page_dot)
                }

                1 -> {
                    it.paginationImageView1.setImageResource(R.drawable.off_page_dot)
                    it.paginationImageView2.setImageResource(R.drawable.on_page_dot)
                    it.paginationImageView3.setImageResource(R.drawable.off_page_dot)
                    it.paginationImageView4.setImageResource(R.drawable.off_page_dot)
                    it.paginationImageView5.setImageResource(R.drawable.off_page_dot)
                }

                2 -> {
                    it.paginationImageView1.setImageResource(R.drawable.off_page_dot)
                    it.paginationImageView2.setImageResource(R.drawable.off_page_dot)
                    it.paginationImageView3.setImageResource(R.drawable.on_page_dot)
                    it.paginationImageView4.setImageResource(R.drawable.off_page_dot)
                    it.paginationImageView5.setImageResource(R.drawable.off_page_dot)
                }

                3 -> {
                    it.paginationImageView1.setImageResource(R.drawable.off_page_dot)
                    it.paginationImageView2.setImageResource(R.drawable.off_page_dot)
                    it.paginationImageView3.setImageResource(R.drawable.off_page_dot)
                    it.paginationImageView4.setImageResource(R.drawable.on_page_dot)
                    it.paginationImageView5.setImageResource(R.drawable.off_page_dot)
                }

                4 -> {
                    it.paginationImageView1.setImageResource(R.drawable.off_page_dot)
                    it.paginationImageView2.setImageResource(R.drawable.off_page_dot)
                    it.paginationImageView3.setImageResource(R.drawable.off_page_dot)
                    it.paginationImageView4.setImageResource(R.drawable.off_page_dot)
                    it.paginationImageView5.setImageResource(R.drawable.on_page_dot)
                }
            }
        }
    }

    fun initCheckBoxOption(closeOption: Int) {
        when (closeOption) {
            // 다시 보지 않기
            1 -> {
                binding?.let {
                    it.noticeCloseOptionTextView.setText(context.getString(R.string.main_notice_popup_option_1))
                }
            }
            // 3일 동안 보지 않기
            2 -> {
                binding?.let {
                    it.noticeCloseOptionTextView.setText(context.getString(R.string.main_notice_popup_option_2))
                }
            }
            // 오늘 하루 보지 않기
            3 -> {
                binding?.let {
                    it.noticeCloseOptionTextView.setText(context.getString(R.string.main_notice_popup_option_3))
                }
            }
            // 다시 보지 않기(기본)
            else -> {
                binding?.let {
                    it.noticeCloseOptionTextView.setText(context.getString(R.string.main_notice_popup_option_1))
                }
            }
        }
    }

    fun toggleCheckBox(isCheck: Boolean) {
        isCloseOptionCheckBox = isCheck

        if (isCheck) {
            binding?.let {
                it.roundCheckBoxImageView.setImageResource(R.drawable.on_round_check_box)
            }
        } else {
            binding?.let {
                it.roundCheckBoxImageView.setImageResource(R.drawable.off_round_check_box)
            }
        }
    }

    private val mCloseClickListener = View.OnClickListener {
        clickListener.onClick(it)
        dismiss()
    }

    private val mCheckBoxClickListener = View.OnClickListener {
        toggleCheckBox(!isCloseOptionCheckBox)
    }
}