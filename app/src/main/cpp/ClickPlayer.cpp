#include <android/asset_manager.h>
#include "ClickPlayer.h"
#include <time.h>
#include <android/log.h>


ClickPlayer::ClickPlayer(AAssetManager *assetManager, JavaVM *jvm, jobject lstnr) {
    this->assetManager = assetManager;
    gJavaVM = jvm;
    listener = lstnr;

    rotateClick = new SoundRenderer(assetManager, "rotate_click.raw");

    clickSound = new SoundRenderer(assetManager, "beat1.raw");
    subAccentSound = new SoundRenderer(assetManager, "sub1.raw");
    accentSound = new SoundRenderer(assetManager, "acc1.raw");
    mMixer.addTrack(clickSound);
    mMixer.addTrack(subAccentSound);
    mMixer.addTrack(accentSound);
    mMixer.addTrack(rotateClick);
    interval = 1;

    init();
}

void ClickPlayer::init() {

    oboe::AudioStreamBuilder builder;
    builder.setFormat(oboe::AudioFormat::I16);
    builder.setAudioApi(oboe::AudioApi::Unspecified);
    builder.setDeviceId(oboe::kUnspecified);
    builder.setSampleRate(kSampleRateHz);
    builder.setChannelCount(1);

    builder.setDirection(oboe::Direction::Output);
    builder.setPerformanceMode(oboe::PerformanceMode::LowLatency);
    builder.setSharingMode(oboe::SharingMode::Exclusive);
    builder.setCallback(this);

    oboe::AudioStream *stream;
    oboe::Result result = builder.openStream(&stream);

    if (result == oboe::Result::OK) {
        stream->requestStart();
    }
}

void ClickPlayer::setSoundPreset(int8_t id) {
    char clickSoundName[12];
    char subAccentSoundName[12];
    char accentSoundName[12];

    sprintf(clickSoundName, "beat%d.raw", id);
    sprintf(subAccentSoundName, "sub%d.raw", id);
    sprintf(accentSoundName, "acc%d.raw", id);

    clickSound->reloadAsset(clickSoundName);
    subAccentSound->reloadAsset(subAccentSoundName);
    accentSound->reloadAsset(accentSoundName);
}

void ClickPlayer::setBeatSequence(int8_t seq[], int8_t size) {
    if (beatSequence != nullptr)
        delete beatSequence;

    beatSequence = new int8_t[size];
    memcpy(beatSequence, seq, size * sizeof(int8_t));

    beatsPerBar = size;
}

oboe::DataCallbackResult
ClickPlayer::onAudioReady(oboe::AudioStream *audioStream, void *audioData, int32_t numFrames) {

    if (isPlaying) {

        if (mCurrentFrame % interval == 0) { //click!

            beat++;
            if (beat >= beatsPerBar) {
                beat = 0;
            }

            switch (trainingType) {
                case TEMPO_INCREASING_BY_BARS:
                    if (beat == 0) {

                        if (barCount >= bars) {
                            newBpm += increment;
                            barCount = 0;
                            completionPercentage =
                                    (float) (newBpm - startBpm) / (endBpm - startBpm);
                            if (newBpm >= endBpm) {
                                newBpm = endBpm;
                                completionPercentage = 1;
                                trainingType = NONE;
                            }
                        }
                        barCount++;
                    }
                    break;
                case TEMPO_INCREASING_BY_TIME: {
                    long currentTime = std::chrono::system_clock::now().time_since_epoch().count();

                    completionPercentage = (float) (currentTime - startTime) / timeInterval;
                    newBpm = (startBpm + (int16_t) ((float) bpmInterval * completionPercentage));

                    if (currentTime > endTime) {
                        newBpm = endBpm;
                        completionPercentage = 1;
                        trainingType = NONE;
                    }
                }
                    break;
                case BAR_DROPPING_RANDOM:
                    if (beat == 0) {
                        std::srand(unsigned(
                                std::chrono::system_clock::now().time_since_epoch().count() %
                                1000));
                        int random = std::rand() % 101;
                        isMuted = random <= chance;
                    }
                    break;
                case BAR_DROPPING_BY_COUNT:
                    if (beat == 0) {
                        if (barCount >= normalBars) {
                            isMuted = true;
                            if (barCount >= normalBars + mutedBars) {
                                barCount = 0;
                                isMuted = false;
                            }
                        }
                        barCount++;
                    }
                    break;
                case BEAT_DROPPING: {
                    std::srand(unsigned(
                            std::chrono::system_clock::now().time_since_epoch().count() % 1000));
                    int random = std::rand() % 101;
                    isMuted = random <= chance;
                }
                    break;
                case NONE:
                    isMuted = false;
                    break;
            }

            if (!isMuted) {

                switch (beatSequence[beat]) {
                    case 0: //MUTED
                        break;
                    default:
                    case 1: //BEAT
                        clickSound->setPlaying(true);
                        break;
                    case 2: //SUBACCENT
                        subAccentSound->setPlaying(true);
                        break;
                    case 3: //ACCENT
                        accentSound->setPlaying(true);
                        break;
                }
            }
            if (bpm != newBpm) {
                bpm = newBpm;
                interval = 60 * kSampleRateHz / numFrames / bpm;
            }
            callback_handler();

            mCurrentFrame = 0;
        }
        mCurrentFrame++;
    }

    for (int i = 0; i < numFrames; ++i) {
        mMixer.renderAudio(static_cast<int16_t *>(audioData) + i, 1);
    }

    return oboe::DataCallbackResult::Continue;
}

void ClickPlayer::callback_handler() {
    int status;
    JNIEnv *env;
    bool isAttached = false;

    status = gJavaVM->GetEnv((void **) &env, JNI_VERSION_1_6);
    if (status < 0) {
        status = gJavaVM->AttachCurrentThread(&env, NULL);
        if (status < 0) {
            return;
        }

        isAttached = true;
    }

    jclass interfaceClass = env->GetObjectClass(listener);
    if (!interfaceClass) {
        if (isAttached) gJavaVM->DetachCurrentThread();
        return;
    }

    jmethodID method = env->GetMethodID(
            interfaceClass, "onTick", "(IIF)V");
    if (!method) {
        if (isAttached) gJavaVM->DetachCurrentThread();
        return;
    }
    env->CallVoidMethod(listener, method, beat, bpm, trainingType, completionPercentage);
    if (isAttached) gJavaVM->DetachCurrentThread();
}

void ClickPlayer::setBpm(int16_t bpm) {
    this->newBpm = bpm;
}

void ClickPlayer::play() {
    isPlaying = true;
}

void ClickPlayer::stop() {
    isPlaying = false;
    mCurrentFrame = 0;
    isMuted = false;
    trainingType = NONE;
    beat = -1;
    barCount = 0;
    completionPercentage = 0;
}

void ClickPlayer::startTempoIncreasingByBars(int16_t startBpm, int16_t endBpm, int8_t bars,
                                             int16_t increment) {
    trainingType = TEMPO_INCREASING_BY_BARS;
    this->startBpm = startBpm;
    this->endBpm = endBpm;
    this->bars = bars;
    this->increment = increment;
    newBpm = startBpm;
}

void ClickPlayer::startTempoIncreasingByTime(int16_t startBpm, int16_t endBpm, int16_t minutes) {
    trainingType = TEMPO_INCREASING_BY_TIME;
    this->startBpm = startBpm;
    this->endBpm = endBpm;

    startTime = std::chrono::system_clock::now().time_since_epoch().count();
    endTime = startTime + minutes * 60 * 1000 * 1000;
    timeInterval = endTime - startTime;
    bpmInterval = endBpm - startBpm;
    newBpm = startBpm;
}

void ClickPlayer::startBarDroppingByRandom(int8_t chance) {
    trainingType = BAR_DROPPING_RANDOM;
    this->chance = chance;
    completionPercentage = (float) chance / 100;
}

void ClickPlayer::startBarDroppingByCount(int8_t normalBars, int8_t mutedBars) {
    trainingType = BAR_DROPPING_BY_COUNT;
    this->normalBars = normalBars;
    this->mutedBars = mutedBars;
}

void ClickPlayer::startBeatDropping(int8_t chance) {
    trainingType = BEAT_DROPPING;
    this->chance = chance;
    completionPercentage = (float) chance / 100;
}

void ClickPlayer::playRotateClick() {
    rotateClick->setPlaying(true);
}
