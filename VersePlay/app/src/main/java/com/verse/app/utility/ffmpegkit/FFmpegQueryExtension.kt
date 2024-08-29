package com.verse.app.utility.ffmpegkit

class FFmpegQueryExtension {
    public var FRAME_RATE: Int = 25 // Default value

    val OPTION_VALUE_START_MS_ZERO =
        "00:00:00.000" // FFMPEG Start Second              (option name => -ss)
    val OPTION_VALUE_HIGHLIGHT_VFRAMES =
        "1" // FFMPEG Video Frame               (option name => -vframes)

//    const val OPTION_VALUE_START_MS_ZERO =
//        "00:00:00.000" // FFMPEG Start Second              (option name => -ss)
//    const val OPTION_VALUE_HIGHLIGHT_VFRAMES =
//        "1" // FFMPEG Video Frame               (option name => -vframes)

    fun addThumbnailOnVideo(inputVideo: String, output: String):
            Array<String> {
        val inputs: ArrayList<String> = ArrayList()

        inputs.apply {
            add("-ss")
            add(
                OPTION_VALUE_START_MS_ZERO
            )
            add("-i")
            add(inputVideo)
            add("-vframes")
            add(
                OPTION_VALUE_HIGHLIGHT_VFRAMES
            )
            add(output)
        }
        return inputs.toArray(arrayOfNulls<String>(inputs.size))
    }

//    fun dd(inputVideoPath: String, startTime: String?, endTime: String?, vframes: String? ) {
//        // 썸네일 파일 인코딩 [Start]
//
//        // 썸네일 파일 인코딩 [Start]
////        val thumbnailCommand = arrayOf<String>(
////            "-ss", OPTION_VALUE_START_MS_ZERO,
////            "-i", mHighlightFilePath,
////            "-vframes", "1",
////            mThumbnailFilePath
////        )
//
//        val inputs: ArrayList<String> = ArrayList()
//        inputs.apply {
//            add("-i")
//            add(inputVideoPath)
//            add("-ss")
//            add(startTime.toString())
//            add("-vframes")
//            add(vframes.toString())
//            add(output)
//        }
//        return inputs.toArray(arrayOfNulls<String>(inputs.size))
//    }

//    mThumbnailFilePath = mEncodedDirectoryPath + mRandomStr + ENCODED_SONG_FILE_THUMBNAIL // 인코딩한 썸네일 파일

    fun getThumbnailFile(inputVideo: String, output: String): Array<String> {
        val inputs: ArrayList<String> = ArrayList()
// 썸네일 파일 인코딩 [Start]

        // 썸네일 파일 인코딩 [Start]
//        val thumbnailCommand = arrayOf<String>(
//            "-ss", OPTION_VALUE_START_MS_ZERO,
//            "-i", mHighlightFilePath,
//            "-vframes", OPTION_VALUE_HIGHLIGHT_VFRAMES,
//            mThumbnailFilePath
//        )
        inputs.apply {
            add("-ss")
            add(
                "00:00:00.000" // FFMPEG Start Second              (option name => -ss)
            )
            add("-i")
            add(inputVideo)
            add("-vframes")
            add(
                "1" // FFMPEG Video Frame               (option name => -vframes)
            )
            add(output)
        }
        return inputs.toArray(arrayOfNulls<String>(inputs.size))
    }

    /**
     * startTime = 00:00:00 HH:MM:SS
     * endTime = 00:00:00 HH:MM:SS
     */
    fun cutVideo(inputVideoPath: String, startTime: String?, endTime: String?, output: String): Array<String> { Common.getFrameRate(inputVideoPath)
        val inputs: ArrayList<String> = ArrayList()
        inputs.apply {
            add("-i")
            add(inputVideoPath)
            add("-ss")
            add(startTime.toString())
            add("-to")
            add(endTime.toString())
            add("-r")
            add("$FRAME_RATE")
            add("-preset")
            add("ultrafast")
            add(output)
        }
        return inputs.toArray(arrayOfNulls<String>(inputs.size))
    }
}