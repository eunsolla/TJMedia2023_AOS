package com.verse.app.ui.fake

import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.verse.app.contants.HttpStatusType
import com.verse.app.ui.dialog.NetworkErrorDialog

/**
 * Description : 배경색이 투명한 Fake Activity
 *
 * Created by juhongmin on 2023/06/06
 */
class InternalFakeActivity : AppCompatActivity() {

    companion object {
        const val ERROR_STATUS = "ERROR_STATUS"
        const val IS_EXIT = "IS_EXIT_STATUS"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        } catch (ignore: IllegalStateException) {
        }

        val statusType: HttpStatusType =
            (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getSerializableExtra(ERROR_STATUS, HttpStatusType::class.java)
            } else {
                intent.getSerializableExtra(ERROR_STATUS)
            } ?: {
                finish()
            }) as HttpStatusType

        val isExit :Boolean = intent.getBooleanExtra(IS_EXIT,false)
        NetworkErrorDialog(this, statusType,isExit).show()
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, 0)
    }
}