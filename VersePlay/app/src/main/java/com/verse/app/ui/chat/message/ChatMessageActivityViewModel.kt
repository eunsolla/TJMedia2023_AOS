package com.verse.app.ui.chat.message

import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.verse.app.R
import com.verse.app.base.viewmodel.ActivityViewModel
import com.verse.app.base.model.PagingModel
import com.verse.app.contants.AppData
import com.verse.app.contants.ChatMsgType
import com.verse.app.contants.ExtraCode
import com.verse.app.contants.NationLanType
import com.verse.app.extension.applyApiScheduler
import com.verse.app.extension.getChatMessageTime
import com.verse.app.extension.toLongOrDef
import com.verse.app.gallery.ui.GalleryBottomSheetDialog
import com.verse.app.livedata.ListLiveData
import com.verse.app.livedata.NonNullLiveData
import com.verse.app.livedata.SingleLiveEvent
import com.verse.app.model.base.BaseModel
import com.verse.app.model.chat.ChatInitOtherProfileModel
import com.verse.app.model.chat.ChatMessageDateModel
import com.verse.app.model.chat.ChatMessageIntentModel
import com.verse.app.model.chat.ChatMessageModel
import com.verse.app.model.chat.ChatMessagesResponse
import com.verse.app.model.chat.ChatMyMessageModel
import com.verse.app.model.chat.ChatMyPhotoModel
import com.verse.app.model.chat.ChatOtherMessageModel
import com.verse.app.model.chat.ChatOtherPhotoModel
import com.verse.app.model.chat_recv.ReceiveChatMessage
import com.verse.app.model.chat_recv.ResponseResourcePath
import com.verse.app.model.chat_recv.ResponseSendChatMessage
import com.verse.app.model.chat_send.RequestResourcePath
import com.verse.app.model.chat_send.SendChatMessage
import com.verse.app.model.param.ChatMemberRoomBody
import com.verse.app.model.param.ChatRoomMessageQueryMap
import com.verse.app.repository.http.ApiService
import com.verse.app.repository.preferences.AccountPref
import com.verse.app.repository.tcp.NettyClient
import com.verse.app.repository.tcp.handler.ChatListener
import com.verse.app.utility.*
import com.verse.app.utility.manager.LoginManager
import com.verse.app.utility.provider.FileProvider
import com.verse.app.utility.provider.ResourceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.schedulers.Schedulers
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Description : 채팅 > [메시지_보내기]
 *
 * Created by juhongmin on 2023/06/14
 */
@HiltViewModel
class ChatMessageActivityViewModel @Inject constructor(
    private val apiService: ApiService,
    private val nettyClient: NettyClient,
    private val accountPref: AccountPref,
    private val loginManager: LoginManager,
    private val fileProvider: FileProvider,
    private val resourceProvider: ResourceProvider
) : ActivityViewModel(),
    ChatListener,
    GalleryBottomSheetDialog.Listener {

    private val _startFinishEvent: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }
    val startFinishEvent: LiveData<Unit> get() = _startFinishEvent
    private val _startGalleryDialogEvent: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }
    val startGalleryDialogEvent: LiveData<Unit> get() = _startGalleryDialogEvent
    private val _startFocusingPositionEvent: SingleLiveEvent<Int> by lazy { SingleLiveEvent() }
    val startFocusingPositionEvent: LiveData<Int> get() = _startFocusingPositionEvent
    private val _startPayloadEvent: SingleLiveEvent<Int> by lazy { SingleLiveEvent() }
    val startPayloadEvent: LiveData<Int> get() = _startPayloadEvent
    private val _startInvalidPageEvent: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }
    val startInvalidPageEvent: LiveData<Unit> get() = _startInvalidPageEvent
    private val _startMoveToProfileEvent: SingleLiveEvent<ChatMessageIntentModel> by lazy { SingleLiveEvent() }
    val startMoveToProfileEvent: LiveData<ChatMessageIntentModel> get() = _startMoveToProfileEvent

    private val intentModel: MutableLiveData<ChatMessageIntentModel> by lazy { MutableLiveData() }
    val targetImagePath: LiveData<String> get() = Transformations.map(intentModel) { it.targetProfileImagePath }
    val targetNickname: LiveData<String> get() = Transformations.map(intentModel) { it.targetNickName }
    val targetDesc: LiveData<String> get() = Transformations.map(intentModel) { it.targetDesc }

    private val _uiList: ListLiveData<BaseModel> by lazy { ListLiveData() }
    val uiList: ListLiveData<BaseModel> get() = _uiList
    private val dateSet: HashSet<String> by lazy { hashSetOf() }

    val messageText: MutableLiveData<String> by lazy { MutableLiveData() }
    val hintText: MutableLiveData<String> by lazy { MutableLiveData() }
    val setChatEnable: NonNullLiveData<Boolean> by lazy { NonNullLiveData(true) }
    private val _isSendReady: NonNullLiveData<Boolean> by lazy { NonNullLiveData(true) }
    val isSendReady: LiveData<Boolean> get() = _isSendReady

    // [s] Parameter
    private val currentLanguage: String by lazy { accountPref.getPreferenceLocaleLanguage() }
    private val queryMap: ChatRoomMessageQueryMap by lazy { ChatRoomMessageQueryMap() }
    val pagingModel: PagingModel by lazy { PagingModel() }
    private var uploadImageUrl: String? = null
    private var roomCode: String = ""
    private var frontUiModel: BaseModel? = null
    private var rearUiModel: BaseModel? = null
    // [e] Parameter

    fun start() {
        val intent = getIntentData<ChatMessageIntentModel>(ExtraCode.CHAT_MESSAGE_ROOM_DATA)
        if (intent == null) {
            _startFinishEvent.call()
            return
        }

        intentModel.value = intent

        if (intent.targetBlockYn == AppData.Y_VALUE) {
            hintText.value = resourceProvider.getString(R.string.chat_send_message_hint_block)
            setChatEnable.value = false

        } else if (intent.targetPrivateYn == AppData.Y_VALUE) {
            hintText.value = resourceProvider.getString(R.string.chat_send_message_hint_private)
            setChatEnable.value = false
        } else {
            hintText.value = resourceProvider.getString(R.string.chat_send_message_hint)
        }

        initParameter()
        reqRoomCode(intent)
            .flatMap { tcpAndChatHistory(it, intent) }
            .doOnSubscribe {
                onLoadingShow()
                pagingModel.isLoading = true
            }
            .applyApiScheduler()
            .doOnSuccess {
                _uiList.addAll(it)

                // 첫번쨰 페이지에서 맨 앞쪽에 있는 데이터 모델 저장 처리 -> 실시간으로 UiModel 가지고 처리해야 함
                if (queryMap.pageNo == 1) {
                    val firstModel = it.getOrNull(0)
                    if (firstModel != null) {
                        saveFrontUiModel(firstModel)
                    }
                }
                queryMap.pageNo++
                onLoadingDismiss()
                pagingModel.isLoading = false
            }
            .doOnError {
                onLoadingDismiss()
                pagingModel.isLoading = false
                _startInvalidPageEvent.call()
            }
            .subscribe().addTo(compositeDisposable)
        nettyClient.setListener(this)
    }

    private fun initParameter() {
        queryMap.pageNo = 1
        pagingModel.initParams()
        _uiList.clear()
        roomCode = ""
        frontUiModel = null
        rearUiModel = null
        uploadImageUrl = ""
        sendEnable(true)
    }

    private fun tcpAndChatHistory(
        roomCode: String,
        model: ChatMessageIntentModel
    ): Single<List<BaseModel>> {
        model.roomCode = roomCode
        this.roomCode = roomCode
        return Single.zip(
            startTcp(roomCode, model.targetMemberCode),
            reqPrevChatHistory(model)
        ) { isSuccess, initList ->
            return@zip isSuccess to initList
        }.subscribeOn(Schedulers.io()).map { it.second }
    }

    private fun reqRoomCode(model: ChatMessageIntentModel): Single<String> {
        val roomCode = model.roomCode
        return if (roomCode.isNullOrEmpty()) {
            apiService.fetchMemberChatRoomInfo(ChatMemberRoomBody(model.targetMemberCode))
                .map { it.result.roomCode }
        } else {
            Single.just(roomCode).subscribeOn(Schedulers.io())
        }
    }

    private fun startTcp(
        roomCode: String,
        targetMemberCode: String
    ): Single<Unit> {
        return nettyClient.start(
            roomCode,
            targetMemberCode
        ).subscribeOn(Schedulers.io())
            .map { if (!it) throw IllegalStateException("Invalid Server Error") }
    }

    private fun reqPrevChatHistory(model: ChatMessageIntentModel): Single<List<BaseModel>> {
        queryMap.code = model.roomCode.toString()
        return apiService.fetchChatMessages(queryMap)
            .map { getUiModels(it) }
            .subscribeOn(Schedulers.io())
    }

    /**
     * 서버에서 전달 받은 데이터로
     */
    private fun getUiModels(res: ChatMessagesResponse): List<BaseModel> {
        val list = mutableListOf<BaseModel>()
        // 프로필 UiModel 추가
        if (queryMap.pageNo == res.result.totalPageCnt) {
            val initModel = getOtherProfileModel()
            if (initModel != null) {
                list.add(initModel)
                saveRearUiModel(initModel)
            }
        }

        // 반대로 뒤집어서 추가한다음 리턴할때 다시 원복한다
        // 수신자 UiModel를 그룹핑해야 하기 때문
        val sortList = res.list
            .sortedBy { it.chatTs.toLongOrDef(System.currentTimeMillis()) }
        sortList.forEach { item ->
            val chatTimeDate = getTimeDateDay(item.chatTs)
            if (!dateSet.contains(chatTimeDate)) {
                val dateModel = ChatMessageDateModel(chatTimeDate)
                saveRearUiModel(dateModel)
                dateSet.add(chatTimeDate)
                list.add(dateModel)
            }
            val model = toChatUiModel(item, rearUiModel)
            saveRearUiModel(model)
            list.add(model)
        }
        return list.reversed()
    }

    /**
     * [uiList] 중 가장 첫번째에 있는 모델을 저장하는 함수
     * 실시간 채팅때 해당 함수를 호출한다.
     * @param model [uiList] Index 0 번째 UiModel
     */
    private fun saveFrontUiModel(model: BaseModel) {
        DLogger.d("Save Front UiModel $model")
        frontUiModel = model
    }

    /**
     * [uiList] 중 가장 마지막에 있는 모델을 저장하는 함수
     * 이전 채팅 기록들을 추가 할때 처리한다.
     */
    private fun saveRearUiModel(model: BaseModel) {
        DLogger.d("Save Rear UiModel $model")
        rearUiModel = model
    }

    private fun getOtherProfileModel(): BaseModel? {
        val intentModel = intentModel.value ?: return null
        return ChatInitOtherProfileModel(intentModel)
    }

    private fun reqChatList() {
        apiService.fetchChatMessages(queryMap)
            .doOnSubscribe { pagingModel.isLoading = true }
            .map { getUiModels(it) }
            .applyApiScheduler()
            .doOnSuccess {
                _uiList.addAll(it)
                pagingModel.isLast = it.isEmpty()
                pagingModel.isLoading = false
                queryMap.pageNo++
            }
            .doOnError {
                pagingModel.isLast = true
                pagingModel.isLoading = false
            }
            .subscribe().addTo(compositeDisposable)

    }

    /**
     * 타임 스템프에 따라 yyyy년 MM월 DD일 금요일로 처리하는 함수
     * @param ts 타임 스템프
     */
    private fun getTimeDateDay(ts: String): String {
        return try {
            val date: SimpleDateFormat = if (currentLanguage == NationLanType.KO.code) {
                SimpleDateFormat("yyyy년 MM월 dd일 E요일", Locale.KOREA)
            } else {
                SimpleDateFormat("yyyy. MM. dd. E", Locale.US)
            }
            date.format(Date(ts.toLongOrDef(System.currentTimeMillis())))
        } catch (ex: Exception) {
            ""
        }
    }

    /**
     * 서버한테 받은 데이터를 각 타입에 맞는 UI Model 변환 하는 함수
     */
    private fun toChatUiModel(item: ChatMessageModel, prevModel: BaseModel?): BaseModel {
        return if (item.isMine == AppData.Y_VALUE) {
            // 내 사용자
            if (item.chatType() == ChatMsgType.TEXT) {
                ChatMyMessageModel(roomCode, item, currentLanguage)
            } else {
                ChatMyPhotoModel(roomCode, item, currentLanguage)
            }
        } else {
            // 다른 사용자
            if (item.chatType() == ChatMsgType.TEXT) {
                ChatOtherMessageModel(item, currentLanguage, prevModel)
            } else {
                ChatOtherPhotoModel(item, currentLanguage, prevModel)
            }
        }
    }

    fun onBack() {
        _startFinishEvent.call()
    }

    fun sendMessage() {
        val intentModel = intentModel.value ?: return
        val msg = messageText.value?.replace("\n", "<br>")
        if (!msg.isNullOrEmpty()) {
            val data = SendChatMessage.createMsg(
                msg,
                loginManager.getUserLoginData(),
                intentModel,
                currentLanguage
            )
            nettyClient.send(data)
            val model = ChatMyMessageModel(roomCode, msg, "")
            addOrSkipDateModel(System.currentTimeMillis().toString())
            addChatModel(model, 200)
            saveFrontUiModel(model)
            messageText.value = ""
            sendEnable(false)
        }
    }

    fun showGalleryDialog() {
        _startGalleryDialogEvent.call()
    }

    fun onLoadPage() {
        reqChatList()
    }

    /**
     * 현재 메시지를 전송하거나 받을때 날짜 UiModel 을 처리하기 위한 함수
     * 실시간 채팅시 사용
     * @param ts 타임스템프
     */
    private fun addOrSkipDateModel(ts: String) {
        val timeDate = getTimeDateDay(ts)
        if (!dateSet.contains(timeDate)) {
            val model = ChatMessageDateModel(timeDate)
            _uiList.add(0, model)
            saveFrontUiModel(model)
            dateSet.add(timeDate)
        }
    }

    /**
     * 수동으로 UiModel 추가 하는 경우 해당 함수를 호출한다
     * RecyclerView Focusing 처리하는 이슈 때문
     */
    private fun addChatModel(model: BaseModel, delay: Long = 0) {
        Single.just(Unit)
            .delay(delay, TimeUnit.MILLISECONDS)
            .applyApiScheduler()
            .doOnSuccess {
                _uiList.add(0, model)
                _startFocusingPositionEvent.value = 0
            }
            .subscribe().addTo(compositeDisposable)
    }

    /**
     * 보내기 버튼 활 / 비활성화 처리
     * @param isEnabled true 활성화, false 비활성화
     * 비공개/차단 시 비활성화
     */
    private fun sendEnable(isEnabled: Boolean) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            if (isSendReady.value != isEnabled) {
                _isSendReady.value = isEnabled
            }
        } else {
            if (isSendReady.value != isEnabled) {
                _isSendReady.postValue(isEnabled)
            }
        }
    }

    /**
     * 상대방 프로필 보기 이벤트
     */
    fun moveToProfile(model: ChatMessageIntentModel) {
        _startMoveToProfileEvent.value = model
    }

    override fun onGalleryConfirm(imageList: List<String>) {
        val roomCode = intentModel.value?.roomCode ?: return
        val data = RequestResourcePath(
            roomCode = roomCode,
            senderMemberCode = loginManager.getUserLoginData().memCd
        )
        nettyClient.send(data)
        uploadImageUrl = imageList.getOrNull(0)
        // 보내기 버튼 일시 중지
        sendEnable(false)
    }
    override fun onGalleryDismiss() {}

    /**
     * 메시지 보내고 난후 정상 처리 됐을때 받는 콜백
     * @param msg 전송된 시간을 가지고 있는 패킷
     */
    override fun onSendedMessage(msg: ResponseSendChatMessage) {
        if (msg.chatType() == ChatMsgType.TEXT) {
            Single.just(msg)
                .delay(200, TimeUnit.MILLISECONDS)
                .map {
                    val tempData = uiList.data()
                    var findIndex: Int = -1
                    for (idx in tempData.indices) {
                        val item = tempData[idx]
                        if (item is ChatMyMessageModel && item.sendTime == "") {
                            item.sendTime = getChatMessageTime(msg.timeStamp, currentLanguage)
                            findIndex = idx
                            break
                        }
                    }
                    findIndex
                }
                .applyApiScheduler()
                .doOnSuccess { _startPayloadEvent.value = it }
                .subscribe().addTo(compositeDisposable)
        }
    }

    /**
     * 사진 업로드 이후 업로드한 패킷 보내는 함수
     */
    private fun sendPhotoPacket(msg: ResponseResourcePath, intentModel: ChatMessageIntentModel) {
        val data = SendChatMessage.createPhoto(
            msg.uploadPath,
            loginManager.getUserLoginData(),
            intentModel,
            currentLanguage
        )
        nettyClient.send(data)
    }

    /**
     * 패킷이 정상적으로 처리됐을때 콜백 받는 함수
     */
    override fun onSuccessPacket() {
        sendEnable(true)
    }

    /**
     * 업로드할 이미지 리소스 받는 함수
     */
    override fun onUploadImagePath(msg: ResponseResourcePath) {
        val intentModel = intentModel.value ?: return
        val uploadUrl = uploadImageUrl
        if (!uploadUrl.isNullOrEmpty()) {
            fileProvider.requestImageUpload(uploadUrl, msg.uploadPath)
                .doLoading()
                .map { sendPhotoPacket(msg, intentModel) }
                .applyApiScheduler()
                .request(success = {
                    val model = ChatMyPhotoModel(roomCode, msg, currentLanguage)
                    addOrSkipDateModel(msg.timeStamp)
                    addChatModel(model, 200)
                    saveFrontUiModel(model)
                    sendEnable(true)
                }, failure = {
                    sendEnable(true)
                })
        } else {
            sendEnable(true)
        }
    }

    /**
     * 모두 읽음 처리
     */
    override fun onReadAllMsg() {
        try {
            val tempData = uiList.data()
            // 모두 읽음 모델이 있는 순간 반복문 종료
            for (idx in tempData.indices) {
                val item = tempData[idx]
                if (item is ChatMyMessageModel) {
                    if (!item.isReadMessage) {
                        item.isReadMessage = true
                    } else {
                        break
                    }
                } else if (item is ChatMyPhotoModel) {
                    if (!item.isReadMessage) {
                        item.isReadMessage = true
                    } else {
                        break
                    }
                }
            }
            RxBus.publish(RxBusEvent.ChatReadNotifyEvent(roomCode))
        } catch (ex: Exception) {
            //ignore
        }
    }

    /**
     * 수신자 (상대방) 한테 메시지가 실시간으로 온경우 콜백 받는 함수
     */
    override fun onReceiveMessage(msg: ReceiveChatMessage) {
        val profileImagePath = intentModel.value?.targetProfileImagePath ?: return
        val model = if (msg.chatType() == ChatMsgType.TEXT) {
            ChatOtherMessageModel(msg, currentLanguage, profileImagePath, frontUiModel)
        } else {
            ChatOtherPhotoModel(msg, currentLanguage, profileImagePath, frontUiModel)
        }
        addOrSkipDateModel(msg.timeStamp)
        addChatModel(model, 200)
        saveFrontUiModel(model)
    }
}
