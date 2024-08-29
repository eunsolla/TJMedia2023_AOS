package com.verse.app

import org.junit.Test

/**
 * Description :
 *
 * Created by juhongmin on 2023/06/27
 */
class CheckNicknameUniTest {

    @Test
    fun 닉네임_유효성_검사(){
        // checkRegNickName
        // 1. 특수문자,공백,이모지 입력안됨
        //2. 금칙어 입력후 중복검사시 fgProhibitYn 이 Y일때
        var text = "😭이모짐오모지모지오밎#@!@#$%eeefef"
        assert(!isNickNameValidate(text))

        text = "ishifttest"
        assert(isNickNameValidate(text))
    }

    private fun isNickNameValidate(str: String) : Boolean{
        // if (str.contains(" ")) return false
        // .*[a-zA-Z가-힣]+.*
        val regex = "^[0-9|a-z|A-Z|ㄱ-ㅎ|ㅏ-ㅣ|가-힣]*\$".toRegex()
        return regex.matches(str)
    }
}