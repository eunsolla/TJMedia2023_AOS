package com.verse.app.livedata

import androidx.lifecycle.MutableLiveData

/**
 * Description : NonNull Mutable LiveData.
 *
 * Created by jhlee on 2023-01-01
 */
class NonNullLiveData<T : Any>(defValue: T) : MutableLiveData<T>() {

    init {
        value = defValue
    }

    override fun getValue() = super.getValue()!!
}