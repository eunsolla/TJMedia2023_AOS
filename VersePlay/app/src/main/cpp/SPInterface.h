//
// Created by de-cgh on 2019-07-30.
//

#ifndef verse_SPINTERFACE_H
#define verse_SPINTERFACE_H

#include <math.h>
#include <AndroidIO/SuperpoweredAndroidAudioIO.h>
#include <SuperpoweredAdvancedAudioPlayer.h>
#include <SuperpoweredEcho.h>
#include <SuperpoweredReverb.h>
#include <SuperpoweredRecorder.h>

#include <Superpowered.h>
#include <SuperpoweredFilter.h>
#include <SuperpoweredTimeStretching.h>

#include <malloc.h>
#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_AndroidConfiguration.h>
#include <android/log.h>
#include <SuperpoweredCPU.h>
#include <jni.h>
#include <SuperpoweredSimple.h>

// fx
#include <SuperpoweredFX.h>
#include <SuperpoweredRoll.h>
#include <SuperpoweredFlanger.h>
#include <SuperpoweredWhoosh.h>
#include <Superpowered3BandEQ.h>
#include <SuperpoweredGate.h>
#include <SuperpoweredCompressor.h>
#include <SuperpoweredLimiter.h>
#include <SuperpoweredAutomaticVocalPitchCorrection.h>
#include <SuperpoweredGuitarDistortion.h>



typedef  struct __list {
    struct __node *head;
    struct __node *tail;
} LinkedList;

typedef struct __node {
    Superpowered::FX *filter;
    struct __node *next;
} Node;


class SPInterface {
public:
    SPInterface(const char*licenseKey,unsigned int samplerate, unsigned int buffersize, jboolean isRecord);
    ~SPInterface();

    bool process(short int *output, int numberOfSamples, int samplerate);
    bool process_sync(short int *output, int numberOfFrames, int samplerate);
    void MrOpen(const char *path_, jint offset, jint length);
    void VoiceOpen(const char *path_, jint offset, jint length);
    bool TogglePlayback();
    void OnPause();
    double getCurrentMrMs();
    double getCurrentVoiceMs();
    void setCurrentVoiceMs(double ms , double startMs);
    void setCurrentMrMs(double ms);
    void setCurrentMrAndMs(double mrMs,double voiceMs);
    void setPlaySynchronizedToPosition(double ms);
    void setLoopBetween(double startMs,double endMs, int numLoops);
    void setExitLoop(bool value);

    // record
    void SetRecorder(const char *tempPath);
    void RecordStart(const char *savePath);
    void RecordStop();

    double getCurrentMs();
    float getCurrentPersent();
    double getTotalMs();

//    void echoOnOff(bool value);
//    void soundEffectOnOff(bool value);
//    void reverbOnOff(bool value);
    void voiceOnOff(bool value);
    void setMrVolume(float volume);
    float getMrVolume();
    void setVoiceVolume(float volume);
    float getVoiceVolume();

    void onBackGround();
    void onForeGround();

    bool getCheckVoice();

    void clear();

    int checkOpenMrVoice();
    bool checkRecordStop();
    bool checkMrPlayFinish();
    bool isPlaying();

    unsigned int mSamplerate;
    unsigned int mBufferSize;

    void setFilterType(int type);

    void getLastLog();

private:

    SuperpoweredAndroidAudioIO *audioSystem = nullptr;
    Superpowered::AdvancedAudioPlayer *mrPlayer = nullptr;
    Superpowered::AdvancedAudioPlayer *voicePlayer = nullptr;
    Superpowered::Echo *echo = nullptr;
    Superpowered::Reverb *reverb = nullptr;
    Superpowered::Recorder *recorder = nullptr;
    Superpowered::Filter *voice = nullptr;
    Superpowered::TimeStretching *timeStretching = nullptr;

    Superpowered::AutomaticVocalPitchCorrection *automaticVocalPitchCorrection = nullptr;
    Superpowered::Whoosh *vinyl = nullptr;
    Superpowered::Reverb *concertHall = nullptr;
    Superpowered::Echo *noraebang = nullptr;
    Superpowered::GuitarDistortion *megaPhone = nullptr;
    Superpowered::Reverb *cave = nullptr;
    Superpowered::ThreeBandEQ *chipmunk = nullptr;
    Superpowered::Reverb *recordingStudio = nullptr;


    double syncMs = 0;
    float mMrVolumn = 0.5f;
    float mVoiceVolumn = 0.5f;
    bool isVoiceBufferAdd = true;
    double currentMs = 0;
    float currentPersent = 0;
    bool isStarted = false;
    bool isVoiceOn = false;
    double jumpMs = 0;
    double jumpOriMs = 0;

    LinkedList *heliumFilter = NULL;
    LinkedList *robotFilter = NULL;
    LinkedList *ghostFilter = NULL;
    LinkedList *voiceChangerFilter = NULL;
    LinkedList *automaticVocalPitchCorrectionFilter = NULL;
//    float *mPcm = NULL;
    bool addNewFilter(LinkedList * targetFilter, Superpowered::FX* tempFilter);
    void destroyNode(Node* node);
    void destroyNodeAll(LinkedList *targetFilter);
    int mfilterType = -1;

};

bool isInit = false;
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

#endif //VERSE_SPINTERFACE_H
