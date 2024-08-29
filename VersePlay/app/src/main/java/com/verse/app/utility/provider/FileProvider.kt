package com.verse.app.utility.provider

import android.content.Context
import android.graphics.Bitmap
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target
import com.verse.app.contants.Config
import com.verse.app.repository.http.ApiService
import com.verse.app.repository.http.ProgressResponseBody
import com.verse.app.repository.http.UploadProgressRequestBody
import com.verse.app.tracking.interceptor.TrackingHttpInterceptor
import com.verse.app.utility.DLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import okhttp3.Call
import okhttp3.Callback
import okhttp3.ConnectionPool
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLConnection
import java.util.concurrent.TimeUnit
import javax.inject.Inject


/**
 * Description : FileDownload Provider Class
 *
 * Created by jhlee on 2023-04-11
 */
interface FileProvider {
    fun onDownload(filePath: String, onUpdate: (Int) -> Unit): Single<ResponseBody> //파일 다운로드
    suspend fun saveFile(
        body: ResponseBody,
        folderPath: String,
        downloadPath: String
    ): String //파일 저장

    fun saveToFile(
        body: ResponseBody,
        folderPath: String,
        downloadPath: String
    ): String //파일 저장


    fun isFileExists(folderPath: String, downloadPath: String): String //파일 체크 후 경로 반환
    suspend fun onUploadFiles(
        filePath: String,
        uploadUrl: String,
        contentType: String
    ): Boolean  //업로드

    suspend fun onUploadFiles(
        filePath: String,
        uploadUrl: String,
        contentType: String,
        onUpdate: (Int) -> Unit
    ): Boolean

  /*
    2023-07-14 주석
    fun getMimeTypeVideoWebm(): String
    fun getMimeTypeImage(): String
    fun getMimeTypeAudio(): String
    fun getMimeTypeVideo(): String*/
    fun getMimeTypeOctetStream(): String

    /**
     * 이미지 업로드
     * @param list Pair(로컬 이미지 경로, 업로드할 Remote 경로)
     */
    fun requestImageUploads(list: List<Pair<String, String>>): Single<List<String>>

    /**
     * 이미지 업로드 요청
     * @param imageUri content://media/external/images/media/11
     * @param uploadUrl 업로드 할 이미지 경로
     */
    fun requestImageUpload(imageUri: String, uploadUrl: String): Single<String>

    /**
     * 카메라에서 찍은 이미지 업로드 요청
     * @param pictureUri Picture Uri
     * @param uploadUrl 업로드할 이미지 경로
     * @return 업로드된 이미지 경로
     */
    fun requestCameraPictureUpload(pictureUri: String, uploadUrl: String): Single<String>
}

class FileProviderImpl @Inject constructor(
    @ApplicationContext
    val context: Context,
    val apiService: ApiService,
    private val trackingInterceptor: TrackingHttpInterceptor,
    private val retrofitLogger: RetrofitLogger
) : FileProvider {

    companion object {
        const val TAG = "FileUploadBinary"
        const val CONTENT_MIME_TYPE_VIDEO_WEBM = "video/webm" // 비디오 content type
        const val CONTENT_MIME_TYPE_IMAGE_PNG = "image/png" // 이미지 파일 content type
        const val CONTENT_MIME_TYPE_AUDIO_WAV = "audio/wav" // 오디오 파일 content type
        const val CONTENT_MIME_TYPE_OCTET_STREAM = "application/octet-stream" //2023-07-14  웹에서 컨텐츠 다운로드 받기 위함
        const val KEY_UPLOAD_URL_DATA_VIDEO = "video_file_uri" // 비디오 파일 uri key
        const val KEY_UPLOAD_METHOD_TYPE_PUT = "PUT"
        const val KEY_UPLOAD_HEADER_ACCEPT = "Accept"
        const val KEY_UPLOAD_HEADER_CONTENT_TYPE = "Content-Type"
        const val KEY_UPLOAD_HEADER_VERSION = "x-ms-version"
        const val VALUE_UPLOAD_HEADER_VERSION = "2021-06-08"
        const val KEY_UPLOAD_HEADER_BLOB_TYPE = "x-ms-blob-type"
        const val VALUE_UPLOAD_HEADER_BLOCK_BLOB = "BlockBlob"
    }

    /**
     * 파일 다운로드
     */
    override fun onDownload(filePath: String, onUpdate: (Int) -> Unit): Single<ResponseBody> {
        return Retrofit.Builder().apply {
            baseUrl(Config.BASE_API_URL)
            client(createProgressClient(onUpdate))
            addCallAdapterFactory(RxJava3CallAdapterFactory.create())
        }.build().create(ApiService::class.java)
            .fetchDownloadFile(filePath)
    }

    /**
     * 파일 다운로드 클라이언트
     * @param  다은로드 진행률
     */
    private fun createProgressClient(onUpdate: (Int) -> Unit) = OkHttpClient.Builder().apply {
        retryOnConnectionFailure(false)
        connectTimeout(60, TimeUnit.SECONDS)
        readTimeout(60, TimeUnit.SECONDS)
        writeTimeout(60, TimeUnit.SECONDS)
        connectionPool(ConnectionPool(5, 1, TimeUnit.SECONDS))
        addInterceptor { chain ->
            val originalResponse = chain.proceed(chain.request())
            originalResponse.newBuilder()
                .body(originalResponse.body?.let { ProgressResponseBody(it, onUpdate) })
                .build()
        }
        if (Config.IS_DEBUG) {
            addInterceptor(retrofitLogger)
            addInterceptor(trackingInterceptor)
        }
    }.build()

    override fun saveToFile(body: ResponseBody, folderPath: String, downloadPath: String): String {

        if (body == null) {
            return ""
        }
        var input: InputStream? = null
        var output: OutputStream? = null

        try {

            val dirPath = File(folderPath)

            if (!dirPath.exists()) {
                dirPath.mkdirs()
                DLogger.d("saveFile 폴더 생성 ${dirPath}")
            } else {
                DLogger.d("saveFile 폴더 이미 생성 ${dirPath}")
            }

            val targetFile = isFileExists(folderPath, downloadPath)

            //파일이 있으면 경로 리턴
            if (!targetFile.isNullOrEmpty()) {
                return targetFile
            }

            val filePath = File("$folderPath/$downloadPath")

            input = body.byteStream()
            output = FileOutputStream(filePath)

            output.use { output ->
                val buffer = ByteArray(4 * 1024) // or other buffer size
                var read: Int
                while (input.read(buffer).also { read = it } != -1) {
                    output.write(buffer, 0, read)
                }
                output.flush()
            }
            DLogger.d("saveFile Success=>${downloadPath}")
            return filePath.path
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            input?.close()
            output?.close()
        }
        return ""
    }

    /**
     * 해당 경로에 저장
     */
    override suspend fun saveFile(
        body: ResponseBody,
        folderPath: String,
        downloadPath: String
    ): String {

        if (body == null) {
            return ""
        }
        var input: InputStream? = null
        var output: OutputStream? = null

        try {

            val dirPath = File(folderPath)

            if (!dirPath.exists()) {
                dirPath.mkdirs()
                DLogger.d("saveFile 폴더 생성 ${dirPath}")
            } else {
                DLogger.d("saveFile 폴더 이미 생성 ${dirPath}")
            }

            val targetFile = isFileExists(folderPath, downloadPath)

            //파일이 있으면 경로 리턴
            if (!targetFile.isNullOrEmpty()) {
                return targetFile
            }

            val filePath = File("$folderPath/$downloadPath")

            input = body.byteStream()
            output = FileOutputStream(filePath)

            output.use { output ->
                val buffer = ByteArray(4 * 1024) // or other buffer size
                var read: Int
                while (input.read(buffer).also { read = it } != -1) {
                    output.write(buffer, 0, read)
                }
                output.flush()
            }
            DLogger.d("saveFile Success=>${downloadPath}")
            return filePath.path
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            input?.close()
            output?.close()
        }
        return ""
    }


    /**
     * AZURE CDN 업로드
     */
    override suspend fun onUploadFiles(
        filePath: String,
        uploadUrl: String,
        contentType: String
    ): Boolean {

        DLogger.d(
            TAG,
            "[s]onUploadFiles ============================================================"
        )


        //AZURE CDN 정보
        val host = Config.BASE_FILE_URL

        var result = false
        var urlconnection: URLConnection? = null
        var bos: BufferedOutputStream? = null
        var bis: BufferedInputStream? = null
        try {

            val file = File(filePath)
            val url = URL(host + uploadUrl)

            urlconnection = url.openConnection()
            urlconnection.doOutput = true
            urlconnection.doInput = true
            val absoluteFilePath = file.absolutePath

            if (urlconnection is HttpURLConnection) {
                urlconnection.requestMethod = KEY_UPLOAD_METHOD_TYPE_PUT
                urlconnection.setRequestProperty(KEY_UPLOAD_HEADER_ACCEPT, contentType)
                urlconnection.setRequestProperty(KEY_UPLOAD_HEADER_CONTENT_TYPE, contentType)
                urlconnection.setRequestProperty(
                    KEY_UPLOAD_HEADER_VERSION,
                    VALUE_UPLOAD_HEADER_VERSION
                )
                urlconnection.setRequestProperty(
                    KEY_UPLOAD_HEADER_BLOB_TYPE,
                    VALUE_UPLOAD_HEADER_BLOCK_BLOB
                )
                urlconnection.connect()
            }


            bos = BufferedOutputStream(urlconnection.getOutputStream())
            bis = BufferedInputStream(FileInputStream(absoluteFilePath))

            var i: Int
            val buffer = ByteArray(2048)
            while (bis.read(buffer).also { i = it } > 0) {
                bos.write(buffer, 0, i)
            }
            bis.close()
            bos.close()

            val responseCode = (urlconnection as HttpURLConnection?)!!.responseCode

            DLogger.d(TAG, "filePath=> ${filePath}")
            DLogger.d(TAG, "URl_Host_path=> ${url.host} <>  ${url.path}")
            DLogger.d(TAG, "uploadUrl=> ${host + uploadUrl}  ")
            DLogger.d(TAG, "ContentType=> ${contentType}")

            DLogger.d(TAG, "FILE UPLOAD RESPONSE MESSAGE : " + urlconnection!!.responseMessage)

            if (responseCode in 200..202) {
                DLogger.d(
                    "파일 업로드 헤더 : ",
                    String.format(
                        "Accept : %s, Content-Type : %s, x-ms-blob-type",
                        urlconnection.getRequestProperty(KEY_UPLOAD_HEADER_ACCEPT),
                        urlconnection.getRequestProperty(KEY_UPLOAD_HEADER_CONTENT_TYPE),
                        urlconnection.getRequestProperty(KEY_UPLOAD_HEADER_BLOB_TYPE)
                    )
                )
                result = true
            }
            urlconnection!!.disconnect()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                bis?.close()
                bos?.close()
                if (urlconnection != null) (urlconnection as HttpURLConnection).disconnect()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return result
    }

    override suspend fun onUploadFiles(
        filePath: String,
        uploadUrl: String,
        contentType: String,
        onUpdate: (Int) -> Unit
    ): Boolean {
        return try {
            // onUploadFiles HttpURLConnection 로직 그대로 OkHttp3로 변환함
            val file = File(filePath)
            val requestBody = UploadProgressRequestBody(
                file.asRequestBody(contentType.toMediaType()),
                onUpdate
            )
            val request = Request.Builder()
                .url(Config.BASE_FILE_URL.plus(uploadUrl))
                .addHeader(KEY_UPLOAD_HEADER_ACCEPT, contentType)
                .addHeader(KEY_UPLOAD_HEADER_CONTENT_TYPE, contentType)
                .addHeader(KEY_UPLOAD_HEADER_VERSION, VALUE_UPLOAD_HEADER_VERSION)
                .addHeader(KEY_UPLOAD_HEADER_BLOB_TYPE, VALUE_UPLOAD_HEADER_BLOCK_BLOB)
                .put(requestBody)
                .build()

            val client = OkHttpClient.Builder()
                .retryOnConnectionFailure(true)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .connectionPool(ConnectionPool(5, 1, TimeUnit.SECONDS))
                .addInterceptor(trackingInterceptor)
                .build()
            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (ex: Exception) {
            false
        }
    }

    /**
     * 파일 유무 체크
     */
    override fun isFileExists(folderPath: String, downloadPath: String): String {

        val filePath = File("$folderPath/$downloadPath")

        DLogger.d("isFileExists  ${filePath.path}")

        return if (filePath.exists()) {
            DLogger.d("isFileExists 파일 있음 ${filePath}")
            filePath.path
        } else {
            DLogger.d("isFileExists 파일 없음 ${filePath}")
            ""
        }
    }


    /**
     * webm
     */
//    override fun getMimeTypeVideoWebm(): String {
//        return CONTENT_MIME_TYPE_VIDEO_WEBM
//    }

    /**
     * image
     */
//    override fun getMimeTypeImage(): String {
//        return CONTENT_MIME_TYPE_IMAGE_PNG
//    }

    /**
     * audio
     */
//    override fun getMimeTypeAudio(): String {
//        return CONTENT_MIME_TYPE_AUDIO_WAV
//    }

    /**
     * video
     */
//    override fun getMimeTypeVideo(): String {
//        return KEY_UPLOAD_URL_DATA_VIDEO
//    }

    /**
     * video
     */
    override fun getMimeTypeOctetStream(): String {
        return CONTENT_MIME_TYPE_OCTET_STREAM
    }


    override fun requestImageUploads(list: List<Pair<String, String>>): Single<List<String>> {
        val works = mutableListOf<Single<String>>()
        list.forEach {
            works.add(requestImageUpload(it.first, it.second).subscribeOn(Schedulers.io()))
        }
        return Single.zip(works) { array ->
            val response = mutableListOf<String>()
            array.forEach { response.add(it as String) }
            return@zip response
        }
    }

    override fun requestImageUpload(imageUri: String, uploadUrl: String): Single<String> {
        return Single.create { emitter ->
            try {
                var bitmap = Glide.with(context).asBitmap().load(imageUri).submit().get()
                // 이미지 리사이징 처리 필요에 따라 사용할지 말지 정의.
                if (1080 < bitmap.width) {
                    // 비율에 맞게 높이값 계산
                    val height = 1080 * bitmap.height / bitmap.width
                    bitmap = Bitmap.createScaledBitmap(bitmap, 1080, height, true)
                }
                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                val request = Request.Builder()
                    .url(Config.BASE_FILE_URL.plus(uploadUrl))
                    .addHeader(KEY_UPLOAD_HEADER_ACCEPT, getMimeTypeOctetStream())
                    .addHeader(KEY_UPLOAD_HEADER_CONTENT_TYPE, getMimeTypeOctetStream())
                    .addHeader(KEY_UPLOAD_HEADER_VERSION, VALUE_UPLOAD_HEADER_VERSION)
                    .addHeader(KEY_UPLOAD_HEADER_BLOB_TYPE, VALUE_UPLOAD_HEADER_BLOCK_BLOB)
                    .put(
                        stream.toByteArray()
                            .toRequestBody(CONTENT_MIME_TYPE_OCTET_STREAM.toMediaType())
                    )
                    .build()

                val client = OkHttpClient.Builder()
                    .retryOnConnectionFailure(true)
                    .addInterceptor(trackingInterceptor)
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .writeTimeout(15, TimeUnit.SECONDS)
                    .connectionPool(ConnectionPool(5, 1, TimeUnit.SECONDS))
                    .build()
                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        emitter.onError(e)
                    }

                    override fun onResponse(call: Call, response: Response) {
                        emitter.onSuccess(uploadUrl)
                        // emitter.onSuccess(call.request().url.toString())
                    }
                })
            } catch (ex: Exception) {
                emitter.onError(ex)
            }
        }.subscribeOn(Schedulers.io())
    }

    override fun requestCameraPictureUpload(
        pictureUri: String,
        uploadUrl: String
    ): Single<String> {
        return Single.create<String> { emitter ->
            try {
                var bitmap = Glide.with(context)
                    .asBitmap()
                    .load(pictureUri)
                    .submit(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                    .get()

                // 이미지 리사이징 처리 필요에 따라 사용할지 말지 정의.
                if (1080 < bitmap.width) {
                    // 비율에 맞게 높이값 계산
                    val height = 1080 * bitmap.height / bitmap.width
                    bitmap = Bitmap.createScaledBitmap(bitmap, 1080, height, true)
                }

                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                val request = Request.Builder()
                    .url(Config.BASE_FILE_URL.plus(uploadUrl))
                    .addHeader(KEY_UPLOAD_HEADER_ACCEPT, getMimeTypeOctetStream())
                    .addHeader(KEY_UPLOAD_HEADER_CONTENT_TYPE, getMimeTypeOctetStream())
                    .addHeader(KEY_UPLOAD_HEADER_VERSION, VALUE_UPLOAD_HEADER_VERSION)
                    .addHeader(KEY_UPLOAD_HEADER_BLOB_TYPE, VALUE_UPLOAD_HEADER_BLOCK_BLOB)
                    .put(
                        stream.toByteArray()
                            .toRequestBody(CONTENT_MIME_TYPE_OCTET_STREAM.toMediaType())
                    )
                    .build()

                val client = OkHttpClient.Builder()
                    .retryOnConnectionFailure(true)
                    .addInterceptor(trackingInterceptor)
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .writeTimeout(15, TimeUnit.SECONDS)
                    .connectionPool(ConnectionPool(5, 1, TimeUnit.SECONDS))
                    .build()
                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        emitter.onError(e)
                    }

                    override fun onResponse(call: Call, response: Response) {
                        emitter.onSuccess(uploadUrl)
                        // emitter.onSuccess(call.request().url.toString())
                    }
                })
            } catch (ex: Exception) {
                emitter.onSuccess("")
            }
        }.subscribeOn(Schedulers.io())

    }
}
