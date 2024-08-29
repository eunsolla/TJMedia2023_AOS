package com.verse.app.ui.dialog

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Window
import androidx.appcompat.app.AppCompatDialog
import com.verse.app.R
import com.verse.app.contants.LoadingDialogState
import com.verse.app.extension.onMain
import kotlinx.coroutines.delay


/**
 * Description : Loading Dialog Class
 *
 * Created by jhlee on 2023-01-01
 */
class LoadingDialog(ctx: Context) : AppCompatDialog(ctx) {
    companion object {
        var gIsShowing: Boolean = false
    }

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_loading)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        setCanceledOnTouchOutside(false)
        setCancelable(false)
    }

     fun setState(state: LoadingDialogState) {
         onMain {
             if (state == LoadingDialogState.SHOW) {
                 synchronized(this) {
                     show()
                 }
             } else {
                 synchronized(this) {
                     dismiss()
                 }
             }
         }
    }

    fun setVisible(isVisible: Boolean) {
        if (isVisible) {
            show()
        } else {
            dismiss()
        }
    }


    override fun show() {
        try {
            if (!gIsShowing) {
                super.show()
                gIsShowing = true
            }
        } catch (e: Exception) {
            if (gIsShowing) {
                dismiss()
            }
        }
    }

    override fun dismiss() {
        try {
            super.dismiss()
            gIsShowing = false
        } catch (e: Exception) {
            if (gIsShowing) {
                super.dismiss()
                gIsShowing = false
            }
        }
    }
}