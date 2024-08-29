package com.verse.app.ui.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.View
import android.view.WindowManager
import androidx.annotation.StringRes
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import com.verse.app.R
import com.verse.app.contants.AppData
import com.verse.app.utility.DLogger


/**
 * Description : 공통 팝업
 *
 * Created by jhlee on 2023-02-14
 */
class CommonDialog(private val context: Context) {

    interface Listener {
        fun onClick(which: Int)
    }

    private val rootView: View by lazy {
        View.inflate(context, R.layout.common_dialog, null)
    }
    private val llButtons: LinearLayoutCompat by lazy { rootView.findViewById(R.id.llButton) }
    private val positiveButton: AppCompatTextView by lazy { rootView.findViewById(R.id.tvPositive) }
    private val negativeButton: AppCompatTextView by lazy { rootView.findViewById(R.id.tvNegative) }
    private var tvTitle: AppCompatTextView? = null
    private var tvContents: AppCompatTextView? = null
    private var ivIcon: AppCompatImageView? = null
    private var isCancel: Boolean = false
    private var listener: Listener? = null
    private var buttonCnt = 0

    private lateinit var dialog: Dialog

    companion object {
        const val POSITIVE: Int = 1
        const val NEGATIVE: Int = 2
    }

    fun setTitle(@StringRes id: Int): CommonDialog {
        return setTitle(context.getString(id))
    }
    fun setTitle(text: String): CommonDialog {
        if (tvTitle == null) {
            tvTitle = rootView.findViewById(R.id.tvTitle)
        }

        if (text.isNotEmpty()) {
            tvTitle?.let { textView ->
                textView.visibility = View.VISIBLE
                textView.text = HtmlCompat.fromHtml(
                    text.replace("\n", "<br>"),
                    HtmlCompat.FROM_HTML_MODE_LEGACY
                )
            }
        }
        return this
    }

    fun setContents(@StringRes id: Int): CommonDialog {
        return setContents(context.getString(id))
    }

    fun setContents(text: String): CommonDialog {
        if (tvContents == null) {
            tvContents = rootView.findViewById(R.id.tvContents)
        }

        if (text.isNotEmpty()) {
            tvContents?.let { textView ->
                textView.visibility = View.VISIBLE
                textView.text = HtmlCompat.fromHtml(
                    text.replace("\n", "<br>"),
                    HtmlCompat.FROM_HTML_MODE_LEGACY
                )
            }
        }
        return this
    }

    fun setNegativeButton(@StringRes id: Int): CommonDialog {
        // ID 유효성 체크.
        return if (id == View.NO_ID) {
            this
        } else {
            setNegativeButton(context.getString(id))
        }
    }

    fun setNegativeButton(text: String): CommonDialog {
        llButtons.visibility = View.VISIBLE
        negativeButton.apply {
            this.text = text
            visibility = View.VISIBLE
            setOnClickListener {
                dismiss()
                listener?.onClick(NEGATIVE)
                negativeButton.setBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        R.color.color_2fc2ff
                    )
                )
                negativeButton.setTextColor(ContextCompat.getColor(context, R.color.white))
            }
        }
        buttonCnt++
        return this
    }

    // 상단 아이콘 세팅
    fun setIcon(text: String): CommonDialog {

        if (text.isNullOrEmpty()) {
            if (ivIcon == null) {
                ivIcon = rootView.findViewById(R.id.iv_icon)
                ivIcon?.visibility = View.GONE
            }
        } else {

            if (ivIcon == null) {
                ivIcon = rootView.findViewById(R.id.iv_icon)
                ivIcon?.visibility = View.VISIBLE
            }

            if (text == AppData.POPUP_HELP) {
                ivIcon?.setImageDrawable(
                    ContextCompat.getDrawable(
                        context, R.drawable.ic_help
                    )
                )
            } else if (text == AppData.POPUP_WARNING) {
                ivIcon?.setImageDrawable(
                    ContextCompat.getDrawable(
                        context, R.drawable.ic_warning
                    )
                )
            } else if (text == AppData.POPUP_COMPLETE){
                ivIcon?.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,R.drawable.ic_complete
                    )
                )
            } else if (text == AppData.POPUP_BLOCK){
                ivIcon?.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,R.drawable.ic_block_2
                    )
                )
            } else if (text == AppData.POPUP_UNINTEREST){
                ivIcon?.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,R.drawable.ic_more_not_interested_2
                    )
                )
            }
        }

        return this
    }

    fun setPositiveButton(@StringRes id: Int): CommonDialog {
        // ID 유효성 체크.
        return if (id == View.NO_ID) {
            this
        } else {
            setPositiveButton(context.getString(id))
        }
    }

    fun setPositiveButton(text: String): CommonDialog {
        positiveButton.apply {
            this.text = text
            visibility = View.VISIBLE
            setOnClickListener {
                dismiss()
                listener?.onClick(POSITIVE)
                positiveButton.setBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        R.color.color_2fc2ff
                    )
                )
                positiveButton.setTextColor(ContextCompat.getColor(context, R.color.white))
            }
        }
        buttonCnt++
        return this
    }

    fun setCancelable(isCancel: Boolean): CommonDialog {
        this.isCancel = isCancel
        return this
    }

    fun setListener(listener: Listener): CommonDialog {
        this.listener = listener
        return this
    }

    fun show() {
        // 원버튼인경우
        if (buttonCnt == 1) {
            if (positiveButton.visibility == View.VISIBLE) {
                negativeButton.visibility = View.GONE
            } else if (negativeButton.visibility == View.VISIBLE) {
                positiveButton.visibility = View.GONE
            }
        } else {
            // 투버튼인 경우
//            vDividerLine.visibility = View.VISIBLE
//            vTopLine.visibility = View.VISIBLE
        }

        val builder = AlertDialog.Builder(context, R.style.CommonOneBtnDialog).setView(rootView)
        builder.setCancelable(isCancel)
        runCatching {
            dialog = builder.create()
            dialog.window?.setBackgroundDrawableResource(com.google.android.material.R.color.mtrl_btn_transparent_bg_color)
            dialog.show()
        }
    }

    fun show(dismissListener: DialogInterface.OnDismissListener) {
        val builder = AlertDialog.Builder(context, R.style.CommonOneBtnDialog).setView(rootView)
        builder.setCancelable(isCancel)
        dialog = builder.create().apply {
            try {
                dialog.window?.setBackgroundDrawableResource(com.google.android.material.R.color.mtrl_btn_transparent_bg_color)
                setOnDismissListener(dismissListener)
                show()
            } catch (ex: WindowManager.BadTokenException) {
                DLogger.e(ex.toString())
            }
        }
    }

    fun dismiss() {
        Handler(Looper.getMainLooper()).post {
            try {
                dialog.dismiss()
            } catch (ex: WindowManager.BadTokenException) {
                DLogger.e("Dismiss BadToken Error ${ex.message}")
            }
        }
    }

}