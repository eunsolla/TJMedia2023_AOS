package com.verse.app.model.encode

import android.os.Parcelable
import com.verse.app.contants.VideoUploadPageType
import com.verse.app.model.song.SongMainData
import com.verse.app.utility.DLogger
import com.verse.app.utility.ffmpegkit.CommandProviderImpl
import kotlinx.parcelize.Parcelize


@Parcelize
data class EncodeData(
    //[s]녹화/녹음 정보
    var songMainData: SongMainData? = null,
    var singType: String = "",                                    //피드파트유형코드(PA001 : 솔로 / PA002 : 듀엣 / PA003 : 그룹 / PA004 : 배틀 / PA005 : 일반영상)
    var mediaType: String = "",                                  //피드미디어유형코드(MD001 : 녹화 / MD002 : 녹음)
    var partType: String = "",                                    //부르기파트(SP001: A / SP002 : B / SP003 : T)
    var isOff: Boolean = false,                                    //off 여부
    var syncMs: Int = 0,                                           //싱크 ms
    var totalMs: Int = 0,                                           //총 시간 ms
    var instVolume: Double = 0.0,                                       //mr 볼륨
    var micVolume: Double = 0.0,                                       //voice 볼륨
    var previewStartMs: Int = 0,                                 //프리뷰 ms
    var singPoint: Int = 0,                                          // 부르기 점수
    var isInitContents: Boolean = true,                          //최초 생성 여부
    var score: Int = 0,                                                //스코어 점수
    var songName: String = "",                                     //곡 명

    //[s]구간
    var isSaveAndFinish: Boolean = false,                          //1분후 저장 여부
    var isSection: Boolean = false,                                   //구간 여부  부르기유형코드(SI001 : 전체부르기 / SI002 : 구간부르기)
    var sectionStartMs: Int = 0,                                      //구간 시작
    var sectionEndMs: Int = 0,                                        //구간 종료
    var feedDesc: String = "",                                          //피드설명
    var feedTag: String = "",                                           //피드 태그
    var useReply: String = "",                                          //댓글 허용 여부
    var showContentsType: String = "",                            //콘텐츠노출설정코드(SH002 : 나만허용 / SH003 : 친구허용 / SH001 : 전체허용)

    //[s]파일 경로 정보
    var mixPath: String = "",                                                            //mr+voice mix 경로
    var mrMp3Path: String = "",                                                       //mp3 경로
    var mrWavPath: String = "",                                                       //mr 경로
    var mrSectionWavPath: String = "",                                             //mr 구간 경로
    var videoMp4Path: String = "",                                                    //mp4 경로( 바누바 녹화본)
    var orgConPath: String = "",                                                       //최초 생성자 비디오
    var reOrgConPath: String = "",                                                 //최초 생성자 비디오 (사운드 제거)
    var orgAudioPath: String = "",                                                       //최초 생성자 audio

    var encodeThumbPath: String = "",                                               //인코딩 섬네일 저장 경로
    var encodeHighlightPath: String = "",                                             //인코딩 프리뷰 저장 경로
    var encodeOriginVideoWebmPath: String = "",                                  //인코딩 풀 영상 저장 경로
    var encodeOriginAudioWavPath: String = "",                                    //인코딩 오디오 저장 경로
    var encodeDirPath: String = "",                                                      //인코딩 파일 경로
    var singDirPath: String = "",                                                          //인코딩 대상 파일 경로
    var albumDirPath: String = "",                                                        //인코딩 대상 앨범 파일 경로
    var waterMarkPath:String = "",                                                       //워터마크 이미지 경로
    var isEncodedCompleted: Boolean = false,                                       //인코딩 성공 여부
    var uploadType: VideoUploadPageType = VideoUploadPageType.ALBUM // 업로드 타입 ( 콘텐츠 / 앨범 )
) : Parcelable {

    /**
     * 하이라이트 시간 가져오는 함수
     */
    fun getHighlightTime(): Pair<String, String> {

        DLogger.d("query getHighlightTime -> 프리뷰 시작:  ${getTime(previewStartMs)} /구간 시작:   ${getTime(sectionStartMs)} /구간 끝:  ${getTime(sectionEndMs)} ")

        return if (!isSection && !isSaveAndFinish) {
            DLogger.d("query getHighlightTime")
            millisecondsToStringFormat(previewStartMs.toLong()) to CommandProviderImpl.OPTION_VALUE_HIGHLIGHT_END_MS
        } else {

            //60초 기준 체크
            val seconds60 = (sectionEndMs - sectionStartMs) >= CommandProviderImpl.HIGHLIGHT_STANDARD_MILLISECONDS
            //30초 기준 체크
            val seconds30 = (sectionEndMs - sectionStartMs) >= CommandProviderImpl.HIGHLIGHT_MILLISECONDS

            DLogger.d("query getHighlightTime seconds60=> ${seconds60} /  ${seconds30} / ${getTime((sectionEndMs - sectionStartMs))} / ${(sectionEndMs - sectionStartMs)}")
            if (seconds60 || seconds30) {
                millisecondsToStringFormat(previewStartMs.toLong()) to CommandProviderImpl.OPTION_VALUE_HIGHLIGHT_END_MS
            } else {
                CommandProviderImpl.OPTION_VALUE_START_MS_ZERO to CommandProviderImpl.OPTION_VALUE_HIGHLIGHT_END_MS
            }
        }
    }

    private fun getTime(ms: Int): String? {
        val seconds = ms / 1000
        val rem = seconds % 3600
        val mn = rem / 60
        val sec = rem % 60
        return String.format("%02d", mn) + ":" + String.format("%02d", sec)
    }


    fun getHighlightTimeMs(): Pair<Long, Long> {
        return if (!isSection || !isSaveAndFinish) {
            previewStartMs.toLong() to (previewStartMs + CommandProviderImpl.HIGHLIGHT_MILLISECONDS).toLong()
        } else {
            //60초 기준 체크
            val seconds60 =
                (sectionEndMs - sectionStartMs) >= CommandProviderImpl.HIGHLIGHT_STANDARD_MILLISECONDS
            if (seconds60) {
                sectionStartMs.toLong() to sectionEndMs.toLong()
            } else {
                //30초 기준 체크
                val seconds30 =
                    (sectionEndMs - sectionStartMs) >= CommandProviderImpl.HIGHLIGHT_MILLISECONDS
                if (seconds30) {
                    sectionStartMs.toLong() to sectionEndMs.toLong()
                } else {
                    0L to 30_000L
                }
            }
        }
    }

    /**
     * TimeConvert
     */
    private fun millisecondsToStringFormat(milliseconds: Long): String {
        val seconds = milliseconds / 1000
        val hours = seconds / 60 / 60
        val minutes = seconds / 60
        val sec = seconds - minutes * 60
        val ms = milliseconds - seconds * 1000
        return String.format("%02d:%02d:%02d.%02d", hours, minutes, sec, ms)
    }
}
