#include <jni.h>
#include <cstdio>
#include <cstdlib>
#include <cstring>
#include <random>
#include <android/log.h>
#include <sys/time.h>

extern "C"
JNIEXPORT jstring JNICALL
Java_moe_fuqiuluo_xposed_actions_PullConfig_testNativeLibrary(JNIEnv *env, jobject thiz) {
    return env->NewStringUTF("加载Shamrock库成功~");
}
