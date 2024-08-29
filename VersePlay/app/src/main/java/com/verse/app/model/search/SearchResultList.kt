package com.verse.app.model.search

import com.verse.app.model.feed.FeedContentsData
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SearchResultList(
    @SerialName("popSongList")
    val popSongList: List<SearchResultSongData> = listOf(),
    @SerialName("relateFeedList")
    val relateFeedLIst: List<FeedContentsData> = listOf(),
    @SerialName("feedList")
    val feedList: List<FeedContentsData> = listOf(),
    @SerialName("songList")
    val songList: List<SearchResultSongData> = listOf(),
    @SerialName("tagList")
    val tagList: List<SearchResultTagData> = listOf(),
    @SerialName("userList")
    val userList: List<SearchResultUserData> = listOf()
)
