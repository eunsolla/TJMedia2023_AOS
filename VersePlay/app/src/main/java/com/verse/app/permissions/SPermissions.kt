package com.verse.app.permissions

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.verse.app.permissions.internal.InternalExtraCode
import com.verse.app.permissions.internal.InternalPermissionsActivity
import com.verse.app.permissions.internal.PermissionsListener

/**
 * Description : AppCompat 1.3.0 부터 startActivityResult, permissionsResult
 *
 * 처리 정책이 콜백 방식으로 변경되어 해당 이슈 대응하기 위한 처리
 *
 * @see <a href="https://github.com/sieunju/SimplePermissions#readme">참고</a>
 * Created by juhongmin on 2023/05/12
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
open class SPermissions {
    protected val context: Context

    constructor(context: Context) {
        this.context = context
    }

    constructor(fragment: Fragment) {
        this.context = fragment.requireContext()
    }

    constructor(activity: Activity) {
        this.context = activity.baseContext
    }

    companion object {
        internal var listener: PermissionsListener? = null
    }

    protected var requestPermissions: Array<String>? = null

    /**
     * Request Permissions
     * @param permissions Manifest.CAMERA, Manifest.STORAGE ...
     */
    open fun requestPermissions(vararg permissions: String): SPermissions {
        this.requestPermissions = Array(permissions.size) { idx ->
            permissions[idx]
        }
        return this
    }

    /**
     * 껍데기인 권한 액티비티 실행 함수
     * @param negativePermissions 거부된 권한들
     */
    protected fun movePermissionsActivity(negativePermissions: List<String>) {
        Intent(context, InternalPermissionsActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra(InternalExtraCode.PERMISSIONS, negativePermissions.toTypedArray())
            context.startActivity(this)
        }
    }

    /**
     * Build 이후 setListener
     */
    protected fun buildAndSetListener(callback: () -> Unit) {
        // Single Listener
        if (listener != null) listener = null

        listener = object : PermissionsListener {
            override fun onResult() {
                callback()
            }
        }
    }

    /**
     * Build
     * @param callback {전부다 승인한 경우 true, 아닌경우 false}, 거부된 권한들
     */
    open fun build(callback: (Boolean, Array<String>) -> Unit) {
        if (requestPermissions == null) throw NullPointerException("권한은 필수 값입니다.")

        // 권한 거부인것들만 권한 팝업 처리하도록
        val negativePermissions = requestPermissions!!
            .filter {
                ContextCompat.checkSelfPermission(
                    context,
                    it
                ) == PackageManager.PERMISSION_DENIED
            }

        if (negativePermissions.isEmpty()) {
            callback(true, emptyArray())
        } else {
            movePermissionsActivity(negativePermissions)

            // Single Listener
            if (listener != null) listener = null

            listener = object : PermissionsListener {
                override fun onResult() {
                    val resultNegativePermissions = requestPermissions!!.filter {
                        ContextCompat.checkSelfPermission(
                            context,
                            it
                        ) == PackageManager.PERMISSION_DENIED
                    }

                    callback(
                        resultNegativePermissions.isEmpty(),
                        resultNegativePermissions.toTypedArray()
                    )
                }
            }
        }
    }
}