package com.verse.app.model.song

import android.os.Parcelable
import com.verse.app.contants.ListPagedItemType
import com.verse.app.model.base.BaseModel
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Description : 신곡,인기곡
 *
 * Created by jhlee on 2023-02-27
 */
@Parcelize
@Serializable
data class SongData(

    @SerialName("idx")
    val idx: Int = 0,

    @SerialName("soundIdx")
    val soundIdx: Int = 0,

    @SerialName("songname")
    val songName: String = "",

    @SerialName("singer")
    val singer: String = "",

    @SerialName("sound_file")
    val soundFile: String = "",

    @SerialName("highlight_file")
    val highlightFile: String = "",

    @SerialName("img_file")
    val imgFile: String = "",

    @SerialName("height_img_file")
    val heightImgFile: String = "",

    @SerialName("json_file")
    val json_file: String = "",

    @SerialName("duet_yn")
    val duetYn: String = "",

    @SerialName("userFlag")
    val userFlag: String = "",
) : Parcelable, BaseModel() {

    override fun getViewType(): ListPagedItemType {
        return itemViewType
    }

    override fun getClassName(): String {
        return "SongData"
    }

    override fun areItemsTheSame(newItem: Any): Boolean {
        val asItem = newItem as SongData
        return this.idx == asItem.idx
    }

    override fun areContentsTheSame(newItem: Any): Boolean {
        val asItem = newItem as SongData
        return this == asItem
    }
}