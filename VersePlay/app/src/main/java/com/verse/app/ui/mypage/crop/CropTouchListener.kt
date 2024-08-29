package com.verse.app.ui.mypage.crop

import android.graphics.Matrix
import android.graphics.PointF
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.widget.ImageView

class CropTouchListener(private val cropParams: CropParams?, matrix: Matrix?) : OnTouchListener {
    private val circle: Circle?
    private var mode = Mode.NONE
    var matrix = Matrix()
    var savedMatrix = Matrix()
    var firstTouchPoint = PointF()
    var scaleCenterPoint = PointF()
    var fingersDistance = 1f

    init {
        circle = cropParams!!.circle
        this.matrix.set(matrix)
        savedMatrix.set(matrix)
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        val iv = v as ImageView
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                savedMatrix.set(matrix)
                firstTouchPoint[event.x] = event.y
                mode = Mode.DRAG
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                fingersDistance = spacing(event)
                if (fingersDistance > 10f) {
                    savedMatrix.set(matrix)
                    midPoint(scaleCenterPoint, event)
                    mode = Mode.ZOOM
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> mode = Mode.NONE
            MotionEvent.ACTION_MOVE -> if (mode == Mode.DRAG) {
                onImageDragged(event, iv)
            } else if (mode == Mode.ZOOM) {
                val newDist = spacing(event)
                if (newDist > 10f) {
                    onImageScaled(iv, newDist)
                }
            }
        }

        // CropImageView의 onDraw로 넘어감.
        iv.imageMatrix = matrix
        return true
    }

    private fun onImageScaled(view: ImageView, newDist: Float) {
        var scale = newDist / fingersDistance
        val matrixParams: MatrixParams = MatrixParams.Companion.fromMatrix(savedMatrix)
        Log.d(
            "imageSizeTest",
            "view.getDrawable().getIntrinsicWidth() : " + view.drawable.intrinsicWidth
        )
        Log.d(
            "imageSizeTest",
            "view.getDrawable().getIntrinsicHeight() : " + view.drawable.intrinsicHeight
        )


        // 이미지의 크기가 minimum size보다 크면
        if (view.drawable.intrinsicHeight > CropParams.Companion.minImageSize && view.drawable.intrinsicWidth > CropParams.Companion.minImageSize) {
            // 이미지뷰에 저장된 이미지의 크기
            val imageWidth = view.drawable.intrinsicWidth * matrixParams.scaleWidth
            val imageHeight = view.drawable.intrinsicHeight * matrixParams.scaleHeight


            // scaleCenterPoint -> 손가락 두개 위치 가운데 Point
            val savedDistanceLeft = scaleCenterPoint.x - matrixParams.x
            val savedDistanceRight = matrixParams.x + imageWidth - scaleCenterPoint.x
            val savedDistanceTop = scaleCenterPoint.y - matrixParams.y
            val savedDistanceBottom = matrixParams.y + imageHeight - scaleCenterPoint.y


            // getLeftBound -> 0
            val imageLeft = scaleCenterPoint.x - savedDistanceLeft * scale
            if (imageLeft > circle!!.leftBound) {
                scale = (scaleCenterPoint.x - circle!!.leftBound) / savedDistanceLeft
            }


            // getRightBound -> getWidth
            val imageRight = scaleCenterPoint.x + savedDistanceRight * scale
            if (imageRight < circle!!.rightBound) {
                scale = (circle!!.rightBound - scaleCenterPoint.x) / savedDistanceRight
            }


            // getTopBound -> 0
            val imageTop = scaleCenterPoint.y - savedDistanceTop * scale
            if (imageTop > circle!!.topBound) {
                scale = (scaleCenterPoint.y - circle!!.topBound) / savedDistanceTop
            }


            // getBottomBound -> getHeight
            val imageBottom = scaleCenterPoint.y + savedDistanceBottom * scale
            if (imageBottom < circle!!.bottomBound) {
                scale = (circle!!.bottomBound - scaleCenterPoint.y) / savedDistanceBottom
            }
            val currentScale = scale * matrixParams.scaleWidth
            if (currentScale > CropParams.Companion.maxZoom) {
                scale = CropParams.Companion.maxZoom / matrixParams.scaleWidth
            }
            matrix.set(savedMatrix)
            matrix.postScale(scale, scale, scaleCenterPoint.x, scaleCenterPoint.y)
        } else {
        }
    }

    private fun onImageDragged(event: MotionEvent, view: ImageView) {
        matrix.set(savedMatrix)
        var translationX = event.x - firstTouchPoint.x
        var translationY = event.y - firstTouchPoint.y
        val matrixValues = FloatArray(9)
        savedMatrix.getValues(matrixValues)
        val scaleWidth = matrixValues[0]
        val scaleHeight = matrixValues[4]
        val x = matrixValues[2]
        val y = matrixValues[5]
        val circleCenterX = view.width / 2
        val circleCenterY = view.height / 2
        val circleRadius: Int = CropParams.Companion.circleRadius
        val imageWidth: Int = CropParams.Companion.ImageWidth

        // 이미지뷰에 저장된 이미지의 크기
        val width = view.drawable.intrinsicWidth * scaleWidth
        val height = view.drawable.intrinsicHeight * scaleHeight
        Log.d("onImageDragged", "translationX : $translationX")
        Log.d("onImageDragged", "x : $x")
        Log.d("onImageDragged", "circleCenterX : $circleCenterX")
        Log.d("onImageDragged", "circleRadius : $circleRadius")
        if (translationX + x > circleCenterX - imageWidth) {
            translationX = circleCenterX - imageWidth - x
        } else if (translationX + x + width < circleCenterX + imageWidth) {
            translationX = circleCenterX + imageWidth - x - width
        }
        if (translationY + y > circleCenterY - circleRadius) {
            translationY = circleCenterY - circleRadius - y
        } else if (translationY + y + height < circleCenterY + circleRadius) {
            translationY = circleCenterY + circleRadius - y - height
        }
        matrix.postTranslate(translationX, translationY)
    }

    /** Determine the space between the first two fingers  */
    private fun spacing(event: MotionEvent): Float {
        val x = event.getX(0) - event.getX(1)
        val y = event.getY(0) - event.getY(1)
        return Math.sqrt(x.toDouble() * x + y * y).toFloat()
    }

    /** Calculate the scaleCenterPoint point of the first two fingers  */
    private fun midPoint(point: PointF, event: MotionEvent) {
        val x = event.getX(0) + event.getX(1)
        val y = event.getY(0) + event.getY(1)
        point[x / 2] = y / 2
    }

    private enum class Mode {
        NONE, DRAG, ZOOM
    }
}