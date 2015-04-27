/*
 * VmUtil.cpp
 *
 *  Created on: 27-04-2015
 *      Author: Tomek
 */

#include "VmUtil.h"
#include <jni.h>
#include <stddef.h>
#include <android/log.h>

JavaVM *cached_jvm;
JNIEnv *cached_env;

void VmUtil::dumpReferenceTables(JNIEnv* env)
{
    jclass vm_debug_class_local = env->FindClass("dalvik/system/VMDebug");
    if (vm_debug_class_local)
    {
        jclass vm_debug_class_global = (jclass) env->NewGlobalRef(vm_debug_class_local);
        env->DeleteLocalRef(vm_debug_class_local);
        jmethodID dumpReferenceTables_mid = env->GetStaticMethodID(vm_debug_class_local, "dumpReferenceTables", "()V");

        if (dumpReferenceTables_mid)
        {
            env->CallStaticVoidMethod(vm_debug_class_local, dumpReferenceTables_mid);
        }

        env->DeleteGlobalRef(vm_debug_class_global);
    }
}

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *jvm, void *reserved)
{
    jint onLoad_err = -1;
    jclass k;
    cached_jvm = jvm;

    // Checks JNI version
    if (jvm->GetEnv((void**) &cached_env, JNI_VERSION_1_6) != JNI_OK)
    {
        __android_log_print(ANDROID_LOG_INFO, "##_JNI", "JNI_OnLoad: error 1");
        return onLoad_err;
    }

    // Checks if environment is null
    if (cached_env == NULL)
    {
        __android_log_print(ANDROID_LOG_INFO, "##_JNI", "JNI_OnLoad: error 2");
        return onLoad_err;
    }

    __android_log_print(ANDROID_LOG_INFO, "##_JNI", "JNI_OnLoad: OK");

    return JNI_VERSION_1_6;
}

JNIEnv* VmUtil::pInitialize_JNI()
{
    __android_log_print(ANDROID_LOG_INFO, "##_JNI", "pInitialize_JNI: Create new reference to JNIEnv");

    JNIEnv *env;
    int getEnvStat = cached_jvm->GetEnv((void **) &env, JNI_VERSION_1_6);

    if (getEnvStat == JNI_EDETACHED)
    {
        __android_log_print(ANDROID_LOG_INFO, "##_JNI", "pInitialize_JNI: JNI_EDETACHED, attaching...");

        if(!cached_jvm) {
            __android_log_print(ANDROID_LOG_INFO, "##_JNI", "cached_jvm is bad");
        }
        else if (cached_jvm->AttachCurrentThread(&cached_env, NULL) != 0)
        {
            __android_log_print(ANDROID_LOG_INFO, "##_JNI", "pInitialize_JNI: attaching failed");
        }
    }
    else if (getEnvStat == JNI_OK)
    {
        __android_log_print(ANDROID_LOG_INFO, "##_JNI", "pInitialize_JNI: ATTACHED");
    }
    else if (getEnvStat == JNI_EVERSION)
    {
        __android_log_print(ANDROID_LOG_INFO, "##_JNI", "JNI version not supported");
    }

    return cached_env;
}

void VmUtil::UnInitialize_JNI()
{
    __android_log_print(ANDROID_LOG_INFO, "##_JNI", "pUnInitialize_JNI");

    JNIEnv *env;
    int getEnvStat = cached_jvm->GetEnv((void **) &env, JNI_VERSION_1_6);

    if (getEnvStat == JNI_EDETACHED)
    {
        __android_log_print(ANDROID_LOG_INFO, "##_JNI", "pUnInitialize_JNI: JNI_EDETACHED");

    }
    else if (getEnvStat == JNI_OK)
    {
        __android_log_print(ANDROID_LOG_INFO, "##_JNI", "pUnInitialize_JNI: Detaching...");

        if(!cached_jvm) {
            __android_log_print(ANDROID_LOG_INFO, "##_JNI", "pUnInitialize_JNI: cached_jvm is bad");
        }
        else if (cached_jvm->DetachCurrentThread())
        {
            __android_log_print(ANDROID_LOG_INFO, "##_JNI", "pUnInitialize_JNI: detaching failed");
        }
    }
    else if (getEnvStat == JNI_EVERSION)
    {
        __android_log_print(ANDROID_LOG_INFO, "##_JNI", "pUnInitialize_JNI: JNI version not supported");
    }
}
