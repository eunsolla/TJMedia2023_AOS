package com.verse.app.gallery.core

import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import io.reactivex.rxjava3.core.Single
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

/**
 * Description : GalleryProvider API
 *
 * @see <a href="https://github.com/sieunju/gallery">참고</a>
 *
 * Created by juhongmin on 2023/05/12
 */
interface GalleryProvider {
    @Throws(IllegalStateException::class, Exception::class)
    fun fetchDirectories(): Single<List<GalleryFilterData>>

    @Throws(IllegalStateException::class, NullPointerException::class)
    fun fetchGallery(params: GalleryQueryParameter): Single<Cursor>

    @Throws(IOException::class, IllegalArgumentException::class, FileNotFoundException::class)
    fun pathToBitmap(path: String): Single<Bitmap>

    @Throws(IOException::class, IllegalArgumentException::class, FileNotFoundException::class)
    fun pathToBitmap(path: String, limitSize: Int): Single<Bitmap>

    fun deleteCacheDirectory(): Boolean

    @Throws(NullPointerException::class, IllegalArgumentException::class)
    fun createGalleryPhotoUri(): Single<Uri>

    @Throws(Exception::class)
    fun saveGalleryPicture(pictureUri: String): Single<Pair<Boolean, String>>

    @Throws(
        FileNotFoundException::class,
        SecurityException::class,
        NullPointerException::class,
        IllegalArgumentException::class
    )
    fun saveBitmapToFile(bitmap: Bitmap): Single<File>

    fun getImageType(imagePath: String): ImageType
}