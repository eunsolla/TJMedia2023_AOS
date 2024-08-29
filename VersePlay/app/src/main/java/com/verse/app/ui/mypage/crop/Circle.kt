package com.verse.app.ui.mypage.crop

class Circle(var cx: Int, var cy: Int, var circleRadius: Int, var imgWidth: Int) {
    val diameter: Int
        get() = radius * 2
    val leftBound: Int
        get() = cx - imgWidth
    val rightBound: Int
        get() = cx + imgWidth
    val topBound: Int
        get() = cy - radius
    val bottomBound: Int
        get() = cy + radius
    val radius: Int = circleRadius
}