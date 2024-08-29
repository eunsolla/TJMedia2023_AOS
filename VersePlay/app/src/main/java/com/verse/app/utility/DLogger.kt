package com.verse.app.utility

import android.util.Log
import com.verse.app.contants.Config

/**
 * Description : Logger Util Class
 *
 * Created by jhlee on 2023-01-01
 */
class DLogger {
    companion object {
        private const val TAG = "JLogger"

        fun d(msg: String) {
            if (Config.IS_DEBUG) {
                val ste = Thread.currentThread().stackTrace[4]
                val sb = StringBuilder()
                sb.append(ste.className.substringAfterLast("."))
                Log.d("[$TAG:$sb]", msg)
            }
        }

        fun d(tag: String, msg: String) {
            if (Config.IS_DEBUG) {
                val ste = Thread.currentThread().stackTrace[4]
                val sb = StringBuilder()
                sb.append(ste.className.substringAfterLast("."))
                Log.d("[$tag:$sb]", msg)
            }
        }

        fun e(msg: String) {
            if (Config.IS_DEBUG) {
                val ste = Thread.currentThread().stackTrace[4]
                val sb = StringBuilder()
                sb.append(ste.className.substringAfterLast("."))
                Log.e("[$TAG:$sb]", msg)
            }
        }
    }
}