package com.verse.app.utility

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.verse.app.R
import com.verse.app.contants.AppData
import com.verse.app.contants.ExtraCode
import com.verse.app.extension.clearNotificationMessage
import com.verse.app.model.mypage.UploadSettingBody
import com.verse.app.repository.http.ApiService
import com.verse.app.repository.preferences.AccountPref
import com.verse.app.ui.main.MainActivity
import com.verse.app.ui.push.PushRouterActivity
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

@AndroidEntryPoint
class TjFirebaseMessagingService : FirebaseMessagingService() {
    @Inject
    lateinit var apiService: ApiService

    @Inject
    lateinit var accountPref:AccountPref


    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        logRemoteMessage(remoteMessage)

        val data = remoteMessage.data

        if (data.isNotEmpty()) {
            val title = data["TITLE"]
            val message = data["DESCRIPTION"]
            val linkCd = data["LINK_CD"]
            val linkData = data["LINK_DATA"]
            val attImagePath = data["ATT_IMAGE_PATH"]
            val showType = data["SHOW_TYPE"]

            DLogger.d("TjFirebaseMessagingService onMessageReceived title : ${title}")
            DLogger.d("TjFirebaseMessagingService onMessageReceived message : ${message}")
            DLogger.d("TjFirebaseMessagingService onMessageReceived linkCd : ${linkCd}")
            DLogger.d("TjFirebaseMessagingService onMessageReceived linkData : ${linkData}")
            DLogger.d("TjFirebaseMessagingService onMessageReceived attImagePath : ${attImagePath}")
            DLogger.d("TjFirebaseMessagingService onMessageReceived showType : ${showType}")

            sendNotification(title, message, linkCd, linkData, attImagePath, showType)
        }

        // Check if message contains a notification payload.
        remoteMessage.notification?.let {
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        DLogger.d("TjFirebaseMessagingService onNewToken : ${token}")
        accountPref.setFcmPushToken(token)
    }

    /**
     * Schedule async work using WorkManager.
     */
    private fun scheduleJob() {
//        OneTimeWorkRequest work = new OneTimeWorkRequest.Builder(MyWorker.class)
//                .build();
//        WorkManager.getInstance().beginWith(work).enqueue();
    }

    /**
     * Handle time allotted to BroadcastReceivers.
     */
    private fun handleNow() {}


    private fun logRemoteMessage(message: RemoteMessage) {
        if (!com.verse.app.BuildConfig.DEBUG) {
            return
        }
        val notification = message.notification ?: return
    }

    private fun sendNotification(title: String?, message: String?, linkCd: String?, linkData: String?, attImagePath: String?, showType: String?) {
        val channelId = getString(R.string.notification_channel_id)
        val channel = (System.currentTimeMillis() / 1000).toInt()
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val intent = Intent(this, PushRouterActivity::class.java)

        intent.putExtra(ExtraCode.PUSH_MSG_TITLE, title)
        intent.putExtra(ExtraCode.PUSH_MSG_MESSAGE, message)
        intent.putExtra(ExtraCode.PUSH_MSG_LINK_TYPE, linkCd)
        intent.putExtra(ExtraCode.PUSH_MSG_LINK_DATA, linkData)
        intent.putExtra(ExtraCode.PUSH_MSG_ATT_IMAGE, attImagePath)
        intent.putExtra(ExtraCode.PUSH_MSG_SHOW_TYPE, showType)

        val pendingIntent: PendingIntent

        if(!AppData.IS_SING_ING){
            pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.getActivity(this, channel, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            } else {
                PendingIntent.getActivity(this, channel, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            }
        }else{
            val broadcastIntent  = Intent(this,NotificationReceiver::class.java)
            pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.getBroadcast(this,channel,broadcastIntent,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            } else {
                PendingIntent.getBroadcast(this, channel, broadcastIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            }
        }

        val notificationBuilder: NotificationCompat.Builder =
            NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.app_icon)
                .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.app_icon))
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setDefaults(NotificationCompat.DEFAULT_SOUND or NotificationCompat.DEFAULT_VIBRATE)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
//                        .setFullScreenIntent(pendingIntent, true);
//                        .setContentIntent(pendingIntent);

        val notificationManager = NotificationManagerCompat.from(this)

        // Since android Oreo notification channel is needed.
        //                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//        if (null == notificationManager) {
//            return;
//        }

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelId,
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.setShowBadge(true)
            channel.lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            channel.enableVibration(true)
            channel.enableLights(true)
            channel.setBypassDnd(true)
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(channel /* ID of notification */, notificationBuilder.build())

    }

    //부르기 화면에서 받는 푸쉬 누르면 지움
    inner class NotificationReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.cancelAll()
        }
    }
}