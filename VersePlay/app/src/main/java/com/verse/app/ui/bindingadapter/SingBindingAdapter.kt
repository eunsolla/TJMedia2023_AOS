package com.verse.app.ui.bindingadapter

import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.verse.app.contants.SingingCommandType
import com.verse.app.model.base.BaseModel
import com.verse.app.model.xtf.XTF_LYRICE_DTO
import com.verse.app.ui.sing.viewmodel.SingViewModel
import com.verse.app.widget.views.TJTextView


/**
 * Description : Feed BindingAdapter
 *
 * Created by jhlee on 2023-02-13
 */
object SingBindingAdapter {

    var currentEventIdx = 0

    @Suppress("UNCHECKED_CAST")
    @JvmStatic
    @BindingAdapter(value = ["tvRemaining", "currentMs", "totalMs"], requireAll = false)
    fun <T : BaseModel> setFeedProgress(
        progress: ProgressBar,
        textView: TextView,
        currentMs: Double,
        totalMs: Double,
    ) {
//        val persent: Int = (currentMs / totalMs * 100).toInt()
//
//        if (persent != progress.progress) {
//            progress.progress = persent
//        }
        if (textView.text != getTime(currentMs.toInt())) {
            textView.text = getTime(currentMs.toInt())
        }
    }

    private fun getTime(ms: Int): String? {
        val seconds = ms / 1000
        val rem = seconds % 3600
        val mn = rem / 60
        val sec = rem % 60
        return String.format("%02d", mn) + ":" + String.format("%02d", sec)
    }

    @JvmStatic
    @BindingAdapter(value = ["eventType", "isFinishedEvent"], requireAll = false)
    fun <T : BaseModel> setCountDown(
        view: ImageView,
        eventType: SingingCommandType?,
        isFinished: Boolean
    ) {

        if(eventType == null){
            return
        }

        if(eventType.res > -1){
            view.setImageDrawable(
                ContextCompat.getDrawable(
                    view.context, eventType.res
                )
            )
        }
        view.visibility = if (!isFinished) View.VISIBLE else View.INVISIBLE
    }

    @JvmStatic
    @BindingAdapter(value = ["position", "dto", "viewModel"], requireAll = false)
    fun <T : BaseModel> setTJText(
        textview: TJTextView,
        position: Int,
        data: XTF_LYRICE_DTO,
        viewModel: SingViewModel,
    ) {
        textview.setConfigInfo(data, position,viewModel.isTjSong)
        viewModel.setTjTextView(textview, position)
    }

    /**
     * 부르기 다음 라인 스크롤
     */
    @JvmStatic
    @BindingAdapter("scrollPosition")
    fun <T : BaseModel> moveToScrollPosition(
        recyclerView: RecyclerView,
        scrollPosition: Int,
    ) {
        if (scrollPosition <= 0) {
            recyclerView.scrollToPosition(0)
            return
        }

        recyclerView.adapter?.let { a ->
            if (a.itemCount > 0) {
                recyclerView.findViewHolderForAdapterPosition(scrollPosition)?.let {
                    recyclerView.smoothScrollBy(0, it.itemView.y.toInt())
                }
            }
        }
    }
}