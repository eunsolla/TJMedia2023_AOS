package com.verse.app.repository.http

import com.verse.app.utility.DLogger
import okhttp3.MediaType
import okhttp3.ResponseBody
import okio.*

/**
 * Description : File Download Progress Class
 *
 * Created by jhlee on 2023-04-13
 */
class ProgressResponseBody(val respBody: ResponseBody, val onUpdate: (Int) -> Unit) : ResponseBody() {

    private var bufferedSource = source(respBody.source()).buffer()

    override fun contentLength(): Long {
        return respBody.contentLength()
    }

    override fun contentType(): MediaType? {
        return respBody.contentType()
    }

    override fun source(): BufferedSource {
        return bufferedSource
    }

    private fun source(source: Source): Source {
        return object : ForwardingSource(source) {
            var totalBytesRead = 0L
            override fun read(sink: Buffer, byteCount: Long): Long {
                val bytesRead = super.read(sink, byteCount)

                totalBytesRead += if (bytesRead != -1L) bytesRead else 0

                val percent = if (bytesRead == -1L) 100f else totalBytesRead.toFloat() / respBody.contentLength().toFloat() * 100

                onUpdate(percent.toInt())

                return bytesRead
            }
        }
    }
}