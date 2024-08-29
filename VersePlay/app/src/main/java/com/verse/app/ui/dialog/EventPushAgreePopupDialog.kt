package com.verse.app.ui.dialog

import android.app.Dialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View.OnClickListener
import android.view.Window
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.verse.app.R
import com.verse.app.databinding.EventPushAgreePopupDialogBinding
import com.verse.app.repository.preferences.AccountPref

/**
 * Description : 야간 푸쉬 이벤트 알림 수신 동의 팝업
 *
 * Created by jhlee on 2023-02-14
 */

interface EventPushAgreeListener {
    fun onAgree(isOnSwitch: Boolean)
}

class EventPushAgreePopupDialog constructor(
    context: Context,
    private var preference: AccountPref,
    private var eventPushAgreeListener: EventPushAgreeListener,

) : Dialog(context, R.style.CommonOneBtnDialog) {
    private var isOnSwitchValue: Boolean = false
    private var binding: EventPushAgreePopupDialogBinding? = null
    val notificationManager = NotificationManagerCompat.from(context)

    companion object {
        const val POSITIVE: Int = 1
        const val CHANNEL_ID = "VERSE PLAY"
        const val CHANNEL_NAME = "Alrim Notification"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        EventPushAgreePopupDialogBinding.inflate(LayoutInflater.from(context)).apply {
            binding = this

            this.tvPositive.setOnClickListener(mCloseClickListener)
            this.ivAgreeSwitch.setOnClickListener(mAgreeSwitchClickListener)

            setContentView(this.root)
        }
        setCanceledOnTouchOutside(false)
        setCancelable(false)
    }

    override fun show() {
        super.show()
    }

    override fun dismiss() {
        preference.setNightTimeAgreePushPopup(true)
        super.dismiss()
    }

    fun toggleSwitch(isOnSwitch: Boolean) {
        isOnSwitchValue = isOnSwitch

        if (isOnSwitch) {
            binding?.let {
                it.ivAgreeSwitch.setImageResource(R.drawable.ic_on)
            }
        } else {
            binding?.let {
                it.ivAgreeSwitch.setImageResource(R.drawable.ic_off)
            }
        }
    }

    private val mCloseClickListener = OnClickListener {
        registerDefaultNotificationChannel()
        eventPushAgreeListener.onAgree(isOnSwitchValue)
        dismiss()
    }

    private val mAgreeSwitchClickListener = OnClickListener {
        toggleSwitch(!isOnSwitchValue)
    }

    /**
     * Create Channel
     */
    private fun registerDefaultNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(createDefaultNotificationChannel())
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createDefaultNotificationChannel() =
        NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH).apply {
            this.setShowBadge(true)
            this.lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            this.enableVibration(true)
            this.enableLights(true)
            this.setBypassDnd(true)
        }
}