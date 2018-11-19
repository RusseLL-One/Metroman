#include <android/log.h>
#include "SoundRenderer.h"

void SoundRenderer::renderAudio(int16_t *targetData, int32_t numFrames){

    if (mIsPlaying){

        // Check whether we're about to reach the end of the recording
        if (mReadFrameIndex + numFrames >= mTotalFrames){
            numFrames = mTotalFrames - mReadFrameIndex;
            mIsPlaying = false;
        }

        for (int i = 0; i < numFrames; ++i) {
            targetData[i] = mData[mReadFrameIndex];

            // Increment and handle wraparound
            if (++mReadFrameIndex >= mTotalFrames) mReadFrameIndex = 0;
        }

    } else {
        // fill with zeros to output silence
        for (int i = 0; i < numFrames; ++i) {
            targetData[i] = 0;
        }
    }
}

SoundRenderer::SoundRenderer(AAssetManager *assetManager, const char *filename) {

    this->assetManager = assetManager;

    // Load the backing track
    AAsset* asset = AAssetManager_open(assetManager, filename, AASSET_MODE_BUFFER);

    if (asset == nullptr){
        return;
    }

    // Get the length of the track (we assume it is stereo 48kHz)
    off_t trackLength = AAsset_getLength(asset);

    // Load it into memory
    mData = static_cast<const int16_t*>(AAsset_getBuffer(asset));

    if (mData == nullptr){
        return;
    }

    // There are 4 bytes per frame because
    // each sample is 2 bytes and
    // it's a stereo recording which has 2 samples per frame.
    mTotalFrames = static_cast<int32_t>(trackLength / 2);
}

void SoundRenderer::reloadAsset(const char *filename) {
    AAsset* asset = AAssetManager_open(assetManager, filename, AASSET_MODE_BUFFER);

    if (asset == nullptr){
        return;
    }

    off_t trackLength = AAsset_getLength(asset);

    delete mData;
    mData = static_cast<const int16_t*>(AAsset_getBuffer(asset));

    if (mData == nullptr){
        return;
    }

    mTotalFrames = static_cast<int32_t>(trackLength / 2);
}
