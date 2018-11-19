#ifndef METROMAN_SOUNDRENDERER_H
#define METROMAN_SOUNDRENDERER_H

#include <cstdint>
#include <array>

#include <chrono>
#include <memory>
#include <atomic>

#include <android/asset_manager.h>

class SoundRenderer {

public:
    SoundRenderer(AAssetManager *assetManager, const char * filename);
    void renderAudio(int16_t *targetData, int32_t numFrames);
    void resetPlayHead() { mReadFrameIndex = 0; };
    void setPlaying(bool isPlaying) { mIsPlaying = isPlaying; resetPlayHead(); };
    void reloadAsset(const char *filename);

private:
    AAssetManager *assetManager = nullptr;
    int32_t mReadFrameIndex = 0;
    const int16_t* mData = nullptr;
    int32_t mTotalFrames = 0;
    std::atomic<bool> mIsPlaying { false };
};

#endif //METROMAN_SOUNDRENDERER_H
