package com.verse.app.ui.mypage.crop

import android.graphics.Matrix

class MatrixParams {
    var x = 0f
        private set
    var y = 0f
        private set
    var scaleWidth = 0f
        private set
    var scaleHeight = 0f
        private set

    companion object {
        fun fromMatrix(matrix: Matrix): MatrixParams {
            val matrixValues = FloatArray(9)
            matrix.getValues(matrixValues)

//        value[2]. value[5] = x,y 이동거리
//        value[0], value[4] = x,y 확대비율 -> Device에 나타날 때, 실제크기에 비해 확대 및 축소된 비율
            val matrixParams = MatrixParams()
            matrixParams.x = matrixValues[2]
            matrixParams.y = matrixValues[5]
            matrixParams.scaleWidth = matrixValues[0]
            matrixParams.scaleHeight = matrixValues[4]
            return matrixParams
        }
    }
}