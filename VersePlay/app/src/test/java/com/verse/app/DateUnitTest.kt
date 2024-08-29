package com.verse.app

import org.junit.Test
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Description :
 *
 * Created by juhongmin on 2023/06/17
 */
class DateUnitTest {

    @Test
    fun 날자_포멧_테스트(){
        val date = SimpleDateFormat("YYYY년 MM월 dd일 E요일",Locale.KOREA)
        val format = DateFormat.getDateInstance(DateFormat.FULL)
        println("기본 포멧 ${format.format(Date())}")
        println("좀더 커스텀한 방식 ${date.format(Date())}")
    }
}