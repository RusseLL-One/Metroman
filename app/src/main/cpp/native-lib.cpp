#include <jni.h>
#include <memory>
#include <oboe/Oboe.h>
#include <android/asset_manager_jni.h>
#include <android/log.h>
#include "ClickPlayer.h"

extern "C" {

std::unique_ptr<ClickPlayer> player;

JNIEXPORT void JNICALL
Java_com_one_russell_metroman_TickService_native_1init(
        JNIEnv *env,
        jobject instance,
        jobject jAssetManager) {

    JavaVM *jvm;
    env->GetJavaVM( &jvm );

    jobject ref = env->NewGlobalRef(instance);

    AAssetManager *assetManager = AAssetManager_fromJava(env, jAssetManager);
    player = std::make_unique<ClickPlayer>(assetManager, jvm, ref);
}

JNIEXPORT void JNICALL
Java_com_one_russell_metroman_TickService_native_1set_1beatsequence(
        JNIEnv *env,
        jobject instance,
        jobject sequence) {
    jclass ArrayList_class = env->GetObjectClass(sequence);
    jmethodID ArrayList_get_id = env->GetMethodID(ArrayList_class, "get", "(I)Ljava/lang/Object;");
    jmethodID ArrayList_size_id = env->GetMethodID(ArrayList_class, "size", "()I");
    int8_t size = (int8_t)env->CallIntMethod(sequence, ArrayList_size_id);

    int8_t seq[size];
    for(int8_t i=0; i<size; i++) {
        jobject item = (jobject)env->CallObjectMethod(sequence, ArrayList_get_id, i);
        jclass BeatType_class = env->GetObjectClass(item);
        jmethodID BeatType_ordinal = env->GetMethodID(BeatType_class, "ordinal", "()I");
        int8_t value = (int8_t)env->CallIntMethod(item, BeatType_ordinal);
        seq[i] = value;
    }

    player->setBeatSequence(seq, size);
}

JNIEXPORT void JNICALL
Java_com_one_russell_metroman_TickService_native_1start_1clicking(
        JNIEnv *env,
        jobject instance) {
    player->play();
}

JNIEXPORT void JNICALL
Java_com_one_russell_metroman_TickService_native_1stop_1clicking(
        JNIEnv *env,
        jobject instance) {
    player->stop();
}

JNIEXPORT void JNICALL
Java_com_one_russell_metroman_TickService_native_1set_1bpm(
        JNIEnv *pEnv,
        jobject pThis,
        int16_t bpm) {
    player->setBpm(bpm);
}

JNIEXPORT void JNICALL
Java_com_one_russell_metroman_TickService_native_1set_1soundpreset(
        JNIEnv *pEnv,
        jobject pThis,
        int8_t id) {
    player->setSoundPreset(id);
}

JNIEXPORT void JNICALL
Java_com_one_russell_metroman_TickService_native_1start_1tempo_1increasing_1by_1bars(
        JNIEnv *pEnv,
        jobject pThis,
        int16_t startBpm,
        int16_t endBpm,
        int8_t bars,
        int16_t increment) {
    player->startTempoIncreasingByBars(startBpm, endBpm, bars, increment);
}

JNIEXPORT void JNICALL
Java_com_one_russell_metroman_TickService_native_1start_1tempo_1increasing_1by_1time(
        JNIEnv *pEnv,
        jobject pThis,
        int16_t startBpm,
        int16_t endBpm,
        int16_t minutes) {
    player->startTempoIncreasingByTime(startBpm, endBpm, minutes);
}

JNIEXPORT void JNICALL
Java_com_one_russell_metroman_TickService_native_1start_1bar_1dropping_1by_1random(
        JNIEnv *pEnv,
        jobject pThis,
        int8_t chance) {
    player->startBarDroppingByRandom(chance);
}

JNIEXPORT void JNICALL
Java_com_one_russell_metroman_TickService_native_1start_1bar_1dropping_1by_1count(
        JNIEnv *pEnv,
        jobject pThis,
        int8_t normalBars,
        int8_t mutedBars) {
    player->startBarDroppingByCount(normalBars, mutedBars);
}

JNIEXPORT void JNICALL
Java_com_one_russell_metroman_TickService_native_1start_1beat_1dropping(
        JNIEnv *pEnv,
        jobject pThis,
        int8_t chance) {
    player->startBeatDropping(chance);
}

JNIEXPORT void JNICALL
Java_com_one_russell_metroman_MainActivity_native_1tap_1click(
        JNIEnv *pEnv,
        jobject pThis) {
    player->playRotateClick();
}

JNIEXPORT void JNICALL
Java_com_one_russell_metroman_views_RotaryKnobView_native_1rotate_1click(
        JNIEnv *pEnv,
        jobject pThis) {
    player->playRotateClick();
}
}