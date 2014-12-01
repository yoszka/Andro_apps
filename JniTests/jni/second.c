/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
#include "first.h"
#include "com_example_twolibs_FooClass.h"
#include <jni.h>

#define FALSE   0
#define TRUE    1

jint Java_com_example_twolibs_TwoLibs_add(JNIEnv* env, jobject this, jint x, jint y) {
    return first(x, y);
}

jint callJavaMethodFromNative(JNIEnv *env) {
    jclass jc = (*env)->FindClass(env, "com/example/twolibs/TwoLibs");

    if (jc != 0) {
        jmethodID mid = (*env)->GetStaticMethodID(env, jc, "isSELinuxEnforced2", "()I");

        if (mid != 0) {
            return (*env)->CallStaticIntMethod(env, jc, mid);
        }
    }

    return -1;
}

jboolean callJavaNativeMethodFromNative(JNIEnv *env) {
    jclass jc = (*env)->FindClass(env, "com/example/twolibs/FooClass");

    if (jc != 0) {
        jmethodID mid = (*env)->GetStaticMethodID(env, jc, "someNativMethod", "()Z");

        if (mid != 0) {
            return (*env)->CallStaticBooleanMethod(env, jc, mid);
        }
    }

    return FALSE;
}

JNIEXPORT jint JNICALL Java_com_example_twolibs_FooClass_baarMethod(JNIEnv *env, jclass jCallingClas) {
    jboolean firstValue = callJavaNativeMethodFromNative(env);
    jint retVal = -2;

    if (firstValue) {
        retVal = callJavaMethodFromNative(env);
    }

    return retVal;
}

JNIEXPORT jboolean JNICALL Java_com_example_twolibs_FooClass_someNativMethod(JNIEnv *env, jclass jCallingClas) {
    return TRUE;
}


jint callJavaAndroidFrameworkMethodFromNative(JNIEnv *env) {
    jclass jc = (*env)->FindClass(env, "android/os/Process");

    if (jc != 0) {
        jmethodID mid = (*env)->GetStaticMethodID(env, jc, "myPid", "()I");

        if (mid != 0) {
            return (*env)->CallStaticIntMethod(env, jc, mid);
        }
    }

    return -1;
}

JNIEXPORT jint JNICALL Java_com_example_twolibs_FooClass_getMyPid(JNIEnv *env, jclass jCallingClas) {
    return callJavaAndroidFrameworkMethodFromNative(env);
}


// call this method from Android Java Activity
JNIEXPORT jstring JNICALL Java_com_example_twolibs_TwoLibs_getSystemSecureSetting(JNIEnv *env, jclass act) {
    jclass clsContentResolver = (*env)->FindClass(env, "android/content/Context");


    jmethodID  mid_getContentResolver = (*env)->GetMethodID(env, clsContentResolver, "getContentResolver", "()Landroid/content/ContentResolver;");

    jobject contentObj = (*env)->CallObjectMethod(env, act, mid_getContentResolver);

    jclass secClass = (*env)->FindClass(env, "android/provider/Settings$Secure");

    jmethodID secMid = (*env)->GetStaticMethodID(env, secClass, "getString", "(Landroid/content/ContentResolver;Ljava/lang/String;)Ljava/lang/String;");

    jstring jStringParam = (*env)->NewStringUTF(env, "android_id");
    jstring jandroid_id= (jstring) (*env)->CallStaticObjectMethod(env, secClass, secMid, contentObj, jStringParam);
    (*env)->DeleteLocalRef(env, jStringParam);

    return jandroid_id;
}

// call this method from any class, as argument pass ContentResolver
// returns String
JNIEXPORT jstring JNICALL Java_com_example_twolibs_FooClass_getSystemSecureSetting (JNIEnv *env, jclass jc, jobject jContentResolverObject) {

    jclass secClass = (*env)->FindClass(env, "android/provider/Settings$Secure");

    jmethodID secMid = (*env)->GetStaticMethodID(env, secClass, "getString", "(Landroid/content/ContentResolver;Ljava/lang/String;)Ljava/lang/String;");

    jstring jStringParam = (*env)->NewStringUTF(env, "android_id");
//    jstring jStringParam = (*env)->NewStringUTF(env, "package_verifier_enable");
    jstring jandroid_id= (jstring) (*env)->CallStaticObjectMethod(env, secClass, secMid, jContentResolverObject, jStringParam);
    (*env)->DeleteLocalRef(env, jStringParam);

    return jandroid_id;
}

// call this method from any class, as argument pass ContentResolver
// returns int
JNIEXPORT jint JNICALL Java_com_example_twolibs_FooClass_getSystemSecureSettingInt (JNIEnv *env, jclass jc, jobject jContentResolverObject) {

    const int defaultValue = -1;
    jclass secClass = (*env)->FindClass(env, "android/provider/Settings$Secure");

    jmethodID secMid = (*env)->GetStaticMethodID(env, secClass, "getInt", "(Landroid/content/ContentResolver;Ljava/lang/String;I)I");

    jstring jStringParam = (*env)->NewStringUTF(env, "package_verifier_enable");
    jint jintParam = (jint) (*env)->CallStaticIntMethod(env, secClass, secMid, jContentResolverObject, jStringParam, defaultValue);
    (*env)->DeleteLocalRef(env, jStringParam);

    return jintParam;
}
