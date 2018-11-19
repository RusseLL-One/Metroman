#ifndef METROMAN_MIXER_H
#define METROMAN_MIXER_H


#include "SoundRenderer.h"

constexpr int32_t kBufferSize = 192*10; // Temporary buffer is used for mixing
constexpr uint8_t kMaxTracks = 10;

class Mixer {

public:
    void addTrack(SoundRenderer *renderer);
    void renderAudio(int16_t *audioData, int32_t numFrames);

private:

    int16_t *mixingBuffer = new int16_t[kBufferSize];
    SoundRenderer *mTracks[kMaxTracks];
    uint8_t mNextFreeTrackIndex = 0;
};


#endif //METROMAN_MIXER_H
