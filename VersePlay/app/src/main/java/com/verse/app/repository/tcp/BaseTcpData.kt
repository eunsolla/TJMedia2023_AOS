package com.verse.app.repository.tcp

import com.verse.app.contants.TcpHeaderType
import com.verse.app.model.base.TcpOrder
import java.lang.reflect.Field

/**
 * Description : TCP Data
 *
 * Created by juhongmin on 2023/06/12
 */
open class BaseTcpData(val type: TcpHeaderType) {
    fun classToStr(separator: Char = '|'): String {
        val buffer = StringBuffer()
        if (javaClass.superclass.isAssignableFrom(BaseTcpData::class.java)) {
            val field = javaClass.superclass.declaredFields[0]
            if (!field.isAccessible) field.isAccessible = true
            val type = field.get(this)
            if (type is TcpHeaderType) {
                buffer.append(type.key)
                buffer.append(separator)
            }
        }

        val fieldList = arrayListOf<Pair<Int, Field>>()
        javaClass.declaredFields.forEach {
            if (it.isAnnotationPresent(TcpOrder::class.java)) {
                fieldList.add((it.getAnnotation(TcpOrder::class.java)?.num ?: 0) to it)
            }
        }
        fieldList.sortedBy { it.first }.forEach { pair ->
            val field = pair.second
            if (!field.isAccessible) field.isAccessible = true
            buffer.append(field.get(this))
            buffer.append(separator)
        }
        // 마지막 '|' 제거.
        if (buffer.last() == separator) {
            buffer.deleteCharAt(buffer.lastIndexOf(separator))
        }
        return buffer.toString()
    }
}
