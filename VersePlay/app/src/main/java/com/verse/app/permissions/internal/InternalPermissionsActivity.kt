package com.verse.app.permissions.internal

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.verse.app.permissions.SPermissions

/**
 * Description : AppCompat 1.3.0 부터 startActivityResult, permissionsResult
 *
 * 처리 정책이 콜백 방식으로 변경되어 해당 이슈 대응하기 위한 처리
 *
 * @see <a href="https://github.com/sieunju/SimplePermissions#readme">참고</a>
 *
 * Created by juhongmin on 2023/05/12
 */
class InternalPermissionsActivity : AppCompatActivity() {
    /**
     * 권한 팝업 노출후 결과값 받는 부분
     */
    private val permissionsResult = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { handleCallback() }

    private val requestPermissions: Array<String>? by lazy {
        intent.getStringArrayExtra(InternalExtraCode.PERMISSIONS)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        overridePendingTransition(0, 0)
        super.onCreate(savedInstanceState)
        permissionsResult.launch(requestPermissions)
    }

    /**
     * SimplePermissions 클래스에 리스너 전달 처리 함수
     */
    private fun handleCallback() {
        finish()
        Handler(Looper.getMainLooper()).post {
            SPermissions.listener?.onResult()
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, 0)
    }
}