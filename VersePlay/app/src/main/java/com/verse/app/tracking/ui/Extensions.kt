package com.verse.app.tracking.ui

import android.util.Base64
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.verse.app.tracking.interceptor.model.*
import com.verse.app.tracking.ui.models.BaseTrackingUiModel
import com.verse.app.tracking.ui.models.TrackingBodyUiModel
import com.verse.app.tracking.ui.models.TrackingHeaderUiModel
import com.verse.app.tracking.ui.models.TrackingMultipartBodyUiModel
import com.verse.app.tracking.ui.models.TrackingQueryUiModel
import java.net.URLDecoder

internal object Extensions {

    // Gson
    private val gson: Gson by lazy {
        GsonBuilder()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .serializeNulls()
            .create()
    }

    /**
     * Header UiModel 변환 처리함수
     */
    fun parseHeaderUiModel(map: Map<String, String>): List<BaseTrackingUiModel> {
        val uiList = mutableListOf<BaseTrackingUiModel>()
        map.forEach { entry ->
            uiList.add(
                TrackingHeaderUiModel(
                    key = entry.key,
                    value = entry.value
                )
            )
        }
        return uiList
    }

    /**
     * Request Url to Query List
     */
    fun toReqQueryList(fullUrl: String?): List<String> {
        if (fullUrl == null) return emptyList()
        val list = mutableListOf<String>()
        val startIdx = fullUrl.indexOf("?")
        if (startIdx != -1) {
            val pathOrQuery = fullUrl.substring(startIdx.plus(1))
            list.addAll(pathOrQuery.split("&"))
        }
        return list
    }

    /**
     * Request Query 값 UiModel 변환 처리 함수
     */
    fun parseQueryUiModel(fullUrl: String?): List<BaseTrackingUiModel> {
        if (fullUrl == null) return emptyList()
        val uiList = mutableListOf<BaseTrackingUiModel>()
        val strBuilder = StringBuilder()
        toReqQueryList(fullUrl).forEach { str ->
            str.runCatching {
                val pair = splitQuery(this)
                if (pair != null) {
                    strBuilder.append(pair.first)
                        .append("=")
                        .append(pair.second)
                        .append("\n")
                }
            }
        }
        if (strBuilder.isNotEmpty()) {
            uiList.add(TrackingQueryUiModel(strBuilder.toString()))
        }
        return uiList
    }

    /**
     * Converter RequestEntity to Request BodyUiModel
     * @param req Base Request Entity
     */
    fun toReqBodyUiModels(req: BaseTrackingRequestEntity?): List<BaseTrackingUiModel> {
        if (req == null) return emptyList()
        val uiList = mutableListOf<BaseTrackingUiModel>()
        if (req is TrackingRequestMultipartEntity) {
            req.binaryList.forEach {
                uiList.add(
                    TrackingMultipartBodyUiModel(
                        mediaType = it.type,
                        binary = Base64.encodeToString(it.bytes, Base64.DEFAULT) ?: ""
                    )
                )
            }
        } else if (req is TrackingRequestEntity) {
            if (!req.body.isNullOrEmpty()) {
                uiList.add(TrackingBodyUiModel(toJsonBody(req.body)))
            }
        }
        return uiList
    }

    /**
     * Converter ResponseEntity to Response BodyUiModel
     * @param body Response String
     */
    fun parseResBodyUiModel(body: String): List<BaseTrackingUiModel> {
        val uiList = mutableListOf<BaseTrackingUiModel>()
        uiList.add(TrackingBodyUiModel(toJsonBody(body)))
        return uiList
    }

    fun toJsonBody(body: String?): String {
        if (body == null) return ""
        return try {
            val je = JsonParser.parseString(body)
            gson.toJson(je)
        } catch (ex: Exception) {
            ""
        }
    }

    /**
     * Split HTTP Query
     * Key=Value
     * @param txt Full Url
     */
    private fun splitQuery(txt: String): Pair<String, String>? {
        val idx = txt.indexOf("=")
        return if (idx != -1) {
            var key = txt.substring(0, idx)
            key = try {
                URLDecoder.decode(key, Charsets.UTF_8.name())
            } catch (ex: UnsupportedOperationException) {
                key
            } catch (ex: IllegalArgumentException) {
                key
            }
            var value = txt.substring(idx.plus(1))
            value = try {
                URLDecoder.decode(value, Charsets.UTF_8.name())
            } catch (ex: UnsupportedOperationException) {
                value
            } catch (ex: IllegalArgumentException) {
                value
            }
            key to value
        } else {
            null
        }
    }
}
