package com.verse.app.ui.mypage.crop

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream

object CompressFile {
    const val IMGTYPE_JPG = 0
    const val IMGTYPE_PNG = 1
    const val LIMITED_IMG_SIZE = 20000000
    fun compressFile(
        selectedBitmap: Bitmap,
        newHeight: Int,
        newWidth: Int,
        imgType: Int,
        resultFile: File?
    ): File? {
        return try {
            // 파일 압축
            val width = selectedBitmap.width
            val height = selectedBitmap.height
            val scaleWidth = newWidth.toFloat() / width
            val scaleHeight = newHeight.toFloat() / height
            val scale = Math.min(scaleHeight, scaleWidth)


            // Create a matrix for the manipulation
            val matrix = Matrix()

            // Resize the bit map
            matrix.postScale(scale, scale)

            // Recreate the new Bitmap
            val resizedBitmap =
                Bitmap.createBitmap(selectedBitmap, 0, 0, width, height, matrix, false)
            val outputStream = FileOutputStream(resultFile)
            if (imgType == IMGTYPE_JPG) {
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            } else {
                resizedBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            }

//            // resize를 했는데도 크기가 크면.
//            if(resultFile.length() > LIMITED_IMG_SIZE){
//                resultFile =  compressFileByInsample(resultFile, getInResample(imgType, resultFile.length()), imgType);
//            }else{
//
//            }
            resultFile
        } catch (e: Exception) {
            null
        }
    }

    fun getFileFromBitmap(file: File, bitmap: Bitmap, imgType: Int): File {
        var outputStream: FileOutputStream? = null
        try {
            outputStream = FileOutputStream(file)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        if (imgType == IMGTYPE_JPG) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        } else {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        }
        return file
    }

    fun compressFileByInsample(file: File?, reqInsample: Int, imgType: Int): File? {
        return try {
            val o = BitmapFactory.Options()
            o.inSampleSize = reqInsample
            val inputStream = FileInputStream(file)
            val selectedBitmap = BitmapFactory.decodeStream(inputStream, null, o)
            inputStream.close()
            val outputStream = FileOutputStream(file)
            if (imgType == IMGTYPE_JPG) {
                selectedBitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            } else {
                selectedBitmap!!.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            }
            file
        } catch (e: Exception) {
            null
        }
    }

    fun getInResample(imgType: Int, ImgSize: Long): Int {
        var reqInsample = 0
        reqInsample = if (imgType == IMGTYPE_JPG) {
            if (ImgSize / 5 < LIMITED_IMG_SIZE) {
                4
            } else if (ImgSize / 8 < LIMITED_IMG_SIZE) {
                6
            } else if (ImgSize / 15 < LIMITED_IMG_SIZE) {
                8
            } else {
                10
            }
        } else {
            if (ImgSize / 4 < LIMITED_IMG_SIZE) {
                2
            } else if (ImgSize / 10 < LIMITED_IMG_SIZE) {
                4
            } else if (ImgSize / 30 < LIMITED_IMG_SIZE) {
                6
            } else {
                8
            }
        }
        return reqInsample
    }

    fun rotateBitmap(orientation: Int, bitmap: Bitmap): Bitmap {
        var bitmap = bitmap
        val matrix = Matrix()
        if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
            matrix.postRotate(90f)
        } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
            matrix.postRotate(180f)
        } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
            matrix.postRotate(270f)
        }
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        return bitmap
    }
}