package com.verse.app.ui.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.annotation.StringRes
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import com.verse.app.R
import com.verse.app.contants.AppData
import com.verse.app.databinding.DialogCommonVBinding
import com.verse.app.databinding.DialogServerCheckBinding
import com.verse.app.model.common.VersionData
import com.verse.app.utility.DLogger
import com.verse.app.utility.LocaleUtils
import com.verse.app.widget.views.CustomConstraintLayout
import com.verse.app.widget.views.CustomTextView

/**
 * Description : 서버 점검 팝업
 *
 * Created by esna on 2023-03-23
 */

class ServerCheckDialog(private val context: Context) {

    interface Listener {
        fun onClick(which: Int)
    }

    private val rootView: View by lazy {
        View.inflate(context, R.layout.dialog_server_check, null)
    }
    private val llButtons: LinearLayoutCompat by lazy { rootView.findViewById(R.id.llButton) }
    private val positiveButton: AppCompatTextView by lazy { rootView.findViewById(R.id.tvPositive) }
    private val negativeButton: AppCompatTextView by lazy { rootView.findViewById(R.id.tvNegative) }


    private val tvTitle: CustomTextView by lazy { rootView.findViewById(R.id.tv_title) }
    private val tvSubTitle: CustomTextView by lazy { rootView.findViewById(R.id.tv_sub_title) }
    private val tvCheckDateTitle: CustomTextView by lazy { rootView.findViewById(R.id.tv_check_date_title) }
    private val tvCheckContentsTitle: CustomTextView by lazy { rootView.findViewById(R.id.tv_check_contents_title) }
    private val tvStDt: CustomTextView by lazy { rootView.findViewById(R.id.tv_st_dt) }
    private val tvDateSep: CustomTextView by lazy { rootView.findViewById(R.id.tv_date_sep) }
    private val tvFnDt: CustomTextView by lazy { rootView.findViewById(R.id.tv_fn_dt) }
    private val tvCheckContents: CustomTextView by lazy { rootView.findViewById(R.id.tv_check_contents) }

    private val clCheckContents: CustomConstraintLayout by lazy { rootView.findViewById(R.id.cl_check_contents) }
    private val tvBottomNotice: CustomTextView by lazy { rootView.findViewById(R.id.tv_bottom_notice) }

    private var isCancel: Boolean = false
    private var listener: CommonDialog.Listener? = null
    private var buttonCnt = 0
    private var versionData: VersionData? = null

    private lateinit var dialog: Dialog

    companion object {
        const val POSITIVE: Int = 1
        const val NEGATIVE: Int = 2
    }

    fun setDialogData(versionData: VersionData): ServerCheckDialog {
        this.versionData = versionData

        if (versionData.fgCheckServerYn == AppData.Y_VALUE) {
            setCheckServerUI()
        } else {
            if (versionData.updType == "S") {
                setOptionalUpdatePopupUI()
            } else {
                setRequireUpdatePopupUI()
            }
        }

        return this
    }

    fun setCheckServerUI() {
        clCheckContents.visibility = View.VISIBLE
        tvStDt.visibility = View.VISIBLE
        tvDateSep.visibility = View.VISIBLE
        tvFnDt.visibility = View.VISIBLE

        if (versionData!!.checkType == "N") {
            tvTitle.text = context.getString(R.string.service_popup_check_title1)
        } else if (versionData!!.checkType == "E") {
            tvTitle.text = context.getString(R.string.service_popup_check_title2)
        } else if (versionData!!.checkType == "S") {
            tvTitle.text = context.getString(R.string.service_popup_check_title3)
        } else {
            tvTitle.text = context.getString(R.string.service_popup_check_title1)
        }

        tvSubTitle.text = context.getString(R.string.service_popup_check_sub_title)
        tvBottomNotice.text = context.getString(R.string.service_popup_check_bottom_notice)
        tvCheckDateTitle.text = context.getString(R.string.service_popup_check_date)
        tvCheckContentsTitle.text = context.getString(R.string.service_popup_check_content)
        tvStDt.text = LocaleUtils.getLocalizationTime(versionData!!.checkStDt, false)
        tvFnDt.text = LocaleUtils.getLocalizationTime(versionData!!.checkFnDt, false)
        tvCheckContents.text = versionData!!.checkDesc

        setPositiveButton(R.string.str_confirm)
    }

    fun setRequireUpdatePopupUI() {
        if (versionData!!.fgUseNotYn == AppData.Y_VALUE) {
            clCheckContents.visibility = View.VISIBLE
            tvStDt.visibility = View.VISIBLE
            tvDateSep.visibility = View.GONE
            tvFnDt.visibility = View.GONE

            tvStDt.text = versionData!!.notTitle
            tvCheckContents.text = versionData!!.notContent
        } else {
            clCheckContents.visibility = View.GONE
            tvStDt.visibility = View.GONE
            tvDateSep.visibility = View.GONE
            tvFnDt.visibility = View.GONE
        }

        if (versionData!!.updType == "E") {
            tvTitle.text = context.getString(R.string.service_popup_update_title1)
        } else if (versionData!!.updType == "F") {
            tvTitle.text = context.getString(R.string.service_popup_update_title2)
        } else if (versionData!!.updType == "S") {
            tvTitle.text = context.getString(R.string.service_popup_update_title3)
        } else {
            tvTitle.text = context.getString(R.string.service_popup_update_title1)
        }

        tvSubTitle.text = context.getString(R.string.service_popup_update_sub_title)
        tvBottomNotice.text = context.getString(R.string.service_popup_update_bottom_notice)
        tvCheckDateTitle.text = context.getString(R.string.service_popup_update_date)
        tvCheckContentsTitle.text = context.getString(R.string.service_popup_update_content)

        setNegativeButton(R.string.str_finish)
        setPositiveButton(R.string.str_update)
    }

    fun setOptionalUpdatePopupUI() {
        if (versionData!!.fgUseNotYn == AppData.Y_VALUE) {
            clCheckContents.visibility = View.VISIBLE
            tvStDt.visibility = View.VISIBLE
            tvDateSep.visibility = View.GONE
            tvFnDt.visibility = View.GONE

            tvStDt.text = versionData!!.notTitle
            tvCheckContents.text = versionData!!.notContent
        } else {
            clCheckContents.visibility = View.GONE
            tvStDt.visibility = View.GONE
            tvDateSep.visibility = View.GONE
            tvFnDt.visibility = View.GONE
        }

        if (versionData!!.updType == "E") {
            tvTitle.text = context.getString(R.string.service_popup_update_title1)
        } else if (versionData!!.updType == "F") {
            tvTitle.text = context.getString(R.string.service_popup_update_title2)
        } else if (versionData!!.updType == "S") {
            tvTitle.text = context.getString(R.string.service_popup_update_title3)
        } else {
            tvTitle.text = context.getString(R.string.service_popup_update_title1)
        }

        tvSubTitle.text = context.getString(R.string.service_popup_update_sub_title)
        tvBottomNotice.text = context.getString(R.string.service_popup_update_bottom_notice)
        tvCheckDateTitle.text = context.getString(R.string.service_popup_update_date)
        tvCheckContentsTitle.text = context.getString(R.string.service_popup_update_content)

        setNegativeButton(R.string.str_update_later)
        setPositiveButton(R.string.str_update)
    }

    fun setNegativeButton(@StringRes id: Int): ServerCheckDialog {
        // ID 유효성 체크.
        return if (id == View.NO_ID) {
            this
        } else {
            setNegativeButton(context.getString(id))
        }
    }

    fun setNegativeButton(text: String): ServerCheckDialog {
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

    fun setPositiveButton(@StringRes id: Int): ServerCheckDialog {
        // ID 유효성 체크.
        return if (id == View.NO_ID) {
            this
        } else {
            setPositiveButton(context.getString(id))
        }
    }

    fun setPositiveButton(text: String): ServerCheckDialog {
        llButtons.visibility = View.VISIBLE
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

    fun setCancelable(isCancel: Boolean): ServerCheckDialog {
        this.isCancel = isCancel
        return this
    }

    fun setListener(listener: CommonDialog.Listener): ServerCheckDialog {
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