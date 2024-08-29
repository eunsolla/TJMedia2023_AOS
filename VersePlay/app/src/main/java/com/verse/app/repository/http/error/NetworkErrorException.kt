package com.verse.app.repository.http.error


class NetworkErrorException(val code: Int, val msg: String) : Exception()
