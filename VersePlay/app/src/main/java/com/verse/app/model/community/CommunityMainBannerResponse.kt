package com.verse.app.model.community

import com.verse.app.model.base.BaseResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Description : 커뮤니터 메인 배너 Response
 *
 * Created by juhongmin on 2023/05/15
 */
@Serializable
data class CommunityMainBannerResponse(
    @SerialName("result")
    val result: Result = Result()
) : BaseResponse() {
    @Serializable
    data class Result(
        @SerialName("dataList")
        val list: List<DataList> = listOf()
    )

    @Serializable
    data class DataList(
        val comBnMngCd: String = "",
        val comBnImgPath1: String = "",
        val comBnImgPath2: String = "",
        val comBnImgPath3: String = "",
        val comBnImgPath4: String = "",
        val comBnImgPath5: String = "",
        val comBnUrl1: String = "",
        val comBnUrl2: String = "",
        val comBnUrl3: String = "",
        val comBnUrl4: String = "",
        val comBnUrl5: String = "",
        val svcLdTpCd1: String = "",
        val svcLdTpCd2: String = "",
        val svcLdTpCd3: String = "",
        val svcLdTpCd4: String = "",
        val svcLdTpCd5: String = ""
    ) {
        fun getModel1(): CommunityMainBannerData? {
            return if (comBnImgPath1.isNotEmpty()) {
                CommunityMainBannerData(
                    commonCode = comBnMngCd,
                    imagePath = comBnImgPath1,
                    url = comBnUrl1.ifEmpty { null },
                    bannerCode = svcLdTpCd1.ifEmpty { null }
                )
            } else {
                null
            }
        }

        fun getModel2(): CommunityMainBannerData? {
            return if (comBnImgPath2.isNotEmpty()) {
                CommunityMainBannerData(
                    commonCode = comBnMngCd,
                    imagePath = comBnImgPath2,
                    url = comBnUrl2.ifEmpty { null },
                    bannerCode = svcLdTpCd2.ifEmpty { null }
                )
            } else {
                null
            }
        }

        fun getModel3(): CommunityMainBannerData? {
            return if (comBnImgPath3.isNotEmpty()) {
                CommunityMainBannerData(
                    commonCode = comBnMngCd,
                    imagePath = comBnImgPath3,
                    url = comBnUrl3.ifEmpty { null },
                    bannerCode = svcLdTpCd3.ifEmpty { null }
                )
            } else {
                null
            }
        }

        fun getModel4(): CommunityMainBannerData? {
            return if (comBnImgPath4.isNotEmpty()) {
                CommunityMainBannerData(
                    commonCode = comBnMngCd,
                    imagePath = comBnImgPath4,
                    url = comBnUrl4.ifEmpty { null },
                    bannerCode = svcLdTpCd4.ifEmpty { null }
                )
            } else {
                null
            }
        }

        fun getModel5(): CommunityMainBannerData? {
            return if (comBnImgPath5.isNotEmpty()) {
                CommunityMainBannerData(
                    commonCode = comBnMngCd,
                    imagePath = comBnImgPath5,
                    url = comBnUrl5.ifEmpty { null },
                    bannerCode = svcLdTpCd5.ifEmpty { null }
                )
            } else {
                null
            }
        }
    }
}
