package com.verse.app.ui.sing.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.verse.app.base.viewmodel.ActivityViewModel
import com.verse.app.contants.ExtraCode
import com.verse.app.extension.applyApiScheduler
import com.verse.app.livedata.SingleLiveEvent
import com.verse.app.model.xtf.XTF_DTO
import com.verse.app.repository.preferences.AccountPref
import com.verse.app.utility.DLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.kotlin.addTo
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import javax.inject.Inject


/**
 * Description : Section ViewModel
 *
 * Created by jhlee on 2023-03-29
 */
@HiltViewModel
class SectionViewModel @Inject constructor(
    @ApplicationContext
    val context: Context,
    private val accountPref: AccountPref,
) : ActivityViewModel() {

    private val _startConfirm: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }
    val startConfirm: LiveData<Unit> get() = _startConfirm

    private val _startCloseEvent: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }
    val startCloseEvent: LiveData<Unit> get() = _startCloseEvent

    private val _lyricsData: MutableLiveData<XTF_DTO> by lazy { MutableLiveData() }
    val lyricsData: LiveData<XTF_DTO> get() = _lyricsData

    private val _curIndexInfo: MutableLiveData<Pair<Int, Int>> by lazy { MutableLiveData() }
    val curIndexInfo: LiveData<Pair<Int, Int>> get() = _curIndexInfo

    private val _songInfo: MutableLiveData<Pair<String, String>> by lazy { MutableLiveData() }
    val songInfo: LiveData<Pair<String, String>> get() = _songInfo

    private val _isGuide: SingleLiveEvent<Boolean> by lazy { SingleLiveEvent() }
    val isGuide: LiveData<Boolean> get() = _isGuide

    private val _bg: MutableLiveData<String> by lazy { MutableLiveData() }
    val bg: LiveData<String> get() = _bg


    fun start() {

        _isGuide.value = accountPref.istSectionGuidePage()

        if (!accountPref.istSectionGuidePage()) {
            accountPref.setSectionGuidePage(true)
        }

        val xtfPath: String? = savedStateHandle[ExtraCode.SECTION_DTO_DATA]
        val curIndexInfo: Pair<Int, Int>? = savedStateHandle[ExtraCode.SECTION_INDEX_INFO]
        val songInfo: Pair<String, String>? = savedStateHandle[ExtraCode.SECTION_SONG_INFO]
        val bg: String? = savedStateHandle[ExtraCode.SECTION_SONG_BG]

        if (xtfPath.isNullOrEmpty()) {
            _startCloseEvent.call()
            return
        }

        _curIndexInfo.value = curIndexInfo
        _songInfo.value = songInfo
        _bg.value = bg

        parsingSection(xtfPath)

    }

    private fun parsingSection(path: String) {

        Single.just(path)
            .map { path ->
                runCatching {
                    File(path).let { file ->
                        FileInputStream(file).let { inputStream ->
                            val reader =
                                BufferedReader(InputStreamReader(inputStream)).let { buffer ->
                                    val sb = StringBuilder()
                                    var line: String? = null
                                    while (buffer.readLine().also { line = it } != null) {
                                        sb.append(line).append("\n")
                                    }
                                    buffer.close()
                                    sb.toString()
                                }
                            inputStream.close()
                            reader
                        }
                    }
                }
            }
            .applyApiScheduler()
            .subscribe({ result ->
                result.onSuccess { json ->
                    val decodeData = Json {
                        isLenient = true
                        ignoreUnknownKeys = true
                        coerceInputValues = true
                    }.decodeFromString<XTF_DTO>(json)

                    _lyricsData.value = decodeData
                }
                result.onFailure {
                    DLogger.e("XTF ERROR ${it}")
                    onClose()
                }
            },
                {
                    DLogger.e("XTF ERROR ${it}")
                    _startCloseEvent.call()
                }).addTo(compositeDisposable)
    }

    fun onConfirm() {
        _startConfirm.call()
    }

    fun onClose() {
        _startCloseEvent.call()
    }

    fun onCloseGuide() {
        _isGuide.value = true
    }
}
