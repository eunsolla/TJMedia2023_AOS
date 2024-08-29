package com.verse.app.model.vote

import com.verse.app.contants.AppData
import com.verse.app.extension.toDoubleOrDef
import com.verse.app.extension.toIntOrDef
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Description :
 *
 * Created by juhongmin on 2023/05/24
 */
@Serializable
data class VoteResultData(
    @SerialName("comVoteMngCd")
    val code: String = "",
    @SerialName("comVoteStDt")
    val startDt: String = "",
    @SerialName("comVoteFnDt")
    val endDt: String = "",
    @SerialName("stCd")
    val statusCode: String = "",
    @SerialName("comVoteTitle")
    val title: String = "",
    @SerialName("comVoteImgPath")
    val imagePath: String = "",
    @SerialName("joinVoteYn")
    val joinVoteYn: String = "",
    @SerialName("voteItem")
    val voteItem: String = "", // 이전에 투표 선택한 번호 (1 ~ 4)
    @SerialName("voteTopIndex")
    val voteTopIndex: String = "",
    @SerialName("distItem1")
    val selectOne: String = "",
    @SerialName("distItem2")
    val selectTwo: String = "",
    @SerialName("distItem3")
    val selectThree: String = "",
    @SerialName("distItem4")
    val selectFour: String = "",
    @SerialName("itemCount1")
    val selectOneCount: String = "", // 선택시 1 투표수
    @SerialName("itemCount2")
    val selectTwoCount: String = "", // 선택시 2 투표수
    @SerialName("itemCount3")
    val selectThreeCount: String = "", // 선택시 3 투표수
    @SerialName("itemCount4")
    val selectFourCount: String = "", // 선택시 4 투표수
    @SerialName("itemRate1")
    val selectOneRate: String = "", // 선택지 1 투표율
    @SerialName("itemRate2")
    val selectTwoRate: String = "", // 선택지 2 투표율
    @SerialName("itemRate3")
    val selectThreeRate: String = "", // 선택지 3 투표율
    @SerialName("itemRate4")
    val selectFourRate: String = "" // 선택지 4 투표율
) {

    data class VoteRaking(
        val title: String,
        val count: Int,
        val rate: String,
        val isMySelected: Boolean // 내가 선택한 거 true, 내가 선택 하지 않은거 false
    ) {
        val rateTxt: String = "${String.format("%.0f", rate.toDoubleOrDef(0.0))}%"
        var isTop: Boolean = false
        var isNotVote: Boolean = false // true 아무도 투표를 안했습니다, false 투표를 한경우

        // 파란색 색상 표시
        val isBlueBg: Boolean
            get() {
                return if (isNotVote) {
                    false
                } else {
                    isTop
                }
            }
        val isBlueCheck: Boolean
            get() {
                return if (isNotVote) {
                    false
                } else isMySelected && isTop
            }
        val isGrayCheck: Boolean
            get() {
                return if (isNotVote) {
                    false
                } else isMySelected && !isTop
            }

    }

    private val rankingList: MutableList<VoteRaking> by lazy { mutableListOf() }

    val firstInfo: VoteRaking? get() = rankingList.getOrNull(0)
    val secondInfo: VoteRaking? get() = rankingList.getOrNull(1)
    val thirdInfo: VoteRaking? get() = rankingList.getOrNull(2)
    val fourthInfo: VoteRaking? get() = rankingList.getOrNull(3)

    fun calculateRanking() {
        // 투표를 아무도 하지 않은 경우 == itemCountX 모두 0일 경우 1번 조건에 부합하지 않느다.
        val list = mutableListOf<VoteRaking>()
        var topCount = 0 // 투표한 1등
        val mySelectPos = if (joinVoteYn == AppData.Y_VALUE) {
            voteItem.toIntOrDef(1)
        } else {
            -1
        }
        if (selectOne.isNotEmpty()) {
            val selectCount = selectOneCount.toIntOrDef(0)
            if (topCount < selectCount) {
                topCount = selectCount
            }
            list.add(
                VoteRaking(
                    selectOne,
                    selectCount,
                    selectOneRate,
                    mySelectPos == 1
                )
            )
        }
        if (selectTwo.isNotEmpty()) {
            val selectCount = selectTwoCount.toIntOrDef(0)
            if (topCount < selectCount) {
                topCount = selectCount
            }
            list.add(
                VoteRaking(
                    selectTwo,
                    selectCount,
                    selectTwoRate,
                    mySelectPos == 2
                )
            )
        }
        if (selectThree.isNotEmpty()) {
            val selectCount = selectThreeCount.toIntOrDef(0)
            if (topCount < selectCount) {
                topCount = selectCount
            }
            list.add(
                VoteRaking(
                    selectThree,
                    selectCount,
                    selectThreeRate,
                    mySelectPos == 3
                )
            )
        }
        if (selectFour.isNotEmpty()) {
            val selectCount = selectFourCount.toIntOrDef(0)
            if (topCount < selectCount) {
                topCount = selectCount
            }
            list.add(
                VoteRaking(
                    selectFour,
                    selectCount,
                    selectFourRate,
                    mySelectPos == 4
                )
            )
        }


        list.forEach { item ->
            // 1등 투표한 개 0인경우
            if (topCount == 0) {
                item.isNotVote = true
            }
            // 1등된 투표 셋팅
            item.isTop = topCount != 0 && item.count == topCount
        }

        rankingList.addAll(list)
//        rankingList.addAll(list.sortedByDescending { it.count })
    }
}
