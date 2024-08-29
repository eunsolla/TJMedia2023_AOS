package com.verse.app.ui.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import androidx.annotation.StringRes
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import com.verse.app.R
import com.verse.app.contants.ShowContentsType
import com.verse.app.extension.toEmptyStr
import com.verse.app.utility.DLogger


/**
 * Description : 컨텐츠 노출 변경
 *
 * Created by jhlee on 2023-06-07
 */
class ChangeContentsDialog(private val context: Context) {

    interface Listener {
        fun onClick(which: Int, type: String)
    }

    private val rootView: View by lazy {
        View.inflate(context, R.layout.change_contents_dialog, null)
    }
    private val llButtons: LinearLayoutCompat by lazy { rootView.findViewById(R.id.llButton) }
    private val positiveButton: AppCompatTextView by lazy { rootView.findViewById(R.id.tvPositive) }
    private val negativeButton: AppCompatTextView by lazy { rootView.findViewById(R.id.tvNegative) }
    private val ivOnlyMe: AppCompatImageView by lazy { rootView.findViewById(R.id.iv_only_me) }
    private val ivFriends: AppCompatImageView by lazy { rootView.findViewById(R.id.iv_friends) }
    private val ivAll: AppCompatImageView by lazy { rootView.findViewById(R.id.iv_all) }
    private val clEmpty: View by lazy { rootView.findViewById(R.id.cl_empty) }
    private var tvContents: AppCompatTextView? = null
    private var ivIcon: AppCompatImageView? = null
    private var isCancel: Boolean = false
    private var listener: Listener? = null
    private var buttonCnt = 0
    private var currentTypeCode: String = ""

    private lateinit var dialog: Dialog

    companion object {
        const val POSITIVE: Int = 1
        const val NEGATIVE: Int = 2
    }

    fun setContents(@StringRes id: Int): ChangeContentsDialog {
        return setContents(context.getString(id))
    }

    fun setContents(text: String): ChangeContentsDialog {
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

    fun setNegativeButton(@StringRes id: Int): ChangeContentsDialog {
        // ID 유효성 체크.
        return if (id == View.NO_ID) {
            this
        } else {
            setNegativeButton(context.getString(id))
        }
    }

    fun setNegativeButton(text: String): ChangeContentsDialog {
        llButtons.visibility = View.VISIBLE
        negativeButton.apply {
            this.text = text
            visibility = View.VISIBLE
            setOnClickListener {
                dismiss()
                listener?.onClick(NEGATIVE, currentTypeCode)
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

    fun setPositiveButton(@StringRes id: Int): ChangeContentsDialog {
        // ID 유효성 체크.
        return if (id == View.NO_ID) {
            this
        } else {
            setPositiveButton(context.getString(id))
        }
    }

    fun setPositiveButton(text: String): ChangeContentsDialog {
        positiveButton.apply {
            this.text = text
            visibility = View.VISIBLE
            setOnClickListener {
                dismiss()
                listener?.onClick(POSITIVE, currentTypeCode)
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

    private fun setSwitching() {

        ivOnlyMe.apply {
            setOnClickListener {
                handleState(ShowContentsType.PRIVATE.code)
            }
        }

        ivFriends.apply {
            setOnClickListener {
                handleState(ShowContentsType.ALLOW_FRIENDS.code)
            }
        }

        ivAll.apply {
            setOnClickListener {
                handleState(ShowContentsType.ALLOW_ALL.code)
            }
        }
    }

    private fun handleState(type: String) {
        when (type) {
            ShowContentsType.PRIVATE.code -> {
                ivOnlyMe.isSelected = !ivOnlyMe.isSelected
                ivFriends.isSelected = false
                ivAll.isSelected = false
                clEmpty.visibility = View.GONE

                currentTypeCode = if(ivOnlyMe.isSelected){
                    type
                }else{
                    ""
                }
            }

            ShowContentsType.ALLOW_FRIENDS.code -> {
                ivOnlyMe.isSelected = false
                ivFriends.isSelected = !ivFriends.isSelected
                ivAll.isSelected = false

                currentTypeCode = if(ivFriends.isSelected){
                    type
                }else{
                    ""
                }
            }

            ShowContentsType.ALLOW_ALL.code -> {
                ivOnlyMe.isSelected = false
                ivAll.isSelected = !ivAll.isSelected

                if (ivAll.isSelected) {
                    currentTypeCode = type
                    ivFriends.isSelected = ivAll.isSelected
                    clEmpty.visibility = View.VISIBLE
                } else {
                    currentTypeCode = ShowContentsType.ALLOW_FRIENDS.code
                    clEmpty.visibility = View.GONE
                }
            }
            else -> {
            }
        }
    }

    fun setCancelable(isCancel: Boolean): ChangeContentsDialog {
        this.isCancel = isCancel
        return this
    }

    fun setCurrentType(type:String): ChangeContentsDialog {
        type?.let {
            handleState(it)
        }
        return this
    }

    fun setListener(listener: Listener): ChangeContentsDialog {
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

        setSwitching()

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