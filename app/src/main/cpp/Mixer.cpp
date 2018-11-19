#include "Mixer.h"

void Mixer::renderAudio(int16_t *audioData, int32_t numFrames) {

    // Zero out the incoming container array
    for (int j = 0; j < numFrames; ++j) {
        audioData[j] = 0;
    }

    for (int i = 0; i < mNextFreeTrackIndex; ++i) {
        mTracks[i]->renderAudio(mixingBuffer, numFrames);

        for (int j = 0; j < numFrames; ++j) {
            audioData[j] += mixingBuffer[j];
        }
    }
}

void Mixer::addTrack(SoundRenderer *renderer){
    mTracks[mNextFreeTrackIndex++] = renderer;
};