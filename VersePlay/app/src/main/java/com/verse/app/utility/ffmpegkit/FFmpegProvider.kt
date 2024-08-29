package com.verse.app.utility.ffmpegkit

import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.FFmpeg
import com.arthenica.mobileffmpeg.FFprobe
import com.arthenica.mobileffmpeg.Statistics
import com.arthenica.mobileffmpeg.StatisticsCallback
import com.verse.app.contants.VideoUploadPageType
import com.verse.app.extension.onWithIO
import com.verse.app.extension.toDoubleOrDef
import com.verse.app.extension.toLongOrDef
import com.verse.app.model.encode.EncodeData
import com.verse.app.model.ffmpeg.EncodeDelegateState
import com.verse.app.utility.DLogger
import javax.inject.Inject

/**
 * Description : FFMPEG Encoding Provider Class
 *
 * Created by jhlee on 2023-01-01
 */
interface FFmpegProvider {

    /**
     * MR -> Wave 변환
     */
    suspend fun onConvertMrToWav(encodeData: EncodeData): Boolean

    /**
     * 구간 Wave 자름
     */
    suspend fun onResultWav(encodeData: EncodeData): Boolean

    /**
     * 듀엣/배틀 참여 시 원본 영상 사운드 제거된 파일 생성
     */
    suspend fun removeAudioFromVideo(encodeData: EncodeData): Boolean

    /**
     * 솔로 듀엣/배틀 생성
     */
    suspend fun runOriginVideoWebm(encodeData: EncodeData): Boolean

    /**
     * 솔로 듀엣/배틀 생성
     */
    suspend fun runOriginVideoWebm(encodeData: EncodeData, callback: (EncodeDelegateState) -> Unit): Boolean

    /**
     * 솔로 듀엣/배틀 참여
     */
    suspend fun runJoinOriginVideoWebm(encodeData: EncodeData): Boolean

    /**
     * 솔로 듀엣/배틀 참여
     */
    suspend fun runJoinOriginVideoWebm(encodeData: EncodeData, callback: (EncodeDelegateState) -> Unit): Boolean

    /**
     * 솔로 듀엣/배틀 참여
     */
    suspend fun runHighlight(encodeData: EncodeData): Boolean

    /**
     * 프리뷰 생성
     */
    suspend fun runHighlight(encodeData: EncodeData, callback: (EncodeDelegateState) -> Unit): Boolean

    /**
     * 듀엣/배틀 오디오 생성
     */
    suspend fun runOriginAudioWav(encodeData: EncodeData): Boolean

    /**
     * 썸네일 생성
     */
    suspend fun runThumbnail(encodeData: EncodeData): Boolean
    suspend fun getVideoTotalFrame(videoPath: String): Long
    suspend fun runGetVideoCodec(encodeData: EncodeData): Boolean
}

class FFmpegProviderImpl @Inject constructor(
    private val cmProvider: CommandProvider
) : FFmpegProvider {

    /**
     * Create wav
     */
    override suspend fun onConvertMrToWav(encodeData: EncodeData): Boolean {
        val query = cmProvider.getConvertMrToWavCommand(encodeData)
        queryLog(query, "onConvertMrToWav")
        return FFmpeg.execute(query) == Config.RETURN_CODE_SUCCESS
    }

    override suspend fun onResultWav(encodeData: EncodeData): Boolean {

        if (!encodeData.isSection && !encodeData.isSaveAndFinish) {
            return true
        }

        val query = cmProvider.getCutSectionWavCommand(encodeData)
        queryLog(query, "cutWav")
        return FFmpeg.execute(query) == Config.RETURN_CODE_SUCCESS
    }

    override suspend fun removeAudioFromVideo(encodeData: EncodeData): Boolean {
        if (encodeData.isInitContents) {
            return true
        }
        val query = cmProvider.getRemoveAudioFromVideoCommand(encodeData)
        queryLog(query, "removeAudioFromVideo")
        return FFmpeg.execute(query) == Config.RETURN_CODE_SUCCESS
    }

    /**
     * 솔로 오디오/비디오 Full view Webm
     */
    override suspend fun runOriginVideoWebm(encodeData: EncodeData): Boolean {
        val query = run {

            if (encodeData.uploadType == VideoUploadPageType.SING_CONTENTS) {
                DLogger.d("FFmpegProvider runOriginVideoWebm=> SING_CONTENTS ")
                //피드콘텐츠인경우
                cmProvider.getOriginVideoCommand(encodeData)
            } else {
                DLogger.d("FFmpegProvider runOriginVideoWebm=> ALBUM ")
                //앨범인경우
                cmProvider.getOriginVideoAlbumCommand(encodeData)
            }
        }

        queryLog(query, "runOriginVideoWebm")

        return FFmpeg.execute(query) == Config.RETURN_CODE_SUCCESS
    }

    override suspend fun runOriginVideoWebm(
        encodeData: EncodeData,
        callback: (EncodeDelegateState) -> Unit
    ): Boolean {
        val totalFrame = onWithIO { getVideoTotalFrame(encodeData.videoMp4Path) }
        val listener: StatisticsCallback = object : StatisticsCallback {
            override fun apply(statistics: Statistics?) {
                if (statistics == null) return
                callback(EncodeDelegateState(statistics, totalFrame))
            }
        }
        // 초기화
        Config.resetStatistics()
        Config.enableStatisticsCallback(listener)
        val result = runOriginVideoWebm(encodeData)
        callback(EncodeDelegateState(100.0F, 0, 0))
        Config.enableStatisticsCallback(null)
        return result
    }

    /**
     * 참여 오디오/비디오 Full view Webm
     */
    override suspend fun runJoinOriginVideoWebm(encodeData: EncodeData): Boolean {
        val query = cmProvider.getJoinOriginVideoCommand(encodeData)
        queryLog(query, "runJoinOriginVideoWebm")
        return FFmpeg.execute(query) == Config.RETURN_CODE_SUCCESS
    }

    override suspend fun runJoinOriginVideoWebm(
        encodeData: EncodeData,
        callback: (EncodeDelegateState) -> Unit
    ): Boolean {
        val totalFrame = onWithIO { getVideoTotalFrame(encodeData.videoMp4Path) }
        val listener: StatisticsCallback = object : StatisticsCallback {
            override fun apply(statistics: Statistics?) {
                if (statistics == null) return
                callback(EncodeDelegateState(statistics, totalFrame))
            }
        }
        // 초기화
        Config.resetStatistics()
        Config.enableStatisticsCallback(listener)
        val result = runJoinOriginVideoWebm(encodeData)
        callback(EncodeDelegateState(100.0F, 0, 0))
        Config.enableStatisticsCallback(null)
        return result
    }

    /**
     * 솔로 오디오/비디오 Highligh (프리뷰)
     */
    override suspend fun runHighlight(encodeData: EncodeData): Boolean {
        val query = cmProvider.getHighlightCommand(encodeData)
        queryLog(query, "runHighlight")
        return FFmpeg.execute(query) == Config.RETURN_CODE_SUCCESS
    }

    override suspend fun runHighlight(
        encodeData: EncodeData,
        callback: (EncodeDelegateState) -> Unit
    ): Boolean {
        val highlightTime = encodeData.getHighlightTimeMs()
        val startMs = highlightTime.first.toFloat()
        val endMs = highlightTime.second.toFloat()
        val totalFrame = (((endMs - startMs) / 1000.0) * 24).toLong()
        if (totalFrame > 0) {
            // 초기화
            Config.resetStatistics()
            val listener: StatisticsCallback = object : StatisticsCallback {
                override fun apply(statistics: Statistics?) {
                    if (statistics == null) return
                    callback(EncodeDelegateState(statistics, totalFrame))
                }
            }
            Config.enableStatisticsCallback(listener)
        } else {
            callback(EncodeDelegateState(100.0F, 0, 0))
        }
        val result = runHighlight(encodeData)
        callback(EncodeDelegateState(100.0F, 0, 0))
        Config.enableStatisticsCallback(null)
        return result
    }

    /**
     * temp_mix.wav -> origin_audio.wav 변환
     */
    override suspend fun runOriginAudioWav(encodeData: EncodeData): Boolean {
        val query = cmProvider.getOriginAudio(encodeData)
        queryLog(query, "runOriginAudioWav")
        return FFmpeg.execute(query) == Config.RETURN_CODE_SUCCESS
    }

    /**
     * 썸네일 생성
     */
    override suspend fun runThumbnail(encodeData: EncodeData): Boolean {
        val query = cmProvider.getThumbnailCommand(encodeData)
        queryLog(query, "runThumbnail")
        return FFmpeg.execute(query) == Config.RETURN_CODE_SUCCESS
    }

    /**
     * ### progress 그리는 방법 ###
     * ## 예제 데이터 ##
     * avg_frame_rate
     * 1518000/48467
     * nb_frames=
     * 2024
     *
     * ## 계산 수식 ##
     * calculated frame rate = 31.32
     * converted total frames(nb_frames * target_frame_rate / calculated frame rate) = 2024 * 24 / 31.32 = 1550.95
     * converted total frames를 기준으로 잡고, 인코딩 진행 될 때 statistics의 getVideoFrameNumber를 통해 처리된 frame 수를 통해 인코딩 진행률을 계산합니다.
     * (단, 해당 진행률은 100%가 되지 않거나 100%를 초과 할 수 '있음'으로 100%를 만드는 기준은 인코딩 완료 시점을 기준으로, 100 미만의 값만 progress에 반영될 수 있도록 해줘야 합니다.)
     * (촬영시 영상의 vbr 처리로 인해 특정 프레임의 반복 또는 무시로 인해 측정된 프레임 수와 같을 수 없습니다.)
     */
    override suspend fun getVideoTotalFrame(videoPath: String): Long {
        val info = FFprobe.getMediaInformation(videoPath)
        val stream = info.streams.find { it.type == "video" } ?: return 0
        val avgFrameRate: Double = try {
            val split = stream.getStringProperty("avg_frame_rate")
                .split("/").map { it.toDoubleOrDef(0.0) }
            split[0] / split[1]
        } catch (ex: Exception) {
            1.0
        }

        val nbFrames = stream.getStringProperty("nb_frames").toLongOrDef()
        val totalFrameCount = nbFrames.toDouble() / (avgFrameRate / 24.0)
        return totalFrameCount.toLong()
    }

    /**
     * 비디오 코덱 가져오기
     */
    override suspend fun runGetVideoCodec(encodeData: EncodeData): Boolean {
        val query = cmProvider.getVideoCodec(encodeData)
        queryLog(query, "runGetVideoCodec")
        return FFprobe.execute(query) == Config.RETURN_CODE_SUCCESS
    }

    private fun queryLog(query: Array<String>, name: String) {
        if (query.isNotEmpty()) {
            DLogger.d("=============================================================")
            DLogger.d("[s] ====FFmpegProvider Query ${name} =================================== ")
            query.forEachIndexed { index, str ->
                if (index % 2 == 0) {
                    DLogger.d("Query option=> ${str} ")
                } else {
                    DLogger.d("Query value=> ${str} ")
                }
            }
            DLogger.d("[e]====FFmpegProvider Query ${name} ===================================")
            DLogger.d("=============================================================")
        } else {
            DLogger.d("query is null")
        }
    }
}