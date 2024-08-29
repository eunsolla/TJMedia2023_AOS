package com.verse.app.base.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.annotation.AnimRes
import androidx.fragment.app.FragmentActivity
import kotlin.reflect.KClass

/**
 * Description : ActivityViewModel, FragmentViewModel 에서 페이지 이동에 필요한 함수
 * @see ActivityViewModel
 * @see FragmentViewModel
 * Created by juhongmin on 2023/05/12
 */
data class ActivityResult(
    val requestCode: Int = -1, // 페이지 갔다가 다시 돌아오는 경우에 대한 Code 이지만 필요시 해당 로직 넣을 예정
    val targetActivity: KClass<out FragmentActivity>,
    val flags: Int = -1,
    val data: Bundle = Bundle(),
    @AnimRes val enterAni: Int = -1,
    @AnimRes val exitAni: Int = -1
) {
    companion object {
        fun toNormalActivity(activity: KClass<out FragmentActivity>): ActivityResult {
            return ActivityResult(targetActivity = activity)
        }
    }

    fun getIntent(context: Context): Intent {
        return Intent(context, targetActivity.java).apply {
            if (this@ActivityResult.flags != -1) {
                this.flags = this@ActivityResult.flags
            }
            putExtras(this@ActivityResult.data)
        }
    }
}