package com.verse.app.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.verse.app.R
import com.verse.app.contants.AppData
import com.verse.app.databinding.DialogCommonVBinding
import com.verse.app.utility.DLogger

/**
 * Description :  vertical popup
 *
 * Created by jhlee on 2023-04-27
 */
class CommonVerticalDialog(
    context: Context,
) : Dialog(context, R.style.CommonVerticalDialog) {

    private val rootView: View by lazy {
        View.inflate(context, R.layout.dialog_common_v, null)
    }

    interface Listener {
        fun onClick(which: Int)
    }

    companion object {
        var IS_SHOW = false
        const val POSITION_1: Int = 1
        const val POSITION_2: Int = 2
        const val POSITION_3: Int = 3
        const val POSITION_4: Int = 4
        const val POSITION_5: Int = 5
        const val POSITION_6: Int = 6
        const val POSITION_7: Int = 7
        const val POSITION_99: Int = 99
    }

    private var binding: DialogCommonVBinding? = null

    private var imgUrlPath: String = ""        //상단 이미지
    private var messageText: String? = ""       //메세지
    private var btnOneText: String = ""        //버튼 text
    private var btnTwoText: String = ""        //버튼 text
    private var btnThreeText: String = ""        //버튼 text
    private var btnFourText: String = ""        //버튼 text
    private var btnFiveText: String = ""        //버튼 text
    private var btnSixText: String = ""        //버튼 text
    private var btnSevenText: String = ""        //버튼 text
    private var btnLastlText: String = ""        //취소
    private var iconText: String = ""        // 이미지
    private var listener: Listener? = null
    private var ivIcon: ImageView? = null // 아이콘 이미지
    private var isCanDismiss: Boolean = true       //dismiss 여부

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        requestWindowFeature(Window.FEATURE_NO_TITLE)
//        window?.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))

        DialogCommonVBinding.inflate(LayoutInflater.from(context)).apply {
            binding = this

            if (!imgUrlPath.isNullOrEmpty()) {
                imgUrl = run {
                    imgUrlPath
                }
            }
            if (!messageText.isNullOrEmpty()) {
                message = run {
                    messageText
                }
            }
            if (!btnOneText.isNullOrEmpty()) {
                btnOne = run {
                    btnOneText
                }
                setBtn(tvBtnOne, POSITION_1)
            }
            if (!btnTwoText.isNullOrEmpty()) {
                btnTwo = run {
                    btnTwoText
                }
                setBtn(tvBtnTwo, POSITION_2)
            }
            if (!btnThreeText.isNullOrEmpty()) {
                btnThree = run {
                    btnThreeText
                }
                setBtn(tvBtnThree, POSITION_3)
            }
            if (!btnFourText.isNullOrEmpty()) {
                btnFour = run {
                    btnFourText
                }
                setBtn(tvBtnFour, POSITION_4)
            }
            if (!btnFiveText.isNullOrEmpty()) {
                btnFive = run {
                    btnFiveText
                }
                setBtn(tvBtnFive, POSITION_5)
            }
            if (!btnSixText.isNullOrEmpty()) {
                btnSix = run {
                    btnSixText
                }
                setBtn(tvBtnSix, POSITION_6)
            }
            if (!btnSevenText.isNullOrEmpty()) {
                btnSeven = run {
                    btnSevenText
                }
                setBtn(tvBtnSeven, POSITION_7)
            }
            // 취소 버튼
            setBtn(tvBtnCancel, POSITION_99)
            // 아이콘
            setIcon(iconText)
            setContentView(this.root)
        }
        setCanceledOnTouchOutside(false)
        setCancelable(false)
    }


    /**
     * 클릭 리스너
     */
    fun setListener(listener: Listener): CommonVerticalDialog {
        this.listener = listener
        return this
    }

    /**
     * 상단 이미지
     */
    fun setHeaderImageUrl(url: String): CommonVerticalDialog {
        imgUrlPath = url
        return this
    }

    /**
     * 메인 메세지
     */
    fun setContents(text: String): CommonVerticalDialog {
        messageText = text
        return this
    }

    /**
     * 버튼
     */
    fun setBtnOne(text: String): CommonVerticalDialog {
        btnOneText = text
        return this
    }

    /**
     * 버튼 2
     */
    fun setBtnTwo(text: String): CommonVerticalDialog {
        btnTwoText = text
        return this
    }

    /**
     * 버튼 3
     */
    fun setBtnThree(text: String): CommonVerticalDialog {
        btnThreeText = text
        return this
    }

    /**
     * 버튼 4
     */
    fun setBtnFour(text: String): CommonVerticalDialog {
        btnFourText = text
        return this
    }

    /**
     * 버튼 5
     */
    fun setBtnFive(text: String): CommonVerticalDialog {
        btnFiveText = text
        return this
    }

    /**
     * 버튼 6
     */
    fun setBtnSix(text: String): CommonVerticalDialog {
        btnSixText = text
        return this
    }

    /**
     * 버튼 7
     */
    fun setBtnSeven(text: String): CommonVerticalDialog {
        btnSevenText = text
        return this
    }

    /**
     * 취소 /닫기 버튼
     */
    fun setLastText(text: String): CommonVerticalDialog {
        btnLastlText = text
        return this
    }

    fun setCanDismiss(isDismiss: Boolean): CommonVerticalDialog {
        isCanDismiss = isDismiss
        return this
    }

    /**
     * 아이콘 세팅
     */
    fun setIcon(text: String): CommonVerticalDialog {

        if (text.isNullOrEmpty()) {
            ivIcon = rootView.findViewById(R.id.iv_v_icon)
            ivIcon?.visibility = View.GONE
        } else {
            when (text) {
                AppData.POPUP_CHANGE_PROFILE -> {
                    ivIcon = rootView.findViewById(R.id.iv_v_icon)
                    ivIcon?.visibility = View.VISIBLE
                    ivIcon?.setImageResource(R.drawable.ic_profile_change)
                }
            }
        }
        return this
    }

    /**
     * onClick
     */
    private fun setBtn(view: TextView, position: Int) {
        view.apply {
            setOnClickListener {
                if (isCanDismiss) {
                    dismiss()
                    listener?.onClick(position)
                } else {
                    if (position == POSITION_1 || position == POSITION_2 ||  position == POSITION_4  ||  position == POSITION_99) {
                        listener?.onClick(position)
                    }else{
                        dismiss()
                        listener?.onClick(position)
                    }
                }
            }
        }
    }

    override fun show() {
        runCatching {
            if (!IS_SHOW) {
                super.show()
                IS_SHOW = true
            }
        }
    }

    override fun dismiss() {
        runCatching {
            super.dismiss()
            IS_SHOW = false
        }.onFailure {
            DLogger.d("dismiss $it")
        }
    }
}