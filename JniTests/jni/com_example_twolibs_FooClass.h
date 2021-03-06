/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_example_twolibs_FooClass */

#ifndef _Included_com_example_twolibs_FooClass
#define _Included_com_example_twolibs_FooClass
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     com_example_twolibs_FooClass
 * Method:    baarMethod
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_example_twolibs_FooClass_baarMethod
  (JNIEnv *, jclass);

/*
 * Class:     com_example_twolibs_FooClass
 * Method:    someNativMethod
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_com_example_twolibs_FooClass_someNativMethod
  (JNIEnv *, jclass);

/*
 * Class:     com_example_twolibs_FooClass
 * Method:    getMyPid
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_example_twolibs_FooClass_getMyPid
  (JNIEnv *, jclass);

/*
 * Class:     com_example_twolibs_FooClass
 * Method:    getSystemSecureSetting
 * Signature: (Ljava/lang/Object;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_example_twolibs_FooClass_getSystemSecureSetting
  (JNIEnv *, jclass, jobject);

/*
 * Class:     com_example_twolibs_FooClass
 * Method:    getSystemSecureSettingInt
 * Signature: (Ljava/lang/Object;)I
 */
JNIEXPORT jint JNICALL Java_com_example_twolibs_FooClass_getSystemSecureSettingInt
  (JNIEnv *, jclass, jobject);

/*
 * Class:     com_example_twolibs_FooClass
 * Method:    getInt
 * Signature: (Ljava/lang/Object;Ljava/lang/String;I)I
 */
JNIEXPORT jint JNICALL Java_com_example_twolibs_FooClass_getInt
  (JNIEnv *, jclass, jobject, jstring, jint);

/*
 * Class:     com_example_twolibs_FooClass
 * Method:    getSomeStringArray
 * Signature: ()[Ljava/lang/String;
 */
JNIEXPORT jobjectArray JNICALL Java_com_example_twolibs_FooClass_getSomeStringArray
  (JNIEnv *, jclass);

/*
 * Class:     com_example_twolibs_FooClass
 * Method:    getInstalledPackages
 * Signature: (Ljava/lang/Object;)Ljava/util/ArrayList;
 */
JNIEXPORT jobject JNICALL Java_com_example_twolibs_FooClass_getInstalledPackages
  (JNIEnv *, jclass, jobject);

/*
 * Class:     com_example_twolibs_FooClass
 * Method:    getApplicationObject
 * Signature: ()Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL Java_com_example_twolibs_FooClass_getApplicationObject
  (JNIEnv *, jclass);

/*
 * Class:     com_example_twolibs_FooClass
 * Method:    getApplicationContext
 * Signature: ()Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL Java_com_example_twolibs_FooClass_getApplicationContext
  (JNIEnv *, jclass);

#ifdef __cplusplus
}
#endif
#endif
