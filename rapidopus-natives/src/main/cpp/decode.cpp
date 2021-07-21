#include <jni.h>

#include "com_walker_rapidopus_OpusDecoder.h"
#include <opus/opus.h>

JNIEXPORT jlong JNICALL Java_com_walker_rapidopus_OpusDecoder_decoderCreate(JNIEnv *env, jclass jClazz, jint jSampleRate, jint jChannels) {
    int err = OPUS_OK;

    auto *decoder = opus_decoder_create(jSampleRate, jChannels, &err);

    if(err != OPUS_OK) {
        env->ThrowNew(env->FindClass("java/lang/IllegalArgumentException"), opus_strerror(err));
    }

    return reinterpret_cast<jlong>(decoder);
}

JNIEXPORT void JNICALL Java_com_walker_rapidopus_OpusDecoder_decoderDestroy(JNIEnv *env, jclass jClazz, jlong pDecoder) {
    opus_decoder_destroy(reinterpret_cast<OpusDecoder *>(pDecoder));
}

JNIEXPORT jint JNICALL Java_com_walker_rapidopus_OpusDecoder_decode__J_3BLjava_nio_ByteBuffer_2II
    (JNIEnv *env, jclass jClazz, jlong pDecoder, jbyteArray jInputData, jobject jOutBuffer, jint jFrameSize, jint jDecodeFec) {

    jboolean isCopy = JNI_FALSE;
    int inSize = env->GetArrayLength(jInputData);
    jbyte *inData = env->GetByteArrayElements(jInputData, &isCopy);

    auto *outData = reinterpret_cast<opus_int16 *>(env->GetDirectBufferAddress(jOutBuffer));

    int ret = opus_decode(reinterpret_cast<OpusDecoder *>(pDecoder), reinterpret_cast<unsigned char *>(inData), inSize, outData, jFrameSize, jDecodeFec);

    env->ReleaseByteArrayElements(jInputData, inData, JNI_ABORT);

    return ret;
}

JNIEXPORT jint JNICALL Java_com_walker_rapidopus_OpusDecoder_decode__JLjava_nio_ByteBuffer_2Ljava_nio_ByteBuffer_2II
    (JNIEnv *env, jclass jClazz, jlong pDecoder, jobject jInputBuffer, jobject jOutputBuffer, jint jFrameSize, jint jDecodeFec) {

    auto *inData = reinterpret_cast<unsigned char *>(env->GetDirectBufferAddress(jInputBuffer));
    int inSize = env->GetDirectBufferCapacity(jInputBuffer);
    auto *outData = reinterpret_cast<opus_int16 *>(env->GetDirectBufferAddress(jOutputBuffer));

    return opus_decode(reinterpret_cast<OpusDecoder *>(pDecoder), inData, inSize, outData, jFrameSize, jDecodeFec);
}

JNIEXPORT jint JNICALL Java_com_walker_rapidopus_OpusDecoder_decodeFloat__J_3BLjava_nio_ByteBuffer_2II
    (JNIEnv *env, jclass jClazz, jlong pDecoder, jbyteArray jInputData, jobject jOutputBuffer, jint jFrameSize, jint jDecodeFec) {

    jboolean isCopy = JNI_FALSE;
    int inSize = env->GetArrayLength(jInputData);
    jbyte *inData = env->GetByteArrayElements(jInputData, &isCopy);

    auto *outData = reinterpret_cast<float *>(env->GetDirectBufferAddress(jOutputBuffer));

    int ret = opus_decode_float(reinterpret_cast<OpusDecoder *>(pDecoder), reinterpret_cast<unsigned char *>(inData), inSize, outData, jFrameSize, jDecodeFec);

    env->ReleaseByteArrayElements(jInputData, inData, JNI_ABORT);

    return ret;
}

JNIEXPORT jint JNICALL Java_com_walker_rapidopus_OpusDecoder_decodeFloat__JLjava_nio_ByteBuffer_2Ljava_nio_ByteBuffer_2II
    (JNIEnv *env, jclass jClazz, jlong pDecoder, jobject jInputBuffer, jobject jOutputBuffer, jint jFrameSize, jint jDecodeFec) {

    auto *inData = reinterpret_cast<unsigned char *>(env->GetDirectBufferAddress(jInputBuffer));
    int inSize = env->GetDirectBufferCapacity(jInputBuffer);
    auto *outData = reinterpret_cast<float *>(env->GetDirectBufferAddress(jOutputBuffer));

    return opus_decode_float(reinterpret_cast<OpusDecoder *>(pDecoder), inData, inSize, outData, jFrameSize, jDecodeFec);
}

JNIEXPORT jint JNICALL Java_com_walker_rapidopus_OpusDecoder_decoderGetNbSamples__J_3B(JNIEnv *env, jclass jClazz, jlong pDecoder, jbyteArray jInputData) {
    jboolean isCopy = JNI_FALSE;
    int inSize = env->GetArrayLength(jInputData);
    jbyte *inData = env->GetByteArrayElements(jInputData, &isCopy);

    int ret = opus_decoder_get_nb_samples(reinterpret_cast<OpusDecoder *>(pDecoder), reinterpret_cast<unsigned char *>(inData), inSize);

    env->ReleaseByteArrayElements(jInputData, inData, JNI_ABORT);

    return ret;
}

JNIEXPORT jint JNICALL Java_com_walker_rapidopus_OpusDecoder_decoderGetNbSamples__JLjava_nio_ByteBuffer_2(JNIEnv *env, jclass jClazz, jlong pDecoder, jobject jInputBuffer) {
    auto *inData = reinterpret_cast<unsigned char *>(env->GetDirectBufferAddress(jInputBuffer));
    int inSize = env->GetDirectBufferCapacity(jInputBuffer);

    return opus_decoder_get_nb_samples(reinterpret_cast<OpusDecoder *>(pDecoder), inData, inSize);
}

JNIEXPORT jint JNICALL Java_com_walker_rapidopus_OpusDecoder_packetGetBandwidth___3B(JNIEnv *env, jclass jClazz, jbyteArray jInputData) {
    jboolean isCopy = JNI_FALSE;
    jbyte *inData = env->GetByteArrayElements(jInputData, &isCopy);

    int ret = opus_packet_get_bandwidth(reinterpret_cast<unsigned char *>(inData));

    env->ReleaseByteArrayElements(jInputData, inData, JNI_ABORT);

    return ret;
}

JNIEXPORT jint JNICALL Java_com_walker_rapidopus_OpusDecoder_packetGetBandwidth__Ljava_nio_ByteBuffer_2(JNIEnv *env, jclass jClazz, jobject jInputBuffer) {
    auto *inData = reinterpret_cast<unsigned char *>(env->GetDirectBufferAddress(jInputBuffer));

    return opus_packet_get_bandwidth(inData);
}

JNIEXPORT jint JNICALL Java_com_walker_rapidopus_OpusDecoder_packetGetNbChannels___3B(JNIEnv *env, jclass jClazz, jbyteArray jInputData) {
    jboolean isCopy = JNI_FALSE;
    jbyte *inData = env->GetByteArrayElements(jInputData, &isCopy);

    int ret = opus_packet_get_nb_channels(reinterpret_cast<unsigned char *>(inData));

    env->ReleaseByteArrayElements(jInputData, inData, JNI_ABORT);

    return ret;
}

JNIEXPORT jint JNICALL Java_com_walker_rapidopus_OpusDecoder_packetGetNbChannels__Ljava_nio_ByteBuffer_2(JNIEnv *env, jclass jClazz, jobject jInputBuffer) {
    auto *inData = reinterpret_cast<unsigned char *>(env->GetDirectBufferAddress(jInputBuffer));

    return opus_packet_get_nb_channels(inData);
}

JNIEXPORT jint JNICALL Java_com_walker_rapidopus_OpusDecoder_packetGetNbFrames___3B(JNIEnv *env, jclass jClazz, jbyteArray jInputData) {
    jboolean isCopy = JNI_FALSE;
    int inSize = env->GetArrayLength(jInputData);
    jbyte *inData = env->GetByteArrayElements(jInputData, &isCopy);

    int ret = opus_packet_get_nb_frames(reinterpret_cast<unsigned char *>(inData), inSize);

    env->ReleaseByteArrayElements(jInputData, inData, JNI_ABORT);

    return ret;
}

JNIEXPORT jint JNICALL Java_com_walker_rapidopus_OpusDecoder_packetGetNbFrames__Ljava_nio_ByteBuffer_2(JNIEnv *env, jclass jClazz, jobject jInputBuffer) {
    auto *inData = reinterpret_cast<unsigned char *>(env->GetDirectBufferAddress(jInputBuffer));
    int inSize = env->GetDirectBufferCapacity(jInputBuffer);

    return opus_packet_get_nb_frames(inData, inSize);
}

JNIEXPORT jint JNICALL Java_com_walker_rapidopus_OpusDecoder_packetGetNbSamples___3BI(JNIEnv *env, jclass jClazz, jbyteArray jInputData, jint jSampleRate) {
    jboolean isCopy = JNI_FALSE;
    int inSize = env->GetArrayLength(jInputData);
    jbyte *inData = env->GetByteArrayElements(jInputData, &isCopy);

    int ret = opus_packet_get_nb_samples(reinterpret_cast<unsigned char *>(inData), inSize, jSampleRate);

    env->ReleaseByteArrayElements(jInputData, inData, JNI_ABORT);

    return ret;
}

JNIEXPORT jint JNICALL Java_com_walker_rapidopus_OpusDecoder_packetGetNbSamples__Ljava_nio_ByteBuffer_2I(JNIEnv *env, jclass jClazz, jobject jInputBuffer, jint jSampleRate) {
    auto *inData = reinterpret_cast<unsigned char *>(env->GetDirectBufferAddress(jInputBuffer));
    int inSize = env->GetDirectBufferCapacity(jInputBuffer);

    return opus_packet_get_nb_samples(inData, inSize, jSampleRate);
}

JNIEXPORT jint JNICALL Java_com_walker_rapidopus_OpusDecoder_packetGetSamplesPerFrame___3BI(JNIEnv *env, jclass jClazz, jbyteArray jInputData, jint jSampleRate) {
    jboolean isCopy = JNI_FALSE;
    jbyte *inData = env->GetByteArrayElements(jInputData, &isCopy);

    int ret = opus_packet_get_samples_per_frame(reinterpret_cast<unsigned char *>(inData), jSampleRate);

    env->ReleaseByteArrayElements(jInputData, inData, JNI_ABORT);

    return ret;
}

JNIEXPORT jint JNICALL Java_com_walker_rapidopus_OpusDecoder_packetGetSamplesPerFrame__Ljava_nio_ByteBuffer_2I(JNIEnv *env, jclass jClazz, jobject jInputBuffer, jint jSampleRate) {
    auto *inData = reinterpret_cast<unsigned char *>(env->GetDirectBufferAddress(jInputBuffer));
    int inSize = env->GetDirectBufferCapacity(jInputBuffer);

    return opus_packet_get_samples_per_frame(inData, jSampleRate);
}

JNIEXPORT void JNICALL Java_com_walker_rapidopus_OpusDecoder_nPcmSoftClip___3FII_3F(JNIEnv *env, jclass jClazz, jfloatArray jPcmData, jint jFrameSize, jint jChannels, jfloatArray jMem) {
    jboolean isCopy = JNI_FALSE;
    jfloat *pcmData = env->GetFloatArrayElements(jPcmData, &isCopy);
    jfloat *mem = env->GetFloatArrayElements(jMem, &isCopy);

    opus_pcm_soft_clip(pcmData, jFrameSize, jChannels, mem);

    env->ReleaseFloatArrayElements(jMem, mem, JNI_ABORT);
    env->ReleaseFloatArrayElements(jPcmData, pcmData, JNI_ABORT);
}

JNIEXPORT void JNICALL Java_com_walker_rapidopus_OpusDecoder_nPcmSoftClip__Ljava_nio_ByteBuffer_2II_3F
    (JNIEnv *env, jclass jClazz, jobject jPcmBuffer, jint jFrameSize, jint jChannels, jfloatArray jMem) {

    jboolean isCopy = JNI_FALSE;
    auto *pcmData = reinterpret_cast<float *>(env->GetDirectBufferAddress(jPcmBuffer));
    jfloat *mem = env->GetFloatArrayElements(jMem, &isCopy);

    opus_pcm_soft_clip(pcmData, jFrameSize, jChannels, mem);

    env->ReleaseFloatArrayElements(jMem, mem, JNI_ABORT);
}

JNIEXPORT void JNICALL Java_com_walker_rapidopus_OpusDecoder_nPcmSoftClip___3FIILjava_nio_ByteBuffer_2
    (JNIEnv *env, jclass jClazz, jfloatArray jPcmData, jint jFrameSize, jint jChannels, jobject jMemBuffer) {

    jboolean isCopy = JNI_FALSE;
    jfloat *pcmData = env->GetFloatArrayElements(jPcmData, &isCopy);
    auto *mem = reinterpret_cast<float *>(env->GetDirectBufferAddress(jMemBuffer));

    opus_pcm_soft_clip(pcmData, jFrameSize, jChannels, mem);

    env->ReleaseFloatArrayElements(jPcmData, pcmData, JNI_ABORT);
}

JNIEXPORT void JNICALL Java_com_walker_rapidopus_OpusDecoder_nPcmSoftClip__Ljava_nio_ByteBuffer_2IILjava_nio_ByteBuffer_2
    (JNIEnv *env, jclass jClazz, jobject jPcmBuffer, jint jFrameSize, jint jChannels, jobject jMemBuffer) {

    auto *pcmData = reinterpret_cast<float *>(env->GetDirectBufferAddress(jPcmBuffer));
    auto *mem = reinterpret_cast<float *>(env->GetDirectBufferAddress(jMemBuffer));

    opus_pcm_soft_clip(pcmData, jFrameSize, jChannels, mem);
}

JNIEXPORT void JNICALL Java_com_walker_rapidopus_OpusDecoder_decoderResetState(JNIEnv *env, jclass jClazz, jlong pDecoder) {
    opus_decoder_ctl(reinterpret_cast<OpusDecoder *>(pDecoder), OPUS_RESET_STATE);
}

JNIEXPORT jint JNICALL Java_com_walker_rapidopus_OpusDecoder_decoderGetBandwidth(JNIEnv *env, jclass jClazz, jlong pDecoder) {
    opus_int32 bw;
    opus_decoder_ctl(reinterpret_cast<OpusDecoder *>(pDecoder), OPUS_GET_BANDWIDTH(&bw));
    return bw;
}

JNIEXPORT jint JNICALL Java_com_walker_rapidopus_OpusDecoder_decoderGetSampleRate(JNIEnv *env, jclass jClazz, jlong pDecoder) {
    opus_int32 sampleRate;
    opus_decoder_ctl(reinterpret_cast<OpusDecoder *>(pDecoder), OPUS_GET_SAMPLE_RATE(&sampleRate));
    return sampleRate;
}

JNIEXPORT jint JNICALL Java_com_walker_rapidopus_OpusDecoder_decoderGetGain(JNIEnv *env, jclass jClazz, jlong pDecoder) {
    opus_int32 gain;
    opus_decoder_ctl(reinterpret_cast<OpusDecoder *>(pDecoder), OPUS_GET_GAIN(&gain));
    return gain;
}

JNIEXPORT void JNICALL Java_com_walker_rapidopus_OpusDecoder_decoderSetGain(JNIEnv *env, jclass jClazz, jlong pDecoder, jint jGain) {
    opus_decoder_ctl(reinterpret_cast<OpusDecoder *>(pDecoder), OPUS_SET_GAIN(jGain));
}

JNIEXPORT jint JNICALL Java_com_walker_rapidopus_OpusDecoder_decoderGetLastFramePitch(JNIEnv *env, jclass jClazz, jlong pDecoder) {
    opus_int32 pitch;
    opus_decoder_ctl(reinterpret_cast<OpusDecoder *>(pDecoder), OPUS_GET_PITCH(&pitch));
    return pitch;
}

JNIEXPORT jint JNICALL Java_com_walker_rapidopus_OpusDecoder_decoderGetLastPacketDuration(JNIEnv *env, jclass jClazz, jlong pDecoder) {
    opus_int32 duration;
    opus_decoder_ctl(reinterpret_cast<OpusDecoder *>(pDecoder), OPUS_GET_LAST_PACKET_DURATION(&duration));
    return duration;
}
