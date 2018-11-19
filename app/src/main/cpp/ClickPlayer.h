#ifndef METROMAN_CLICKPLAYER_H
#define METROMAN_CLICKPLAYER_H
#include <oboe/Oboe.h>
#include <jni.h>
#include <stdlib.h>
#include "SoundRenderer.h"
#include "Mixer.h"

enum TrainingType {
    TEMPO_INCREASING_BY_BARS,
    TEMPO_INCREASING_BY_TIME,
    BAR_DROPPING_RANDOM,
    BAR_DROPPING_BY_COUNT,
    BEAT_DROPPING,
    NONE
};

class ClickPlayer : public oboe::AudioStreamCallback {
public:
    ClickPlayer(AAssetManager *assetManager, JavaVM *jvm, jobject lstnr);

    void init();

    void play();
    void stop();

    void callback_handler();

    void setBpm(int16_t bpm);
    void setBeatSequence(int8_t seq[], int8_t size);
    void setSoundPreset(int8_t id);
    void playRotateClick();

        oboe::DataCallbackResult
    onAudioReady(oboe::AudioStream *audioStream, void *audioData, int32_t numFrames);

        //Training
    void startTempoIncreasingByBars(int16_t startBpm, int16_t endBpm, int8_t bars, int16_t increment);
    void startTempoIncreasingByTime(int16_t startBpm, int16_t endBpm, int16_t minutes);
    void startBarDroppingByRandom(int8_t chance);
    void startBarDroppingByCount(int8_t normalBars, int8_t mutedBars);
    void startBeatDropping(int8_t chance);
private:
    AAssetManager *assetManager;
    SoundRenderer *accentSound{nullptr};
    SoundRenderer *subAccentSound{nullptr};
    SoundRenderer *clickSound{nullptr};
    SoundRenderer *rotateClick{nullptr};
    Mixer mMixer;
    int64_t mCurrentFrame = 0;
    int64_t interval = 0;
    bool isPlaying = false;
    TrainingType trainingType = NONE;
    int16_t bpm = 0;
    int16_t newBpm = 500;
    int kSampleRateHz = 44100;
    bool isMuted = false;

    int8_t barCount = 0;
    int8_t beatsPerBar = 4;
    int8_t beat = -1;
    int8_t *beatSequence = nullptr;


    JavaVM *gJavaVM;
    jobject listener;
    jmethodID method;

    //Training
    int16_t startBpm;
    int16_t endBpm;
    int8_t bars;
    int16_t increment;

    long startTime;
    long endTime;
    long timeInterval;
    int16_t bpmInterval;
    float completionPercentage = 0;

    int8_t chance;

    int8_t normalBars;
    int8_t mutedBars;
};

#endif //METROMAN_CLICKPLAYER_H
