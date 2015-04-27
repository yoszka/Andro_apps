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

#ifdef __cplusplus
extern "C" {
#endif
#include "first.h"
#ifdef __cplusplus
}
#endif

#include "com_example_twolibs_FooClass.h"
#include <jni.h>
#include <android/log.h>
#include "VmUtil.h"

#include <pthread.h>
#include <unistd.h>
int g_thread_rc = -1;

void vThreadDoWork(void)
{
    __android_log_print(ANDROID_LOG_INFO, "##_JNI", "from pThread, doing something");
}

void (*pvThreadDoWork)();

#define FALSE   0
#define TRUE    1

#ifdef __cplusplus
extern "C" {
#endif
jint Java_com_example_twolibs_TwoLibs_add(JNIEnv* env, jobject thisObj, jint x, jint y) {
    return first(x, y);
}
#ifdef __cplusplus
}
#endif

void *print_message(void*)
{
    JNIEnv* env = VmUtil::pInitialize_JNI();

    __android_log_print(ANDROID_LOG_INFO, "##_JNI", "from pThread");
    sleep(1);
    __android_log_print(ANDROID_LOG_INFO, "##_JNI", "from pThread after 1s");

    for(int i = 0; i < 60; i++)
    {
        if(pvThreadDoWork)
        {
            pvThreadDoWork();
            pvThreadDoWork = NULL;
            jstring someString = env->NewStringUTF("some str");
            VmUtil::dumpReferenceTables(env);
        }
        else
        {
            __android_log_print(ANDROID_LOG_INFO, "##_JNI", "from pThread, NOTHING to do");
        }
        sleep(1);
    }

    VmUtil::UnInitialize_JNI();
    pthread_exit(NULL);
}

void doThreading()
{
    if (-1 == g_thread_rc)
    {
        pthread_t t1;
        g_thread_rc = pthread_create(&t1, NULL, &print_message, NULL);
    }

    pvThreadDoWork = vThreadDoWork;
}


jint callJavaMethodFromNative(JNIEnv *env) {
    jclass jc = env->FindClass("com/example/twolibs/TwoLibs");

    if (jc != 0) {
        jmethodID mid = env->GetStaticMethodID(jc, "isSELinuxEnforced2", "()I");

        if (mid != 0) {
            return env->CallStaticIntMethod(jc, mid);
        }
    }

    return -1;
}

jboolean callJavaNativeMethodFromNative(JNIEnv *env) {
    jclass jc = env->FindClass("com/example/twolibs/FooClass");

    if (jc != 0) {
        jmethodID mid = env->GetStaticMethodID(jc, "someNativMethod", "()Z");

        if (mid != 0) {
            return env->CallStaticBooleanMethod(jc, mid);
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
    jclass jc = env->FindClass("android/os/Process");

    if (jc != 0) {
        jmethodID mid = env->GetStaticMethodID(jc, "myPid", "()I");

        if (mid != 0) {
            return env->CallStaticIntMethod(jc, mid);
        }
    }

    return -1;
}

JNIEXPORT jint JNICALL Java_com_example_twolibs_FooClass_getMyPid(JNIEnv *env, jclass jCallingClas) {
    return callJavaAndroidFrameworkMethodFromNative(env);
}


// call this method from Android Java Activity
JNIEXPORT jstring JNICALL Java_com_example_twolibs_TwoLibs_getSystemSecureSetting(JNIEnv *env, jclass act) {
    jclass clsContentResolver = env->FindClass("android/content/Context");


    jmethodID  mid_getContentResolver = env->GetMethodID(clsContentResolver, "getContentResolver", "()Landroid/content/ContentResolver;");

    jobject contentObj = env->CallObjectMethod(act, mid_getContentResolver);

    jclass secClass = env->FindClass("android/provider/Settings$Secure");

    jmethodID secMid = env->GetStaticMethodID(secClass, "getString", "(Landroid/content/ContentResolver;Ljava/lang/String;)Ljava/lang/String;");

    jstring jStringParam = env->NewStringUTF("android_id");
    jstring jandroid_id= (jstring) env->CallStaticObjectMethod(secClass, secMid, contentObj, jStringParam);
    env->DeleteLocalRef(jStringParam);

    return jandroid_id;
}

// call this method from any class, as argument pass ContentResolver
// returns String
JNIEXPORT jstring JNICALL Java_com_example_twolibs_FooClass_getSystemSecureSetting (JNIEnv *env, jclass jc, jobject jContentResolverObject) {

    jclass secClass = env->FindClass("android/provider/Settings$Secure");

    jmethodID secMid = env->GetStaticMethodID(secClass, "getString", "(Landroid/content/ContentResolver;Ljava/lang/String;)Ljava/lang/String;");

    jstring jStringParam = env->NewStringUTF("android_id");
//    jstring jStringParam = env->NewStringUTF("package_verifier_enable");
    jstring jandroid_id= (jstring) env->CallStaticObjectMethod(secClass, secMid, jContentResolverObject, jStringParam);
    env->DeleteLocalRef(jStringParam);

    return jandroid_id;
}

// call this method from any class, as argument pass ContentResolver
// returns int
JNIEXPORT jint JNICALL Java_com_example_twolibs_FooClass_getSystemSecureSettingInt (JNIEnv *env, jclass jc, jobject jContentResolverObject) {

    const int defaultValue = -1;
    jclass secClass = env->FindClass("android/provider/Settings$Secure");

    jmethodID secMid = env->GetStaticMethodID(secClass, "getInt", "(Landroid/content/ContentResolver;Ljava/lang/String;I)I");

    jstring jStringParam = env->NewStringUTF("package_verifier_enable");
    jint jintParam = (jint) env->CallStaticIntMethod(secClass, secMid, jContentResolverObject, jStringParam, defaultValue);
    env->DeleteLocalRef(jStringParam);

    return jintParam;
}

// call this method from any class, as argument pass ContentResolver
// write settings - application require to be system app
//JNIEXPORT jint JNICALL Java_com_example_twolibs_FooClass_getSystemSecureSettingInt (JNIEnv *env, jclass jc, jobject jContentResolverObject) {
//
//    const int valueToSet = 1;
//    jclass secClass = env->FindClass("android/provider/Settings$Global");
//
//    jmethodID secMid = env->GetStaticMethodID(secClass, "putInt", "(Landroid/content/ContentResolver;Ljava/lang/String;I)Z");
//
//    jstring jStringParam = env->NewStringUTF("package_verifier_enable");
//    env->CallStaticBooleanMethod(secClass, secMid, jContentResolverObject, jStringParam, valueToSet);
//    env->DeleteLocalRef(jStringParam);
//
//    return 0;
//}

JNIEXPORT jobjectArray JNICALL Java_com_example_twolibs_FooClass_getSomeStringArray(JNIEnv *env, jclass jcls) {
    int i;
    jobjectArray returnArray = (jobjectArray)env->NewObjectArray(3, env->FindClass("java/lang/String"), env->NewStringUTF(""));

    char *message[3]= {"first", "second", "third"};

    for(i = 0; i<3; i++) {
        env->SetObjectArrayElement(returnArray, i, env->NewStringUTF(message[i]));
    }

    return returnArray;
}

JNIEXPORT jobject JNICALL Java_com_example_twolibs_FooClass_getInstalledPackages(JNIEnv *env, jclass jcls, jobject jPackageManager) {
    // dump JNI reference tables on function enter
    VmUtil::dumpReferenceTables(env);

    // Construct JAVA ArrayList object to store return value
    jclass jArrayListClazz = env->FindClass("java/util/ArrayList");
//    jmethodID jArrayListClazzConstructor = env->GetMethodID(jArrayListClazz, "<init>", "()V");
//    jobject jArrayListObject = env->NewObject(jArrayListClazz, jArrayListClazzConstructor);


    // PackageManager class
    jclass jPackageManagerClazz = env->FindClass("android/content/pm/PackageManager");
    // and method: getInstalledPackages
    jmethodID getInstalledPackagesMethodId = env->GetMethodID(jPackageManagerClazz, "getInstalledPackages", "(I)Ljava/util/List;");

    // call PackageManager.getInstalledPackages returns List<PackageInfo>
    jobject jArrayListObject = (jobject) env->CallObjectMethod(jPackageManager, getInstalledPackagesMethodId, 0);


    // get size of ArrayList
    jmethodID jArrayListClazzSizeMethod = env->GetMethodID(jArrayListClazz, "size", "()I");
    // retrieve size
    jint packagesCount = (jint) env->CallIntMethod(jArrayListObject, jArrayListClazzSizeMethod);

    // get method for ArrayList
    jmethodID jArrayListClazzGetMethod = env->GetMethodID(jArrayListClazz, "get", "(I)Ljava/lang/Object;");

    __android_log_print(ANDROID_LOG_INFO, "##_JNI", "Packages count: %i", packagesCount);

    // PackageManager class
    jclass jPackageInfoClazz = env->FindClass("android/content/pm/PackageInfo");
    // field PackageManager.packageName
    jfieldID jPackageInfo_packageNamefieldID = env->GetFieldID(jPackageInfoClazz, "packageName", "Ljava/lang/String;");

    // Display packages
    int i;

    for(i = 0; i < packagesCount; i++) {
        // iterate through PackageInfo's
        jobject jPackageInfoObject = env->CallObjectMethod(jArrayListObject, jArrayListClazzGetMethod, i);
        jstring jStringPackageName = (jstring) env->GetObjectField(jPackageInfoObject, jPackageInfo_packageNamefieldID);

        const char *pcStringPackageName = env->GetStringUTFChars(jStringPackageName, 0);
        __android_log_print(ANDROID_LOG_INFO, "##_JNI", "Found %3i: %s", (i+1), pcStringPackageName);

        env->DeleteLocalRef(jPackageInfoObject);
        env->DeleteLocalRef(jStringPackageName);
    }

    // Delete JNI references
//    env->DeleteLocalRef(jArrayListObject);    // Since ICS this is absolutely wrong, because "jArrayListObject" is return value to JAVA, which be handles by GarbageColector
    env->DeleteLocalRef(jArrayListClazz);       // Since this method is called explicit from JAVA and returns to JAVA, is no need to delete local references.
    env->DeleteLocalRef(jPackageManagerClazz);  // Local references will be discarded by JNI on return from a native method.
    env->DeleteLocalRef(jPackageInfoClazz);     // --//--

    // dump JNI reference tables on function exit - in order to compare to values from enter to function
    VmUtil::dumpReferenceTables(env);

    doThreading();

    return jArrayListObject;
}


JNIEXPORT jobject JNICALL Java_com_example_twolibs_FooClass_getApplicationObject(JNIEnv *env, jclass jc){

    jclass jMyApplicationClazz = env->FindClass("com/example/twolibs/MyApplication");

    jfieldID jMyApplication_mInstance_fId = env->GetStaticFieldID(jMyApplicationClazz, "mInstance", "Lcom/example/twolibs/MyApplication;");

    jobject jInstance = (jobject) env->GetStaticObjectField(jMyApplicationClazz, jMyApplication_mInstance_fId);

    // Delete JNI references
    env->DeleteLocalRef(jMyApplicationClazz);

    return jInstance;

}

JNIEXPORT jobject JNICALL Java_com_example_twolibs_FooClass_getApplicationContext(JNIEnv *env, jclass jc) {

    jclass jMyApplicationClazz = env->FindClass("com/example/twolibs/MyApplication");

    jmethodID jMyApplication_getContext_mId = env->GetStaticMethodID(jMyApplicationClazz, "getContext", "()Landroid/content/Context;");

    jobject jApplicationContextObj = env->CallStaticObjectMethod(jMyApplicationClazz, jMyApplication_getContext_mId);

    // Delete JNI references
    env->DeleteLocalRef(jMyApplicationClazz);

    return jApplicationContextObj;
}

