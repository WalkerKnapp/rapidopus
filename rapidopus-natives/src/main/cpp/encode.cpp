#include <jni.h>

#include "com_walker_rapidopus_OpusEncoder.h"
#include <opus/opus.h>

JNIEXPORT jlong JNICALL Java_com_walker_rapidopus_OpusEncoder_encoderCreate(JNIEnv *env, jclass jClazz, jint jSampleRate, jint jChannels, jint jApplicationMode) {
    int error = OPUS_OK;

    auto *encoder = opus_encoder_create(jSampleRate, jChannels, jApplicationMode, &error);

    if(error != OPUS_OK) {
        env->ThrowNew(env->FindClass("java/lang/IllegalArgumentException"), opus_strerror(error));
    }

    return reinterpret_cast<jlong>(encoder);
}

JNIEXPORT void JNICALL Java_com_walker_rapidopus_OpusEncoder_encoderDestroy(JNIEnv *env, jclass jClazz, jlong pEncoder) {
    opus_encoder_destroy(reinterpret_cast<OpusEncoder *>(pEncoder));
}

JNIEXPORT jint JNICALL Java_com_walker_rapidopus_OpusEncoder_encode__J_3SILjava_nio_ByteBuffer_2
    (JNIEnv *env, jclass jClazz, jlong pEncoder, jshortArray jInputData, jint jFrameSize, jobject jOutputBuffer) {

    jboolean isCopy = JNI_FALSE;
    jshort *inData = env->GetShortArrayElements(jInputData, &isCopy);

    int outSize = env->GetDirectBufferCapacity(jOutputBuffer);
    auto *outData = reinterpret_cast<unsigned char *>(env->GetDirectBufferAddress(jOutputBuffer));

    int ret = opus_encode(reinterpret_cast<OpusEncoder *>(pEncoder), inData, jFrameSize, outData, outSize);

    env->ReleaseShortArrayElements(jInputData, inData, JNI_ABORT);

    return ret;
}

JNIEXPORT jint JNICALL Java_com_walker_rapidopus_OpusEncoder_encode__JLjava_nio_ByteBuffer_2ILjava_nio_ByteBuffer_2
    (JNIEnv *env, jclass jClazz, jlong pEncoder, jobject jInputBuffer, jint jFrameSize, jobject jOutputBuffer) {

    auto *inData = reinterpret_cast<short *>(env->GetDirectBufferAddress(jInputBuffer));
    int outSize = env->GetDirectBufferCapacity(jOutputBuffer);
    auto *outData = reinterpret_cast<unsigned char *>(env->GetDirectBufferCapacity(jOutputBuffer));

    return opus_encode(reinterpret_cast<OpusEncoder *>(pEncoder), inData, jFrameSize, outData, outSize);
}

JNIEXPORT jint JNICALL Java_com_walker_rapidopus_OpusEncoder_encodeFloat__J_3FILjava_nio_ByteBuffer_2
    (JNIEnv *env, jclass jClazz, jlong pEncoder, jfloatArray jInputData, jint jFrameSize, jobject jOutputBuffer) {

    jboolean isCopy = JNI_FALSE;
    jfloat *inData = env->GetFloatArrayElements(jInputData, &isCopy);

    int outSize = env->GetDirectBufferCapacity(jOutputBuffer);
    auto *outData = reinterpret_cast<unsigned char *>(env->GetDirectBufferAddress(jOutputBuffer));

    int ret = opus_encode_float(reinterpret_cast<OpusEncoder *>(pEncoder), inData, jFrameSize, outData, outSize);

    env->ReleaseFloatArrayElements(jInputData, inData, JNI_ABORT);

    return ret;
}

JNIEXPORT jint JNICALL Java_com_walker_rapidopus_OpusEncoder_encodeFloat__JLjava_nio_ByteBuffer_2ILjava_nio_ByteBuffer_2
    (JNIEnv *env, jclass jClazz, jlong pEncoder, jobject jInputBuffer, jint jFrameSize, jobject jOutputBuffer) {

    auto *inData = reinterpret_cast<float *>(env->GetDirectBufferAddress(jInputBuffer));
    int outSize = env->GetDirectBufferCapacity(jOutputBuffer);
    auto *outData = reinterpret_cast<unsigned char *>(env->GetDirectBufferCapacity(jOutputBuffer));

    return opus_encode_float(reinterpret_cast<OpusEncoder *>(pEncoder), inData, jFrameSize, outData, outSize);
}
