package com.verse.app.base.viewmodel

import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import com.verse.app.base.activity.ActivityResult
import com.verse.app.livedata.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Description : Fragment 전용 ViewModel
 *
 * Created by juhongmin on 2023/05/11
 */
@HiltViewModel
open class FragmentViewModel @Inject constructor() : BaseViewModel() {
    @Inject
    lateinit var savedStateHandle: SavedStateHandle

    private val _startActivityPage: SingleLiveEvent<ActivityResult> by lazy { SingleLiveEvent() }
    val startActivityPage: LiveData<ActivityResult> get() = _startActivityPage

    /**
     * ViewModel 단에서 페이지 이동 시킬수 있는 함수
     */
    protected fun moveToPage(page: ActivityResult) {
        try {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                _startActivityPage.value = page
            } else {
                _startActivityPage.postValue(page)
            }
        } catch (ex: Exception) {
            _startActivityPage.postValue(page)
        }
    }

    protected fun setInitViewCreated() {
        savedStateHandle["isOnCreatedPage"] = true
    }

    /**
     * Fragment 특성상 onCreateView <-> onDestroyView 를 반복적으로 하고
     * ViewModel 은 Fragment 가 완전 메모리에서 해제 될때 onCleared 를 호출하는것을 이용해서 불필요하게
     * init 처리하는 걸 방지하기 위한 함수
     */
    protected fun isInitViewCreated(): Boolean {
        return savedStateHandle.get<Boolean>("isOnCreatedPage") ?: false
    }

    /**
     * Fragment getIntentData 함수
     * @return NonNull
     */
    inline fun <reified T> getIntentData(key: String, default: T): T {
        return savedStateHandle.get<T>(key) ?: default
    }

    /**
     * Fragment getIntentData 함수
     * @return Nullable
     */
    inline fun <reified T> getIntentData(key: String): T? {
        return savedStateHandle.get<T>(key)
    }

    fun setResultSaveData(key: String, value: Any) {
        savedStateHandle[key] = value
    }
}