package com.verse.app.utility

import com.verse.app.contants.*
import com.verse.app.model.encode.EncodeData
import com.verse.app.model.feed.CurrentFeedData
import com.verse.app.model.feed.FeedContentsData
import com.verse.app.model.user.UserData
import com.verse.app.utility.RxBusEvent.SingUploadProgressEvent.Type.END
import com.verse.app.utility.RxBusEvent.SingUploadProgressEvent.Type.PROGRESS
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject

/**
 * Description : RxBusEvent
 *
 * Created by jhlee on 2023-01-01
 */

object RxBus {

    private val publisher = PublishSubject.create<Any>()

    fun publish(event: Any) {
        publisher.onNext(event)
    }

    fun <T : Any> listen(eventType: Class<T>): Observable<T> = publisher.ofType(eventType)
}

class RxBusEvent {

    /**
     * 메인 진입 제어 이벤트
     */
    data class MainEnterEvent(
        val type: MainEntryType,
        val isFlag: Boolean,
    )

    /**
     * 메인 ViewPager 상태
     */
    data class MainSwipeEvent(
        val type: MainStructureType,
        val isRefresh: Boolean,
    )

    /**
     * 메인 ViewPager 상태
     */
    data class MainTabSwipeEvent(
        val type: ExoPageType,
        val onPlay: Boolean,
    )

    /**
     * Bottom navi move type
     */
    data class NaviEvent(
        val type: NaviType,
        val isRefresh: Boolean = false,      //로그 아웃  후 이동 시  구분을 위함.
        val communityEnterTabType: Int = 0  // 커뮤니티 탭 포지션 구분을 위함
    )

    /**
     * Encode State
     */
    data class EncodeEvent(
        val isSuccess: Boolean,
        val encodeData: EncodeData
    )

    /**
     * Encode State
     */
    data class EncodeFailEvent(
        val encodeData: EncodeData? = null,
        val isFile: Boolean
    )

    /**
     * 재생 컨텐츠 -> 우측 마이페이지 전달
     */
    data class VideoUserInfoEvent(
        val currentFeedData: CurrentFeedData
    )

    /**
     * 씽패스 Resfresh Event
     */
    data class SingPassEvent(
        val isRefresh: Boolean
    )

    open class LoungeRefreshEvent(
        val isTop: Boolean = true
    )

    open class LoungeLikeRefreshEvent(
        val isTop: Boolean = true
    )

    /**
     * 답글/댓글 페이지 목록 변경 시 갱신 처리
     * @param 피드 컨텐츠 mngcd
     * @param 최신 댓글 총 수
     */
    open class FeedCommentRefreshEvent(
        val feedMngCd: String,
        val commentCount: Int
    )

    /**
     * 마이 페이지 상태 변경 되었을때 갱신 처리하는 로직
     * @param 프로필 편집한 멤버 코드 -> UserData로 변경
     */
    data class MyPageRefreshEvent(
        val memberData: UserData
    )

    data class MyPageUploadRefreshEvent(
        val type: VideoUploadPageType
    )

    /**
     * 마이 페이지 이미지 변경 되었을때 갱신 처리하는 로직
     * @param 프로필 편집한 멤버 코드 -> UserData로 변경
     */
    data class ProfileRefreshEvent(
        val profileImg: String = "",
        val bgProfileImg: String = "",
        val profileType: Boolean = false,
        val bgProfileType: Boolean = false
    )

    /**
     * 좋아요,북마크,삭제 등 컨텐츠 갱신시 사용
     */
    data class FeedRefreshEvent(
        val feedContentData: FeedContentsData,
        val deleteRefreshFeedList: DeleteRefreshFeedList
    )

    /**
     * 팔로우 해제/팔로잉 했을 경우 컨텐츠 갱신 위해 follow api 호출
     */
    data class FollowRefreshEvent(
        val memberCode: String
    )

    data class MypageFollowRefreshEvent(
        val memberCode: String
    )

    data class HttpStatusErrorEvent(
        val type: HttpStatusType,
        val isExitApp : Boolean = false
    )

    /**
     * 채팅 모두 읽음 처리 관련 갱신 처리 이벤트
     */
    data class ChatReadNotifyEvent(
        val roomCode: String
    )

    open class RefreshDataEvent

    sealed class SingUploadProgressEvent(
        open val state: Type
    ) {
        /**
         * 녹음 업로드
         */
        data class AudioType(
            val progress: Float, // 0.0% ~ 100%
            override val state: Type
        ) : SingUploadProgressEvent(state) {
            constructor(progress: Float) : this(
                progress = progress,
                state = PROGRESS
            )

            constructor() : this(
                progress = 100.0F,
                state = END
            )
        }

        /**
         * 영상 업로드
         */
        data class VideoType(
            val type: Type,
            val progress: Float, // 0.0% ~ 100%
            override val state: SingUploadProgressEvent.Type
        ) : SingUploadProgressEvent(state) {
            enum class Type(val maxPercent: Float) {
                ORIGIN(50.0F),
                HIGHLIGHT(20.0F),
                UPLOAD(30.0F)
            }

            fun getRelativePercent(): Float {
                // 비례식 공식 사용
                val value = progress * type.maxPercent
                return value / 100.0F
            }

            constructor(type: Type, progress: Float) : this(
                type = type,
                progress = progress,
                state = PROGRESS
            )

            constructor(type: Type) : this(
                type = type,
                progress = 100.0F,
                state = END
            )
        }

        open class UploadEnd : SingUploadProgressEvent(END)

        /**
         * @see PROGRESS 업로드 진행
         * @see END 업로드 종료
         */
        enum class Type {
            PROGRESS, END
        }
    }
}