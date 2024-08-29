package com.verse.app.ui.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import androidx.annotation.StringRes
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView
import com.verse.app.R
import com.verse.app.model.common.SelectBoxData
import com.verse.app.ui.adapter.SelectBoxAdapter
import com.verse.app.utility.DLogger

/**
 * Description : select 팝업
 *
 * Created by esna on 2023-05-04
 */
class SelectDialog (private val context: Context){

    interface Listener {
        fun onClick(which: Int)

    }

    interface SelectCallback {
        fun selectedCallback(selectBoxData: String, selectBoxCode:String)
    }


    private val rootView: View by lazy {
        View.inflate(context, R.layout.dialog_select_box, null)
    }
    private var isCancel: Boolean = false
    private var listener: Listener? = null
    private var callback: SelectCallback?= null
    private var tvTitle: AppCompatTextView? = null
    private val noData: LinearLayout by lazy { rootView.findViewById(R.id.empty_view) }
    private var lvSelectBox: RecyclerView? = null
    private lateinit var dialog: Dialog
    var categoryTitle = ""

    private val mCloseClickListener = View.OnClickListener {
        dialog.dismiss()
    }

    fun setTitle(@StringRes id: Int): SelectDialog {
        return setTitle(context.getString(id))
    }

    fun setTitle(text: String): SelectDialog {
        if (tvTitle == null) {
            tvTitle = rootView.findViewById(R.id.tv_select_box_title)
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

    fun setList(arrayList: ArrayList<SelectBoxData>, selectCallback: SelectCallback): SelectDialog {
        if (lvSelectBox == null) {
            lvSelectBox = rootView.findViewById(R.id.lv_selected_box)
            noData.visibility = View.VISIBLE
        }
        if (arrayList.isNotEmpty()) {
            lvSelectBox?.let { it ->
                it.visibility = View.VISIBLE
                noData.visibility = View.GONE
                val selectadapter = SelectBoxAdapter(arrayList, mCloseClickListener, selectCallback)
                it.adapter = selectadapter
            }
        }
        return this
    }

    fun setCancelable(isCancel: Boolean): SelectDialog {
        this.isCancel = isCancel
        return this
    }

    fun setListener(listener: Listener): SelectDialog {
        this.listener = listener
        return this
    }

    fun setCallBack(selectCallback: SelectCallback): SelectDialog {
        this.callback = selectCallback
        return this
    }

    fun show() {
        val builder = AlertDialog.Builder(context, R.style.CommonVerticalDialog).setView(rootView)
        builder.setCancelable(isCancel)
        runCatching {
            dialog = builder.create()
            dialog.window?.setBackgroundDrawableResource(com.google.android.material.R.color.mtrl_btn_transparent_bg_color)
            dialog.show()
        }
    }

    fun show(dismissListener: DialogInterface.OnDismissListener) {
        val builder = AlertDialog.Builder(context, R.style.CommonVerticalDialog).setView(rootView)
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