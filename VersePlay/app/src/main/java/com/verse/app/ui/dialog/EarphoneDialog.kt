package com.verse.app.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import com.verse.app.R
import com.verse.app.contants.SingType
import com.verse.app.databinding.DialogCommonVBinding
import com.verse.app.databinding.DialogEarphoneBinding
import com.verse.app.utility.DLogger

/**
 * Description : 이어폰 팝업
 *
 * Created by jhlee on 2023-04-27
 */
class EarphoneDialog(
    context: Context,
) : Dialog(context, R.style.EarPhoneDialog) {

    interface Listener {
        fun onClick(which: Int)
    }

    companion object {
        var IS_SHOW = false
    }

    private var binding: DialogEarphoneBinding? = null
    private var curSingType = SingType.SOLO
    private var listener: EarphoneDialog.Listener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        DialogEarphoneBinding.inflate(LayoutInflater.from(context)).apply {
            binding = this

            curType = run {
                curSingType
            }

            //취소 버튼
            setBtn(tvBtnConfirm ,0)
            setContentView(this.root)
        }

        setCanceledOnTouchOutside(false)
        setCancelable(false)
    }


    /**
     * 클릭 리스너
     */
    fun setListener(listener: EarphoneDialog.Listener): EarphoneDialog {
        this.listener = listener
        return this
    }

    /**
     * SingType
     */
    fun setSingType(type: SingType): EarphoneDialog {
        curSingType = type
        return this
    }

    /**
     * onClick
     */
    private fun setBtn(view: TextView, position: Int) {
        view.apply {
            setOnClickListener {
                dismiss()
                listener?.onClick(position)
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