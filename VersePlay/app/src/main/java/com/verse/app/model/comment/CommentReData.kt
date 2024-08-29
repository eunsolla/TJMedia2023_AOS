package com.verse.app.model.comment

import com.verse.app.contants.AppData
import com.verse.app.contants.ListPagedItemType
import com.verse.app.model.base.BaseModel
import com.verse.app.utility.LocaleUtils
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CommentReData(

    @SerialName("replyMngCd")
    val replyMngCd: String = "",                       //답글관리코드
    @SerialName("comtMngCd")
    val comtMngCd: String = "",                       //댓글관리코드
    @SerialName("conMngCd")
    val conMngCd: String = "",                        //콘텐츠관리코드(피드/라운지/투표)
    @SerialName("comment")
    val comment: String = "",                         //댓글내용
    @SerialName("writerMemCd")
    val writerMemCd: String = "",                    //댓글작성자회원관리코드
    @SerialName("writerMemNk")
    val writerMemNk: String = "",                    //댓글작성자닉네임
    @SerialName("profileImgPath")
    val profileImgPath: String = "",                  //댓글작성자프로필이미지경로
    @SerialName("fgLikeYn")
    var fgLikeYn: String = "N",                           //좋아요여부(Y/N)
    @SerialName("likeCount")
    var likeCount: Int = 0,                           //좋아요수
    @SerialName("replyCount")
    var replyCount: Int = 0,                        //답글수
    @SerialName("updDt")
    val updDt: String = "",                           //작성일시

) : BaseModel() {

    var dateText: String? = null
        get() {
            if (field == null) {
                field = try {
                    LocaleUtils.getLocalizationTime(updDt, true)
                } catch (ex: Exception) {
                    ""
                }
            }
            return field
        }

    //좋아요
    var isLike: Boolean = false
        get() {
            return fgLikeYn == AppData.Y_VALUE
        }
        set(value) {
            fgLikeYn = if (value) {
                AppData.Y_VALUE
            } else {
                AppData.N_VALUE
            }
            field = value
        }

    //답글 더보기
    var isMore: Boolean = false

    //상위 댓글
    var parentCommentData :CommentData? = null
    //신고 여부
    var isReport :Boolean = false

    //부모 포지션값
    var parentPosition: Int = 0

    //현재 포지션
    var curPosition: Int = 0

    override fun getViewType(): ListPagedItemType {
        return itemViewType
    }

    override fun getClassName(): String {
        return "CommentReData"
    }

    override fun areItemsTheSame(newItem: Any): Boolean {
        val asItem = newItem as CommentReData
        return this.replyMngCd == asItem.replyMngCd
    }

    override fun areContentsTheSame(newItem: Any): Boolean {
        val asItem = newItem as CommentReData
        return this == asItem
    }

}

