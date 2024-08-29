package com.verse.app.base.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

/**
 * Description : ViewPager2
 * Fragment Child 전용 Base PagerAdapter Class
 * Created by jhlee on 2023-01-01
 */
abstract class BaseChildFragmentPagerAdapter<T>(ctx: Fragment) : FragmentStateAdapter(
    ctx
) {

    abstract fun onCreateFragment(pos: Int): Fragment

    val dataList: MutableList<T> by lazy { mutableListOf() }

    fun setDataList(list: MutableList<T>) {
        dataList.clear()
        dataList.addAll(list)
        notifyDataSetChanged()
    }

    override fun getItemCount() = dataList.size

    override fun createFragment(position: Int): Fragment {
        return onCreateFragment(position)
    }

}