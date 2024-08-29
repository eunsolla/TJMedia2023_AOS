//
// Created by de-cgh on 2019-07-30.
//

#include "SPInterface.h"

using namespace Superpowered;

bool audioProcessing(
        void *__unused clientdata, // custom pointer
        short int *audio,           // buffer of interleaved samples
        int numberOfFrames,         // number of frames to process
        int __unused samplerate     // sampling rate
) {
    if (clientdata != nullptr && ((SPInterface *) clientdata) != nullptr) {
        return ((SPInterface *) clientdata)->process(audio, numberOfFrames, samplerate);
    }
    return false;
}

bool audioProcessing_Sync(
        void *__unused clientdata, // custom pointer
        short int *audio,           // buffer of interleaved samples
        int numberOfFrames,         // number of frames to process
        int __unused samplerate     // sampling rate
) {
    if (clientdata != nullptr && ((SPInterface *) clientdata) != nullptr) {
        return ((SPInterface *) clientdata)->process_sync(audio, numberOfFrames, samplerate);
    }
    return false;
}


SPInterface::SPInterface(const char *licenseKey, unsigned int samplerate, unsigned int buffersize, jboolean isRecord) {

    //    // base setting
    mSamplerate = samplerate;
    mBufferSize = buffersize;

//    floatBuffer = (float *)malloc(sizeof(float) * 2 * buffersize);
//    mPcm = (float *)malloc(sizeof(float) * 2 * 2048);
//    mPcm = (float *)malloc(sizeof(float) * 2 * buffersize);


//    __android_log_print(ANDROID_LOG_ERROR, "NATIVE", "SPInterface call");
    if (!isInit) {
        isInit = true;
        Superpowered::Initialize(licenseKey);
    }

//    __android_log_print(ANDROID_LOG_ERROR, "NATIVE", "native 2");
    if (isRecord) {
//        __android_log_print(ANDROID_LOG_ERROR, "NATIVE", "native *");
        __android_log_print(ANDROID_LOG_ERROR, "NATIVE", "native **");
        mrPlayer = new Superpowered::AdvancedAudioPlayer(mSamplerate, 0);
        voice = new Superpowered::Filter(Superpowered::Filter::Bandlimited_Bandpass, samplerate);
        voice->frequency = 261.6f;
        voice->octave = 3.0f;

        audioSystem = new SuperpoweredAndroidAudioIO(
                samplerate,                     // sampling rate
                buffersize,                     // buffer size
                true,                           // enableInput
                true,                           // enableOutput
                audioProcessing,                // process callback function
                this,                           // clientData
                -1,                             // inputStreamType (-1 = default)
                SL_ANDROID_STREAM_MEDIA         // outputStreamType (-1 = default)
//                -1                              // outputStreamType (-1 = default)
        );

//        __android_log_print(ANDROID_LOG_ERROR, "NATIVE", "audioSystem : %p",audioSystem);
//        __android_log_print(ANDROID_LOG_ERROR, "NATIVE", "audioSystem : %p",mrPlayer);
//        __android_log_print(ANDROID_LOG_ERROR, "NATIVE", "audioSystem : %p",voice);

    } else {
        mrPlayer = new AdvancedAudioPlayer(mSamplerate, 0);
        voicePlayer = new AdvancedAudioPlayer(mSamplerate, 0);

        audioSystem = new SuperpoweredAndroidAudioIO(
                samplerate,                     // sampling rate
                buffersize,                     // buffer size
                false,                           // enableInput
                true,                           // enableOutput
                audioProcessing_Sync,                // process callback function
                this,                           // clientData
                -1,                             // inputStreamType (-1 = default)
                SL_ANDROID_STREAM_MEDIA         // outputStreamType (-1 = default)
        );

//        __android_log_print(ANDROID_LOG_ERROR, "NATIVE", "audioSystem : %p",audioSystem);
//        __android_log_print(ANDROID_LOG_ERROR, "NATIVE", "audioSystem : %p",mrPlayer);
//        __android_log_print(ANDROID_LOG_ERROR, "NATIVE", "audioSystem : %p",voicePlayer);
    }

    try {
        echo = new Echo(mSamplerate);
        echo->enabled = true;

        reverb = new Reverb(mSamplerate);
        reverb->enabled = true;

//        timeStretching = new TimeStretching(mSamplerate, 0.5,1);
        timeStretching = new TimeStretching(mSamplerate, 0.5);
        timeStretching->sound = 1;

        // helium filter (3)
        heliumFilter = (LinkedList *) malloc(sizeof(LinkedList));
        heliumFilter->head = nullptr;
        heliumFilter->tail = nullptr;
        Superpowered::Echo *heliumFilter_option1 = new Superpowered::Echo(mSamplerate);
        heliumFilter_option1->enabled = true;
        heliumFilter_option1->setMix(0.5);
        heliumFilter_option1->bpm = 128.0f;
        heliumFilter_option1->beats = 0.4f;
        heliumFilter_option1->decay = 0.4f;
        addNewFilter(heliumFilter, heliumFilter_option1);

        // robot (4)
        robotFilter = (LinkedList *) malloc(sizeof(LinkedList));
        robotFilter->head = nullptr;
        robotFilter->tail = nullptr;
        Superpowered::Flanger *robotFilter_option1 = new Superpowered::Flanger(mSamplerate);
        robotFilter_option1->enabled = true;
        robotFilter_option1->wet = 1.0f;
        robotFilter_option1->depth = 0.9f;
        robotFilter_option1->lfoBeats = 128.0f;
        robotFilter_option1->bpm = 50.0f;
        robotFilter_option1->clipperThresholdDb = 3;
        robotFilter_option1->clipperMaximumDb = 3;
        addNewFilter(robotFilter, robotFilter_option1);

        // ghost (5)
        ghostFilter = (LinkedList *) malloc(sizeof(LinkedList));
        ghostFilter->head = nullptr;
        ghostFilter->tail = nullptr;
        Superpowered::Flanger *ghostFilter_option1 = new Superpowered::Flanger(mSamplerate);
        ghostFilter_option1->enabled = true;
        ghostFilter_option1->wet = 1.0f;
        ghostFilter_option1->depth = 0.9f;
        ghostFilter_option1->lfoBeats = 128.0f;
        ghostFilter_option1->bpm = 128.0f;
        ghostFilter_option1->clipperThresholdDb = 1;
        ghostFilter_option1->clipperMaximumDb = 3;
        addNewFilter(ghostFilter, ghostFilter_option1);

        Superpowered::Reverb *ghostFilter_option2 = new Superpowered::Reverb(mSamplerate);
        ghostFilter_option2->enabled = true;
        ghostFilter_option2->dry = 0.5f;
        ghostFilter_option2->wet = 0.8f;
        ghostFilter_option2->mix = 0.9f;
        ghostFilter_option2->width = 1.0f;
        ghostFilter_option2->damp = 0.5f;
        ghostFilter_option2->roomSize = 0.8f;
        ghostFilter_option2->predelayMs = 0.0f;
        ghostFilter_option2->lowCutHz = 0.0f;
        addNewFilter(ghostFilter, ghostFilter_option2);

        // voice changer filter (6)
        voiceChangerFilter = (LinkedList *) malloc(sizeof(LinkedList));
        voiceChangerFilter->head = nullptr;
        voiceChangerFilter->tail = nullptr;
        Superpowered::Echo *voiceChangerFilter_option1 = new Superpowered::Echo(mSamplerate);
        voiceChangerFilter_option1->enabled = true;
        voiceChangerFilter_option1->setMix(0.5);
        voiceChangerFilter_option1->bpm = 128.0f;
        voiceChangerFilter_option1->beats = 0.4f;
        voiceChangerFilter_option1->decay = 0.4f;
        addNewFilter(voiceChangerFilter, voiceChangerFilter_option1);

        //[s] auto tune
        automaticVocalPitchCorrection = new Superpowered::AutomaticVocalPitchCorrection();
        automaticVocalPitchCorrection->samplerate = samplerate;
        automaticVocalPitchCorrection->scale = Superpowered::AutomaticVocalPitchCorrection::CHROMATIC;

        // Set the vocal range of the singer.
        automaticVocalPitchCorrection->range = Superpowered::AutomaticVocalPitchCorrection::WIDE;
        automaticVocalPitchCorrection->speed = Superpowered::AutomaticVocalPitchCorrection::EXTREME;
        automaticVocalPitchCorrection->frequencyOfA = 440;

        automaticVocalPitchCorrectionFilter = (LinkedList *) malloc(sizeof(LinkedList));
        automaticVocalPitchCorrectionFilter->head = nullptr;
        automaticVocalPitchCorrectionFilter->tail = nullptr;

        Superpowered::Compressor *autotuneFilter_option1 = new Superpowered::Compressor(samplerate);
        autotuneFilter_option1->inputGainDb = 0;
        autotuneFilter_option1->outputGainDb = 0;
        autotuneFilter_option1->wet = 1;
        autotuneFilter_option1->attackSec = 0.003;
        autotuneFilter_option1->releaseSec = 0.3;
        autotuneFilter_option1->ratio = 3;
        autotuneFilter_option1->thresholdDb = 0;
        autotuneFilter_option1->hpCutOffHz = 1;
        autotuneFilter_option1->enabled = true;
        addNewFilter(automaticVocalPitchCorrectionFilter, autotuneFilter_option1);

        Superpowered::Reverb *autotuneFilter_option2 = new Superpowered::Reverb(mSamplerate);
        autotuneFilter_option2->mix = 0.04;
        autotuneFilter_option2->width = 1;
        autotuneFilter_option2->damp = 0.5;
        autotuneFilter_option2->roomSize = 0.8;
        autotuneFilter_option2->predelayMs = 0;
        autotuneFilter_option2->lowCutHz = 0;
        autotuneFilter_option2->enabled = true;
        addNewFilter(automaticVocalPitchCorrectionFilter, autotuneFilter_option2);
        //[e] auto tune

        //[s] vinyl
        vinyl = new Superpowered::Whoosh(samplerate);
        vinyl->wet = 0.01;
        vinyl->frequency = 3093.53;
        vinyl->enabled = true;
        //[e] vinyl

        //[s] concert
        concertHall = new Reverb(samplerate);
        concertHall->mix = 0.55;
        concertHall->width = 1;
        concertHall->damp = 0.6;
        concertHall->roomSize = 0.71;
        concertHall->predelayMs = 140;
        concertHall->lowCutHz = 100;
        concertHall->enabled = true;
        //[e] concert

        //[s] noraebang
        noraebang = new Echo(samplerate);
        noraebang->dry = 0.5;
        noraebang->wet = 1;
        noraebang->bpm = 140;
        noraebang->beats = 0.5;
        noraebang->decay = 0.45;
        noraebang->enabled = true;
        //[e] noraebang

        //[s] megaPhone
        megaPhone = new GuitarDistortion(samplerate);
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
        //[e] megaPhone

        //[s] cave
        cave = new Reverb(samplerate);
        cave->mix = 0.79;
        cave->width = 1;
        cave->damp = 0.6;
        cave->roomSize = 0.75;
        cave->predelayMs = 0;
        cave->lowCutHz = 100;
        cave->enabled = true;
        //[e] cave

        //[s] chipmunk
        chipmunk = new Superpowered::ThreeBandEQ(samplerate);
        chipmunk->low = 1.22;
        chipmunk->mid = 1.4;
        chipmunk->high = 1.4;
        chipmunk->enabled = true;
        //[e] chipmunk

        //[s] recordingStudio
        recordingStudio = new Reverb(samplerate);
        recordingStudio->mix = 0.5;
        recordingStudio->width = 1;
        recordingStudio->damp = 0.6;
        recordingStudio->roomSize = 0.6;
        recordingStudio->predelayMs = 0;
        recordingStudio->lowCutHz = 100;
        recordingStudio->enabled = true;
        //[e] recordingStudio

    } catch (int ex) {
        __android_log_print(ANDROID_LOG_ERROR, "NATIVE", "filter load error : %d", ex);
    }

    currentMs = 0;
    currentPersent = 0;
    syncMs = 0;
    mMrVolumn = 0.5f;
    mVoiceVolumn = 0.5f;
    isStarted = false;
    isVoiceOn = false;
    mfilterType = TYPE_NONE;

    audioSystem->start();
    __android_log_print(ANDROID_LOG_ERROR, "NATIVE", "SPInterface call finish");
}

SPInterface::~SPInterface() {
    __android_log_print(ANDROID_LOG_ERROR, "NATIVE", "~SPInterface");
}


void SPInterface::destroyNode(Node *node) {
    delete node->filter;
    node->filter = nullptr;
    free(node);
}

void SPInterface::destroyNodeAll(LinkedList *targetFilter) {
    if (targetFilter->head == nullptr) {
        return;
    }

    Node *cur = targetFilter->head;
    while (cur != nullptr) {
        Node *temp = cur;
        cur = cur->next;
        destroyNode(temp);
    }
}

bool SPInterface::addNewFilter(LinkedList *targetFilter, Superpowered::FX *tempFilter) {
    Node *newNode = (Node *) malloc(sizeof(Node));
    if (tempFilter == nullptr) {
        free(newNode);
        return false;
    }

    tempFilter->enabled = true;

    newNode->filter = tempFilter;
    newNode->next = nullptr;

    if (targetFilter->head == nullptr && targetFilter->tail == nullptr) {
        targetFilter->head = newNode;
        targetFilter->tail = newNode;
    } else {
        targetFilter->tail->next = newNode;
        targetFilter->tail = newNode;
    }

    return true;
}

void SPInterface::setFilterType(int type) {

    mfilterType = type;

    switch (mfilterType) {
        case TYPE_ROBOT: {
            if (timeStretching) {
                timeStretching->pitchShiftCents = -500;
            }
        }
            break;
        case TYPE_NONE:
        case TYPE_EHCO:
        case TYPE_REVERB:
        case TYPE_VINYL:
        case TYPE_CONCERT_HALL:
        case TYPE_NORAE_BANG:
        case TYPE_MEGA_PHONE:
        case TYPE_CAVE:
        case TYPE_RECORDING_STUDIO:
        case TYPE_AUTO_TUNE: {
            if (timeStretching) {
                timeStretching->pitchShiftCents = 0;
            }
        }
            break;
        case TYPE_VOICE_CHANGER: {
            if (timeStretching) {
                timeStretching->pitchShiftCents = 500;
            }
        }
            break;
        case TYPE_HELIUM: {
            if (timeStretching) {
                timeStretching->pitchShiftCents = 800;
            }
        }
            break;
        case TYPE_GHOST: {
            if (timeStretching) {
                timeStretching->pitchShiftCents = 1000;
            }
        }
            break;
        case TYPE_CHIPMUNK: {
            if (timeStretching) {
                timeStretching->pitchShiftCents = 1200;
            }
        }
            break;
        default: {
            if (timeStretching) {
                timeStretching->pitchShiftCents = 0;
            }
        }
            break;
    }
}

/** for record */
bool SPInterface::process(
        short int *output,         // buffer to receive output samples
        int numberOfFrames,     // number of frames requested
        int samplerate
) {
    if (mrPlayer == nullptr)
        return false;

    try {
        float floatBuffer[numberOfFrames * 2];
        float voiceBuffer[numberOfFrames * 2];

        ShortIntToFloat(output, floatBuffer, (unsigned int) numberOfFrames);
        ShortIntToFloat(output, voiceBuffer, (unsigned int) numberOfFrames);
        mrPlayer->outputSamplerate = (unsigned int) samplerate;

        if (mrPlayer->isPlaying()) {

//            if (jumpMs > 0) {
//                mrPlayer->setPosition(jumpMs, false, false, true);
//                jumpMs = 0;
//            }

            currentMs = mrPlayer->getDisplayPositionMs();
            currentPersent = mrPlayer->getDisplayPositionPercent();

            if (recorder != nullptr) {
                recorder->recordInterleaved(floatBuffer, (unsigned int) numberOfFrames);
            }

            if (voice != nullptr) {
                voice->process(voiceBuffer, voiceBuffer, (unsigned int) numberOfFrames);
                float voiceCheckValue = fabs(*voiceBuffer);
                if (voiceCheckValue >= 0.08f) {
                    isVoiceOn = true;
                } else {
                    isVoiceOn = false;
                }
            }
        }

        if (isVoiceBufferAdd) {
            if (timeStretching != nullptr) {
                timeStretching->addInput(floatBuffer, numberOfFrames);
                timeStretching->getOutput(floatBuffer, numberOfFrames);
            }
            switch (mfilterType) {

                case TYPE_EHCO: {
                    // echo
                    if (echo != nullptr && echo->enabled) {
                        echo->process(floatBuffer, floatBuffer, (unsigned int) numberOfFrames);
                    }
                }
                    break;
                case TYPE_REVERB: {
                    // reverb
                    if (reverb != nullptr && reverb->enabled) {
                        reverb->process(floatBuffer, floatBuffer, (unsigned int) numberOfFrames);
                    }
                }
                    break;
                case TYPE_HELIUM: {
                    // helium
                    if (heliumFilter != nullptr && heliumFilter->head != nullptr) {
                        Node *cur = heliumFilter->head;
                        while (cur != nullptr) {
                            cur->filter->process(floatBuffer, floatBuffer, (unsigned int) numberOfFrames);
                            cur = cur->next;
                        }
                    }
                }
                    break;
                case TYPE_ROBOT: {
                    // robot
                    if (robotFilter != nullptr && robotFilter->head != nullptr) {
                        Node *cur = robotFilter->head;
                        while (cur != nullptr) {
                            cur->filter->process(floatBuffer, floatBuffer, (unsigned int) numberOfFrames);
                            cur = cur->next;
                        }
                    }
                }
                    break;
                case TYPE_GHOST: {
                    // ghost
                    if (ghostFilter != nullptr && ghostFilter->head != nullptr) {
                        Node *cur = ghostFilter->head;
                        while (cur != nullptr) {
                            cur->filter->process(floatBuffer, floatBuffer, (unsigned int) numberOfFrames);
                            cur = cur->next;
                        }
                    }
                }
                    break;
                case TYPE_VOICE_CHANGER: {
                    // voice changer
                    if (voiceChangerFilter != nullptr && voiceChangerFilter->head != nullptr) {
                        Node *cur = voiceChangerFilter->head;
                        while (cur != nullptr) {
                            cur->filter->process(floatBuffer, floatBuffer, (unsigned int) numberOfFrames);
                            cur = cur->next;
                        }
                    }
                }
                    break;
                case TYPE_AUTO_TUNE: {
                    automaticVocalPitchCorrection->process(
                            floatBuffer,  // Input pointer (audio in 32-bit floating point numbers).
                            floatBuffer, // Output pointer (audio in 32-bit floating point numbers).
                            true,   // True: input and output are interleaved stereo. False: mono.
                            (unsigned int) numberOfFrames     // Number of frames to process.
                    );

                    if (automaticVocalPitchCorrectionFilter != nullptr &&
                        automaticVocalPitchCorrectionFilter->head != nullptr) {
                        Node *cur = automaticVocalPitchCorrectionFilter->head;
                        while (cur != nullptr) {
                            cur->filter->process(floatBuffer, floatBuffer, (unsigned int) numberOfFrames);
                            cur = cur->next;
                        }
                    }
                }
                    break;
                case TYPE_VINYL: {
                    if (vinyl != nullptr && vinyl->enabled) {
                        vinyl->process(floatBuffer, floatBuffer, (unsigned int) numberOfFrames);
                    }
                }
                    break;
                case TYPE_CONCERT_HALL: {
                    if (concertHall != nullptr && concertHall->enabled) {
                        concertHall->process(floatBuffer, floatBuffer, (unsigned int) numberOfFrames);
                    }
                }
                    break;
                case TYPE_NORAE_BANG: {
                    if (noraebang != nullptr && noraebang->enabled) {
                        noraebang->process(floatBuffer, floatBuffer, (unsigned int) numberOfFrames);
                    }
                }
                    break;
                case TYPE_MEGA_PHONE: {
                    if (megaPhone != nullptr && megaPhone->enabled) {
                        megaPhone->process(floatBuffer, floatBuffer, (unsigned int) numberOfFrames);
                    }
                }
                    break;
                case TYPE_CAVE: {
                    if (cave != nullptr && cave->enabled) {
                        cave->process(floatBuffer, floatBuffer, (unsigned int) numberOfFrames);
                    }
                }
                    break;
                case TYPE_CHIPMUNK: {
                    if (chipmunk != nullptr && chipmunk->enabled) {
                        chipmunk->process(floatBuffer, floatBuffer, (unsigned int) numberOfFrames);
                    }
                }
                    break;
                case TYPE_RECORDING_STUDIO: {
                    if (recordingStudio != nullptr && recordingStudio->enabled) {
                        recordingStudio->process(floatBuffer, floatBuffer, (unsigned int) numberOfFrames);
                    }
                }
                    break;
                default: {
                }
                    break;
            }
        }

        bool p1 = mrPlayer->processStereo(floatBuffer, isVoiceBufferAdd, (unsigned int) numberOfFrames, mMrVolumn);
        FloatToShortInt(floatBuffer, output, (unsigned int) numberOfFrames);
        return p1 || isVoiceBufferAdd;
    } catch (int e) {
        __android_log_print(ANDROID_LOG_ERROR, "NATIVE", "process catch %d", e);
        return false;
    }
}

/** for sync */
bool SPInterface::process_sync(short int *output, int numberOfFrames, int samplerate) {
    if (mrPlayer == nullptr)
        return false;

    try {
        float floatBuffer[numberOfFrames * 2];
        ShortIntToFloat(output, floatBuffer, (unsigned int) numberOfFrames);

        mrPlayer->outputSamplerate = (unsigned int) samplerate;
        voicePlayer->outputSamplerate = (unsigned int) samplerate;

        bool isSilence = false;

        if (voicePlayer != nullptr && voicePlayer->isPlaying()) {
            voicePlayer->processStereo(floatBuffer, false, (unsigned int) numberOfFrames, mVoiceVolumn);

            if (timeStretching != nullptr) {
                timeStretching->addInput(floatBuffer, numberOfFrames);
                timeStretching->getOutput(floatBuffer, numberOfFrames);
            }

            switch (mfilterType) {
                case TYPE_EHCO: {
                    // echo
                    if (echo != nullptr && echo->enabled) {
                        echo->process(floatBuffer, floatBuffer, (unsigned int) numberOfFrames);
                    }
                }
                    break;
                case TYPE_REVERB: {
                    // reverb
                    if (reverb != nullptr && reverb->enabled) {
                        reverb->process(floatBuffer, floatBuffer, (unsigned int) numberOfFrames);
                    }
                }
                    break;
                case TYPE_HELIUM: {
                    // helium
                    if (heliumFilter != nullptr && heliumFilter->head != nullptr) {
                        Node *cur = heliumFilter->head;
                        while (cur != nullptr) {
                            cur->filter->process(floatBuffer, floatBuffer, (unsigned int) numberOfFrames);
                            cur = cur->next;
                        }
                    }
                }
                    break;
                case TYPE_ROBOT: {
                    // robot
                    if (robotFilter != nullptr && robotFilter->head != nullptr) {
                        Node *cur = robotFilter->head;
                        while (cur != nullptr) {
                            cur->filter->process(floatBuffer, floatBuffer, (unsigned int) numberOfFrames);
                            cur = cur->next;
                        }
                    }
                }
                    break;
                case TYPE_GHOST: {
                    // ghost
                    if (ghostFilter != nullptr && ghostFilter->head != nullptr) {
                        Node *cur = ghostFilter->head;
                        while (cur != nullptr) {
                            cur->filter->process(floatBuffer, floatBuffer, (unsigned int) numberOfFrames);
                            cur = cur->next;
                        }
                    }
                }
                    break;
                case TYPE_VOICE_CHANGER: {
                    // voice changer
                    if (voiceChangerFilter != nullptr && voiceChangerFilter->head != nullptr) {
                        Node *cur = voiceChangerFilter->head;
                        while (cur != nullptr) {
                            cur->filter->process(floatBuffer, floatBuffer, (unsigned int) numberOfFrames);
                            cur = cur->next;
                        }
                    }
                }
                    break;
                case TYPE_AUTO_TUNE: {
                    automaticVocalPitchCorrection->process(
                            floatBuffer,  // Input pointer (audio in 32-bit floating point numbers).
                            floatBuffer, // Output pointer (audio in 32-bit floating point numbers).
                            true,   // True: input and output are interleaved stereo. False: mono.
                            (unsigned int) numberOfFrames     // Number of frames to process.
                    );

                    if (automaticVocalPitchCorrectionFilter != nullptr &&
                        automaticVocalPitchCorrectionFilter->head != nullptr) {
                        Node *cur = automaticVocalPitchCorrectionFilter->head;
                        while (cur != nullptr) {
                            cur->filter->process(floatBuffer, floatBuffer, (unsigned int) numberOfFrames);
                            cur = cur->next;
                        }
                    }
                }
                    break;
                case TYPE_VINYL: {
                    if (vinyl != nullptr && vinyl->enabled) {
                        vinyl->process(floatBuffer, floatBuffer, (unsigned int) numberOfFrames);
                    }
                }
                    break;
                case TYPE_CONCERT_HALL: {
                    if (concertHall != nullptr && concertHall->enabled) {
                        concertHall->process(floatBuffer, floatBuffer, (unsigned int) numberOfFrames);
                    }
                }
                    break;
                case TYPE_NORAE_BANG: {
                    if (noraebang != nullptr && noraebang->enabled) {
                        noraebang->process(floatBuffer, floatBuffer, (unsigned int) numberOfFrames);
                    }
                }
                    break;
                case TYPE_MEGA_PHONE: {
                    if (megaPhone != nullptr && megaPhone->enabled) {
                        megaPhone->process(floatBuffer, floatBuffer, (unsigned int) numberOfFrames);
                    }
                }
                    break;
                case TYPE_CAVE: {
                    if (cave != nullptr && cave->enabled) {
                        cave->process(floatBuffer, floatBuffer, (unsigned int) numberOfFrames);
                    }
                }
                    break;
                case TYPE_CHIPMUNK: {
                    if (chipmunk != nullptr && chipmunk->enabled) {
                        chipmunk->process(floatBuffer, floatBuffer, (unsigned int) numberOfFrames);
                    }
                }
                    break;
                case TYPE_RECORDING_STUDIO: {
                    if (recordingStudio != nullptr && recordingStudio->enabled) {
                        recordingStudio->process(floatBuffer, floatBuffer, (unsigned int) numberOfFrames);
                    }
                }
                    break;
                default: {

                }
                    break;
            }
        }

        if (mrPlayer->isPlaying()) {

//            if (jumpMs > 0) {
//                mrPlayer->setPosition(jumpMs, false, false, true);
//                jumpMs = 0;
//            }

            currentMs = mrPlayer->getDisplayPositionMs();
            currentPersent = mrPlayer->getDisplayPositionPercent();

            if (voicePlayer != nullptr && voicePlayer->isPlaying() && !voicePlayer->eofRecently()) {
                isSilence = mrPlayer->processStereo(floatBuffer, true, numberOfFrames, mMrVolumn);
            } else {
                isSilence = mrPlayer->processStereo(floatBuffer, false, numberOfFrames, mMrVolumn);
            }
        }

        if (isSilence) FloatToShortInt(floatBuffer, output, (unsigned int) numberOfFrames);
        return isSilence;
    } catch (int e) {
        __android_log_print(ANDROID_LOG_ERROR, "NATIVE", "process catch %d", e);
        return false;
    }
}

void SPInterface::MrOpen(const char *path_, jint offset, jint length) {
    __android_log_print(ANDROID_LOG_ERROR, "NATIVE", "mr open");
    if (mrPlayer != nullptr) {
        if (offset == 0 && length == 0) {
            __android_log_print(ANDROID_LOG_ERROR, "NATIVE", "MrOpen path open : %s", path_);
            mrPlayer->open(path_);
        } else {
            __android_log_print(ANDROID_LOG_ERROR, "NATIVE", "MrOpen path open2 : %s", path_);
            mrPlayer->open(path_, offset, length);
        }
    } else {
        __android_log_print(ANDROID_LOG_ERROR, "PlayerExample", "mrPlayer is null");
    }
}

void SPInterface::VoiceOpen(const char *path_, jint offset, jint length) {
    __android_log_print(ANDROID_LOG_ERROR, "NATIVE", "voice open");
    if (voicePlayer != nullptr) {
        if (offset == 0 && length == 0) {
            __android_log_print(ANDROID_LOG_ERROR, "NATIVE", "VoiceOpen path open : %s", path_);
            voicePlayer->open(path_);
        } else {
            __android_log_print(ANDROID_LOG_ERROR, "NATIVE", "VoiceOpen path open2 : %s", path_);
            voicePlayer->open(path_, offset, length);
        }
    }
}


bool SPInterface::TogglePlayback() {
    __android_log_print(ANDROID_LOG_ERROR, "NATIVE", "SyncTogglePlayback");
    bool result = false;
    if (mrPlayer != nullptr) {
        __android_log_print(ANDROID_LOG_ERROR, "NATIVE", "SyncTogglePlayback - mr");
        mrPlayer->togglePlayback();
        CPU::setSustainedPerformanceMode(mrPlayer->isPlaying());
        result = mrPlayer->isPlaying();
    }

    if (voicePlayer != nullptr) {
        if (result) {
            if (!voicePlayer->isPlaying()) {
                __android_log_print(ANDROID_LOG_ERROR, "NATIVE", "SyncTogglePlayback - voice");
                voicePlayer->togglePlayback();
            }
        } else {
            if (voicePlayer->isPlaying()) {
                voicePlayer->togglePlayback();
            }
        }
    }

    return result;
}

void SPInterface::OnPause() {
    if (mrPlayer != nullptr) {
        if (mrPlayer->isPlaying()) {
            mrPlayer->pause();
            CPU::setSustainedPerformanceMode(mrPlayer->isPlaying());
            __android_log_print(ANDROID_LOG_ERROR, "NATIVE", "mrPlayer : %s", mrPlayer->isPlaying() ? "true" : "false");
        }
    }
    if (voicePlayer != nullptr) {
        if (voicePlayer->isPlaying()) {
            voicePlayer->pause();
        }
    }
}

void SPInterface::setCurrentVoiceMs(double ms, double startMs) {
    if (voicePlayer != nullptr) {
        double mrPosition = mrPlayer->getDisplayPositionMs();
        double rePosition = (mrPosition - startMs);

        if (rePosition < 0) {
            rePosition = 0;
        }
        syncMs = ms;
        voicePlayer->setPosition(rePosition + syncMs, false, false);
        __android_log_print(ANDROID_LOG_ERROR, "NATIVE", "setCurrentVoiceMs : %f / %f  / %f / %f ", mrPosition, syncMs, (mrPosition + syncMs), rePosition);
    }
}

void SPInterface::setPlaySynchronizedToPosition(double ms) {
//    mrPlayer->playSynchronizedToPosition(ms);
    jumpMs = ms;
    jumpOriMs = ms;
}

void SPInterface::setLoopBetween(double startMs, double endMs, int numLoops) {
    mrPlayer->loopBetween(startMs, endMs, true, 255, true, 1, true);
}

void SPInterface::setExitLoop(bool value) {
    mrPlayer->exitLoop(value);
}


void SPInterface::setCurrentMrMs(double ms) {
    if (mrPlayer != nullptr) {
        mrPlayer->setPosition(ms, false, false);
        currentMs = ms;
    }
}

void SPInterface::setCurrentMrAndMs(double mrMs, double voiceMs) {
    if (voicePlayer != nullptr && mrPlayer != nullptr) {
        mrPlayer->setPosition(mrMs, false, false);
        currentMs = mrMs;
        double tempMs = voiceMs + syncMs;
        double totalMs = voicePlayer->getDurationMs();

        __android_log_print(ANDROID_LOG_ERROR, "NATIVE", "setCurrentMrAndMs : %f / %f / %f / %f", mrMs, voiceMs, tempMs, totalMs);
        if (totalMs > tempMs)
            voicePlayer->setPosition(tempMs, false, false);
        else
            voicePlayer->setPosition(totalMs, false, false);
    }
}


double SPInterface::getCurrentMrMs() {
    return 0;
}

double SPInterface::getCurrentVoiceMs() {
    return 0;
}

void SPInterface::SetRecorder(const char *tempPath) {
    recorder = new Superpowered::Recorder(tempPath);
}

void SPInterface::RecordStart(const char *savePath) {
    if (recorder != nullptr) {
        recorder->prepare(savePath, mSamplerate, false, 1);
    }
}

void SPInterface::RecordStop() {
    if (recorder != nullptr) {
        recorder->stop();
    }
}


double SPInterface::getCurrentMs() {
//    __android_log_print(ANDROID_LOG_ERROR, "NATIVE", "currentMs : %f", currentMs);
    return currentMs;
}

float SPInterface::getCurrentPersent() {
    return currentPersent;
}

bool SPInterface::checkMrPlayFinish() {
    if (mrPlayer != nullptr) {
        return mrPlayer->eofRecently();
    }
    return false;
}

bool SPInterface::isPlaying() {
    if (mrPlayer != nullptr) {
        return mrPlayer->isPlaying();
    }
    return false;
}


double SPInterface::getTotalMs() {
    if (mrPlayer != nullptr) {
        return mrPlayer->getDurationMs();
    }

    return 0;
}


void SPInterface::voiceOnOff(bool value) {
    __android_log_print(ANDROID_LOG_ERROR, "NATIVE", "voiceOnOff : %s", value ? "true" : "false");
    isVoiceBufferAdd = value;
}

void SPInterface::clear() {
    __android_log_print(ANDROID_LOG_ERROR, "NATIVE", "clear");

    if (audioSystem != nullptr) {
        audioSystem->stop();
    }
    delete audioSystem;
    audioSystem = nullptr;

//    __android_log_print(ANDROID_LOG_ERROR,"NATIVE","mrPlayer clear %p", mrPlayer);
    delete mrPlayer;
    mrPlayer = nullptr;

//    __android_log_print(ANDROID_LOG_ERROR,"NATIVE","voicePlayer clear %p", voicePlayer);
    delete voicePlayer;
    voicePlayer = nullptr;

//    __android_log_print(ANDROID_LOG_ERROR,"NATIVE","echo clear %p", echo);
    delete echo;
    echo = nullptr;

//    __android_log_print(ANDROID_LOG_ERROR,"NATIVE","reverb clear %p", reverb);
    delete reverb;
    reverb = nullptr;

//    __android_log_print(ANDROID_LOG_ERROR,"NATIVE","recorder clear %p", recorder);
    delete recorder;
    recorder = nullptr;

//    __android_log_print(ANDROID_LOG_ERROR,"NATIVE","voice clear %p", voice);
    delete voice;
    voice = nullptr;

//    __android_log_print(ANDROID_LOG_ERROR,"NATIVE","timeStretching clear %p", timeStretching);
    delete timeStretching;
    timeStretching = nullptr;
//    __android_log_print(ANDROID_LOG_ERROR,"NATIVE","audioSystem clear %p", audioSystem);

    if (automaticVocalPitchCorrection != nullptr) {
        automaticVocalPitchCorrection->reset();
        delete automaticVocalPitchCorrection;
        automaticVocalPitchCorrection = nullptr;
    }

    delete vinyl;
    vinyl = nullptr;

    delete concertHall;
    concertHall = nullptr;

    delete noraebang;
    noraebang = nullptr;

    delete megaPhone;
    megaPhone = nullptr;

    delete cave;
    cave = nullptr;

    delete chipmunk;
    chipmunk = nullptr;

    delete recordingStudio;
    recordingStudio = nullptr;

    if (heliumFilter != nullptr) {
        destroyNodeAll(heliumFilter);
        free(heliumFilter);
        heliumFilter = nullptr;
    }

    if (robotFilter != nullptr) {
        destroyNodeAll(robotFilter);
        free(robotFilter);
        robotFilter = nullptr;
    }

    if (ghostFilter != nullptr) {
        destroyNodeAll(ghostFilter);
        free(ghostFilter);
        ghostFilter = nullptr;
    }

    if (voiceChangerFilter != nullptr) {
        destroyNodeAll(voiceChangerFilter);
        free(voiceChangerFilter);
        voiceChangerFilter = nullptr;
    }

    if (automaticVocalPitchCorrectionFilter != nullptr) {
        destroyNodeAll(automaticVocalPitchCorrectionFilter);
        free(automaticVocalPitchCorrectionFilter);
        automaticVocalPitchCorrectionFilter = nullptr;
    }


//    free(mPcm);
//    mPcm = NULL;

    isVoiceBufferAdd = false;
    mMrVolumn = 1.0f;
    mVoiceVolumn = 1.0f;
    currentMs = 0;
    currentPersent = 0;
    jumpMs = 0;
    syncMs = 0;

    isInit = false;
    isStarted = false;
    isVoiceOn = false;
    mfilterType = TYPE_NONE;
    __android_log_print(ANDROID_LOG_ERROR, "NATIVE", "clear finish");

}

bool SPInterface::checkRecordStop() {
    return recorder->isFinished();
}


// -1 is default
// 1 is opend mr
// 2 is opend voice
// 3 is opend mr &
int SPInterface::checkOpenMrVoice() {
    if (mrPlayer != nullptr && voicePlayer != nullptr) {
        bool mrOpend = false;
        switch (mrPlayer->getLatestEvent()) {
            case Superpowered::AdvancedAudioPlayer::PlayerEvent_None:
                break;
            case Superpowered::AdvancedAudioPlayer::PlayerEvent_Opening:
                __android_log_print(ANDROID_LOG_ERROR, "PlayerExample", "@ MR 22222222");
                break; // do nothing
            case Superpowered::AdvancedAudioPlayer::PlayerEvent_Opened: {
                mrOpend = true;
                __android_log_print(ANDROID_LOG_ERROR, "PlayerExample", "@ MR 333333333");
            }
                break;
            case Superpowered::AdvancedAudioPlayer::PlayerEvent_OpenFailed: {
                int openError = mrPlayer->getOpenErrorCode();
                __android_log_print(ANDROID_LOG_ERROR, "PlayerExample", "mr Open error %i: %s",
                                    openError,
                                    Superpowered::AdvancedAudioPlayer::statusCodeToString(
                                            openError));
            }
                break;
            case Superpowered::AdvancedAudioPlayer::PlayerEvent_ConnectionLost:
                __android_log_print(ANDROID_LOG_ERROR, "PlayerExample", "Network download failed.");
                break;
            case Superpowered::AdvancedAudioPlayer::PlayerEvent_ProgressiveDownloadFinished:
                __android_log_print(ANDROID_LOG_ERROR, "PlayerExample",
                                    "Download finished. Path: %s",
                                    mrPlayer->getFullyDownloadedFilePath());
                break;
        }


        bool voiceOpend = false;
        switch (voicePlayer->getLatestEvent()) {
            case Superpowered::AdvancedAudioPlayer::PlayerEvent_None:
                break;
            case Superpowered::AdvancedAudioPlayer::PlayerEvent_Opening:
                __android_log_print(ANDROID_LOG_ERROR, "PlayerExample", "@ VR 22222222");
                break; // do nothing
            case Superpowered::AdvancedAudioPlayer::PlayerEvent_Opened:
                voiceOpend = true;
                __android_log_print(ANDROID_LOG_ERROR, "PlayerExample", "@ VR 333333333");
                break;
            case Superpowered::AdvancedAudioPlayer::PlayerEvent_OpenFailed: {
                int openError = voicePlayer->getOpenErrorCode();
                __android_log_print(ANDROID_LOG_ERROR, "PlayerExample", "voice Open error %i: %s", openError, Superpowered::AdvancedAudioPlayer::statusCodeToString(openError));
            }
                break;
            case Superpowered::AdvancedAudioPlayer::PlayerEvent_ConnectionLost:
                __android_log_print(ANDROID_LOG_ERROR, "PlayerExample", "Network download failed.");
                break;
            case Superpowered::AdvancedAudioPlayer::PlayerEvent_ProgressiveDownloadFinished:
                __android_log_print(ANDROID_LOG_ERROR, "PlayerExample",
                                    "Download finished. Path: %s",
                                    mrPlayer->getFullyDownloadedFilePath());
                break;
        }

//        __android_log_print(ANDROID_LOG_ERROR, "PlayerExample", "@ mrOpend d. : %s" ,mrOpend && voiceOpend ? "true" : "false");

        if (mrOpend && voiceOpend) {
            return 2;
        } else {
            return 1;
        }
    }
    return -1;
}

void SPInterface::getLastLog() {
    __android_log_print(ANDROID_LOG_DEBUG, "SP_Playing", "=================MR LAST EVENT=================");
    if (mrPlayer != nullptr) {
        __android_log_print(ANDROID_LOG_DEBUG, "SP_Playing", "MR LAST EVENT: %d", mrPlayer->getLatestEvent());

        switch (mrPlayer->getLatestEvent()) {
            case Superpowered::AdvancedAudioPlayer::PlayerEvent_None:
                __android_log_print(ANDROID_LOG_DEBUG, "SP_Playing", "MR LAST EVEN___-> PlayerEvent_None");
                break;
            case Superpowered::AdvancedAudioPlayer::PlayerEvent_Opening:
                __android_log_print(ANDROID_LOG_DEBUG, "SP_Playing", "MR LAST EVENT___-> PlayerEvent_Opening");
                break; // do nothing
            case Superpowered::AdvancedAudioPlayer::PlayerEvent_Opened: {
                __android_log_print(ANDROID_LOG_DEBUG, "SP_Playing", "MR LAST EVENT___-> PlayerEvent_Opened");
            }
                break;
            case Superpowered::AdvancedAudioPlayer::PlayerEvent_OpenFailed: {
                int openError = mrPlayer->getOpenErrorCode();
                __android_log_print(ANDROID_LOG_ERROR, "SP_Playing", "MR LAST EVENT___-> PlayerEvent_OpenFailed-> %i: %s", openError, Superpowered::AdvancedAudioPlayer::statusCodeToString(openError));
            }
                break;
            case Superpowered::AdvancedAudioPlayer::PlayerEvent_ConnectionLost:
                __android_log_print(ANDROID_LOG_DEBUG, "SP_Playing", "MR LAST EVENT___-> PlayerEvent_ConnectionLost");
                break;
            case Superpowered::AdvancedAudioPlayer::PlayerEvent_ProgressiveDownloadFinished:
                __android_log_print(ANDROID_LOG_DEBUG, "SP_Playing", "MR LAST EVENT___-> PlayerEvent_ProgressiveDownloadFinished Path: %s", mrPlayer->getFullyDownloadedFilePath());
                break;
        }

        int openError = mrPlayer->getOpenErrorCode();
        __android_log_print(ANDROID_LOG_ERROR, "SP_Playing", "MR LAST EVENT]]]=> %d /  %i: %s", mrPlayer->getLatestEvent(),openError, Superpowered::AdvancedAudioPlayer::statusCodeToString(openError));

    }

    if (voicePlayer != nullptr) {
        __android_log_print(ANDROID_LOG_DEBUG, "SP_Playing", "VOICE LAST EVENT: %d", voicePlayer->getLatestEvent());
    }
}

    void SPInterface::setMrVolume(float volume) {
        mMrVolumn = volume;
    }

    float SPInterface::getMrVolume() {
        return mMrVolumn;
    }

    float SPInterface::getVoiceVolume() {
        return mVoiceVolumn;
    }

    void SPInterface::setVoiceVolume(float volume) {
        mVoiceVolumn = volume;

    }

    bool SPInterface::getCheckVoice() {
        return isVoiceOn;
    }

    void SPInterface::onBackGround() {
        if (audioSystem != nullptr) {
            audioSystem->onBackground();
        }
    }

    void SPInterface::onForeGround() {
        if (audioSystem != nullptr) {
            audioSystem->onForeground();
        }
    }