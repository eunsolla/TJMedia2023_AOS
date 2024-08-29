package com.verse.app.model.xtf

import androidx.annotation.ColorRes
import com.verse.app.contants.ListPagedItemType
import com.verse.app.contants.SingType
import com.verse.app.contants.SingingCommandType
import com.verse.app.contants.SingingPartType
import com.verse.app.model.base.BaseModel
import kotlinx.serialization.Serializable

@Serializable
data class XTF_DTO(
    val number: String = "",
    val title1: String = "",
    val title2: String = "",
    val info1: String = "",
    val info2: String = "",
    val info3: String = "",
    val sections: MutableList<XTF_SECTION_DTO> = mutableListOf(),
    val events: MutableList<XTF_EVENT_DTO> = mutableListOf(),
) {
    //가사 정보
    var lyricsText: String = ""
        get() {
            val sb = StringBuilder()
            sections[0].lyrics.forEachIndexed { index, xtfLyriceDto ->
                sb.append(xtfLyriceDto.realText)
                if (index != sections[0].lyrics.size - 1) {
                    sb.append("\n")
                }
            }
            return sb.toString()
        }
}

@Serializable
data class XTF_EVENT_DTO(
    var type: SingingCommandType = SingingCommandType.NONE,
    var eventTime: Int = 0,
)

@Serializable
data class XTF_LYRICE_DTO(
    var textWidth: Int = 0,
    var currentWidth: Int = 0,
    var finishTime: Int = 0,
    var imgPartA: String = "",
    var imgPartB: String = "",
    var isImgPartVisible: Boolean = false,
    var isLineFinished: Boolean = false,
    var realText: String,
    @ColorRes
    var lineColor: Int = -1,
    @ColorRes
    var lineDefaultColor: Int = -1,

    var events: MutableList<XTF_LYRICE_EVENT_DTO>,
    var partType: SingingPartType = SingingPartType.DEFAULT,
) : BaseModel() {

    override fun getViewType(): ListPagedItemType {
        return itemViewType
    }

    override fun getClassName(): String {
        return "XTF_LYRICE_DTO"
    }

    override fun areItemsTheSame(newItem: Any): Boolean {
        val asItem = newItem as XTF_LYRICE_DTO
        return this.realText == asItem.realText
    }

    override fun areContentsTheSame(newItem: Any): Boolean {
        val asItem = newItem as XTF_LYRICE_DTO
        return this == asItem
    }
}


@Serializable
data class XTF_LYRICE_EVENT_DTO(

    var startWidth: Float = 0f,
    var gapWidth: Float = 0f,
    /** use app data  */
    var onVoice: Boolean = false,

    override var startAt: Int = 0,
    override var lastAt: Int = 0,
    override var eventTime: Int = 0,
    override var duration: Int = 0,

    ) : XTF_LYRICE_EVENT_DTO_BASE()

@Serializable
data class XTF_SECTION_DTO(
    var lyrics: MutableList<XTF_LYRICE_DTO> = mutableListOf(),
)


abstract class XTF_LYRICE_DTO_BASE {
    /** 한줄 가사  */
    abstract var realText: String

    /** 한줄 가사 이벤트  */
    abstract var events: MutableList<XTF_LYRICE_EVENT_DTO>

    /** part infomation  */
    abstract var partType: SingingPartType
}


abstract class XTF_LYRICE_EVENT_DTO_BASE {
    abstract var startAt: Int
    abstract var lastAt: Int
    abstract var eventTime: Int
    abstract var duration: Int
}