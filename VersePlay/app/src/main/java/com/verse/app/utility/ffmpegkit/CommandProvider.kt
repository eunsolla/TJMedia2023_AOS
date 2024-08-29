package com.verse.app.utility.ffmpegkit

import android.content.Context
import com.verse.app.contants.MediaType
import com.verse.app.contants.PartType
import com.verse.app.model.encode.EncodeData
import com.verse.app.utility.DLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileFilter
import java.util.regex.Pattern
import javax.inject.Inject


/**
 * Description : FFMPEG Command Provider Class
 *
 * Created by jhlee on 2023-01-01
 */
interface CommandProvider {
    fun getConvertMrToWavCommand(encodeData: EncodeData): Array<String>
    fun getCutSectionWavCommand(encodeData: EncodeData): Array<String>
    fun getRemoveAudioFromVideoCommand(encodeData: EncodeData): Array<String>
    fun getOriginVideoCommand(encodeData: EncodeData): Array<String>
    fun getOriginVideoAlbumCommand(encodeData: EncodeData): Array<String>
    fun getJoinOriginVideoCommand(encodeData: EncodeData): Array<String>
    fun getHighlightCommand(encodeData: EncodeData): Array<String>
    fun getOriginAudio(encodeData: EncodeData): Array<String>
    fun getThumbnailCommand(encodeData: EncodeData): Array<String>
    fun getTotalFrameCount(encodeData: EncodeData): Array<String>
    fun getVideoCodec(encodeData: EncodeData): Array<String>
}

class CommandProviderImpl @Inject constructor(
    @ApplicationContext private val ctx: Context,
) : CommandProvider {

    companion object {
        const val HIGHLIGHT_STANDARD_MILLISECONDS = 60000 // 하이라이트 생성 구간 기준 초
        const val HIGHLIGHT_MILLISECONDS = 30000 // 하이라이트 영상 시간
        const val SONG_ENCODING_NOTIFICATION_ID = 2 // 백그라운드 서비스 실행 알림바 ID
        const val SONG_ENCODING_NOTIFICATION_CHANNEL_ID = "VERSE PLAY" // 노래 인코딩 알림바 채널 아이디
        const val OPTION_VALUE_AAC = "aac" // FFMPEG Audio Codec aac           (option name => -c:a)
        const val OPTION_VALUE_COPY = "copy" // FFMPEG Video Codec               (option name => -c:a)
        const val OPTION_VALUE_VIDEO_FRAMES = "24" // FFMPEG Video Frame               (option name => -r)
        const val OPTION_VALUE_SIZE = "320x690" // FFMPEG Video Frame               (option name => -s)
        const val OPTION_VALUE_VIDEO_BITRATE_2000K = "2000k" // FFMPEG Video Bitrate             (option name => -b:v)
        const val OPTION_VALUE_AUDIO_BITRATE_128K = "128k" // FFMPEG Audio Bitrate             (option name => -b:a)
        const val OPTION_VALUE_AUDIO_SAMPLE_RATES_48000 = "48000" // FFMPEG Audio Sample Rate         (option name => -ar)
        const val OPTION_VALUE_STRICT_VERY = "very" // FFMPEG Strict                    (option name => -strict)
        const val OPTION_VALUE_START_MS_DEFAULT = 400 // FFMPEG Start Second              (option name => -ss)
        const val OPTION_VALUE_START_MS_ZERO = "00:00:00.000" // FFMPEG Start Second              (option name => -ss)
        const val OPTION_VALUE_HIGHLIGHT_END_MS = "00:00:30.000" // FFMPEG End Second                (option name => -t)
        const val OPTION_VALUE_HIGHLIGHT_VFRAMES = "1" // FFMPEG Video Frame               (option name => -vframes)
        const val OPTION_VALUE_MP3 = "mp3" // FFMPEG Audio Codec mp3           (option name => -vframes)
        const val OPTION_VALUE_AUDIO_CHANNEL = "2" // FFMPEG Audio Channel             (option name => -ac)
        const val OPTION_VALUE_LIB_VPX_VP9 = "libvpx-vp9" // FFMPEG Video Codec               (option name => -c:v)
        const val OPTION_VALUE_LIB_OPUS = "libopus" // FFMPEG Audio Codec               (option name => -c:a)
        const val OPTION_VALUE_ENCODE_SPEED = "8" // FFMPEG SPEED                     (option name => -speed)
        const val OPTION_VALUE_ENCODE_QUALITY_REALTIME = "realtime" // FFMPEG QUALITY                   (option name => -quality)
        const val OPTION_VALUE_HSTACK = "hstack"                                    // FFMPEG QUALITY                   (option name => -filter_complex)
        const val OPTION_VALUE_THREAD_CNT = "0"                     // FFMPEG 최적화 쓰레드 수
        const val OPTION_VALUE_PRESET = "slower"
        const val OPTION_VALUE_320_1 = "scale=320:-1"
        const val OPTION_VALUE_320_PADDING = "scale=320x690:force_original_aspect_ratio=decrease,pad=320x690:(ow-iw)/2:(oh-ih)/2"     // FFMPEG
        const val OPTION_VALUE_TRUE = "1"
        const val OPTION_VALUE_FALSE = "0"
        const val OPTION_DELIMITER_EQUAL = "="
        const val OPTION_DELIMITER_COMMA = ","
        const val OPTION_DELIMITER_COLON = ":"


        const val OPTION_Y = "-y" // FFMPEG OPTION
        const val OPTION_I = "-i" // FFMPEG OPTION
        const val OPTION_CV = "-c:v" // FFMPEG OPTION
        const val OPTION_CA = "-c:a" // FFMPEG OPTION
        const val OPTION_BV = "-b:v" // FFMPEG OPTION
        const val OPTION_BA = "-b:a" // FFMPEG OPTION
        const val OPTION_SPEED = "-speed" // FFMPEG OPTION
        const val OPTION_QUALITY = "-quality" // FFMPEG OPTION
        const val OPTION_CODEC = "-codec" // FFMPEG OPTION
        const val OPTION_R = "-r" // FFMPEG OPTION
        const val OPTION_S = "-s" // FFMPEG OPTION
        const val OPTION_T = "-t" // FFMPEG OPTION
        const val OPTION_AR = "-ar" // FFMPEG OPTION
        const val OPTION_AC = "-ac" // FFMPEG OPTION
        const val OPTION_SS = "-ss" // FFMPEG OPTION
        const val OPTION_VF = "-vf" // FFMPEG OPTION
        const val OPTION_VCODEC = "-vcodec" // FFMPEG OPTION
        const val OPTION_AN = "-an" // FFMPEG OPTION
        const val OPTION_VFRAMES = "-vframes" // FFMPEG OPTION
        const val OPTION_STRICT = "-strict" // FFMPEG OPTION
        const val OPTION_ACODEC_LIBMP3LAME = "-acodec libmp3lame" // FFMPEG OPTION
        const val OPTION_FILTER_COMPLEX = "-filter_complex" // FFMPEG OPTION
        const val OPTION_THREAD_CNT = "-threads" // FFMPEG OPTION
        const val OPTION_PRESET = "-preset"
        const val OPTION_VALUE_HSTACK_WITH_SCALE = "[0:v]scale=$OPTION_VALUE_SIZE[v0];[1:v]scale=$OPTION_VALUE_SIZE[v1];[v0][v1]hstack=inputs=2"        // hstack + resize (듀엣 or 배틀 시 바누바에서 640x1380으로 촬영된 영상으로 hstack을 수행할 시 'parsed_hstack_0' 오류 발생하여 해상도 맞춤.
        const val OPTION_VALUE_WATER_MARK = "[1]colorchannelmixer=aa=0.5,scale=iw*0.7:-1[wm];[0][wm]overlay=x=(W-w)/2:y=(H-h)/2"        // 워터 마크 투명도,위치

        const val PROB_OPTION_V = "-v"  // FFPROBE OPTION
        const val PROB_OPTION_VALUE_V_ERROR = "error"  // FFPOBE OPTION
        const val PROB_OPTION_SELECT_STREAM = "-select_streams"  // FFPROBE OPTION
        const val PROB_OPTION_VALUE_VIDEO_STREAM_ZERO = "v:0"  // FFPROBE OPTOIN
        const val PROB_OPTION_SHOW_ENTRIES = "-show_entries"  // FFPROBE OPTION
        const val PROB_OPTION_VALUE_STREAM = "stream"  // FFPROBE OPTION
        const val PROB_OPTION_VALUE_NB_FRAMES = "nb_frames"  // FFPROBE OPTION
        const val PROB_OPTION_VALUE_CODEC_NAME = "codec_name"  // FFPROBE OPTION
        const val PROB_OPTION_OF = "-of"  // FFPROBE OPTION
        const val PROB_OPTION_VALUE_DEFAULT_NOPRINT_WRAPPERS_1 = "default=noprint_wrappers=1"  // FFPROBE OPTION
        const val PROB_OPTION_VALUE_NOKEY = "nokey"  // FFPROBE OPTION
        const val PROB_OPTION_VALUE_AVG_FRAME_RATE = "avg_frame_rate"  // FFPROBE OPTION
    }

    /**
     * MR to Wav
     */
    override fun getConvertMrToWavCommand(encodeData: EncodeData): Array<String> {

        val inputs: ArrayList<String> = ArrayList()
        inputs.apply {

            add(OPTION_Y)
            add(OPTION_I)

            if (encodeData.isInitContents) {
                add(encodeData.mrMp3Path)
            } else {
                add(encodeData.orgAudioPath)
            }

            add(OPTION_AR)
            add(OPTION_VALUE_AUDIO_SAMPLE_RATES_48000)
            add(OPTION_BA)
            add(OPTION_VALUE_AUDIO_BITRATE_128K)
            add(OPTION_THREAD_CNT)
            add(getNumCores())
            add(OPTION_PRESET)
            add(OPTION_VALUE_PRESET)
            add(encodeData.mrWavPath)
        }

        return inputs.toArray(arrayOfNulls<String>(inputs.size))
    }


    override fun getCutSectionWavCommand(encodeData: EncodeData): Array<String> {

        val inputs: ArrayList<String> = ArrayList()

        inputs.apply {
            add(OPTION_Y)
            add(OPTION_SS)
            add(millisecondsToStringFormat(encodeData.sectionStartMs.toLong()))
            add(OPTION_I)
            add(encodeData.mrWavPath)
            add(OPTION_T)
            add(millisecondsToStringFormat((encodeData.totalMs).toLong()))
            add(OPTION_AR)
            add(OPTION_VALUE_AUDIO_SAMPLE_RATES_48000)
            add(OPTION_BA)
            add(OPTION_VALUE_AUDIO_BITRATE_128K)
            add(OPTION_THREAD_CNT)
            add(getNumCores())
            add(OPTION_PRESET)
            add(OPTION_VALUE_PRESET)
            add(encodeData.mrSectionWavPath)
        }
        return inputs.toArray(arrayOfNulls<String>(inputs.size))
    }

    override fun getRemoveAudioFromVideoCommand(encodeData: EncodeData): Array<String> {

        val inputs: ArrayList<String> = ArrayList()

        inputs.apply {

            add(OPTION_Y)
            add(OPTION_I)
            add(encodeData.orgConPath)
            add(OPTION_VCODEC)
            add(OPTION_VALUE_COPY)
            add(OPTION_AN)
            add(OPTION_THREAD_CNT)
            add(getNumCores())
            add(encodeData.reOrgConPath)
        }

        return inputs.toArray(arrayOfNulls<String>(inputs.size))
    }

    /**
     *  비디오 풀 영상
     */
    override fun getOriginVideoCommand(encodeData: EncodeData): Array<String> {
        val inputs: ArrayList<String> = ArrayList()

        inputs.apply {

            val fmtEndTimeSecond = millisecondsToStringFormat((encodeData.sectionEndMs - encodeData.sectionStartMs).toLong())

            add(OPTION_Y)

            if (encodeData.mediaType == MediaType.VIDEO.code) { //비디오 타입
                add(OPTION_SS)
                add(millisecondsToStringFormat((encodeData.syncMs + OPTION_VALUE_START_MS_DEFAULT).toLong()))
                add(OPTION_I)
                add(encodeData.videoMp4Path)
            }

            add(OPTION_I)
            add(encodeData.mixPath)

            //구간부르기 or 1분 편집 후 저장 end time
            if (encodeData.isSection || encodeData.isSaveAndFinish) {
                add(OPTION_T)
                add(fmtEndTimeSecond)
            }

            if (encodeData.mediaType == MediaType.VIDEO.code) { //비디오 타입
                add(OPTION_CV)
                add(OPTION_VALUE_LIB_VPX_VP9)
            }

            add(OPTION_CA)
            add(OPTION_VALUE_LIB_OPUS)

            if (encodeData.mediaType == MediaType.VIDEO.code) { //비디오 타입
                add(OPTION_BV)
                add(OPTION_VALUE_VIDEO_BITRATE_2000K)
            }

            add(OPTION_BA)
            add(OPTION_VALUE_AUDIO_BITRATE_128K)
            add(OPTION_SPEED)
            add(OPTION_VALUE_ENCODE_SPEED)
            add(OPTION_QUALITY)
            add(OPTION_VALUE_ENCODE_QUALITY_REALTIME)
            add(OPTION_THREAD_CNT)
            add(getNumCores())
            add(OPTION_PRESET)
            add(OPTION_VALUE_PRESET)

            if (encodeData.mediaType == MediaType.VIDEO.code) { //비디오 타입
                add(OPTION_R)
                add(OPTION_VALUE_VIDEO_FRAMES)
                add(OPTION_S)
                add(OPTION_VALUE_SIZE)
            }

            add(OPTION_AR)
            add(OPTION_VALUE_AUDIO_SAMPLE_RATES_48000)
            add(OPTION_STRICT)
            add(OPTION_VALUE_STRICT_VERY)
            add(encodeData.encodeOriginVideoWebmPath)
        }
        return inputs.toArray(arrayOfNulls<String>(inputs.size))
    }

    override fun getOriginVideoAlbumCommand(encodeData: EncodeData): Array<String> {
        val inputs: ArrayList<String> = ArrayList()

        inputs.apply {
            add(OPTION_Y)
            add(OPTION_I)
            add(encodeData.videoMp4Path)

            /*add(OPTION_VF)
            add(OPTION_VALUE_320_1)*/
            add(OPTION_CV)
            add(OPTION_VALUE_LIB_VPX_VP9)
            add(OPTION_CA)
            add(OPTION_VALUE_LIB_OPUS)
            add(OPTION_BV)
            add(OPTION_VALUE_VIDEO_BITRATE_2000K)
            add(OPTION_BA)
            add(OPTION_VALUE_AUDIO_BITRATE_128K)
            add(OPTION_SPEED)
            add(OPTION_VALUE_ENCODE_SPEED)
            add(OPTION_QUALITY)
            add(OPTION_VALUE_ENCODE_QUALITY_REALTIME)
            add(OPTION_R)
            add(OPTION_VALUE_VIDEO_FRAMES)
            add(OPTION_AR)
            add(OPTION_VALUE_AUDIO_SAMPLE_RATES_48000)
            add(OPTION_STRICT)
            add(OPTION_VALUE_STRICT_VERY)
            add(OPTION_THREAD_CNT)
            add(getNumCores())
            add(OPTION_PRESET)
            add(OPTION_VALUE_PRESET)
            add(encodeData.encodeOriginVideoWebmPath)
        }
        DLogger.d("getOriginVideoAlbumCommand : ${inputs}")
        return inputs.toArray(arrayOfNulls<String>(inputs.size))
    }

    /**
     * 참여 듀엣/배틀
     */
    override fun getJoinOriginVideoCommand(encodeData: EncodeData): Array<String> {
        val inputs: ArrayList<String> = ArrayList()

        inputs.apply {

//            if (encodeData.syncMs > 0) {
//                add(OPTION_SS)
//                add(millisecondsToStringFormat(encodeData.syncMs.toLong()))
//            }
            add(OPTION_Y)
            if (encodeData.partType == PartType.PART_A.code) {

                add(OPTION_SS)
                add(millisecondsToStringFormat((encodeData.syncMs + OPTION_VALUE_START_MS_DEFAULT).toLong()))

                add(OPTION_I)
                add(encodeData.videoMp4Path)
                add(OPTION_I)
                add(encodeData.reOrgConPath) //다운로드된 최초 컨텐츠 생성자 경로

            } else {

                add(OPTION_I)
                add(encodeData.reOrgConPath)

                add(OPTION_SS)
                add(millisecondsToStringFormat((encodeData.syncMs + OPTION_VALUE_START_MS_DEFAULT).toLong()))

                add(OPTION_I)
                add(encodeData.videoMp4Path)
            }

            add(OPTION_I)
            add(encodeData.mixPath)

            add(OPTION_CV)
            add(OPTION_VALUE_LIB_VPX_VP9)
            add(OPTION_CA)
            add(OPTION_VALUE_LIB_OPUS)
            add(OPTION_BV)
            add(OPTION_VALUE_VIDEO_BITRATE_2000K)
            add(OPTION_BA)
            add(OPTION_VALUE_AUDIO_BITRATE_128K)
            add(OPTION_SPEED)
            add(OPTION_VALUE_ENCODE_SPEED)
            add(OPTION_QUALITY)
            add(OPTION_VALUE_ENCODE_QUALITY_REALTIME)
            add(OPTION_R)
            add(OPTION_VALUE_VIDEO_FRAMES)
            add(OPTION_AC)
            add(OPTION_VALUE_AUDIO_CHANNEL)
            add(OPTION_AR)
            add(OPTION_VALUE_AUDIO_SAMPLE_RATES_48000)
            add(OPTION_STRICT)
            add(OPTION_VALUE_STRICT_VERY)

            add(OPTION_THREAD_CNT)
            add(getNumCores())
            add(OPTION_PRESET)
            add(OPTION_VALUE_PRESET)
            add(OPTION_FILTER_COMPLEX)
            add(OPTION_VALUE_HSTACK_WITH_SCALE)
            add(encodeData.encodeOriginVideoWebmPath)
        }
        return inputs.toArray(arrayOfNulls<String>(inputs.size))
    }

    /**
     *  프리뷰
     */
    override fun getHighlightCommand(encodeData: EncodeData): Array<String> {

        val inputs: ArrayList<String> = ArrayList()

        //구간이 아닌경우 previewStart ms로
        //컨텐츠 길이가 60초이거나, 이하일 경우 전달 전구간 인코딩
        //컨텐츠 길이가 60초이상일 경우 전달 된 구간 정보 기반으로 하이라이트 영상 인코딩
        val highlightTime = encodeData.getHighlightTime()

        inputs.apply {
            add(OPTION_Y)
            add(OPTION_SS)
            add(highlightTime.first)
            add(OPTION_I)
            add(encodeData.encodeOriginVideoWebmPath)

            if (encodeData.mediaType != MediaType.AUDIO.code) { //오디오가 아닌경우
                add(OPTION_CODEC)
                add(OPTION_VALUE_COPY)
            }

            add(OPTION_T)
            add(highlightTime.second)

            if (encodeData.mediaType != MediaType.AUDIO.code) { //오디오가 아닌경우
                add(OPTION_CV)
                add(OPTION_VALUE_LIB_VPX_VP9)
            }

            add(OPTION_CA)
            add(OPTION_VALUE_LIB_OPUS)

            if (encodeData.mediaType != MediaType.AUDIO.code) { //오디오가 아닌경우
                add(OPTION_BV)
                add(OPTION_VALUE_VIDEO_BITRATE_2000K)
            }

            add(OPTION_BA)
            add(OPTION_VALUE_AUDIO_BITRATE_128K)
            add(OPTION_SPEED)
            add(OPTION_VALUE_ENCODE_SPEED)
            add(OPTION_QUALITY)
            add(OPTION_VALUE_ENCODE_QUALITY_REALTIME)

            if (encodeData.mediaType != MediaType.AUDIO.code) { //오디오가 아닌경우
                add(OPTION_R)
                add(OPTION_VALUE_VIDEO_FRAMES)
            }

            add(OPTION_AR)
            add(OPTION_VALUE_AUDIO_SAMPLE_RATES_48000)
            add(OPTION_STRICT)
            add(OPTION_VALUE_STRICT_VERY)
            add(OPTION_THREAD_CNT)
            add(getNumCores())
            add(OPTION_PRESET)
            add(OPTION_VALUE_PRESET)

            add(encodeData.encodeHighlightPath)
        }

        return inputs.toArray(arrayOfNulls<String>(inputs.size))
    }

    /**
     * 피드 최초  오디오
     */
    override fun getOriginAudio(encodeData: EncodeData): Array<String> {
        val inputs: ArrayList<String> = ArrayList()
        inputs.apply {
            add(OPTION_Y)
            add(OPTION_I)
            add(encodeData.mixPath)
//            add(OPTION_ACODEC_LIBMP3LAME)
            add(encodeData.encodeOriginAudioWavPath)
        }
        return inputs.toArray(arrayOfNulls<String>(inputs.size))
    }

    override fun getThumbnailCommand(encodeData: EncodeData): Array<String> {
        val inputs: ArrayList<String> = ArrayList()
        inputs.apply {
            add(OPTION_Y)
            add(OPTION_SS)
            add(OPTION_VALUE_START_MS_ZERO)
            add(OPTION_I)
            add(encodeData.encodeHighlightPath)
            add(OPTION_VFRAMES)
            add(OPTION_VALUE_HIGHLIGHT_VFRAMES)
            add(OPTION_THREAD_CNT)
            add(getNumCores())
            add(OPTION_PRESET)
            add(OPTION_VALUE_PRESET)
            add(encodeData.encodeThumbPath)
        }
        return inputs.toArray(arrayOfNulls<String>(inputs.size))
    }

    /**
     * 비디오 전체 프레임 수 가져오기
     * (only FFPROBE. not working to FFMPEG.)
     */
    override fun getTotalFrameCount(encodeData: EncodeData): Array<String> {
        val inputs = ArrayList<String>()
        inputs.apply {
            add(OPTION_I)
            add(encodeData.videoMp4Path)
            add(PROB_OPTION_V)
            add(PROB_OPTION_VALUE_V_ERROR)
            add(PROB_OPTION_SELECT_STREAM)
            add(PROB_OPTION_VALUE_VIDEO_STREAM_ZERO)
            add(PROB_OPTION_SHOW_ENTRIES)
            add(PROB_OPTION_VALUE_STREAM + OPTION_DELIMITER_EQUAL + PROB_OPTION_VALUE_NB_FRAMES + OPTION_DELIMITER_COMMA + PROB_OPTION_VALUE_AVG_FRAME_RATE)
            add(PROB_OPTION_OF)
            add(PROB_OPTION_VALUE_DEFAULT_NOPRINT_WRAPPERS_1)
        }
        return inputs.toArray(arrayOfNulls<String>(inputs.size))
    }

    /**
     * 비디오 코덱 가져오기
     * (only FFPROBE. not working to FFMPEG.)
     */
    override fun getVideoCodec(encodeData: EncodeData): Array<String> {
        val inputs = ArrayList<String>()
        inputs.apply {
            add(OPTION_I)
            add(encodeData.videoMp4Path)
            add(PROB_OPTION_V)
            add(PROB_OPTION_VALUE_V_ERROR)
            add(PROB_OPTION_SELECT_STREAM)
            add(PROB_OPTION_VALUE_VIDEO_STREAM_ZERO)
            add(PROB_OPTION_SHOW_ENTRIES)
            add(PROB_OPTION_VALUE_STREAM + OPTION_DELIMITER_EQUAL + PROB_OPTION_VALUE_CODEC_NAME)
            add(PROB_OPTION_OF)
            add(PROB_OPTION_VALUE_DEFAULT_NOPRINT_WRAPPERS_1 + OPTION_DELIMITER_COLON + PROB_OPTION_VALUE_NOKEY + OPTION_VALUE_TRUE)
        }
        return inputs.toArray(arrayOfNulls<String>(inputs.size))
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
        return String.format("%02d:%02d:%02d.%03d", hours, minutes, sec, ms)
    }

    private fun getNumCores(): String {

        //Private Class to display only CPU devices in the directory listing
        class CpuFilter : FileFilter {
            override fun accept(pathname: File): Boolean {

                //Check if filename is "cpu", followed by a single digit number
                return Pattern.matches("cpu[0-9]", pathname.name)
            }
        }
        return try {

            //Get directory containing CPU info
            val dir = File("/sys/devices/system/cpu/")

            //Filter to only list the devices we care about
            val files = dir.listFiles(CpuFilter())

            //Return the number of cores (virtual CPU devices)
            DLogger.d("Core Count : ${files.size.toString()}")
            files.size.toString()
        } catch (e: Exception) {
            DLogger.d("Core Count : 0")
            "0"
        }
    }
}