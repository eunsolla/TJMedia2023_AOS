package com.verse.app.ui.mypage.crop

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.ViewTreeObserver
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView

// ImageView에 오류가 있어서 -> AppCompatImageView로 변경 20220523
class CropImageView : AppCompatImageView {
    private var cropParams: CropParams? = null

    constructor(context: Context?) : super(context!!) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs) {}
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context!!,
        attrs,
        defStyleAttr
    ) {
    }

    //사용하지 않는 코드 주석처리 20220523
    //    public CropImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    //        super(context, attrs, defStyleAttr, defStyleRes);
    //
    //    }
    fun init(shape: CroppedShape) {
        setScaleType(ImageView.ScaleType.MATRIX)
        cropParams = CropParams()
        if (shape == CroppedShape.HOLE) {
        } else {
            cropParams!!.shape = CroppedShape.SQUARE
        }
        setDefaultRadius()
        Log.d("onDraw", "init")
        if (getDrawable() != null) {
            val vto: ViewTreeObserver = getViewTreeObserver()
            vto.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {

                    //removeGlobalOnLayoutListener -> removeOnGlobalLayoutListener
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                        getViewTreeObserver().removeOnGlobalLayoutListener(this)
                    } else {
                        getViewTreeObserver().removeOnGlobalLayoutListener(this)
                    }
                    onImageLoaded()
                }
            })
        }
    }

    private fun onImageLoaded() {
        cropParams!!.updateWithView(getWidth(), getHeight())
        setImageCentered()
        setOnTouchListener(CropTouchListener(cropParams, getImageMatrix()))
    }

    private fun setDefaultRadius() {
        val screenSize = ImageUtils.getScreenSize(getContext())
        val minScreenSize = Math.min(screenSize!!.x, screenSize.y)
        if (cropParams!!.shape == CroppedShape.HOLE) {
            CropParams.Companion.circleRadius = (minScreenSize * 0.4f).toInt()
            CropParams.Companion.ImageWidth = (minScreenSize * 0.4f).toInt()
        } else {
            CropParams.Companion.circleRadius = (minScreenSize * 0.4f).toInt()
            CropParams.Companion.ImageWidth = (minScreenSize * 0.5f).toInt()
        }
    }

    private fun setImageCentered() {
        val matrix: Matrix = getImageMatrix()
        val bitmap: Bitmap = bitmap

        // background는 FILL
        // profile은 CENTER
        if (bitmap != null && cropParams!!.circle != null) {
            val drawableRect =
                RectF(0f, 0f, bitmap.getWidth().toFloat(), bitmap.getHeight().toFloat())
            val circle = cropParams!!.circle
            val viewRect: RectF
            if (bitmap.getWidth() > bitmap.getHeight()) {
                val scale: Float = getHeight().toFloat() / bitmap.getHeight()
                val scaledWidth: Float = scale * bitmap.getWidth()
                val x: Float = (scaledWidth - getWidth()) / 2
                viewRect = RectF(-x, 0f, getWidth() + x, getHeight().toFloat())
            } else {
                val scale: Float = getWidth().toFloat() / bitmap.getWidth()
                val scaledHeight: Float = scale * bitmap.getHeight()
                val y: Float = (scaledHeight - getHeight()) / 2
                viewRect = RectF(0f, -y, getWidth().toFloat(), getHeight() + y)
            }
            matrix.setRectToRect(drawableRect, viewRect, Matrix.ScaleToFit.CENTER)
            setImageMatrix(matrix)
        }
    }

    private val bitmap: Bitmap
        private get() = (getDrawable() as BitmapDrawable).getBitmap()

    protected override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        Log.d("onDraw", "onDraw")
        val circle = cropParams!!.circle ?: return
        val paint: Paint?
        val path: Path?
        when (cropParams!!.shape) {
            CroppedShape.HOLE -> {
                paint = cropParams!!.holeParams!!.paint
                path = cropParams!!.holeParams!!.path
                canvas.drawPath(path!!, paint)
            }

            CroppedShape.SQUARE -> {
                paint = cropParams!!.squareParams!!.paint
                path = cropParams!!.squareParams!!.path
                canvas.drawPath(path!!, paint)
            }
        }
    }

    // 원본에서의 지름 구하기
    val croppedBitmap: Bitmap
        get() {
            val matrix: Matrix = getImageMatrix()
            val matrixParams: MatrixParams = MatrixParams.Companion.fromMatrix(matrix)
            val bitmap: Bitmap = bitmap
            val circle = cropParams!!.circle

            // 원본에서의 지름 구하기
            val size = (circle!!.diameter / matrixParams.scaleWidth).toInt()
            val y = getCropTop(matrixParams, circle)
            val x = getCropLeft(matrixParams, circle)
            return Bitmap.createBitmap(
                bitmap,
                x,
                y,
                size,
                size
            )
        }// width

    // height
    // 원본에서의 지름 구하기
    val croppedBitmapForBackground: Bitmap
        //        if(getWidth() > bitmap.getWidth()) {
//            size1 = (int) (getWidth() / matrixParams.getScaleWidth());
//        }else{
//            size1 = bitmap.getWidth();
//        }
        get() {
            val matrix: Matrix = getImageMatrix()
            val matrixParams: MatrixParams = MatrixParams.Companion.fromMatrix(matrix)
            val bitmap: Bitmap = bitmap
            val circle = cropParams!!.circle

            // 원본에서의 지름 구하기
            val size1: Int
            Log.d("widthTest", "getWidth : " + getWidth())
            Log.d("widthTest", "bitmap.getWidth : " + bitmap.getWidth())

//        if(getWidth() > bitmap.getWidth()) {
//            size1 = (int) (getWidth() / matrixParams.getScaleWidth());
//        }else{
//            size1 = bitmap.getWidth();
//        }
            size1 = (getWidth() / matrixParams.scaleWidth).toInt()
            Log.d("widthTest", "size : $size1")
            var size2: Int
            size2 = (circle!!.diameter / matrixParams.scaleHeight).toInt()
            val x = getCropLeft(matrixParams, circle)
            val y = getCropTop(matrixParams, circle)
            if (y + size2 > bitmap.getHeight()) {
                size2 = bitmap.getHeight() - y
            }
            return Bitmap.createBitmap(
                bitmap,
                x,
                y,
                size1,  // width
                size2
            )
        }

    private fun getCropLeft(matrixParams: MatrixParams, circle: Circle?): Int {
        val translationX = matrixParams.x
        var x = circle!!.leftBound - translationX
        Log.d("getCropLeft", "x : $x")
        x = Math.max(x, 0f)

        // 원본에 비교해서 확대된 비율
        x /= matrixParams.scaleWidth
        return x.toInt()
    }

    private fun getCropTop(matrixParams: MatrixParams, circle: Circle?): Int {
        val translationY = matrixParams.y
        var y = circle!!.topBound - translationY
        y = Math.max(y, 0f)

        // 원본에 비교해서 확대된 비율
        y /= matrixParams.scaleWidth
        return y.toInt()
    }
}