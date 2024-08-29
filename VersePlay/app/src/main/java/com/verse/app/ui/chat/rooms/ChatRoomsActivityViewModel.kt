package com.verse.app.ui.chat.rooms

import androidx.lifecycle.LiveData
import com.verse.app.base.viewmodel.ActivityViewModel
import com.verse.app.base.model.PagingModel
import com.verse.app.contants.BlockType
import com.verse.app.contants.ReportType
import com.verse.app.extension.applyApiScheduler
import com.verse.app.livedata.ListLiveData
import com.verse.app.livedata.SingleLiveEvent
import com.verse.app.model.base.BaseModel
import com.verse.app.model.chat.ChatMemberRoomModel
import com.verse.app.model.chat.ChatMemberRoomsResponse
import com.verse.app.model.empty.EmptyData
import com.verse.app.model.param.BlockBody
import com.verse.app.model.param.ChatRoomQueryMap
import com.verse.app.model.param.DeleteChatBody
import com.verse.app.model.param.ReportParam
import com.verse.app.repository.http.ApiService
import com.verse.app.ui.dialogfragment.ChatRoomModifyDialogFragment
import com.verse.app.utility.manager.BlockingUserManager
import com.verse.app.utility.DLogger
import com.verse.app.utility.provider.DeviceProvider
import com.verse.app.utility.manager.LoginManager
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.kotlin.addTo
import javax.inject.Inject

/**
 * Description : 채팅 > [메시지함]
 *
 * Created by juhongmin on 2023/06/14
 */
@HiltViewModel
class ChatRoomsActivityViewModel @Inject constructor(
    private val apiService: ApiService,
    private val loginManager: LoginManager,
    val deviceProvider: DeviceProvider,
) : ActivityViewModel(),
    ChatRoomModifyDialogFragment.Listener {

    private val _startBackEvent: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }
    val startBackEvent: LiveData<Unit> get() = _startBackEvent
    private val _startDialogEvent: SingleLiveEvent<ChatMemberRoomModel> by lazy { SingleLiveEvent() }
    val startDialogEvent: LiveData<ChatMemberRoomModel> get() = _startDialogEvent
    private val _startMoveToMessageEvent: SingleLiveEvent<ChatMemberRoomModel> by lazy { SingleLiveEvent() }
    val startMoveToMessageEvent: LiveData<ChatMemberRoomModel> get() = _startMoveToMessageEvent

    private val _dataList: ListLiveData<BaseModel> by lazy { ListLiveData() }
    val dataList: ListLiveData<BaseModel> get() = _dataList

    // [s] Parameter
    val pagingModel: PagingModel by lazy { PagingModel() }
    private val queryMap: ChatRoomQueryMap by lazy { ChatRoomQueryMap() }
    // [e] Parameter

    fun start() {
        queryMap.pageNo = 1
        reqList()
    }

    fun onLoadPage() {
        reqList()
    }

    private fun reqList() {
        apiService.fetchChatRooms(queryMap)
            .doOnSubscribe {
                pagingModel.isLoading = true
                if (queryMap.pageNo == 1) {
                    onLoadingShow()
                }
            }
//            .map { filterList(it) }
            .map { it.list }
            .applyApiScheduler()
            .doOnSuccess { handleOnSuccess(it) }
            .doOnError { handleOnError(it) }
            .subscribe().addTo(compositeDisposable)
    }

    private fun filterList(res: ChatMemberRoomsResponse): List<BaseModel> {
        return res.list
            .filter { !BlockingUserManager.isRoomBlocking(it.code) }
    }

    private fun handleOnSuccess(list: List<BaseModel>) {
        if (queryMap.pageNo == 1) {
            _dataList.clear()
            onLoadingDismiss()
            if (list.isEmpty()) {
                _dataList.add(EmptyData())
            } else {
                _dataList.addAll(list)
            }
        } else {
            _dataList.addAll(list)
        }

        pagingModel.isLast = list.isEmpty()
        pagingModel.isLoading = false
        queryMap.pageNo++
    }

    private fun handleOnError(err: Throwable) {
        DLogger.d("handleOnError $err")
        onLoadingDismiss()
        _dataList.clear()
        _dataList.add(EmptyData())
        pagingModel.isLoading = false
        pagingModel.isLast = true
    }

    fun onBack() {
        _startBackEvent.call()
    }

    fun moveToChatMessage(data: ChatMemberRoomModel) {
        _startMoveToMessageEvent.value = data
    }

    fun showRoomModifyDialog(data: ChatMemberRoomModel) {
        _startDialogEvent.value = data
    }

    override fun onClick(type: ChatRoomModifyDialogFragment.Type, data: ChatMemberRoomModel) {
        DLogger.d("onClick $type $data")
        when (type) {
            ChatRoomModifyDialogFragment.Type.DELETE -> {
                deleteRoom(data)
            }

            ChatRoomModifyDialogFragment.Type.BLOCKING -> {
                blockingRoom(data)
            }

            ChatRoomModifyDialogFragment.Type.REPORT -> {
                reportRoom(data)
            }
        }
    }

    private fun deleteRoom(data: ChatMemberRoomModel) {
        apiService.deleteChatRoom(DeleteChatBody(data.code))
            .doLoading()
            .applyApiScheduler()
            .request(success = {
                _dataList.remove(data)
            })
    }

    /**
     * 사용자 차단
     */
    private fun blockingRoom(data: ChatMemberRoomModel) {
        val body = BlockBody(
            blockContentCode = data.targetMemberCode,
            blockYn = "Y",
            blockType = BlockType.USER.code
        )
        apiService.updateBlock(body)
            .doLoading()
            .applyApiScheduler()
            .request(success = {
                _dataList.remove(data)
                BlockingUserManager.addRoomBlocking(data.code)
            })
    }

    /**
     * 사용자 신고
     */
    private fun reportRoom(data: ChatMemberRoomModel) {
        val body = ReportParam(
            repTpCd = ReportType.USER.code,
            repMngCd = data.targetMemberCode,
            repConCd = data.code,
            fgLoginYn = loginManager.isLoginYN()
        )
        apiService.requestReport(body)
            .doLoading()
            .applyApiScheduler()
            .request()
    }
}
