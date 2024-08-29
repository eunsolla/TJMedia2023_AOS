package com.verse.app.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import androidx.core.text.HtmlCompat
import com.verse.app.R
import com.verse.app.contants.HttpStatusType
import com.verse.app.databinding.DialogNetworkErrorBinding
import com.verse.app.entrypoint.ViewHolderEntryPoint
import com.verse.app.extension.exitApp
import com.verse.app.extension.getActivity
import com.verse.app.ui.comment.CommentActivity
import com.verse.app.ui.fake.InternalFakeActivity
import com.verse.app.utility.DLogger
import dagger.hilt.EntryPoints

/**
 * Description : 네트워크 에러 팝업
 *
 * Created by jhlee on 2023-03-03
 */
class NetworkErrorDialog(
    context: Context,
    private val type: HttpStatusType,
    private val isExit: Boolean? = false,
) : Dialog(context, R.style.CommonOneBtnDialog) {

    protected val entryPoint: ViewHolderEntryPoint by lazy {
        EntryPoints.get(context.applicationContext, ViewHolderEntryPoint::class.java)
    }


    companion object {
        var IS_SHOW = false
    }

    private var binding: DialogNetworkErrorBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        DialogNetworkErrorBinding.inflate(LayoutInflater.from(context)).apply {
            binding = this

            message = run {
                if (type == HttpStatusType.NO_AUTHENTICATION
                    || type == HttpStatusType.DUPLICATED_LOGIN
                ) {
                    if (type.fromServerErrMsg.isNotEmpty()) {
                        type.fromServerErrMsg
                    } else {
                        HtmlCompat.fromHtml(
                            context.getString(type.fromResErrMsg).replace("\n", "<br>"),
                            HtmlCompat.FROM_HTML_MODE_LEGACY
                        )
                    }
                } else {
                    HtmlCompat.fromHtml(
                        context.getString(type.fromResErrMsg).replace("\n", "<br>"),
                        HtmlCompat.FROM_HTML_MODE_LEGACY
                    )
                }


//                if (type.fromServerErrMsg.isNotEmpty()) {
//                    type.fromServerErrMsg
//                } else {
//                    HtmlCompat.fromHtml(
//                        context.getString(type.fromResErrMsg).replace("\n", "<br>"),
//                        HtmlCompat.FROM_HTML_MODE_LEGACY
//                    )
//                }
            }

            this.tvConfirm.setOnClickListener(mClickLister)

            setContentView(this.root)
        }
        setCanceledOnTouchOutside(false)
        setCancelable(false)
    }

    override fun show() {

        binding?.message = run {
            if (type == HttpStatusType.NO_AUTHENTICATION
                || type == HttpStatusType.DUPLICATED_LOGIN
            ) {
                if (type.fromServerErrMsg.isNotEmpty()) {
                    type.fromServerErrMsg
                } else {
                    HtmlCompat.fromHtml(
                        context.getString(type.fromResErrMsg).replace("\n", "<br>"),
                        HtmlCompat.FROM_HTML_MODE_LEGACY
                    )
                }
            } else {
                HtmlCompat.fromHtml(
                    context.getString(type.fromResErrMsg).replace("\n", "<br>"),
                    HtmlCompat.FROM_HTML_MODE_LEGACY
                )
            }
//            if (type.fromServerErrMsg.isNotEmpty()) {
//                type.fromServerErrMsg
//            } else {
//                HtmlCompat.fromHtml(
//                    context.getString(type.fromResErrMsg).replace("\n", "<br>"),
//                    HtmlCompat.FROM_HTML_MODE_LEGACY
//                )
//            }
        }

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

    private val mClickLister = View.OnClickListener {
        when (type) {
            HttpStatusType.NO_AUTHENTICATION,
            HttpStatusType.DUPLICATED_LOGIN -> {
                entryPoint.loginManager().logout(context)
            }
            else -> {

                if(isExit == true){
                    context.getActivity()?.exitApp()
                }else{
                    if (context.getActivity() is CommentActivity) {
                        (context.getActivity() as CommentActivity).finish()
                    }

                    if(context.getActivity() is InternalFakeActivity){
                        context.getActivity()?.finish()
                    }
                }
            }
        }
        dismiss()
    }
}