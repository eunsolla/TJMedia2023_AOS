package com.verse.app.ui.chat.viewholders

import android.view.ViewGroup
import androidx.core.view.doOnAttach
import androidx.core.view.doOnDetach
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.verse.app.R
import com.verse.app.base.viewholder.BaseViewHolder
import com.verse.app.base.viewmodel.BaseViewModel
import com.verse.app.databinding.ItemChatMyPhotoBinding
import com.verse.app.extension.applyApiScheduler
import com.verse.app.extension.getFragmentActivity
import com.verse.app.gallery.ui.GalleryImageDetailBottomSheetDialog
import com.verse.app.model.chat.ChatMyPhotoModel
import com.verse.app.utility.RxBus
import com.verse.app.utility.RxBusEvent
import io.reactivex.rxjava3.disposables.Disposable

/**
 * Description : 채팅 > [내_사진]
 *
 * Created by juhongmin on 2023/06/15
 */
class ChatMyPhotoViewHolder(
    parent: ViewGroup,
    private val viewModel: BaseViewModel,
    private val itemBinding: ItemChatMyPhotoBinding = createBinding(
        parent,
        R.layout.item_chat_my_photo,
        viewModel
    )
) : BaseViewHolder<ChatMyPhotoModel>(itemBinding), LifecycleEventObserver {

    private var rxBusEventDisposable: Disposable? = null

    init {

        itemView.doOnAttach { v ->
            val owner = getLifecycleOwner(v) ?: return@doOnAttach
            owner.lifecycle.addObserver(this)
        }

        itemView.doOnDetach { v ->
            val owner = getLifecycleOwner(v) ?: return@doOnDetach
            owner.lifecycle.removeObserver(this)
        }

        initReadNotifyEvent()

        itemBinding.ivThumb.setOnClickListener {
            val data = itemBinding.data ?: return@setOnClickListener
            showFullImage(data)
        }
    }

    override fun bindPayload() {
        itemBinding.invalidateAll()
    }

    private fun showFullImage(data: ChatMyPhotoModel) {
        val act = itemView.getFragmentActivity() ?: return
        GalleryImageDetailBottomSheetDialog()
            .setImageUrl(data.imagePath)
            .simpleShow(act.supportFragmentManager)
    }

    private fun initReadNotifyEvent() {
        rxBusEventDisposable = RxBus.listen(RxBusEvent.ChatReadNotifyEvent::class.java)
            .filter { isUpdateData(it) }
            .applyApiScheduler()
            .doOnNext { itemBinding.invalidateAll() }
            .subscribe()
    }

    private fun isUpdateData(rxBus: RxBusEvent.ChatReadNotifyEvent): Boolean {
        val data = itemBinding.data ?: return false
        // 최소한으로 UI 업데이트 하기 위한 처리
        return data.roomCode == rxBus.roomCode
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_DESTROY) {
            closeDisposable()
        }
    }

    private fun closeDisposable() {
        if (rxBusEventDisposable != null) {
            rxBusEventDisposable?.dispose()
            rxBusEventDisposable = null
        }
    }
}
