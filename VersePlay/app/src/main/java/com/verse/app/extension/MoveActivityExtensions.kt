package com.verse.app.extension

import android.app.Activity
import android.app.ActivityManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Process
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.AnimRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.fragment.app.FragmentActivity
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.DynamicLink.SocialMetaTagParameters
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.verse.app.R
import com.verse.app.contants.Config
import com.verse.app.contants.DynamicLinkKeyType
import com.verse.app.contants.Encoded
import com.verse.app.contants.LinkMenuTypeCode
import com.verse.app.utility.DLogger
import com.verse.app.utility.SongEncodeService
import kotlin.system.exitProcess


/**
 *  Move To Activity
 *
 *  EX)
 *   startAct<MainActivity>(
enterAni = android.R.anim.fade_in,
exitAni = android.R.anim.fade_out
)
{
flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
putExtra(ExtraCode.C_O_D_E, value)
}
 */
inline fun <reified T : Activity> Activity.startAct(
    @AnimRes enterAni: Int = -1,
    @AnimRes exitAni: Int = -1,
    flags: Int? = null,
    data: Intent.() -> Unit = {}
) {
    val intent = Intent(this, T::class.java)
    intent.data()
    if (flags != null) {
        intent.addFlags(flags)
    }
    startActivity(intent)
    if (enterAni != -1 && exitAni != -1) {
        overridePendingTransition(enterAni, exitAni)
    } else {
        // 모든 페이지 공통
        overridePendingTransition(R.anim.in_right_to_left, R.anim.out_right_to_left)
    }
}


/**
 * ActivityResult 초기화 함수
 * 기존 onActivityResult -> API 변경됨.
 * @link #lifecycle onCreate 에서 해당 함수를 호출해야함.
 */
inline fun FragmentActivity.initActivityResult(crossinline callback: (ActivityResult) -> Unit) =
    registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        callback.invoke(it)
    }

/**
 * 앱 종료
 */
fun Activity.exitApp() {
    moveTaskToBack(true)
    finishAffinity()
    finishAndRemoveTask()
    exitProcess(Process.myPid())
}

/**
 * 플레이 스토어 이동
 */
fun AppCompatActivity.movePlayStore() {
    try {
        Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("market://details?id=${packageName}")
            startActivity(this)
        }
    } catch (ex: ActivityNotFoundException) {
        Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://play.google.com/store/apps/details")
            startActivity(this)
        }
    }

    finishAffinity()
}

fun Activity.openBrowser(uri: String) {
    if (uri.isNullOrEmpty()) return
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
    this.startActivity(intent)
}


/**
 * 업로드 서비스
 */
inline fun Activity.startSongEncodeService(
    data: Intent.() -> Unit = {},
) {
    val encodeService = Intent(this, SongEncodeService::class.java).apply {
        action = Encoded.SONG_ENCODE_START_SERVICE
    }
    encodeService.data()
    startService(encodeService)
}

/**
 * Service Running Check
 * @param : Service Class
 * @return true : 실행중 , false : 실행중아님
 */
@Suppress("DEPRECATION")
inline fun <T> Context.isServiceRunning(service: Class<T>): Boolean {
    return (getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager)
        .getRunningServices(Integer.MAX_VALUE)
        .any { it -> it.service.className == service.name }
}

/**
 * Service Stop
 * @param : Service Class
 */
inline fun <T> Context.stopService(service: Class<T>) {
    stopService(Intent(this, service))
}