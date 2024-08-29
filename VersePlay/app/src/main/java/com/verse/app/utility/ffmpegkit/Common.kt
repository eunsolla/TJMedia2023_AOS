package com.verse.app.utility.ffmpegkit

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaExtractor
import android.media.MediaFormat
import android.text.TextUtils
import com.verse.app.contants.MediaType
import com.verse.app.contants.SingType
import com.verse.app.contants.VideoUploadPageType
import com.verse.app.model.encode.EncodeData
import com.verse.app.utility.DLogger
import com.verse.app.utility.SongEncodeService
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.nio.file.Path
import java.text.SimpleDateFormat
import java.util.Date

object Common {


    const val OUT_PUT_DIR: String = "Output"

    //Output Files
    const val IMAGE: String = "IMAGE"
    const val VIDEO: String = "VIDEO"
    const val GIF: String = "GIF"
    const val MP3: String = "MP3"

    fun getFrameRate(fileString: String) {
        val extractor = MediaExtractor()
        val file = File(fileString)
        var fis: FileInputStream? = null
        try {
            fis = FileInputStream(file)
            val fd = fis.fd
            extractor.setDataSource(fd)
            val numTracks = extractor.trackCount
            for (i in 0 until numTracks) {
                val format = extractor.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME)
                if (mime?.startsWith("video/") == true) {
                    if (format.containsKey(MediaFormat.KEY_FRAME_RATE)) {
                        FFmpegQueryExtension().FRAME_RATE = format.getInteger(MediaFormat.KEY_FRAME_RATE)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            extractor.release()
            try {
                fis?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getFilePath(context: Context, fileExtension: String): String {
        val dir = File(context.getExternalFilesDir(Common.OUT_PUT_DIR).toString())
        if (!dir.exists()) {
            dir.mkdirs()
        }
        var extension: String? = null
        when {
            TextUtils.equals(fileExtension, IMAGE) -> {
                extension = ".jpg"
            }

            TextUtils.equals(fileExtension, VIDEO) -> {
                extension = ".mp4"
            }

            TextUtils.equals(fileExtension, GIF) -> {
                extension = ".gif"
            }

            TextUtils.equals(fileExtension, MP3) -> {
                extension = ".mp3"
            }
        }
        val dest = File(dir.path + File.separator + Common.OUT_PUT_DIR + System.currentTimeMillis().div(1000L) + extension)
        return dest.absolutePath
    }

    @Throws(IOException::class)
    fun getFileFromAssets(context: Context, fileName: String): File =
        File(context.cacheDir, fileName).also {
            if (!it.exists()) {
                it.outputStream().use { cache ->
                    context.assets.open(fileName).use { inputStream ->
                        inputStream.copyTo(cache)
                    }
                }
            }
        }


    /**
     * 파일 유무 체크
     * @return true/false
     */
    fun isFileExists(path: String): Boolean {

        val filePath = File(path)


        if (filePath.exists()) {
            DLogger.d("isFileExists 파일 있음 ${filePath.path}")
            return true
        } else {
            return false
            DLogger.d("isFileExists 파일 없음 ${filePath.path}")
        }
    }

    /**
     * 파일 유무 체크
     * @return file path
     */
    fun isFilePath(path: String): String {

        val filePath = File(path)

        if (filePath.exists()) {
            DLogger.d("isFileExists 파일 있음 ${filePath.path}")
            return filePath.path
        } else {
            return ""
            DLogger.d("isFileExists 파일 없음 ${filePath.path}")
        }
    }

    /**
     * 파일 유무 체크
     */
    fun isFileExists(dirPath: String, name: String): String {

        val filePath = File("$dirPath/$name")

        if (filePath.exists()) {
            DLogger.d("isFileExists 파일 있음 ${filePath.path}")
            return filePath.path
        } else {
            return ""
            DLogger.d("isFileExists 파일 없음 ${filePath.path}")
        }
    }

    /**
     * 파일 유무 체크
     * @param directoryName
     * @param fileName
     * @return true: 있음 false : 없음
     */
    fun checkFile(directoryName: String, fileName: String): Boolean {
        var isFile = false
        val tempFile = File(directoryName)
        if (tempFile.exists() && tempFile.isDirectory) {
            val childFileList = tempFile.listFiles()
            for (childFile in childFileList) {
                if (childFile.exists()) {
                    if (childFile.name.contains(fileName)) {
                        isFile = true
                        break
                    }
                }
            }
        }
        return isFile
    }


    /**
     * 파일 삭제
     * @param directoryName
     * @param fileName
     */
    fun removeFiles(directoryName: String, fileName: String) {

        if (directoryName.isNullOrEmpty()) {
            DLogger.d("directoryName is null")
            return
        } else {
            DLogger.d("directoryName : ${directoryName} / fileName: ${fileName} ")
        }
        // True : fileName이 포함된 파일 삭제
        // False : 디렉토리 안의 파일들 모두 삭제
        val isContain = fileName.isNullOrEmpty()

        val tempFile = File(directoryName)

        if (tempFile.exists() && tempFile.isDirectory) {
            val childFileList = tempFile.listFiles()
            for (childFile in childFileList) {
                if (childFile.exists()) {
                    if (isContain) {
                        if (childFile.name.contains(fileName)) {
                            childFile.delete()
                        }
                    } else {
                        childFile.delete()
                    }
                }
            }
        }
    }

    /**
     * 디렉토리 삭제
     * 디렉토리 하위 삭제
     */
    fun removeFiles(directoryName: String): Boolean {
        DLogger.d("removeFiles deleteRecursively=> ${directoryName}")
        val tempFile = File(directoryName)
        try {
            return tempFile.deleteRecursively()
        } catch (e: Exception) {
            DLogger.d("error removeFiles => ${e}")
            return false
        }
        return false
    }

    /**
     * 디렉토리 삭제
     * 디렉토리 하위 체크 후 삭제
     */
    fun removeCheckFiles(directoryName: String) {
        DLogger.d("removeFiles deleteRecursively=> ${directoryName}")
        val tempFile = File(directoryName)
        try {
            if (tempFile.exists() && tempFile.isDirectory) {
                tempFile.listFiles()?.let {
                    it.forEach { file ->
                        if (file.exists()) {
                            DLogger.d("file exists=> ${file.path} / ${file.listFiles().size}")
                            if (file.listFiles().isEmpty() || file.listFiles().size <= 1) {
                                DLogger.d("empty child")
                                tempFile.deleteRecursively()
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            DLogger.d("error removeFiles => ${e}")
        }
    }

    /**
     * 현재 날짜  함수
     * ex.) 2023_01_01_01_11_11
     */
    @SuppressLint("SimpleDateFormat")
    fun getDtm(): String {
        // 현재 시간
        val longNow = System.currentTimeMillis()
        // 현재 시간 ->  Date  변환
        val date = Date(longNow)
        // 포멧 설정
        val dateFormat = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss")

        return dateFormat.format(date)
    }

    /**
     * 현재 경과 시간
     */
    fun getTime(ms: Int): Pair<Int, Int> {
        val seconds = ms / 1000
        val rem = seconds % 3600
        val mn = rem / 60
        val sec = rem % 60
        return mn to sec
    }

    /**
     * 현재 경과 시간 - String
     */
    fun getTimeString(ms: Int): String? {
        val seconds = ms / 1000
        val rem = seconds % 3600
        val mn = rem / 60
        val sec = rem % 60
        return String.format("%02d", mn) + ":" + String.format("%02d", sec)
    }

    /**
     * 솔로인경우 Mix,MP4 파일 여부 체크
     * 듀엣배츨 참여인경우  Mix,MP4,orgPath(생성자컨텐츠) 파일 여부 체크
     */
    fun checkSingFiles(encodeData: EncodeData): Boolean {

        when (encodeData.singType) {
            SingType.SOLO.code -> {
                when (encodeData.mediaType) {

                    MediaType.AUDIO.code -> {
                        DLogger.d(SongEncodeService.TAG, "CheckFile SOLO 타입 AUDIO")
                        return isFileExists(encodeData.mixPath)
                    }

                    MediaType.VIDEO.code -> {
                        DLogger.d(SongEncodeService.TAG, "CheckFile SOLO 타입 VIDEO")
                        val isMix = isFileExists(encodeData.mixPath)
                        val isMp4 = isFileExists(encodeData.videoMp4Path)
                        return isMix && isMp4
                    }
                }
            }

            SingType.BATTLE.code,
            SingType.DUET.code -> {
                when (encodeData.mediaType) {
                    MediaType.AUDIO.code -> {
                        DLogger.d(SongEncodeService.TAG, "CheckFile DUET 타입 AUDIO")
                        val isMix = isFileExists(encodeData.mixPath)
                        val isMp4 = isFileExists(encodeData.videoMp4Path)
                        return isMix && isMp4
                    }

                    MediaType.VIDEO.code -> {
                        DLogger.d(SongEncodeService.TAG, "CheckFile DUET 타입 VIDEO")
                        //최초 생성인 경우 오디오(mix) / 영상(바누바) 체크
                        if (encodeData.isInitContents) {
                            val isMix = isFileExists(encodeData.mixPath)
                            val isMp4 = isFileExists(encodeData.videoMp4Path)
                            return isMix && isMp4
                        } else {

                            val isMix = isFileExists(encodeData.mixPath)
                            val isMp4 = isFileExists(encodeData.videoMp4Path)
                            val isOrgCon = isFileExists(encodeData.orgConPath)
                            return isMix && isMp4 && isOrgCon
                        }
                    }
                }
            }
        }
        return false
    }

    /**
     * Sing dtm dir 삭제
     */
     fun removeSingDir(encodeData: EncodeData) {

        val dir = if (encodeData.uploadType == VideoUploadPageType.SING_CONTENTS) {
            encodeData.singDirPath
        } else {
            encodeData.albumDirPath
        }
        //n_dtm 폴더 및 하위 삭제
        removeFiles(dir)
    }

}