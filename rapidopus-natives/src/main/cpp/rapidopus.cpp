#include <jni.h>

#include "com_walker_rapidopus_RapidOpus.h"
#include <opus/opus.h>

JNIEXPORT jstring JNICALL Java_com_walker_rapidopus_RapidOpus_getVersion(JNIEnv *env, jclass jClazz) {
    const char *versionString = opus_get_version_string();

    return env->NewStringUTF(versionString);
}
