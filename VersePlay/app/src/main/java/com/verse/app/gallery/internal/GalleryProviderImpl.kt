package com.verse.app.gallery.internal

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import com.bumptech.glide.Glide
import com.verse.app.gallery.core.GalleryFilterData
import com.verse.app.gallery.core.GalleryProvider
import com.verse.app.gallery.core.GalleryQueryParameter
import com.verse.app.gallery.core.ImageType
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import okio.IOException
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * Description : Gallery Provider API Impl
 * @see <a href="https://github.com/sieunju/gallery">참고</a>
 * Created by juhongmin on 2023/05/12
 */
@Suppress("unused")
internal class GalleryProviderImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : GalleryProvider {

    private val contentResolver: ContentResolver by lazy { context.contentResolver }

    companion object {
        val IMAGE_URI: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val VIDEO_URI: Uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        const val ID = MediaStore.Images.Media._ID
        const val DEFAULT_GALLERY_FILTER_ID = "ALL"
        const val DEFAULT_GALLERY_FILTER_NAME = "최근 항목"

        @SuppressLint("InlinedApi")
        val BUCKET_ID = MediaStore.Images.Media.BUCKET_ID

        @SuppressLint("InlinedApi")
        val BUCKET_NAME = MediaStore.Images.Media.BUCKET_DISPLAY_NAME

        val DISPLAY_NAME = MediaStore.Images.Media.DISPLAY_NAME

        private val DATE_FORMAT: SimpleDateFormat by lazy {
            SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss",
                Locale.KOREA
            )
        }
    }

    private inline fun <reified R : Any> simpleRx(crossinline callback: () -> R): Single<R> {
        return Single.create { emitter ->
            try {
                val result = callback()
                emitter.onSuccess(result)
            } catch (ex: Exception) {
                emitter.onError(ex)
            }
        }.subscribeOn(Schedulers.io())
    }

    private val DATE_FORMAT: SimpleDateFormat by lazy {
        SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss",
            Locale.KOREA
        )
    }

    override fun fetchDirectories(): Single<List<GalleryFilterData>> {
        return simpleRx {
            // Permissions Check
            if (!isReadStoragePermissionsGranted()) throw IllegalStateException("'android.permission.READ_EXTERNAL_STORAGE' is not Granted! ")
            val dataList = mutableListOf<GalleryFilterData>()
            val projection = arrayOf(
                ID,
                BUCKET_ID,
                BUCKET_NAME
            )
            val selection = StringBuilder()
            val selectionArgs = mutableListOf<String>()
            val sort = "$ID DESC "
            var prevPhotoUri = ""
            var prevBucketId = ""
            var prevBucketName = ""
            var prevCount: Int = -1
            try {
                loop@ while (true) {
                    val cursor = contentResolver.query(
                        IMAGE_URI,
                        projection,
                        if (selection.isEmpty()) null else selection.toString(),
                        if (selectionArgs.isEmpty()) null else selectionArgs.toTypedArray(),
                        sort
                    ) ?: break@loop

                    if (cursor.moveToFirst()) {
                        val contentId = cursor.getContentsId()
                        val photoUri = getPhotoUri(contentId)
                        val bucketId = cursor.getBucketId()
                        val bucketName = cursor.getBucketName()
                        val count = cursor.count

                        if (!cursor.isClosed) {
                            cursor.close()
                        }

                        // Where Setting
                        if (selection.isNotEmpty()) {
                            selection.append(" AND ")
                        }

                        selection.append(BUCKET_ID)
                        selection.append(" !=?")
                        selectionArgs.add(bucketId)

                        // 앨범명 유효성 검사.
                        if (prevBucketId.isNotEmpty()) {
                            val diffCount = prevCount - count
                            prevCount = count
                            dataList.add(
                                GalleryFilterData(
                                    bucketId = prevBucketId,
                                    bucketName = prevBucketName,
                                    photoUri = prevPhotoUri,
                                    count = diffCount
                                )
                            )
                        }

                        // 초기값 세팅
                        if (prevCount == -1) {
                            prevCount = count
                            dataList.add(
                                GalleryFilterData(
                                    bucketId = DEFAULT_GALLERY_FILTER_ID,
                                    bucketName = DEFAULT_GALLERY_FILTER_NAME,
                                    photoUri = photoUri,
                                    count = count
                                )
                            )
                        }

                        // Set Data.
                        prevPhotoUri = photoUri
                        prevBucketId = bucketId
                        prevBucketName = bucketName

                    } else {
                        // 맨 마지막 앨범 추가
                        if (prevCount != 0) {
                            dataList.add(
                                GalleryFilterData(
                                    bucketId = prevBucketId,
                                    bucketName = prevBucketName,
                                    photoUri = prevPhotoUri,
                                    count = prevCount
                                )
                            )
                        }

                        if (!cursor.isClosed) {
                            cursor.close()
                        }
                        break@loop
                    }
                }
                return@simpleRx dataList
            } catch (ex: Exception) {
                throw ex
            }
        }
    }

    override fun fetchGallery(params: GalleryQueryParameter): Single<Cursor> {
        return simpleRx {
            if (!isReadStoragePermissionsGranted()) throw IllegalStateException("Permissions PERMISSION_DENIED")
            val order = "$ID ${params.order}"
            val uri = if (params.isVideoType) VIDEO_URI else IMAGE_URI
            return@simpleRx contentResolver.query(
                uri,
                params.getColumns(),
                if (params.isAll) null else "$BUCKET_ID ==?",
                params.selectionArgs,
                order
            ) ?: throw NullPointerException("Cursor NullPointerException")
        }
    }

    override fun pathToBitmap(path: String): Single<Bitmap> {
        return pathToBitmap(path, -1)
    }

    override fun pathToBitmap(path: String, limitSize: Int): Single<Bitmap> {
        return simpleRx { Glide.with(context).asBitmap().override(limitSize).submit().get() }
    }

    override fun deleteCacheDirectory(): Boolean {
        return try {
            val dir = context.getExternalFilesDir(
                Environment.DIRECTORY_PICTURES
            ) ?: return false
            val iterator = dir.listFiles()?.iterator() ?: return false
            while (iterator.hasNext()) {
                try {
                    iterator.next().delete()
                } catch (_: Exception) {
                }
            }
            true
        } catch (ex: Exception) {
            false
        }
    }

    override fun createGalleryPhotoUri(): Single<Uri> {
        return simpleRx {
            val file = createTempFile()
            return@simpleRx FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
        }
    }

    override fun saveGalleryPicture(pictureUri: String): Single<Pair<Boolean, String>> {
        return simpleRx {
            // Scoped Storage
            try {
                val name = "VERSE"
                return@simpleRx if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val values = ContentValues().apply {
                        put(
                            MediaStore.Images.Media.DISPLAY_NAME,
                            "${name}_${DATE_FORMAT.format(System.currentTimeMillis())}.jpg"
                        )
                        put(MediaStore.Images.Media.MIME_TYPE, "image/jpg")
                        put(MediaStore.Images.Media.IS_PENDING, 1)
                        put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis())
                        put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
                    }

                    val collection =
                        MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                    val item = contentResolver.insert(collection, values)
                        ?: return@simpleRx false to "Insert Resolver Error"
                    val parcelFile = contentResolver.openFileDescriptor(item, "w", null)
                        ?: return@simpleRx false to "openFileDescriptor Error"
                    val fos = FileOutputStream(parcelFile.fileDescriptor)
                    val bitmap = pathToBitmap(pictureUri).blockingGet()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                    fos.close()
                    values.clear()
                    values.put(MediaStore.Images.Media.IS_PENDING, 0)
                    contentResolver.update(item, values, null, null)
                    parcelFile.close()
                    true to pictureUri
                } else {
                    // Legacy Version
                    val values = ContentValues().apply {
                        put(MediaStore.Images.Media.DISPLAY_NAME, "${name}.jpg")
                        put(MediaStore.Images.Media.MIME_TYPE, "image/jpg")
                        put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis())
                        put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
                    }
                    val url =
                        contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                            ?: return@simpleRx false to "Insert Resolver Error"
                    val bitmap = pathToBitmap(pictureUri).blockingGet()
                    val imageOut = contentResolver.openOutputStream(url)
                    try {
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, imageOut)
                    } finally {
                        imageOut?.close()
                    }
                    true to pictureUri
                }
            } catch (ex: Exception) {
                return@simpleRx false to ex.toString()
            }
        }
    }

    override fun saveBitmapToFile(bitmap: Bitmap): Single<File> {
        return simpleRx {
            val file = createTempFile()
            val fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos.close()
            return@simpleRx file
        }
    }

    override fun getImageType(imagePath: String): ImageType {
        return try {
            // content://media/external/images/media/1
            val contentUrl = Uri.parse(imagePath)
            val input = contentResolver.openInputStream(contentUrl) ?: return ImageType.UN_KNOWN
            val exifInterface = ExifInterface(input)
            input.close()

            // TAG_MODEL -> 휴대폰 단말기 모델, TAG_F_NUMBER -> 빛의 양, TAG_PHOTOGRAPHIC_SENSITIVITY -> Camera ISO,
            // TAG_EXPOSURE_TIME -> 노출 시간, TAG_APERTURE_VALUE -> 조리개 값
            // 기타 카메라 정보들이 있는 경우 캡처된 이미지 X
            if (exifInterface.getAttribute(ExifInterface.TAG_MODEL) != null && (
                        exifInterface.getAttribute(ExifInterface.TAG_F_NUMBER) != null ||
                                exifInterface.getAttribute(ExifInterface.TAG_PHOTOGRAPHIC_SENSITIVITY) != null ||
                                exifInterface.getAttribute(ExifInterface.TAG_EXPOSURE_TIME) != null ||
                                exifInterface.getAttribute(ExifInterface.TAG_APERTURE_VALUE) != null
                        )
            ) {
                return ImageType.CAMERA
            }

            val projection = arrayOf(
                ID,
                BUCKET_ID,
                BUCKET_NAME,
                DISPLAY_NAME
            )

            val contentId = contentUrl.lastPathSegment ?: return ImageType.ETC

            val cursor = contentResolver.query(
                IMAGE_URI,
                projection,
                "$ID ==?",
                arrayOf(contentId),
                null
            ) ?: return ImageType.UN_KNOWN
            cursor.moveToNext()
            val displayName = cursor.getDisplayName().lowercase()
            val bucketName = cursor.getBucketName().lowercase()
            cursor.close()

            // ScreenShot
            if (bucketName.contains("screenshots") ||
                displayName.startsWith("screenshot_")
            ) {
                return ImageType.SCREENSHOT
            }

            return ImageType.ETC
        } catch (ex: Exception) {
            ImageType.UN_KNOWN
        }
    }

    /**
     * 저장소 읽기 권한 체크 (이미지, 동영상 저장소)
     * @return true 읽기 권한 허용, false 읽기 권한 거부 상태
     */
    private fun isReadStoragePermissionsGranted(): Boolean {
        val pm = context.packageManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.checkPermission(
                Manifest.permission.READ_MEDIA_IMAGES,
                context.packageName
            ) == PackageManager.PERMISSION_GRANTED ||
                    pm.checkPermission(
                        Manifest.permission.READ_MEDIA_VIDEO,
                        context.packageName
                    ) == PackageManager.PERMISSION_GRANTED ||
                    pm.checkPermission(
                        Manifest.permission.READ_MEDIA_AUDIO,
                        context.packageName
                    ) == PackageManager.PERMISSION_GRANTED
        } else {
            pm.checkPermission(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                context.packageName
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * 파일 접미사 기준으로 Bitmap 압축형식 변환 처리함수
     * @param suffix ex.) .png, .jpg..
     *
     * @return Bitmap.CompressFormat
     * @see Bitmap.CompressFormat.JPEG
     * @see Bitmap.CompressFormat.PNG
     */
    fun toCompressFormat(suffix: String): Bitmap.CompressFormat {
        return if (suffix == ".png") {
            Bitmap.CompressFormat.PNG
        } else {
            Bitmap.CompressFormat.JPEG
        }
    }

    /**
     * Media ID 값 리턴하는 함수
     * 에러 발생시 빈값으로 리턴
     *
     * @return Media ID
     */
    private fun Cursor.getContentsId(): String {
        return try {
            getString(getColumnIndexOrThrow(MediaStore.Images.Media._ID))
        } catch (ex: Exception) {
            ""
        }
    }

    /**
     * 앨범 아이디값 리턴하는 함수
     * 에러 발생시 빈값으로 리턴
     *
     * @return Album ID
     */
    private fun Cursor.getBucketId(): String {
        return try {
            getString(getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID))
        } catch (ex: Exception) {
            ""
        }
    }

    /**
     * 앨범명 리턴하는 함수
     * 에러 발생시 빈값으로 리턴
     *
     * @return Album Name
     */
    private fun Cursor.getBucketName(): String {
        return try {
            getString(getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME))
        } catch (ex: Exception) {
            ""
        }
    }

    /**
     * 이미지명 리턴하는 함수
     * 에러 발생시 빈값으로 리턴
     *
     * @return Image Name
     */
    private fun Cursor.getDisplayName(): String {
        return try {
            getString(getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME))
        } catch (ex: Exception) {
            ""
        }
    }

    /**
     * Media ID 값을 content://.. 으로 변환해서 리턴하는 함수
     * @param id Media ID
     *
     * @return content://media/external/images/media/{id}
     */
    private fun getPhotoUri(id: String): String {
        return try {
            Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id).toString()
        } catch (ex: NullPointerException) {
            ""
        }
    }

    private fun createTempFile(): File {
        return createFile("${SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())}_", ".jpg")
    }

    private fun createFile(name: String, suffix: String): File {
        return try {
            File.createTempFile(
                name,
                suffix,
                context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            )
        } catch (ex: IOException) {
            throw ex
        }
    }
}