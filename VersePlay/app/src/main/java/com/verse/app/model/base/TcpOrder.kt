package com.verse.app.model.base

/**
 * Description : TCP 통신중 데이터 가공할때 필요한 annotation class
 * Type 을 제외한 순서는 1 부터 시작
 * Created by juhongmin on 2023/06/16
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class TcpOrder(val num: Int = 0)