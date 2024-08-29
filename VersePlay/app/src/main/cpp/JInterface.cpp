#include <jni.h>
#include <string>
#include "SPInterface.h"

static SPInterface *spInterface = nullptr;

extern "C"
JNIEXPORT void JNICALL
Java_com_verse_app_utility_provider_SuperpoweredProviderImpl_MakeSPInterface(JNIEnv *env, jobject thiz,
                                                                    jstring licenseKey,
                                                                    jint samplerate, jint buffersize,
                                                                    jboolean is_record
) {
    if (spInterface == nullptr){
        const char *key = env->GetStringUTFChars(licenseKey, 0);
        spInterface = new SPInterface(key,(unsigned int) samplerate, (unsigned int) buffersize, is_record);
    }

}

extern "C"
JNIEXPORT void JNICALL
Java_com_verse_app_utility_provider_SuperpoweredProviderImpl_OpenMRFile(JNIEnv *env, jobject thiz, jstring path_,
                                                               jint file_offset, jint file_length) {
    const char *path = env->GetStringUTFChars(path_, 0);

    if (spInterface != nullptr) {
        spInterface->MrOpen(path, file_offset, file_length);
    }

    env->ReleaseStringUTFChars(path_, path);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_verse_app_utility_provider_SuperpoweredProviderImpl_OpenVoiceFile(JNIEnv *env, jobject thiz, jstring path_,
                                                                  jint file_offset, jint file_length) {
    const char *path = env->GetStringUTFChars(path_, 0);

    if (spInterface != nullptr) {
        spInterface->VoiceOpen(path, file_offset, file_length);
    }

    env->ReleaseStringUTFChars(path_, path);
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_verse_app_utility_provider_SuperpoweredProviderImpl_TogglePlayback(JNIEnv *env, jobject thiz) {

    if (spInterface != nullptr) {
        return spInterface->TogglePlayback();
    }

    return false;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_verse_app_utility_provider_SuperpoweredProviderImpl_SetRecord(JNIEnv *env, jobject thiz,
                                                              jstring temp_path) {
    const char *tempPath = env->GetStringUTFChars(temp_path, 0);

    if (spInterface != nullptr) {
        spInterface->SetRecorder(tempPath);
    }

    env->ReleaseStringUTFChars(temp_path, tempPath);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_verse_app_utility_provider_SuperpoweredProviderImpl_StartRecord(JNIEnv *env, jobject thiz,
                                                                jstring save_path) {
    const char *savePath = env->GetStringUTFChars(save_path, 0);

    if (spInterface != nullptr) {
        spInterface->RecordStart(savePath);
    }

    env->ReleaseStringUTFChars(save_path, savePath);
}

extern "C"
JNIEXPORT jdouble JNICALL
Java_com_verse_app_utility_provider_SuperpoweredProviderImpl_GetCurrentMs(JNIEnv *env, jobject thiz) {
    if (spInterface != nullptr) {
        return spInterface->getCurrentMs();
    }

    return 0;
}

extern "C"
JNIEXPORT jfloat JNICALL
Java_com_verse_app_utility_provider_SuperpoweredProviderImpl_GetCurrentPercent(JNIEnv *env, jobject thiz) {
    if (spInterface != nullptr) {
        return spInterface->getCurrentPersent();
    }

    return 0;
}

extern "C"
JNIEXPORT jdouble JNICALL
Java_com_verse_app_utility_provider_SuperpoweredProviderImpl_GetTotalTime(JNIEnv *env, jobject thiz) {
    if (spInterface != nullptr) {
        return spInterface->getTotalMs();
    }

    return 0.0;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_verse_app_utility_provider_SuperpoweredProviderImpl_StopRecord(JNIEnv *env, jobject thiz) {
    if (spInterface != nullptr) {
        spInterface->RecordStop();
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_verse_app_utility_provider_SuperpoweredProviderImpl_ClearAudio(JNIEnv *env, jobject thiz) {
    if (spInterface != nullptr) {
        spInterface->clear();

        delete spInterface;
        spInterface = nullptr;
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_verse_app_utility_provider_SuperpoweredProviderImpl_EnableVoice(JNIEnv *env, jobject thiz,
                                                                jboolean value) {
    if (spInterface != nullptr) {
        spInterface->voiceOnOff(value);
    }
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_verse_app_utility_provider_SuperpoweredProviderImpl_GetRecordStoped(JNIEnv *env, jobject thiz) {
    if (spInterface != nullptr) {
        return spInterface->checkRecordStop();
    }

    return false;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_verse_app_utility_provider_SuperpoweredProviderImpl_GetFinishPlay(JNIEnv *env, jobject thiz) {
    if (spInterface != nullptr) {
        return spInterface->checkMrPlayFinish();
    }

    return false;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_verse_app_utility_provider_SuperpoweredProviderImpl_SetVoiceVolume(JNIEnv *env, jobject thiz,
                                                                   jfloat volume) {
    if (spInterface != nullptr) {
        spInterface->setVoiceVolume(volume);
    }
}

extern "C"
JNIEXPORT jfloat JNICALL
Java_com_verse_app_utility_provider_SuperpoweredProviderImpl_GetVoiceVolume(JNIEnv *env, jobject thiz) {
    if (spInterface != nullptr) {
        return spInterface->getVoiceVolume();
    }

    return 0;
}


extern "C"
JNIEXPORT void JNICALL
Java_com_verse_app_utility_provider_SuperpoweredProviderImpl_SetMrVolume(JNIEnv *env, jobject thiz,
                                                                jfloat volume) {
    if (spInterface != nullptr) {
        spInterface->setMrVolume(volume);
    }
}


extern "C"
JNIEXPORT jfloat JNICALL
Java_com_verse_app_utility_provider_SuperpoweredProviderImpl_GetMrVolume(JNIEnv *env, jobject thiz) {
    if (spInterface != nullptr) {
        return spInterface->getMrVolume();
    }

    return 0;
}



extern "C"
JNIEXPORT jint JNICALL
Java_com_verse_app_utility_provider_SuperpoweredProviderImpl_GetOpenState(JNIEnv *env, jobject thiz) {
    if (spInterface != nullptr) {
        return spInterface->checkOpenMrVoice();
    }

    return -1;
}


extern "C"
JNIEXPORT void JNICALL
Java_com_verse_app_utility_provider_SuperpoweredProviderImpl_OnPause(JNIEnv *env, jobject thiz) {
    if (spInterface != nullptr) {
        spInterface->OnPause();
    }
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_verse_app_utility_provider_SuperpoweredProviderImpl_GetisPlaying(JNIEnv *env, jobject thiz) {
    if (spInterface != nullptr) {
        return spInterface->isPlaying();
    }

    return false;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_verse_app_utility_provider_SuperpoweredProviderImpl_SetPlaySynchronizedToPosition(JNIEnv *env, jobject thiz, jdouble ms) {
    if (spInterface != nullptr) {
        spInterface->setPlaySynchronizedToPosition(ms);
    }
}
extern "C"
JNIEXPORT void JNICALL
Java_com_verse_app_utility_provider_SuperpoweredProviderImpl_SetLoopBetween(JNIEnv *env, jobject thiz, jdouble startMs, jdouble endMs, jint numLoops) {
    if (spInterface != nullptr) {
        spInterface->setLoopBetween(startMs, endMs, numLoops);
    }
}
extern "C"
JNIEXPORT void JNICALL
Java_com_verse_app_utility_provider_SuperpoweredProviderImpl_SetExitLoop(JNIEnv *env, jobject thiz, jboolean value) {
    if (spInterface != nullptr) {
        spInterface->setExitLoop(value);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_verse_app_utility_provider_SuperpoweredProviderImpl_SetCurrentMrMs(JNIEnv *env, jobject thiz,
                                                                   jdouble current_ms) {
    if (spInterface != nullptr) {
        spInterface->setCurrentMrMs(current_ms);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_verse_app_utility_provider_SuperpoweredProviderImpl_SetCurrentMrAndVoiceMs(JNIEnv *env, jobject thiz,
                                                                           jdouble current_ms, jdouble voice_ms) {
    if (spInterface != nullptr) {
        spInterface->setCurrentMrAndMs(current_ms, voice_ms);
    }
}


extern "C"
JNIEXPORT void JNICALL
Java_com_verse_app_utility_provider_SuperpoweredProviderImpl_SetCurrentVoiceMs(JNIEnv *env, jobject thiz,
                                                                      jdouble voice_ms, jdouble start_ms) {
    if (spInterface != nullptr) {
        spInterface->setCurrentVoiceMs(voice_ms, start_ms);
    }
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_verse_app_utility_provider_SuperpoweredProviderImpl_CheckVoice(JNIEnv *env, jobject thiz) {
    if (spInterface != nullptr) {
        return spInterface->getCheckVoice();
    }

    return false;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_verse_app_utility_provider_SuperpoweredProviderImpl_OnBackGround(JNIEnv *env, jobject thiz) {
    if (spInterface != nullptr) {
        spInterface->onBackGround();
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_verse_app_utility_provider_SuperpoweredProviderImpl_OnForeGround(JNIEnv *env, jobject thiz) {
    if (spInterface != nullptr) {
        spInterface->onForeGround();
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_verse_app_utility_provider_SuperpoweredProviderImpl_SetFilterType(JNIEnv *env, jobject thiz, jint type) {
    if (spInterface != nullptr) {
        spInterface->setFilterType(type);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_verse_app_utility_provider_SuperpoweredProviderImpl_GetLastLog(JNIEnv *env, jobject thiz) {
    if (spInterface != nullptr) {
         spInterface->getLastLog();
    }
}

