package com.verse.app.model.comment

import com.verse.app.contants.ReportType

/**
 * Description : 댓글 신고 Local Data
 *
 * Created by jhlee on 2023-04-23
 */
data class CommentReportDialogItem(
    var reportType: ReportType? = null,
    var commentReData: CommentReData?= null,                             //하위 답글
    var mngCd: String = "",                             //해당 코드
    var nickname: String = "",                        //유저 닉네임
    var userProfileUrl: String = "",                    // 유저 이미지 패스
    var isMine: Boolean = false                    // 내 작성글인지 여부
)

