package com.verse.app.utility.provider

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.verse.app.R
import com.verse.app.contants.VideoUploadPageType
import com.verse.app.extension.getResourceUri
import com.verse.app.extension.onIO
import com.verse.app.extension.onMain
import com.verse.app.model.encode.EncodeData
import com.verse.app.repository.preferences.AccountPref
import com.verse.app.utility.DLogger
import com.verse.app.utility.ffmpegkit.Common
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import javax.inject.Inject


/**
 * Description : 부르기 파일 경로 Provider Class
 *
 * Created by jhlee on 2023-04-11
 */
interface SingPathProvider {
    fun getJsonFileName(): String       //json 파일명
    fun getMrFileNameMP3(): String   //mr 파일명
    fun getMrFileNameWAV(): String   //mr wav 파일명
    fun getOrgVideoFileName(): String       //원곡자 비디오 파일명
    fun getOrgAudioFileName(): String   //원곡자 오디오 파일명
    fun initSingingPath(songId: String)                    //부르기 경로 Set
    fun initAlbumVideoPath(name: String): Boolean                    //앨범 경로 Set
    fun getDirSongInfoPath(): String                     // root dir (mr,json)
    fun getDirSingInfoPath(): String                     // root dir (mr,json)
    fun getTempVoicePath(): String                      //sp temp
    fun getTempRecordVoicePath(): String             //sp 녹음 경로
    fun getResultVoicePath(): String                  //sp 녹음 완료
    fun getResultVideoPath(): String                   //바누바 녹화 완료
    fun getResultVideoAlbumPath(): String       //복사된 앨범 영상 경로
    fun clearPaths()                                           //초기화
    fun getSongId(): String                             //song id
    fun getMrMp3Path(): String                        //mp3 full path
    fun getMrWavPath(): String                        //wav full path
    fun getMrSectionWavPath(): String                        //wav full path
    fun getMixFilePath(): String                        //mix full path
    fun getReOrgConPath(): String                   // 사운드 제거된 원본 비디오 경로
    fun getEncodeThumbPath(): String                //인코딩 결과 썸네일 경로
    fun getEncodeHighlightPath(): String            //인코딩 결과 프리뷰 경로
    fun getEncodeOriginVideoPath(): String          //인코딩 결과 webm 경로
    fun getEncodeOriginAudioPath(): String          //인코딩 오디오 경로
    fun createEncodeDir()                                   //인코딩 dir 생성
    fun getPrefixSingInfo(): String                     //sing/sing_files/2023_01_01_01_11_11 부르기 root 경로
    fun getPrefixAlbumInfo(): String                //album_files/2023_01_01_01_11_11_fileName 경로
    fun getDirEncodePath(): String                  //sing/sing_files/2023_01_01_01_11_11/encoded 인코딩 경로
    fun getPageType(): VideoUploadPageType      //앨범 or 부르기 타입 구분
    fun deleteUploadDir(isClear: Boolean)               //반주,앨범 Dir 제거 isClear-> true: 저장 데이터 무시하고 모두 제거 , false : 저장 데이터 실제 파일 체크 후 제거
    fun deleteUploadDir(dirPath: String)               //해당  Dir 제거
    fun deleteSongDir()               //반주음  Dir 제거
    fun getAllDir(): Triple<String, String, String>
    fun getWaterMarkPath(): String
}

class SingPathProviderImpl @Inject constructor(
    @ApplicationContext
    val context: Context,
    val accountPref: AccountPref
) : SingPathProvider {

    companion object {
        const val DIR_ROOT_DIR = "sing"
        const val DIR_SONG_DIR = "song_files"
        const val DIR_SING_DIR = "sing_files"
        const val DIR_AlBUM_DIR = "album_files"
        const val DIR_WATER_MARK_DIR = "watermark_files"
        const val DIR_ENCODED = "encoded"

        const val TEMP_JSON_FILE_NAME = "temp_json.json"
        const val TEMP_MR_FILE_NAME_MP3 = "temp_mr.mp3"             //openMRFile
        const val TEMP_MR_FILE_NAME_WAV = "temp_mr.wav"             // MR map - >MR wav 파일 변환시 사용
        const val TEMP_MR_SECTION_FILE_NAME_WAV = "temp_section_mr.wav"      // MR wav -> MR Section wav 파일 변환시 사용
        const val TEMP_WAV_FILE_NAME = "temp"                               //setRecord
        const val TEMP_MIX_FILE_NAME = "temp_mix.wav"                   // mr + voice
        const val TEMP_ORG_VIDEO_FILE_NAME = "origin_video.webm"    //생성자 비디오
        const val TEMP_NEW_ORG_VIDEO_FILE_NAME = "re_origin_video.webm"    //생성자 비디오 (사운드 제거)
        const val TEMP_ORG_AUDIO_FILE_NAME = "origin_audio.wav"             //생성자 오디오
        const val TEMP_WATER_MARK_FILE_NAME = "water_mark.png"             //워터마크 이미지


        const val TEMP_RESULT_VOICE_FILE_NAME = "temp_voice"                   //startRecord
        const val TEMP_RESULT_VIDEO_FILE_NAME = "temp_video.mp4"           //video Record
        const val TEMP_MR_FILE_NAME_MP4 = "temp_join_video.mp4"             //duet or battle video

        const val TEMP_VOICE_EXT = ".wav"
        const val TEMP_EXT_MP3 = ".mp3"
        const val UNDER_BAR = "_"
        const val SLASH = "/"

        const val ENCODED_SONG_FILE_HIGHLIGHT = "highlight.webm"
        const val ENCODED_SONG_FILE_THUMBNAIL = "thumb.png"
        const val ENCODED_SONG_FILE_ORIGIN_VIDEO = "origin_video.webm"        //fullview.webm
        const val ENCODED_SONG_FILE_ORIGIN_AUDIO = "origin_audio.wav"        //mr+voice mix wav -> wav (combineAudioFilePath)
    }

    private val songInfoDirPath: String by lazy { context.filesDir.absolutePath + SLASH + DIR_ROOT_DIR + SLASH + DIR_SONG_DIR } //mr,xtf dir 경로
    private val singDirPath: String by lazy { context.filesDir.absolutePath + SLASH + DIR_ROOT_DIR + SLASH + DIR_SING_DIR } //녹음/녹화 dir 경로
    private val albumDirPath: String by lazy { context.filesDir.absolutePath + SLASH + DIR_AlBUM_DIR } //앨범 영상 dir 경로
    private val waterMarkDirPath: String by lazy { context.filesDir.absolutePath + SLASH + DIR_WATER_MARK_DIR } //워터마크  dir 경로
    lateinit var tmpVoicePath: String
    lateinit var resultVoiceWavPath: String
    lateinit var resultVideoMp4Path: String
    lateinit var resultVideoFromAlbumPath: String
    private val waterMarkImagePath: String by lazy { waterMarkDirPath + SLASH + TEMP_WATER_MARK_FILE_NAME } //워터마크  dir 경로
    lateinit var encodePath: String
    lateinit var curDtm: String
    lateinit var curSongId: String
    lateinit var curPageType: VideoUploadPageType

    /**
     * 부르기 오디오/영상
     */
    override fun initSingingPath(sId: String) {

        curPageType = VideoUploadPageType.SING_CONTENTS
        //song id
        curSongId = sId
        //현재 날짜/시간 2023_01_01_01_11_11
        curDtm = Common.getDtm()
        //singing/2023_05_01_10_56_17/temp.wav
        tmpVoicePath = getPrefixSingInfo() + SLASH + TEMP_WAV_FILE_NAME + TEMP_VOICE_EXT
        //singing/2023_05_01_10_56_17/temp_voice (녹음 완료 후 확장자 붙음)
        resultVoiceWavPath = getPrefixSingInfo() + SLASH + TEMP_RESULT_VOICE_FILE_NAME
        //singing/2023_05_01_10_56_17/temp_video.mp4
        resultVideoMp4Path = getPrefixSingInfo() + SLASH + TEMP_RESULT_VIDEO_FILE_NAME
        //인코딩 경로 /singing/2023_05_01_10_56_17/encoded
        encodePath = getPrefixSingInfo() + SLASH + DIR_ENCODED

        //sp dir 생성
        createVoiceDir()

        DLogger.d("[S] ==============================================Singing PATH==============================================")
        DLogger.d("Singing curDtm=> ${curDtm}")
        DLogger.d("Singing tmpVoicePath=> ${tmpVoicePath}")
        DLogger.d("Singing resultVoiceWavPath=> ${resultVoiceWavPath}")
        DLogger.d("Singing resultVideoPath=> ${resultVideoMp4Path}")
        DLogger.d("[E] ==============================================Singing PATH==============================================")
    }

    /**
     * 앨범 영상
     */
    override fun initAlbumVideoPath(path: String): Boolean {

        clearPaths()

        curPageType = VideoUploadPageType.ALBUM

        //현재 시간 2023_01_01_01_11_11
        curDtm = Common.getDtm()

        //파일 복사
        val orgFile = copyVideo(path)

        orgFile?.let {
            resultVideoFromAlbumPath = orgFile.path
            //album_files/2023_05_01_10_56_17_{filename}/encoded
            encodePath = getPrefixAlbumInfo() + SLASH + DIR_ENCODED
            createEncodeDir()
            return true
        }

        return false
    }

    private fun copyVideo(orgPath: String): File? {
        return copyFile(orgPath)
    }

    override fun getTempVoicePath(): String {
        tmpVoicePath?.let {
            DLogger.d("Singing getTmpVoicePath=> ${it}")
            return it
        }
        return ""
    }

    override fun getTempRecordVoicePath(): String {
        resultVoiceWavPath?.let {
            DLogger.d("Singing resultVoiceWavPath=> ${it}")
            return it
        }
        return ""
    }

    override fun getResultVoicePath(): String {
        resultVoiceWavPath?.let {
            DLogger.d("Singing resultVoiceWavPath=> ${it}")
            return it + TEMP_VOICE_EXT
        }
        return ""
    }


    override fun getResultVideoPath(): String {
        resultVideoMp4Path?.let {
            DLogger.d("Singing resultVideoPath=> ${it}")
            return it
        }
        return ""
    }

    override fun getResultVideoAlbumPath(): String {
        resultVideoFromAlbumPath?.let {
            DLogger.d("Singing resultVideoFromAlbumPath=> ${it}")
            return it
        }
        return ""
    }

    override fun getJsonFileName(): String {
        return TEMP_JSON_FILE_NAME
    }

    override fun getMrFileNameMP3(): String {
        return TEMP_MR_FILE_NAME_MP3
    }

    override fun getMrFileNameWAV(): String {
        return TEMP_MR_FILE_NAME_WAV
    }

    override fun getOrgVideoFileName(): String {
        return TEMP_ORG_VIDEO_FILE_NAME
    }

    override fun getOrgAudioFileName(): String {
        return TEMP_ORG_AUDIO_FILE_NAME
    }

    override fun getDirSongInfoPath(): String {
        return songInfoDirPath + SLASH
    }

    override fun getDirSingInfoPath(): String {
        return singDirPath + SLASH
    }

    override fun getSongId(): String {
        return curSongId
    }

    override fun getMrMp3Path(): String {
        DLogger.d("[mr wav path]=>${songInfoDirPath + SLASH + curSongId + SLASH + TEMP_MR_FILE_NAME_MP3}")
        return getPrefixSongInfo() + TEMP_MR_FILE_NAME_MP3
    }

    override fun getMrWavPath(): String {
        DLogger.d("[mr wav path]=>${getPrefixSongInfo() + TEMP_MR_FILE_NAME_WAV}")
        return getPrefixSongInfo() + TEMP_MR_FILE_NAME_WAV
    }

    override fun getMrSectionWavPath(): String {
        DLogger.d("[mr section wav path]=>${getPrefixSingInfo() + SLASH + TEMP_MR_SECTION_FILE_NAME_WAV}")
        return getPrefixSingInfo() + SLASH + TEMP_MR_SECTION_FILE_NAME_WAV
    }

    override fun getMixFilePath(): String {
        return getPrefixSingInfo() + SLASH + TEMP_MIX_FILE_NAME
    }

    override fun getEncodeThumbPath(): String {
        return getDirEncodePath() + SLASH + ENCODED_SONG_FILE_THUMBNAIL
    }

    override fun getEncodeHighlightPath(): String {
        return getDirEncodePath() + SLASH + ENCODED_SONG_FILE_HIGHLIGHT
    }

    override fun getEncodeOriginVideoPath(): String {
        return getDirEncodePath() + SLASH + ENCODED_SONG_FILE_ORIGIN_VIDEO
    }

    override fun getEncodeOriginAudioPath(): String {
        return getDirEncodePath() + SLASH + ENCODED_SONG_FILE_ORIGIN_AUDIO
    }

    override fun getDirEncodePath(): String {
        return encodePath
    }

    /**
     * 인코딩 dir 생성
     */
    override fun createEncodeDir() {
        val encodeDir = File(getDirEncodePath())
        DLogger.d("createEncodeDir 11 ${encodeDir}")
        if (!encodeDir.exists()) {
            encodeDir.mkdirs()
        }

        createWaterMark()
    }

    override fun getPageType(): VideoUploadPageType {
        return curPageType
    }

    override fun deleteUploadDir(isClear: Boolean) {
        //반주음 Dir
        Common.removeFiles(songInfoDirPath)
        //앨범 Dir
        Common.removeFiles(albumDirPath)
        //sing Dir
        Common.removeFiles(singDirPath)

        /* 2023-07-25  부르기 타입별 파일 체크 후 제거 로직 주석.
            if(!isClear){
                val encodeDataList = getPrefEncodeDataList()
                DLogger.d("deleteUploadDir encodeDataList size => ${encodeDataList.size}")
                encodeDataList.forEach {
                    if (!Common.checkSingFiles(it)) {
                        DLogger.d("deleteUploadDir 녹음된 파일 일부가 없음. 삭제합니다 => ${it.songName}")
                        Common.removeSingDir(it)
                    }
                }
            }else{
                Common.removeFiles(singDirPath)
                clearPrefEncodeDataList()
            }*/
    }

    override fun deleteSongDir() {
        Common.removeFiles(songInfoDirPath)
    }

    override fun deleteUploadDir(path: String) {
        Common.removeFiles(path)
    }


    /**
     * 앨범 Dir 경로
     */
    override fun getPrefixAlbumInfo(): String {
        return albumDirPath + SLASH + curDtm
    }

    override fun getReOrgConPath(): String {
        return getPrefixSongInfo() + TEMP_NEW_ORG_VIDEO_FILE_NAME
    }

    override fun clearPaths() {
        curPageType = VideoUploadPageType.NONE
        curSongId = ""
        curDtm = ""
        tmpVoicePath = ""
        resultVoiceWavPath = ""
        resultVideoMp4Path = ""
        encodePath = ""
    }

    override fun getAllDir(): Triple<String, String, String> {
        return Triple(songInfoDirPath, singDirPath, albumDirPath)
    }

    override fun getWaterMarkPath(): String {
        waterMarkImagePath?.let {
            DLogger.d("Singing waterMarkImagePath=> ${it}")
            return it
        }
        return ""
    }

    /**
     * 반주,wav.
     */
    fun getPrefixSongInfo(): String {
        return songInfoDirPath + SLASH + curSongId + SLASH
    }

    /**
     * 부르기 녹음/녹화 dir 경로
     */
    override fun getPrefixSingInfo(): String {
        return singDirPath + SLASH + curDtm
    }

    private fun copyFile(oriFilePath: String): File? {
        return createFileFromContentUri(oriFilePath.toUri())
    }

    private fun createFileFromContentUri(fileUri: Uri): File? {
        fileUri?.let { uri ->
            context.contentResolver.getType(uri)?.let {
                val suffix = "." + it.substring(it.lastIndexOf("/") + 1, it.length)
                val iStream: InputStream = context.contentResolver.openInputStream(uri)!!
                val outputFile = createTempFile(getPrefixAlbumInfo(), suffix)
                copyStreamToFile(iStream, outputFile)
                iStream.close()
                return outputFile
            }
        } ?: run {
            return null
        }
    }

    private fun createTempFile(dirPath: String, suffix: String): File {
        val encodeDir = File(dirPath)
        DLogger.d("createEncodeDir-> ${encodeDir}")
        if (!encodeDir.exists()) {
            encodeDir.mkdirs()
        }
        return createFile("${SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())}_", suffix, encodeDir)
    }

    private fun createFile(name: String, suffix: String, dir: File): File {
        DLogger.d("createFile->$name , $suffix , ${dir.path}")
        return try {
            File.createTempFile(
                name,
                suffix,
                dir
            )
        } catch (ex: IOException) {
            throw ex
        }
    }

    private fun copyStreamToFile(inputStream: InputStream, outputFile: File) {
        inputStream.use { input ->
            val outputStream = FileOutputStream(outputFile)
            outputStream.use { output ->
                val buffer = ByteArray(4 * 1024) // buffer size
                while (true) {
                    val byteCount = input.read(buffer)
                    if (byteCount < 0) break
                    output.write(buffer, 0, byteCount)
                }
                output.flush()
            }
        }
    }

    /**
     * 부르기 인코딩 경로
     */
    fun getPrefixEncodedInfo(): String {
        return getPrefixSingInfo() + SLASH + DIR_ENCODED + SLASH
    }

    /**
     * sp dir 생성
     */
    private fun createVoiceDir() {
        val voiceDir = File(getPrefixSingInfo())
        if (!voiceDir.exists()) {
            voiceDir.mkdirs()
        }
    }

    /**
     *  내부에 저장된 encode 데이터 get
     */
    private fun getPrefEncodeDataList(): MutableList<EncodeData> {
        return accountPref.getEncodedData()
    }

    /**
     * 내부 저장된 encode 데이터 모두 지움
     */
    private fun clearPrefEncodeDataList() {
        accountPref.clearEncodedData()
    }


    /**
     * 워터마크 리소스 내부 파일로 저장
     */
    private fun createWaterMark() {

        if (Common.isFileExists(waterMarkDirPath)) {
            return
        }

        onIO {
            val targetPath = R.drawable.auto_tune_active.getResourceUri(context)
            val bm = Glide.with(context).asBitmap().load(targetPath).submit().get()

            val waterMarkDir = File(waterMarkDirPath)
            DLogger.d("createWaterMark waterMarkDir  ${waterMarkDir}")
            if (!waterMarkDir.exists()) {
                waterMarkDir.mkdirs()
            }

            val file = File(waterMarkImagePath)

            if (!file.exists()) {
                try {
                    val outStream = FileOutputStream(file)
                    bm.compress(Bitmap.CompressFormat.PNG, 100, outStream)
                    outStream.flush()
                    outStream.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}
