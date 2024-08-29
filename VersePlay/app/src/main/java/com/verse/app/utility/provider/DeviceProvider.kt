package com.verse.app.utility.provider

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import android.text.TextUtils
import android.util.DisplayMetrics
import android.view.Window
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.core.content.pm.PackageInfoCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.verse.app.R
import com.verse.app.utility.DLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import java.net.NetworkInterface
import java.util.*
import javax.inject.Inject

/**
 * Description : Device Provider Class
 *
 * Created by jhlee on 2023-01-01
 */
interface DeviceProvider {
    fun getContext(): Context
    fun isNavigationBar(): Boolean
    fun getVersionName(): String
    fun getVersionCode(): Int
    fun getDeviceWidth(): Int
    fun getDeviceHeight(): Int
    fun getStatusBarHeight(): Int
    fun getNavigationBarHeight(): Int
    fun getMaxVolume(): Int
    fun getVolume(): Int
    fun getOutputSampleRate(): String
    fun getOutputFramesPerBuffer(): String
    fun setVolume(volume: Int)
    fun isPermissionsCheck(permissions: String): Boolean
    fun getDeviceName(): String
    fun getAndroidVersion(): String
    fun getMacAddress(): String
    fun setDeviceStatusBarColor(window: Window, color: Int)
    fun isHeadSet(): Boolean
}

@Suppress("DEPRECATION")
class DeviceProviderImpl @Inject constructor(
    @ApplicationContext private val ctx: Context,
) : DeviceProvider {
    private val res by lazy { ctx.resources }
    private val audioManager: AudioManager by lazy { ctx.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    private val windowManager: WindowManager by lazy { ctx.getSystemService(Context.WINDOW_SERVICE) as WindowManager }

    override fun getContext() = ctx

    override fun isNavigationBar(): Boolean {
        val id = ctx.resources.getIdentifier("config_showNavigationBar", "bool", "android")
        return id > 0 && res.getBoolean(id)
    }

    override fun getVersionName(): String {
        val packageInfo = ctx.packageManager.getPackageInfo(ctx.packageName, 0)
        return packageInfo.versionName
    }

    override fun getVersionCode(): Int {
        val packageInfo = ctx.packageManager.getPackageInfo(ctx.packageName, 0)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            PackageInfoCompat.getLongVersionCode(packageInfo).toInt()
        } else {
            packageInfo.versionCode
        }
    }

    override fun getDeviceWidth(): Int {
        return res.displayMetrics.widthPixels
    }

    override fun getDeviceHeight(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            windowManager.currentWindowMetrics.bounds.height()
        } else {
            val displayMetrics = DisplayMetrics()
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay.getRealMetrics(displayMetrics)
            displayMetrics.heightPixels
        }
        // return res.displayMetrics.heightPixels
    }

    override fun getStatusBarHeight(): Int {
        val id = res.getIdentifier("status_bar_height", "dimen", "android")
        return if (id > 0) res.getDimensionPixelSize(id) else -1
    }

    override fun getNavigationBarHeight(): Int {
        val id = res.getIdentifier("navigation_bar_height", "dimen", "android")
        return if (id > 0) res.getDimensionPixelSize(id) else -1
    }

    override fun getMaxVolume() = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)

    override fun getVolume() = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)

    override fun setVolume(volume: Int) {
        audioManager.setStreamVolume(
            AudioManager.STREAM_MUSIC,
            volume,
            AudioManager.FLAG_SHOW_UI
        )
    }

    override fun getOutputSampleRate() = audioManager.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE)

    override fun getOutputFramesPerBuffer() = audioManager.getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER)

    override fun isPermissionsCheck(permissions: String) =
        ContextCompat.checkSelfPermission(ctx, permissions) == PackageManager.PERMISSION_GRANTED

    override fun getDeviceName(): String {
        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL
        return if (model.startsWith(manufacturer)) {
            capitalize(model)
        } else capitalize(manufacturer) + " " + model
    }

    private fun capitalize(str: String): String {
        if (TextUtils.isEmpty(str)) {
            return str
        }
        val arr = str.toCharArray()
        var capitalizeNext = true
        val phrase = StringBuilder()
        for (c in arr) {
            if (capitalizeNext && Character.isLetter(c)) {
                phrase.append(c.uppercaseChar())
                capitalizeNext = false
                continue
            } else if (Character.isWhitespace(c)) {
                capitalizeNext = true
            }
            phrase.append(c)
        }
        return phrase.toString()
    }

    override fun getAndroidVersion(): String {
        val release = Build.VERSION.RELEASE
        val sdkVersion = Build.VERSION.SDK_INT
        return "Android SDK: $sdkVersion ($release)"
    }

    override fun getMacAddress(): String {
        val interfaceName = "wlan0"
        try {
            val interfaces: List<NetworkInterface> =
                Collections.list(NetworkInterface.getNetworkInterfaces())
            for (intf in interfaces) {
                if (interfaceName != null) {
                    if (!intf.name.equals(interfaceName, ignoreCase = true)) continue
                }
                val mac = intf.hardwareAddress ?: return ""
                val buf = java.lang.StringBuilder()
                for (idx in mac.indices) buf.append(String.format("%02X:", mac[idx]))
                if (buf.length > 0) buf.deleteCharAt(buf.length - 1)
                return buf.toString()
            }
        } catch (ex: java.lang.Exception) {
        } // for now eat exceptions
        return "00:00:00:00"
    }

    override fun setDeviceStatusBarColor(window: Window, color: Int) {
        window.statusBarColor = res.getColor(color)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = color == R.color.white || color == Color.WHITE
    }


    override fun isHeadSet(): Boolean {

        val mHeadsetTypes = arrayOf(
            AudioDeviceInfo.TYPE_WIRED_HEADSET, AudioDeviceInfo.TYPE_WIRED_HEADPHONES, AudioDeviceInfo.TYPE_BLUETOOTH_A2DP, AudioDeviceInfo.TYPE_USB_HEADSET
        )

        var chkFlag = false

        val devices: Array<AudioDeviceInfo> // API 레벨이 23 미만인 경우(isWiredHeadsetOn 메소드 사용) // isWiredHeadsetOn : 현재 헤드셋이 연결되었는가?

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            if (audioManager.isWiredHeadsetOn) {
                chkFlag = true
            }
            // API 레벨이 23 이상인 경우(getDevices 메소드 사용) // getDevices : 현재 연결된 오디오 기기 목록을 가져오는 메소드
        } else {
            devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS) // 연결된 기기 목록 중, 헤드셋이 있는가?
            for (device in devices) {
                if (mutableListOf<Int>(*mHeadsetTypes).contains(device.type)) {
                    // TYPE_WIRED_HEADSET : 음악 + 통화
                    // TYPE_WIRED_HEADPHONES : 음악
                    if (device.type == AudioDeviceInfo.TYPE_WIRED_HEADSET) {
                        chkFlag = true
                        break
                    }
                }
            }
        }

        DLogger.d("isHeadSet=> ${chkFlag}")
        return chkFlag
    }
}