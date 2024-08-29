package com.verse.app.livedata

import androidx.lifecycle.MutableLiveData

/**
 * Description : List 형식의 LiveData
 *
 * Created by jhlee on 2023-01-01
 */
class ListLiveData<T> : MutableLiveData<MutableList<T>>() {

    private val temp: MutableList<T> by lazy { mutableListOf() }

    init {
        value = temp
    }

    override fun getValue() = super.getValue()!!

    val size: Int get() = value.size

    operator fun get(index: Int) =
        if (size > index) value[index] else throw ArrayIndexOutOfBoundsException("Index $index Size $size")

    fun add(item: T) {
        temp.add(item)
        value = temp
    }

    fun add(index: Int, item: T) {
        temp.add(index, item)
        value = temp
    }

    fun addAll(list: ArrayList<T>) {
        temp.addAll(list)
        value = temp
    }

    fun addAll(list: List<T>) {
        temp.addAll(list)
        value = temp
    }

    fun remove(item: T) {
        temp.remove(item)
        value = temp
    }

    fun removeAll(item: List<T>) {
        temp.removeAll(item)
        value = temp
    }

    fun removeAt(index: Int) {
        temp.removeAt(index)
        value = temp
    }

    fun contains(item: T): Boolean {
        return value.contains(item)
    }

    fun clear() {
        temp.clear()
        value = temp
    }

    fun refresh() {
        value = temp
    }

    fun forEach(action: (T) -> Unit) {
        for (element in value) action(element)
    }

    fun forEachIndexed(action: (index: Int, T) -> Unit): Unit {
        var index = 0
        for (item in value) action(index++, item)
    }

    fun removeLast(): T? {
        return if (temp.isEmpty()) {
            null
        } else {
            val item = temp.removeLast()
            value = temp
            item
        }
    }

    /**
     * 읽기만 가능한 data
     * WorkThread 에서도 사용할수 있는 함수
     */
    fun data(): List<T> {
        return temp.toList()
    }

    fun set(idx: Int, item: T) {
        temp[idx] = item
        value = temp
    }
}