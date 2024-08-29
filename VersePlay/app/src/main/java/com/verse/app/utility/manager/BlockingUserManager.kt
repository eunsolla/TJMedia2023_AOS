package com.verse.app.utility.manager

/**
 * Description : 사용자 차단 이후 해당 콘텐츠 안보이게 처리하는 함수
 *
 * Created by juhongmin on 2023/06/23
 */
object BlockingUserManager {
    private val blockingChattingRooms: HashSet<String> by lazy { hashSetOf() }

    fun addRoomBlocking(roomCode: String) {
        blockingChattingRooms.add(roomCode)
    }

    fun isRoomBlocking(roomCode: String): Boolean {
        return blockingChattingRooms.contains(roomCode)
    }
}
