package com.verse.app.repository.http

import okhttp3.MediaType
import okhttp3.RequestBody
import okio.Buffer
import okio.BufferedSink
import okio.ForwardingSink
import okio.Sink
import okio.buffer
import okio.sink
import java.io.IOException
import java.io.OutputStream


/**
 * Description : Upload Request Progress
 *
 * Created by juhongmin on 2023/07/06
 */
class UploadProgressRequestBody(
    private val requestBody: RequestBody,
    val onUpdate: (Int) -> Unit
) : RequestBody() {

    override fun contentType(): MediaType? {
        return requestBody.contentType()
    }

    override fun contentLength(): Long {
        return try {
            requestBody.contentLength()
        } catch (ex: Exception) {
            -1
        }
    }

    override fun writeTo(sink: BufferedSink) {
        val countingSink = CountingSink(sink)
        val bufferedSink = countingSink.buffer()
        requestBody.writeTo(bufferedSink)
        bufferedSink.flush()
//        val progressOutputStream =
//            ProgressOutputStream(sink.outputStream(), contentLength())
//        val progressSink = progressOutputStream.sink().buffer()
//        requestBody.writeTo(progressSink)
//        progressSink.flush()
    }

    inner class CountingSink(
        sink: Sink
    ) : ForwardingSink(sink) {

        private var totalWritten: Long = 0
        private var progress: Int = 0
        override fun write(source: Buffer, byteCount: Long) {
            super.write(source, byteCount)
            totalWritten += byteCount
            val tempProgress =
                ((totalWritten.toFloat() / contentLength().toFloat()) * 100.0).toInt()
            if (progress <= tempProgress) {
                progress = tempProgress
                onUpdate(progress)
            }
        }
    }

    inner class ProgressOutputStream(
        private val stream: OutputStream?,
        private val total: Long
    ) : OutputStream() {

        private var totalWritten: Long = 0
        private var progress: Int = 0

        @Throws(IOException::class)
        override fun write(b: ByteArray, off: Int, len: Int) {
            stream!!.write(b, off, len)
            if (total < 0) {
                onUpdate(100)
                return
            }
            totalWritten += if (len < b.size) {
                len.toLong()
            } else {
                b.size.toLong()
            }

            val tempProgress = ((totalWritten.toFloat() / total) * 100.0).toInt()
            // 진행도가 바뀐 경우에만 콜백 하도록 처리
            if (progress < tempProgress) {
                progress = tempProgress
                onUpdate(progress)
            }
        }

        @Throws(IOException::class)
        override fun write(b: Int) {
            stream!!.write(b)
            if (total < 0) {
                onUpdate(100)
                return
            }
            totalWritten++
            val tempProgress = ((totalWritten.toFloat() / total) * 100.0).toInt()
            // 진행도가 바뀐 경우에만 콜백 하도록 처리
            if (progress < tempProgress) {
                progress = tempProgress
                onUpdate(progress)
            }
        }

        @Throws(IOException::class)
        override fun close() {
            stream?.close()
        }

        @Throws(IOException::class)
        override fun flush() {
            stream?.flush()
        }
    }

}