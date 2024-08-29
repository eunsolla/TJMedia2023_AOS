package com.verse.app.utility.provider

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHeadset
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.verse.app.BuildConfig
import com.verse.app.utility.DLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Description : Superpowered SDK Provider
 *
 * Created by jhlee on 2023-01-01
 */
interface SuperpoweredProvider {

    fun initialize(isRecord: Boolean)
    fun openMRFile(path: String, fileOffset: Int, fileLength: Int)
    fun openVoiceFile(path: String, fileOffset: Int, fileLength: Int)
    fun onStartRecord(tmpPath: String, resultPath: String)
    fun onStopRecord()
    fun onPause()
    fun onBackGround()
    fun onForeGround()
    fun clearAll()
    fun togglePlayback(): Boolean
    fun isRecordStop(): Boolean
    fun isFinish(): Boolean
    fun getOpenState(): Int
    fun getMrVolume(): Float
    fun getVoiceVolume(): Float
    fun getCurrentMs(): Double
    fun getCurrentPercent(): Float
    fun getTotalTime(): Double
    fun setMrVolume(volume: Float)
    fun setVoiceVolume(volume: Float)
    fun setRecord(tempPath: String)
    fun setEnableVoice(isVoice: Boolean)
    fun setCurrentVoiceMs(voiceMs: Double, startMs: Double)
    fun setCurrentMs(currentMs: Double)
    fun setCurrentMrAndVoiceMs(currentMs: Double, currentVoice: Double)
    fun setFilterType(pos: Int)
    fun isVoiceOn(): Boolean
    fun isPlaying(): Boolean
    fun setJumpToMr(ms: Double)
    fun getSampleRate() : String
    fun checkOpenMrVoice()
    fun getLastLog()
    suspend fun runMixFilterProcess(voicePath: String, mrPath: String, outputPath: String, filterType: Int, syncMs: Double, sampleRate: Int, startMs: Double, mrVolume: Float, voiceVolume: Float): Boolean
}

class SuperpoweredProviderImpl @Inject constructor(
    @ApplicationContext
    val context: Context,
    private val deviceProvider: DeviceProvider,
) : SuperpoweredProvider {

    var isEarphoneOn = false

    companion object {

        const val SAMPLE_RATE = "48000"
        const val BUFFER_SIZE = "480"

        init {
            System.loadLibrary("JInterface")
            System.loadLibrary("SPMixAudio")
        }
    }


    /**
     * @param isRecord ( true : 녹음 ON, false : 녹음 OFF
     */
    override fun initialize(isRecord: Boolean) {
        DLogger.d("SP initialize getOutputSampleRate : ${deviceProvider.getOutputSampleRate().toInt()} / getOutputFramesPerBuffer :  ${deviceProvider.getOutputFramesPerBuffer().toInt()}")

//        var samplerateString = deviceProvider.getOutputSampleRate()
//        var buffersizeString = deviceProvider.getOutputFramesPerBuffer()
//
//        if (samplerateString.isNullOrEmpty()) samplerateString = "48000"
//        if (buffersizeString.isNullOrEmpty()) buffersizeString = "480"

        var samplerateString = SAMPLE_RATE
        var buffersizeString = BUFFER_SIZE

        MakeSPInterface(BuildConfig.SUPER_POWERED_LICENSE_KEY, samplerateString.toInt(), buffersizeString.toInt(), isRecord)
    }

    override fun getSampleRate(): String {
        return SAMPLE_RATE
    }

    override fun openMRFile(path: String, fileOffset: Int, fileLength: Int) {
        OpenMRFile(path, fileOffset, fileLength)
    }

    override fun openVoiceFile(path: String, fileOffset: Int, fileLength: Int) {
        OpenVoiceFile(path, fileOffset, fileLength)
    }

    override fun onStartRecord(tmpPath: String, resultPath: String) {
        setRecord(tmpPath)
        StartRecord(resultPath)
    }

    override fun isVoiceOn(): Boolean {
        return CheckVoice()
    }

    override fun onStopRecord() {
        StopRecord()
    }

    override fun onPause() {
        OnPause()
    }

    override fun onBackGround() {
        OnBackGround()
    }

    override fun onForeGround() {
        OnForeGround()
    }

    override fun clearAll() {
        ClearAudio()
    }

    override fun togglePlayback(): Boolean {
        return TogglePlayback()
    }

    override fun isRecordStop(): Boolean {
        return GetRecordStoped()
    }

    override fun isFinish(): Boolean {
        return GetFinishPlay()
    }

    override fun getOpenState(): Int {
        return GetOpenState()
    }

    override fun getMrVolume(): Float {
        return GetMrVolume()
    }

    override fun getVoiceVolume(): Float {
        return GetVoiceVolume()
    }

    override fun getCurrentMs(): Double {
        return GetCurrentMs()
    }

    override fun getCurrentPercent(): Float {
        return GetCurrentPercent()
    }

    override fun getTotalTime(): Double {
        return GetTotalTime()
    }

    override fun setMrVolume(volume: Float) {
        SetMrVolume(volume)
    }

    override fun setVoiceVolume(volume: Float) {
        SetVoiceVolume(volume)
    }

    override fun setRecord(tempPath: String) {
        SetRecord(tempPath)
    }

    override fun setEnableVoice(isVoice: Boolean) {
        DLogger.d("setEnableVoice  voiceOnOff ${isVoice}")
        isEarphoneOn = isVoice
        EnableVoice(isVoice)
    }

    override fun setCurrentVoiceMs(voiceMs: Double, startMs: Double) {
        SetCurrentVoiceMs(voiceMs, startMs)
    }

    override fun setCurrentMs(currentMs: Double) {
        SetCurrentMrMs(currentMs)
    }

    override fun setCurrentMrAndVoiceMs(currentMs: Double, currentVoice: Double) {
        SetCurrentMrAndVoiceMs(currentMs, currentVoice)
    }

    override fun setFilterType(pos: Int) {
        SetFilterType(pos)
    }

    override fun isPlaying(): Boolean {
        return GetisPlaying()
    }

    override fun setJumpToMr(ms: Double) {
        SetPlaySynchronizedToPosition(ms)
    }

    override fun checkOpenMrVoice() {
        GetOpenState()
    }

    override fun getLastLog() {
        GetLastLog()
    }

    override suspend fun runMixFilterProcess(voicePath: String, mrPath: String, outputPath: String, filterType: Int, syncMs: Double, sampleRate: Int, startMs: Double, mrVolume: Float, voiceVolume: Float): Boolean {
        return mixFilterProcess(voicePath, mrPath, outputPath, filterType, syncMs, sampleRate, startMs, mrVolume, voiceVolume)
    }

    /**
     * = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =
     *  Superpowered JNI Interface
     *  = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =
     */
    private external fun MakeSPInterface(licenseKey: String, sampleRate: Int, buffSize: Int, isRecord: Boolean)
    private external fun OpenMRFile(path: String, fileOffset: Int, fileLength: Int)
    private external fun OpenVoiceFile(path: String, fileOffset: Int, fileLength: Int)
    private external fun TogglePlayback(): Boolean
    private external fun SetRecord(tempPath: String)
    private external fun StopRecord()
    private external fun StartRecord(savePath: String)
    private external fun EnableVoice(value: Boolean)
    private external fun GetCurrentMs(): Double
    private external fun GetCurrentPercent(): Float
    private external fun GetTotalTime(): Double
    private external fun ClearAudio()
    private external fun GetOpenState(): Int
    private external fun GetRecordStoped(): Boolean
    private external fun GetFinishPlay(): Boolean
    private external fun SetVoiceVolume(volume: Float)
    private external fun GetVoiceVolume(): Float
    private external fun SetMrVolume(volume: Float)
    private external fun GetMrVolume(): Float
    private external fun OnPause()
    private external fun GetisPlaying(): Boolean
    private external fun SetCurrentMrMs(currentMs: Double)
    private external fun SetCurrentMrAndVoiceMs(mrMs: Double, voiceMs: Double)
    private external fun SetCurrentVoiceMs(voiceMs: Double, startMs: Double)
    private external fun mixFilterProcess(voicePath: String, mrPath: String, outputPath: String, filterType: Int, syncMs: Double, sampleRate: Int, startMs: Double, mrVolume: Float, voiceVolume: Float): Boolean
    private external fun CheckVoice(): Boolean
    private external fun OnBackGround()
    private external fun OnForeGround()
    private external fun SetFilterType(type: Int)
    private external fun SetPlaySynchronizedToPosition(ms: Double)
    private external fun GetLastLog()

}