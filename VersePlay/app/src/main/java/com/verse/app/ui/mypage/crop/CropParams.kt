package com.verse.app.ui.mypage.crop

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path

class CropParams {
    var circle: Circle? = null
    var shape = CroppedShape.HOLE
    var holeParams: HoleParams? = null
    var squareParams: SquareParams? = null
    var width = 0
    var height = 0
    fun updateWithView(width: Int, height: Int) {
        this.width = width
        this.height = height
        circle = Circle(width / 2, height / 2, circleRadius, ImageWidth)
        holeParams = HoleParams()
        squareParams = SquareParams()
    }

    inner class HoleParams {
        var path: Path? = null
        var paint: Paint

        init {
            setPath()
            paint = Paint()
            paint.color = Color.parseColor("#AA000000")
        }

        private fun setPath() {
            path = Path()
            path!!.fillType = Path.FillType.EVEN_ODD
            path!!.addRect(0f, 0f, width.toFloat(), height.toFloat(), Path.Direction.CW)
            path!!.addCircle(
                circle!!.cx.toFloat(),
                circle!!.cy.toFloat(),
                circle!!.radius.toFloat(),
                Path.Direction.CW
            )
        }
    }

    inner class SquareParams {
        var paint: Paint
        var path: Path? = null

        init {
            setPath()
            paint = Paint()
            paint.color = Color.parseColor("#AA000000")
        }

        fun setPath() {
            path = Path()
            path!!.fillType = Path.FillType.EVEN_ODD
            path!!.addRect(0f, 0f, width.toFloat(), height.toFloat(), Path.Direction.CW)
            path!!.addRect(
                0f,
                (height / 2 - circle!!.radius).toFloat(),
                width.toFloat(),
                (height / 2 + circle!!.radius).toFloat(),
                Path.Direction.CCW
            )
        }
    }

    companion object {
        var circleRadius = 400
        var ImageWidth = 400
        var maxZoom = 4f
        var minImageSize = 200
    }
}