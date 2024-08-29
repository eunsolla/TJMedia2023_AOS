//
// Created by de-cgh on 2019-12-31.
//

#include <jni.h>
#include <string>
#include <SuperpoweredEcho.h>
#include <SuperpoweredReverb.h>
#include <SuperpoweredDecoder.h>
#include <SuperpoweredTimeStretching.h>
#include <SuperpoweredSimple.h>
#include <SuperpoweredMixer.h>
#include <android/log.h>
#include <SuperpoweredFlanger.h>
#include <SuperpoweredAutomaticVocalPitchCorrection.h>
#include <SuperpoweredGuitarDistortion.h>
#include <SuperpoweredCompressor.h>
#include <SuperpoweredWhoosh.h>
#include <Superpowered3BandEQ.h>
#include <SuperpoweredGate.h>


using namespace Superpowered;

const int TYPE_NONE = -1;
const int TYPE_AUTO_TUNE = 0;
const int TYPE_VINYL = 1;
const int TYPE_CONCERT_HALL = 2;
const int TYPE_NORAE_BANG = 3;
const int TYPE_MEGA_PHONE = 4;
const int TYPE_CAVE = 5;
const int TYPE_CHIPMUNK  = 6;
const int TYPE_RECORDING_STUDIO  = 7;
const int TYPE_HELIUM = 8;
const int TYPE_ROBOT = 9;
const int TYPE_GHOST = 10;
const int TYPE_EHCO = 11;
const int TYPE_REVERB = 12;
const int TYPE_VOICE_CHANGER = 13;

typedef struct __list {
    struct __node *head;
    struct __node *tail;
} LinkedList;

typedef struct __node {
    Superpowered::FX *filter;
    struct __node *next;
} Node;

void destroyNode(Node *node) {
    delete node->filter;
    node->filter = NULL;
    free(node);
}

void destroyNodeAll(LinkedList *targetFilter) {
    if (targetFilter->head == NULL) {
        return;
    }

    Node *cur = targetFilter->head;
    while (cur != NULL) {
        Node *temp = cur;
        cur = cur->next;
        destroyNode(temp);
    }
}

bool addNewFilter(LinkedList *targetFilter, Superpowered::FX *tempFilter) {
    Node *newNode = (Node *) malloc(sizeof(Node));
    if (tempFilter == NULL) {
        free(newNode);
        return false;
    }

    tempFilter->enabled = true;

    newNode->filter = tempFilter;
    newNode->next = NULL;

    if (targetFilter->head == NULL && targetFilter->tail == NULL) {
        targetFilter->head = newNode;
        targetFilter->tail = newNode;
    } else {
        targetFilter->tail->next = newNode;
        targetFilter->tail = newNode;
    }

    return true;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_verse_app_utility_provider_SuperpoweredProviderImpl_mixFilterProcess(JNIEnv *env, jobject thiz,
                                                               jstring voice_path,
                                                               jstring mr_path,
                                                               jstring output_path,
                                                               jint filter_type,
                                                               jdouble sync_ms,
                                                               jint sample_rate,
                                                               jdouble start_ms,
                                                               jfloat mrVolume,
                                                               jfloat voiceVolume) {

    const char *voicePath = env->GetStringUTFChars(voice_path, 0);
    const char *mrPath = env->GetStringUTFChars(mr_path, 0);
    const char *outputPath = env->GetStringUTFChars(output_path, 0);

    __android_log_print(ANDROID_LOG_INFO, "native", "[Mix]============================");
    __android_log_print(ANDROID_LOG_INFO, "native", "[Mix Param voice_path] %s ", voicePath);
    __android_log_print(ANDROID_LOG_INFO, "native", "[Mix Param mr_path] %s ", mrPath);
    __android_log_print(ANDROID_LOG_INFO, "native", "[Mix Param output_path %s ", outputPath);
    __android_log_print(ANDROID_LOG_INFO, "native", "[Mix Param filter_type] %d ", filter_type);
    __android_log_print(ANDROID_LOG_INFO, "native", "[Mix Param sync_ms] %f ", sync_ms);
    __android_log_print(ANDROID_LOG_INFO, "native", "[Mix Param sample_rate] %d ", sample_rate);
    __android_log_print(ANDROID_LOG_INFO, "native", "[Mix Param startMs] %f ", start_ms);
    __android_log_print(ANDROID_LOG_INFO, "native", "[Mix Param mrVolume] %f ", mrVolume);
    __android_log_print(ANDROID_LOG_INFO, "native", "[Mix Param voiceVolume] %f ", voiceVolume);
    __android_log_print(ANDROID_LOG_INFO, "native", "[Mix]============================");

    TimeStretching *time = new TimeStretching(sample_rate, 0.5);
    Superpowered::AutomaticVocalPitchCorrection *automaticVocalPitchCorrection = new Superpowered::AutomaticVocalPitchCorrection();
    Decoder *voiceDecoder = new Decoder();
    Decoder *mrDecoder = new Decoder();
    StereoMixer *mixer = new StereoMixer();

    LinkedList *soundEffectFilter = (LinkedList *) malloc(sizeof(LinkedList));
    soundEffectFilter->head = NULL;
    soundEffectFilter->tail = NULL;

    bool isUseTime = false;
    bool isAutomaticVocalPitch = false;

    switch (filter_type) {
        case TYPE_EHCO: {
            Superpowered::Echo *echo = new Superpowered::Echo(sample_rate);
            addNewFilter(soundEffectFilter, echo);
        }
            break;

        case TYPE_REVERB: {
            Superpowered::Reverb *reverb = new Reverb(sample_rate);
            addNewFilter(soundEffectFilter, reverb);
        }
            break;

        case TYPE_HELIUM: {
            isUseTime = true;
            time->pitchShiftCents = 800;
            Superpowered::Echo *heliumFilter_option1 = new Superpowered::Echo(sample_rate);
            heliumFilter_option1->enabled = true;
            heliumFilter_option1->setMix(0.5);
            heliumFilter_option1->bpm = 128.0f;
            heliumFilter_option1->beats = 0.4f;
            heliumFilter_option1->decay = 0.4f;
            addNewFilter(soundEffectFilter, heliumFilter_option1);
        }
            break;

        case TYPE_ROBOT: {
            isUseTime = true;
            time->pitchShiftCents = -500;
            Superpowered::Flanger *robotFilter_option1 = new Superpowered::Flanger(sample_rate);
            robotFilter_option1->enabled = true;
            robotFilter_option1->wet = 1.0f;
            robotFilter_option1->depth = 0.9f;
            robotFilter_option1->lfoBeats = 128.0f;
            robotFilter_option1->bpm = 50.0f;
            robotFilter_option1->clipperThresholdDb = 3;
            robotFilter_option1->clipperMaximumDb = 3;
            addNewFilter(soundEffectFilter, robotFilter_option1);
        }
            break;

        case TYPE_GHOST: {
            isUseTime = true;
            time->pitchShiftCents = 1000;
            Superpowered::Flanger *ghostFilter_option1 = new Superpowered::Flanger(sample_rate);
            ghostFilter_option1->enabled = true;
            ghostFilter_option1->wet = 1.0f;
            ghostFilter_option1->depth = 0.9f;
            ghostFilter_option1->lfoBeats = 128.0f;
            ghostFilter_option1->bpm = 128.0f;
            ghostFilter_option1->clipperThresholdDb = 1;
            ghostFilter_option1->clipperMaximumDb = 3;
            addNewFilter(soundEffectFilter, ghostFilter_option1);

            Superpowered::Reverb *ghostFilter_option2 = new Superpowered::Reverb(sample_rate);
            ghostFilter_option2->enabled = true;
            ghostFilter_option2->dry = 0.5f;
            ghostFilter_option2->wet = 0.8f;
            ghostFilter_option2->mix = 0.9f;
            ghostFilter_option2->width = 1.0f;
            ghostFilter_option2->damp = 0.5f;
            ghostFilter_option2->roomSize = 0.8f;
            ghostFilter_option2->predelayMs = 0.0f;
            ghostFilter_option2->lowCutHz = 0.0f;
            addNewFilter(soundEffectFilter, ghostFilter_option2);
        }
            break;

        case TYPE_VOICE_CHANGER: {
            isUseTime = true;
            time->pitchShiftCents = 500;
            Superpowered::Echo *voiceChangerFilter_option1 = new Superpowered::Echo(sample_rate);
            voiceChangerFilter_option1->enabled = true;
            voiceChangerFilter_option1->setMix(0.5);
            voiceChangerFilter_option1->bpm = 128.0f;
            voiceChangerFilter_option1->beats = 0.4f;
            voiceChangerFilter_option1->decay = 0.4f;
            addNewFilter(soundEffectFilter, voiceChangerFilter_option1);
        }
            break;

        case TYPE_AUTO_TUNE : {
            isAutomaticVocalPitch = true;
            automaticVocalPitchCorrection = new Superpowered::AutomaticVocalPitchCorrection();
            automaticVocalPitchCorrection->samplerate = sample_rate;
            automaticVocalPitchCorrection->scale = Superpowered::AutomaticVocalPitchCorrection::CHROMATIC;

            // Set the vocal range of the singer.
            automaticVocalPitchCorrection->range = Superpowered::AutomaticVocalPitchCorrection::WIDE;
            automaticVocalPitchCorrection->speed = Superpowered::AutomaticVocalPitchCorrection::EXTREME;
            automaticVocalPitchCorrection->frequencyOfA = 440;


            Superpowered::Compressor *autotuneFilter_option1 = new Superpowered::Compressor(sample_rate);
            autotuneFilter_option1->inputGainDb = 0;
            autotuneFilter_option1->outputGainDb = 0;
            autotuneFilter_option1->wet = 1;
            autotuneFilter_option1->attackSec = 0.003;
            autotuneFilter_option1->releaseSec = 0.3;
            autotuneFilter_option1->ratio = 3;
            autotuneFilter_option1->thresholdDb = 0;
            autotuneFilter_option1->hpCutOffHz = 1;
            autotuneFilter_option1->enabled = true;
            addNewFilter(soundEffectFilter, autotuneFilter_option1);

            Superpowered::Reverb *autotuneFilter_option2 = new Superpowered::Reverb(sample_rate);
            autotuneFilter_option2->mix = 0.04;
            autotuneFilter_option2->width = 1;
            autotuneFilter_option2->damp = 0.5;
            autotuneFilter_option2->roomSize = 0.8;
            autotuneFilter_option2->predelayMs = 0;
            autotuneFilter_option2->lowCutHz = 0;
            autotuneFilter_option2->enabled = true;
            addNewFilter(soundEffectFilter, autotuneFilter_option2);

        }
            break;
        case TYPE_VINYL : {
            Superpowered::Whoosh *vinyl =  new Superpowered::Whoosh(sample_rate);
            vinyl->wet = 0.01;
            vinyl->frequency = 3093.53;
            vinyl->enabled = true;
            addNewFilter(soundEffectFilter, vinyl);
        }
            break;
        case TYPE_CONCERT_HALL : {
            Superpowered::Reverb *concertHall = new Reverb(sample_rate);
            concertHall->mix = 0.55;
            concertHall->width = 1;
            concertHall->damp = 0.6;
            concertHall->roomSize = 0.71;
            concertHall->predelayMs = 140;
            concertHall->lowCutHz = 100;
            concertHall->enabled = true;
            addNewFilter(soundEffectFilter, concertHall);
        }
            break;
        case TYPE_NORAE_BANG : {
            Superpowered::Echo *noraebang = new Echo(sample_rate);
            noraebang->dry = 0.5;
            noraebang->wet = 1;
            noraebang->bpm = 140;
            noraebang->beats = 0.5;
            noraebang->decay = 0.45;
            noraebang->enabled = true;
            addNewFilter(soundEffectFilter, noraebang);
        }
            break;
        case TYPE_MEGA_PHONE  : {
            Superpowered::GuitarDistortion *megaPhone = new GuitarDistortion(sample_rate);
            megaPhone->gainDecibel = -20;
            megaPhone->drive = 0;
            megaPhone->bassFrequency = 139.68;
            megaPhone->trebleFrequency = 16289.76;
            megaPhone->eq80HzDecibel = 0;
            megaPhone->eq240HzDecibel = 0;
            megaPhone->eq750HzDecibel = 8.64;
            megaPhone->eq2200HzDecibel = 0;
            megaPhone->eq6600HzDecibel = 0;
            megaPhone->distortion0 = false;
            megaPhone->distortion1 = true;
            megaPhone->marshall = true;
            megaPhone->enabled = true;
            addNewFilter(soundEffectFilter, megaPhone);
        }
            break;
        case TYPE_CAVE  : {
            Superpowered::Reverb *cave = new Reverb(sample_rate);
            cave->mix = 0.79;
            cave->width = 1;
            cave->damp = 0.6;
            cave->roomSize = 0.75;
            cave->predelayMs = 0;
            cave->lowCutHz = 100;
            cave->enabled = true;
            addNewFilter(soundEffectFilter, cave);
        }
            break;
        case TYPE_CHIPMUNK   : {
            isUseTime = true;
            time->pitchShiftCents = 1200;
            Superpowered::ThreeBandEQ *chipmunk = new Superpowered::ThreeBandEQ(sample_rate);
            chipmunk->low = 1.22;
            chipmunk->mid = 1.4;
            chipmunk->high = 1.4;
            chipmunk->enabled = true;
            addNewFilter(soundEffectFilter, chipmunk);
        }
            break;
        case TYPE_RECORDING_STUDIO   : {
            Superpowered::Reverb *recordingStudio = new Reverb(sample_rate);
            recordingStudio->mix = 0.5;
            recordingStudio->width = 1;
            recordingStudio->damp = 0.6;
            recordingStudio->roomSize = 0.6;
            recordingStudio->predelayMs = 0;
            recordingStudio->lowCutHz = 100;
            recordingStudio->enabled = true;
            addNewFilter(soundEffectFilter, recordingStudio);
        }
            break;
        default: {
            __android_log_print(ANDROID_LOG_INFO, "native", "필터 없음");
            time->pitchShiftCents = 0;
        }
            break;
    }

    if (voiceDecoder->open(voicePath, false) || mrDecoder->open(mrPath, false)) {
        delete voiceDecoder;
        delete mrDecoder;
        delete time;
        delete mixer;
        delete automaticVocalPitchCorrection;
        if (soundEffectFilter != NULL) {
            destroyNodeAll(soundEffectFilter);
            free(soundEffectFilter);
            soundEffectFilter = NULL;
        }

        env->ReleaseStringUTFChars(voice_path, voicePath);
        env->ReleaseStringUTFChars(mr_path, mrPath);
        env->ReleaseStringUTFChars(output_path, outputPath);
        return false;
    }

    FILE *fd = createWAV(outputPath, mrDecoder->getSamplerate(), 2);
    if (!fd) {
        delete voiceDecoder;
        delete mrDecoder;
        delete time;
        delete mixer;
        delete fd;
        delete automaticVocalPitchCorrection;
        if (soundEffectFilter != NULL) {
            destroyNodeAll(soundEffectFilter);
            free(soundEffectFilter);
            soundEffectFilter = NULL;
        }

        env->ReleaseStringUTFChars(voice_path, voicePath);
        env->ReleaseStringUTFChars(mr_path, mrPath);
        env->ReleaseStringUTFChars(output_path, outputPath);
        return false;
    }

    //움성 싱크
    if (sync_ms > 0) {
        double secondSync = sync_ms / 1000;
        int syncGap = (int)(secondSync * sample_rate);
        voiceDecoder->setPositionQuick(syncGap);
        __android_log_print(ANDROID_LOG_INFO, "native", "음성싱크 적용 %f / %f / %d  / %d", sync_ms,secondSync , syncGap , sample_rate);
    }else{
        __android_log_print(ANDROID_LOG_INFO, "native", "음성싱크 NONO");
    }

    short int *intVoiceBuffer = (short int *) malloc(voiceDecoder->getFramesPerChunk() * 2 * sizeof(short int) + 16384);
    short int *intMrBuffer = (short int *) malloc(mrDecoder->getFramesPerChunk() * 2 * sizeof(short int) + 16384);
    short int *intOutputBuffer = (short int *) malloc(voiceDecoder->getFramesPerChunk() * 2 * sizeof(short int) + 16384);

    float *floatVoiceBuffer = (float *) malloc(voiceDecoder->getFramesPerChunk() * 2 * sizeof(float) + 1024);
    float *floatMrBuffer = (float *) malloc(mrDecoder->getFramesPerChunk() * 2 * sizeof(float) + 1024);
    float *floatOutputBuffer = (float *) malloc(voiceDecoder->getFramesPerChunk() * 2 * sizeof(float) + 1024);

    unsigned int voiceSamplesDecoded = 0;
    unsigned int mrSamplesDecoded = 0;

    __android_log_print(ANDROID_LOG_INFO, "native", "[Mix voiceDecoder] %d", voiceDecoder->getFramesPerChunk());
    __android_log_print(ANDROID_LOG_INFO, "native", "[Mix mrDecoder] %d", mrDecoder->getFramesPerChunk());

    bool isError = false;

    while (true) {
        voiceSamplesDecoded = voiceDecoder->getFramesPerChunk();
        int stateVoiceCode = voiceDecoder->decodeAudio(intVoiceBuffer, voiceSamplesDecoded);

        if (stateVoiceCode == Superpowered::Decoder::Error) {
            isError = true;
            break;
        }

        if (voiceSamplesDecoded < 1) {
            break;
        }

        mrSamplesDecoded = mrDecoder->getFramesPerChunk();
        int stateMrCode = mrDecoder->decodeAudio(intMrBuffer, mrSamplesDecoded);

        if (stateMrCode == Superpowered::Decoder::Error) {
            isError = true;
            break;
        }

        if (mrSamplesDecoded < 1) {
            break;
        }

        unsigned int samplesDecoded = static_cast<unsigned int>(fmax(voiceSamplesDecoded, mrSamplesDecoded));
        Superpowered::ShortIntToFloat(intVoiceBuffer, floatVoiceBuffer, samplesDecoded);
        Superpowered::ShortIntToFloat(intMrBuffer, floatMrBuffer, samplesDecoded);

        if (isUseTime) {
            time->addInput(floatVoiceBuffer, samplesDecoded);
            time->getOutput(floatVoiceBuffer, samplesDecoded);
        }

        if (isAutomaticVocalPitch) {
            automaticVocalPitchCorrection->process(floatVoiceBuffer, floatVoiceBuffer, true, samplesDecoded);
        }

        if (soundEffectFilter != NULL && soundEffectFilter->head != NULL) {
            Node *cur = soundEffectFilter->head;
            while (cur != NULL) {
                cur->filter->process(floatVoiceBuffer, floatVoiceBuffer, samplesDecoded);
                cur = cur->next;
            }
        }

        // mr, voice volum parameter 추가 필요.
        // ex > voice volum setting 0.5
        // Superpowered::Volume(floatVoiceBuffer, floatVoiceBuffer, 0.5, 0.5, samplesDecoded);
        Superpowered::Volume(floatVoiceBuffer, floatVoiceBuffer, voiceVolume, voiceVolume, samplesDecoded); // voice volum > Min 0.0 - Max 1.0
        Superpowered::Volume(floatMrBuffer, floatMrBuffer, mrVolume, mrVolume, samplesDecoded); // mr volum > Min 0.0 - Max 1.0

        mixer->process(floatVoiceBuffer, floatMrBuffer, nullptr, nullptr, floatOutputBuffer, samplesDecoded);

        Superpowered::FloatToShortInt(floatOutputBuffer, intOutputBuffer, samplesDecoded);
//        fwrite(intBuffer, 1, voiceSamplesDecoded * 4, fd);
        Superpowered::writeWAV(fd, intOutputBuffer, samplesDecoded * 4);
        if (stateMrCode == Superpowered::Decoder::EndOfFile) break;
        if ( stateVoiceCode == Superpowered::Decoder::EndOfFile ) break;
    }

    Superpowered::closeWAV(fd);

    delete voiceDecoder;
    delete mrDecoder;
    delete mixer;
    delete time;
    delete automaticVocalPitchCorrection;

    if (soundEffectFilter != NULL) {
        destroyNodeAll(soundEffectFilter);
        free(soundEffectFilter);
        soundEffectFilter = NULL;
    }

    free(intVoiceBuffer);
    free(intMrBuffer);
    free(intOutputBuffer);
    free(floatVoiceBuffer);
    free(floatMrBuffer);
    free(floatOutputBuffer);

    env->ReleaseStringUTFChars(voice_path, voicePath);
    env->ReleaseStringUTFChars(mr_path, mrPath);
    env->ReleaseStringUTFChars(output_path, outputPath);
    __android_log_print(ANDROID_LOG_ERROR, "NATIVE", "[Mix result]--- %s", isError ? "true" : "false");
    __android_log_print(ANDROID_LOG_ERROR, "native", "----------- 4");
    if (isError) return false;

    return true;
}

