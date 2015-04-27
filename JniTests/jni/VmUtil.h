/*
 * VmUtil.h
 *
 *  Created on: 27-04-2015
 *      Author: Tomek
 */

#ifndef VMUTIL_H_
#define VMUTIL_H_

#include <jni.h>
#include <stddef.h>

class VmUtil
{
public:
    static void dumpReferenceTables(JNIEnv *env);
    static JNIEnv* pInitialize_JNI();
    static void UnInitialize_JNI();
};

#endif /* VMUTIL_H_ */
