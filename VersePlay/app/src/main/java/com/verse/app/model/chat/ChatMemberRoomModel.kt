package com.verse.app.model.chat

import android.text.SpannableStringBuilder
import com.verse.app.contants.ChatMsgType
import com.verse.app.contants.ListPagedItemType
import com.verse.app.model.base.BaseModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Description : 채팅 방 정보 Entity
 *
 * Created by juhongmin on 2023/06/14
 */
@Serializable
data class ChatMemberRoomModel(
    @SerialName("chatRoomCd")
    val code: String = "", // 채팅룸관리코드
    @SerialName("svcNtCd")
    val nationCode: String = "", // 서비스대상국가코드
    @SerialName("targetMemCd")
    val targetMemberCode: String = "", // 상대방회원관리코드
    @SerialName("targetPfImgPath")
    val targetProfileImagePath: String = "", // 상대방프로필이미지경로
    @SerialName("targetMemNk")
    val targetMemberNk: String = "", // 상대방닉네임
    @SerialName("recentChatMsg")
    val recentChatMsg: String = "", // 최근 메시지 내용
    @SerialName("recentChatTpCd")
    val recentChatType: String = "", // 최근 메시지 유형 코드(TEXT : 텍스트 / PHOTO : 사진)
    @SerialName("recentChatReadYn")
    val recentChatReadYn: String = "", // 최근 메시지 읽음 상태 (Y : 읽음 / N : 읽지않음)
    @SerialName("recentChatReadTime")
    val recentChatReadTime: String = "", // 최근 메시지 읽음 시간
    @SerialName("recentChatDt")
    val recentChatDate: String = "", // 최근 메시지 등록 일시
    @SerialName("targetMemDesc")
    val targetMemDesc: String = "", // 상대방회원자기소개메시지
    @SerialName("targetFollowerCnt")
    val targetFollowerCnt: String = "", // 상대방회원팔로워수
    @SerialName("targetContentsCnt")
    val targetContentsCnt: String = "", // 상대방회원게시물수
    @SerialName("fgPrivateYn")
    val fgPrivateYn: String = "", // 비공개 계정 여부
    @SerialName("fgBlockYn")
    val fgBlockYn: String = "" // 차단 대상 여부

) : BaseModel() {

    override fun getViewType(): ListPagedItemType {
        return ListPagedItemType.ITEM_CHAT_ROOM
    }

    override fun getClassName(): String {
        return "ChatMemberRoomModel"
    }

    override fun areItemsTheSame(diffUtil: Any): Boolean {
        return if (diffUtil is ChatMemberRoomModel) {
            code == diffUtil.code && recentChatReadYn == diffUtil.recentChatReadYn
        } else {
            false
        }
    }

    override fun areContentsTheSame(diffUtil: Any): Boolean {
        return if (diffUtil is ChatMemberRoomModel) {
            this == diffUtil
        } else {
            false
        }
    }

    val recentMsg: CharSequence
        get() {
            val str = SpannableStringBuilder()
            if (recentChatType == ChatMsgType.TEXT.code) {
                str.append(recentChatMsg)
            } else {
                str.append("이미지를 보냈습니다.")
            }

            return str
        }

    val isReadMsg: Boolean get() = recentChatReadYn == "Y"
}
