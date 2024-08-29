package com.verse.app.base.adapter

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.verse.app.extension.getFragmentAct

/**
 * Description : ViewPager2
 * Fragment 전용 Base PagerAdapter Class
 * Created by jhlee on 2023-01-01
 */
abstract class BaseFragmentPagerAdapter<T>(ctx: Context) : FragmentStateAdapter(
    ctx.getFragmentAct() ?: throw IllegalArgumentException("Only Fragment Activity")
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