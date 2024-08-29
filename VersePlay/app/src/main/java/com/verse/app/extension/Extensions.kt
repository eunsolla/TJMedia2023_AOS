package com.verse.app.extension

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ContentResolver
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.Base64
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.rxjava3.observable
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.verse.app.contants.Config
import com.verse.app.contants.DynamicLinkKeyType
import com.verse.app.contants.LinkMenuTypeCode
import com.verse.app.contants.NationLanType
import com.verse.app.model.feed.FeedContentsData
import com.verse.app.repository.paging.InitListPagingSource
import com.verse.app.repository.tcp.BaseTcpData
import com.verse.app.utility.DLogger
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okio.IOException
import java.io.File
import java.io.Serializable
import java.security.MessageDigest
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.CoroutineContext

/**
 *  MultiPle Null Check.
 */
inline fun <A, B, R> multiNullCheck(a: A?, b: B?, function: (A, B) -> R): R? {
    return if (a != null && b != null) {
        function(a, b)
    } else {
        null
    }
}

/**
 * MultiPle Null Check.
 */
inline fun <A, B, C, R> multiNullCheck(a: A?, b: B?, c: C?, function: (A, B, C) -> R): R? {
    return if (a != null && b != null && c != null) {
        function(a, b, c)
    } else {
        null
    }
}

/**
 * MultiPle Null Check.
 */
inline fun <A, B, C, D, R> multiNullCheck(
    a: A?,
    b: B?,
    c: C?,
    d: D?,
    function: (A, B, C, D) -> R
): R? {
    return if (a != null && b != null && c != null && d != null) {
        function(a, b, c, d)
    } else {
        null
    }
}


/**
 * 날짜 노출 확장 함수
 * ex.) 2023.01.01
 * @param isTime true -> 시:분 노출, false -> 날짜만 노출.
 */
@SuppressLint("SimpleDateFormat")
fun Long.simpleDtm(isTime: Boolean = false): String {
    val sdf = if (isTime) {
        SimpleDateFormat("yyyy.MM.dd HH:mm")
    } else {
        SimpleDateFormat("yyyy.MM.dd")
    }
    return sdf.format(this)
}

@SuppressLint("SimpleDateFormat")
fun Long.simpleDate(isTime: Boolean = false): Date? {
    val sdf = if (isTime) {
        SimpleDateFormat("yyyy.MM.dd HH:mm")
    } else {
        SimpleDateFormat("yyyy.MM.dd")
    }
    return sdf.parse(sdf.format(this))
}

/**
 * 날짜 노출 확장 함수.
 * ex.) 2023-01-01
 * @param isTime true -> 시:분 노출, false -> 날짜만 노출
 */
@SuppressLint("SimpleDateFormat")
fun Long.simpleDtmHalf(isTime: Boolean = false): String {
    val sdf = if (isTime) {
        SimpleDateFormat("yyyy-MM-dd HH:mm")
    } else {
        SimpleDateFormat("yyyy-MM-dd")
    }
    return sdf.format(this)
}

@SuppressLint("SimpleDateFormat")
fun String.getDateToMilliseconds(): Long {
    if (this.isEmpty()) return 0L
    return SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(this).time
}

fun Context.getFragmentAct(): FragmentActivity? {
    if (this is FragmentActivity) {
        return this
    } else {
        var ctx = this
        while (ctx is ContextWrapper) {
            if (ctx is FragmentActivity) {
                return ctx
            }
            ctx = ctx.baseContext
        }
        return null
    }
}

@Suppress("DEPRECATION")
@SuppressLint("PackageManagerGetSignatures")
fun Context.getKeyHash(): String? {
    /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val info = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
        for (signature in info.signingInfo.signingCertificateHistory) {
            val md = MessageDigest.getInstance("SHA")
            md.update(signature.toByteArray())
            return Base64.encodeToString(md.digest(), Base64.NO_WRAP)
        }
        return null
    } else {*/
    val info = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
    for (signature in info.signatures) {
        val md = MessageDigest.getInstance("SHA")
        md.update(signature.toByteArray())
        return Base64.encodeToString(md.digest(), Base64.NO_WRAP)
    }
    return null
//    }
}

fun Context.isRootingDevice(): Boolean {
    val ROOT_PATH =  /*Environment.getExternalStorageDirectory() + */""
    val ROOTING_PATH_1 = "/system/bin/su"
    val ROOTING_PATH_2 = "/system/xbin/su"
    val ROOTING_PATH_3 = "/system/app/SuperUser.apk"
    val ROOTING_PATH_4 = "/data/data/com.noshufou.android.su"
    val ROOTING_PATH_5 = "/sbin/su"
    val ROOTING_PATH_6 = "/data/local/xbin/su"
    val ROOTING_PATH_7 = "/data/local/bin/su"
    val ROOTING_PATH_8 = "/system/sd/xbin/s"
    val ROOTING_PATH_9 = "/system/bin/failsafe/su"
    val ROOTING_PATH_10 = "/data/local/su"
    val ROOTING_PATH_11 = "/su/bin/su"
    val ROOTING_PATH_12 = "su"
    val ROOTING_PATH_13 = "system/bin/.ext"
    val ROOTING_PATH_14 = "system/xbin/.ext"
    val ROOTING_PATH_15 = "/system/bin/xposed"

    val rootFilePath = arrayOf(
        ROOT_PATH + ROOTING_PATH_1,
        ROOT_PATH + ROOTING_PATH_2, ROOT_PATH + ROOTING_PATH_3,
        ROOT_PATH + ROOTING_PATH_4,
        ROOT_PATH + ROOTING_PATH_5,
        ROOT_PATH + ROOTING_PATH_6,
        ROOT_PATH + ROOTING_PATH_7,
        ROOT_PATH + ROOTING_PATH_8,
        ROOT_PATH + ROOTING_PATH_9,
        ROOT_PATH + ROOTING_PATH_10,
        ROOT_PATH + ROOTING_PATH_11,
        ROOT_PATH + ROOTING_PATH_12,
        ROOT_PATH + ROOTING_PATH_13,
        ROOT_PATH + ROOTING_PATH_14,
        ROOT_PATH + ROOTING_PATH_15
    )
    var checkRooting: Boolean
    checkRooting = try {
        Runtime.getRuntime().exec("su")
        true
    } catch (e: IOException) {
        // Exception 나면 루팅 false
        false
    } catch (e: NullPointerException) {
        false
    } catch (e: IllegalArgumentException) {
        false
    }

    try {
        if (!checkRooting) {
            val files = rootFilePath.map { File(it) }
            files.forEach {
                if (it.exists() && it.isFile) {
                    checkRooting = true
                    return@forEach
                }
            }
        }
        if (!checkRooting) {
            val buildTags = Build.TAGS
            checkRooting = buildTags != null && buildTags.contains("test-keys")
        }
        // Signing Key Check
        if (!checkRooting) {
            val hashKey = getKeyHash()
            // Log.d("DLogger", "Hash $hashKey")
//            if (hashKey != "I+dKng6bjtMfz5PWIP9YHuNHEtw=" && hashKey != "H8skJ2ICIONle8awnVa1xhupzCE=") {
//                checkRooting = true
//            }
        }
    } catch (ex: NullPointerException) {
        checkRooting = false
    } catch (ex: SecurityException) {
        checkRooting = false
    } catch (ex: PackageManager.NameNotFoundException) {
        checkRooting = false
    }

    return checkRooting
}

/**
 * 값 "" 반환
 */
fun String.toEmptyStr(defValue: String = "") =
    try {
        if (this == "null" || this == "NULL" || this.isEmpty()) {
            defValue
        } else {
            this
        }
    } catch (_: NullPointerException) {
        defValue
    }

/**
 * String -> Int
 */
fun String.toIntOrDef(defValue: Int = 0) =
    try {
        Integer.parseInt(this)
    } catch (_: NullPointerException) {
        defValue
    } catch (_: NumberFormatException) {
        defValue
    }

/**
 * String -> Long
 */
fun String.toLongOrDef(defValue: Long = 0L) =
    try {
        java.lang.Long.parseLong(this)
    } catch (_: NullPointerException) {
        defValue
    } catch (_: NumberFormatException) {
        defValue
    }

/**
 * String -> Double
 */
fun String.toDoubleOrDef(defValue: Double = 0.0) =
    try {
        java.lang.Double.parseDouble(this)
    } catch (_: NullPointerException) {
        defValue
    } catch (_: NumberFormatException) {
        defValue
    }


/**
 * 앱 업데이트 유무 판단
 * @param diffVersion {Major}.{Minor}.{Patch}
 * @return true 현재 버전과 diffVersion 을 비교해서 DiffVersion 이 더 큰 경우
 * false 유효한 버전이 아니거나 현재 버전이 더 큰경우.
 */
fun String.isUpdate(diffVersion: String): Boolean {
    // {Major(최대 2자리 까지)}.{Minor(최대 3자리 까지)}.{patch(최대 3자리까지)}
    val regex = "^(?:\\d{2}|\\d).(?:\\d{3}|\\d{2}|\\d).(?:\\d{3}|\\d{2}|\\d)$".toRegex()
    // 비교하려는 문자열이 {Major}.{Minor}.{patch} 버전 형태인지
    if (regex.matches(this) && regex.matches(diffVersion)) {
        val currSplit = this.split(".")
        val diffSplit = diffVersion.split(".")
        // Major 검사
        var curr = currSplit[0].toInt()
        var diff = diffSplit[0].toInt()
        if (curr < diff) {
            return true
        } else if (curr == diff) {
            curr = currSplit[1].toInt()
            diff = diffSplit[1].toInt()
            if (curr < diff) {
                return true
            } else if (curr == diff) {
                curr = currSplit[2].toInt()
                diff = diffSplit[2].toInt()
                if (curr < diff) {
                    return true
                } else if (curr == diff) {
                    return false
                } else {
                    return false
                }
            } else {
                return false
            }
        } else {
            return false
        }
    } else {
        return false
    }
}

/**
 * Int -> comma String
 */
fun Int.comma(): String {
    try {
        return NumberFormat.getInstance().format(this)
    } catch (ex: IllegalArgumentException) {
        return ""
    }
}

/**
 * 프래그먼트 번들값
 */
@Suppress("DEPRECATION")
inline fun <reified T : Serializable> Bundle.customGetSerializable(key: String): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getSerializable(key, T::class.java)
    } else {
        getSerializable(key) as? T
    }
}


/**
 * Get bundle parcelableArrayList
 */
inline fun <reified T : Parcelable> Bundle.parcelableArrayList(key: String): ArrayList<T>? = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> getParcelableArrayList(
        key,
        T::class.java
    )

    else -> @Suppress("DEPRECATION") getParcelableArrayList(key)
}

/**
 * Get bundle parcelableArrayList
 */
inline fun <reified T : Parcelable> Intent.parcelableArrayList(key: String): ArrayList<T>? = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> getParcelableArrayListExtra(
        key,
        T::class.java
    )

    else -> @Suppress("DEPRECATION") getParcelableArrayListExtra(key)
}

inline fun <reified T : Parcelable> Intent.parcelable(key: String): T? = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> getParcelableExtra(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelableExtra(key) as? T
}

inline fun <reified T : Parcelable> Bundle.parcelable(key: String): T? = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> getParcelable(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelable(key) as? T
}

/**
 * Get Bundle SerializableExtra
 */
inline fun <reified T : Serializable> Bundle.serializable(key: String): T? = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> getSerializable(key, T::class.java)
    else -> @Suppress("DEPRECATION") getSerializable(key) as? T
}

/**
 * Get Intent SerializableExtra
 */
inline fun <reified T : Serializable> Intent.serializable(key: String): T? = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> getSerializableExtra(
        key,
        T::class.java
    )

    else -> @Suppress("DEPRECATION") getSerializableExtra(key) as? T
}

/**
 * Fragment Permission 요청
 */
fun Fragment.requestPermissions(
    request: ActivityResultLauncher<Array<String>>,
    permissions: Array<String>
) = request.launch(permissions)

/**
 * Fragment Permission 체크
 */
fun Fragment.isAllPermissionsGranted(permissions: Array<String>) = permissions.all {
    ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
}

/**
 * random String
 */
inline fun randomString(length: Int): String {
    val allowedChar = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    val random = Random()
    val stringBuilder = StringBuilder(length).apply {
        append("/")
        for (i in 0 until length) {
            append(allowedChar[random.nextInt(allowedChar.length)])
        }
        append("_")
    }
    return stringBuilder.toString()
}

inline fun ViewModel.onMain(
    job: CoroutineContext,
    crossinline body: suspend CoroutineScope.() -> Unit
) = viewModelScope.launch(Dispatchers.Main + job) {
    body(this)
}

inline fun ViewModel.onMain(
    crossinline body: suspend CoroutineScope.() -> Unit
) = viewModelScope.launch {
    body(this)
}

inline fun ViewModel.onIO(
    job: CoroutineContext,
    crossinline body: suspend CoroutineScope.() -> Unit
) = viewModelScope.launch(Dispatchers.IO + job) {
    body(this)
}

inline fun ViewModel.onIO(
    crossinline body: suspend CoroutineScope.() -> Unit
) = viewModelScope.launch(Dispatchers.IO) {
    body(this)
}

inline fun ViewModel.onDefault(
    job: CoroutineContext,
    crossinline body: suspend CoroutineScope.() -> Unit
) = viewModelScope.launch(Dispatchers.Default + job) {
    body(this)
}

inline fun ViewModel.onDefault(
    crossinline body: suspend CoroutineScope.() -> Unit
) = viewModelScope.launch(Dispatchers.Default) {
    body(this)
}

inline fun onIO(
    crossinline body: suspend CoroutineScope.() -> Unit
) = CoroutineScope(Dispatchers.IO).launch {
    body(this)
}

inline fun onMain(
    crossinline body: suspend CoroutineScope.() -> Unit
) = CoroutineScope(Dispatchers.Main).launch {
    body(this)
}

suspend inline fun onAsyncIo(
    crossinline body: suspend CoroutineScope.() -> Unit
) = CoroutineScope(Dispatchers.IO).async {
    body(this)
}

suspend inline fun onWithContextDefault(
    crossinline body: suspend CoroutineScope.() -> Unit
) = withContext(Dispatchers.Default) {
    body(this)
}

suspend inline fun onWithContextMain(
    crossinline body: suspend CoroutineScope.() -> Unit
) = withContext(Dispatchers.Main) {
    body(this)
}

suspend inline fun onWithContextIO(
    crossinline body: suspend CoroutineScope.() -> Unit
) = withContext(Dispatchers.IO) {
    body(this)
}

suspend inline fun <T> onWithIO(
    crossinline body: suspend CoroutineScope.() -> T
) = withContext(Dispatchers.IO) {
    body(this)
}

/**
 * 채팅 메시지 전용 시간 포멧터
 * @param ts 타임 스템프
 * @param language 현재 언어
 */
fun getChatMessageTime(ts: String, language: String): String {
    return try {
        val date = if (language == NationLanType.KO.code) {
            SimpleDateFormat("a hh:mm", Locale.KOREA)
        } else {
            SimpleDateFormat("a hh:mm", Locale.US)
        }
        date.format(Date(ts.toLongOrDef(System.currentTimeMillis())))
    } catch (ex: Exception) {
        ""
    }
}


inline fun Activity.share(
    url: String,
) {
    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, url)
        type = "text/plain"
    }
    startActivity(Intent.createChooser(shareIntent, null))
}

/**
 * 다이나믹 링크 공유
 * @param path MAIN
 * @param targetPageCode * LD001 : URL / LD002 : 부르기메인 / LD003 : 피드메인 / LD004 : 씽패스 / LD005 : 마이페이지 / LD006 : 커뮤니티투표 / LD007 : 이벤트 / LD008 : 라운지 /  LD009 : 멤버쉽 / LD010 피드 상세
 * @param mngCd LD010 시 사용  (Optional)
 * @param imgUrl 공유 썸네일 이미지
 * @param title 공유 타이틀
 * @param description 공유 내용
 */
fun Activity.shareDynamicLink(
    path: String,
    targetPageCode: String,
    mngCd: String = "",
    imgUrl: String = "",
    title: String = "",
    description: String = ""
) {
    FirebaseDynamicLinks.getInstance().createDynamicLink()
        .setLink(getDeepLink(path.lowercase(), targetPageCode, mngCd))
        .setDomainUriPrefix(Config.DYNAMIC_LINK_PREFIX)
        .setAndroidParameters(
            DynamicLink.AndroidParameters.Builder(this.packageName)
                .build()
        )
        .setIosParameters(
            DynamicLink.IosParameters.Builder("com.tjmedia.Tjlalala")
                .setAppStoreId("1487739513")
                .build()
        )
        .setSocialMetaTagParameters(
            DynamicLink.SocialMetaTagParameters.Builder()
                .setTitle(title)
                .setDescription(description)
                .setImageUrl(imgUrl.toUri())
                .build()
        )
        .buildShortDynamicLink()
        .addOnCompleteListener(
            this
        ) { task ->
            if (task.isSuccessful) {
                val shortLink: Uri = task.result.shortLink!!
                try {
                    DLogger.d("FirebaseDynamicLinks shortLink=> ${shortLink}")
                    val sendIntent = Intent()
                    sendIntent.action = Intent.ACTION_SEND
                    sendIntent.putExtra(Intent.EXTRA_TEXT, shortLink.toString())
                    sendIntent.type = "text/plain"
                    this.startActivity(Intent.createChooser(sendIntent, "Share"))
                } catch (ignored: ActivityNotFoundException) {
                    DLogger.d("error FirebaseDynamicLinks => ${ignored}")
                }
            } else {
                DLogger.d("else FirebaseDynamicLinks => ${task}")
            }
        }
}

fun getDeepLink(path: String, targetPageCode: String, mngCd: String): Uri {
    DLogger.d("FirebaseDynamicLinks-> ${Config.DYNAMIC_DOMAIN_PREFIX}/${path}/?${DynamicLinkKeyType.ID}=$targetPageCode")
    return if (LinkMenuTypeCode.LINK_FEED_CONTENTS.code == targetPageCode) {
        "${Config.DYNAMIC_DOMAIN_PREFIX}/${path}/?${DynamicLinkKeyType.ID}=$targetPageCode&${DynamicLinkKeyType.MNGCD}=$mngCd".toUri()
    } else {
        "${Config.DYNAMIC_DOMAIN_PREFIX}/${path}/?${DynamicLinkKeyType.ID}=$targetPageCode".toUri()
    }
}

fun FragmentActivity.clearNotificationMessage(){
    val notificationManager = NotificationManagerCompat.from(this)
    notificationManager.cancelAll()
}

/**
 * 리소스 경로 반환
 */
fun Int.getResourceUri(context: Context): String {
    return context.resources.let {
        Uri.Builder()
            .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
            .authority(it.getResourcePackageName(this))
            .appendPath(it.getResourceTypeName(this))
            .appendPath(it.getResourceEntryName(this))
            .build()
            .toString()
    }
}