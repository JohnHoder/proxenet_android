#include "ndkfoo.h"

#include <jni.h>
#include <android/log.h>

#include <sys/types.h>
#include <sys/stat.h>

#include <fcntl.h>
#include <jni.h>
#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <stdlib.h>

#ifndef MYBOOLEAN_H
#define MYBOOLEAN_H
#define false 0
#define true 1
typedef int bool; // or #define bool int
#endif

#define APPNAME ("TESTLIB")
#define LOGD(...) __android_log_print(ANDROID_LOG_WARN, APPNAME, __VA_ARGS__)
#define ALOGV(x) __android_log_print(ANDROID_LOG_VERBOSE, APPNAME, x);

static JavaVM* cachedJVM;
static jobject cachedContext;
static jobject g_javaObj;
static jclass cachedclassID;


jint JNI_OnLoad(JavaVM *jvm, void *reserved)
{
    ALOGV("JNI_OnLoad called");

    cachedJVM = jvm;

    JNIEnv* env;
    if ((*jvm)->GetEnv(jvm, (void **)&env, JNI_VERSION_1_4) != JNI_OK) {
        LOGD("GETENVFAILEDONLOAD");
        return -1;
    }
    return JNI_VERSION_1_4;
}   

void Java_janhodermarsky_proxenet_NativeWrapper_initJNICallback(JNIEnv* env, jobject thiz, jobject jobj) {
        g_javaObj = (*env)->NewGlobalRef(env, jobj);

        cachedContext = thiz;

        jclass storeclassID = (*env)->FindClass(env, "janhodermarsky/proxenet/Bread");
        if ( (*env)->ExceptionCheck(env) == JNI_TRUE ){
                (*env)->ExceptionDescribe(env);
                LOGD("got into exception describe");
        }
        cachedclassID = (jclass)(*env)->NewGlobalRef(env, storeclassID);
        if ( (*env)->ExceptionCheck(env) == JNI_TRUE ){
                (*env)->ExceptionDescribe(env);
                LOGD("got into exception describe");
        }

        LOGD("initJNICallback processed.");
}

typedef struct JniMethodInfo_
{
    JNIEnv*     env;
    jclass      classID;
    jmethodID   methodID;
} JniMethodInfo;

static JNIEnv* getJNIEnv()
{
    if (NULL == cachedJVM) {
        LOGD("Failed to get JNIEnv. JniHelper::getJavaVM() is NULL");
        return NULL;
    }

    JNIEnv *env = NULL;
    // get jni environment
    jint ret = (*cachedJVM)->GetEnv(cachedJVM, (void**)&env, JNI_VERSION_1_4);

    switch (ret) {
        case JNI_OK:
            // Success!
            LOGD("getenv successA");
            return env;

        case JNI_EDETACHED:
            // Thread not attached
            LOGD("thread not attached");
            // TODO : If calling AttachCurrentThread() on a native thread
            // must call DetachCurrentThread() in future.
            // see: http://developer.android.com/guide/practices/design/jni.html

            if ((*cachedJVM)->AttachCurrentThread(cachedJVM, &env, NULL) < 0)
            {
                LOGD("Failed to get the environment using AttachCurrentThread()");
                return NULL;
            } else {
                // Success : Attached and obtained JNIEnv!
                LOGD("getenv successB");
                return env;
            }

        case JNI_EVERSION:
            // Cannot recover from this error
            LOGD("JNI interface version 1.4 not supported");
        default :
            LOGD("Failed to get the environment using GetEnv()");
            return NULL;
        }
}    

static bool getMethodInfo(JniMethodInfo *methodinfo, const char *methodName, const char *paramCode)
{
    jmethodID methodID = 0;
    JNIEnv *pEnv = 0;
    bool bRet = false;

    do
    {
        pEnv = getJNIEnv();
        if (!pEnv)
        {
            LOGD("getJNIEnv Break Called");
            break;
        }

        //jclass classID = getClassID(pEnv);
        jclass classID = cachedclassID;
        if(classID == NULL){
            LOGD("cached classID is NULL");
            break;
        }

        //methodID = (*pEnv)->GetMethodID(pEnv, classID, methodName, paramCode);
        methodID = (*pEnv)->GetStaticMethodID(pEnv, classID, methodName, paramCode);
        if (!methodID)
        {
            LOGD("Failed to find method ID of %s", methodName);
            break;
        }

        //methodinfo->classID = classID;
        methodinfo->env = pEnv;
        methodinfo->methodID = methodID;

        LOGD("methodID = %s", methodID);

        bRet = true;
    } while (0);

    return bRet;
}

void callbackStringJNI(const char *newstr)
{
    LOGD("callbackStringJNI");

    JniMethodInfo methodInfo;
    if (! getMethodInfo(&methodInfo, "makeText", "(Landroid/content/Context;Ljava/lang/CharSequence;)Ljava/lang/String;"))
    {
        LOGD("Cannot find method!");
        return;
    }
    else{
        LOGD("Method messageMe was found! \\o/");
    }

    jstring jstr = (*methodInfo.env)->NewStringUTF(methodInfo.env, newstr);    
    //jobject toastobj = (*methodInfo.env)->CallStaticObjectMethod(methodInfo.env, g_javaObj, methodInfo.methodID, jstr);
    jobject toastobj = (*methodInfo.env)->CallStaticObjectMethod(methodInfo.env, cachedclassID, methodInfo.methodID, cachedContext, jstr);
}

void Java_janhodermarsky_proxenet_NativeWrapper_stringFromJNI(JNIEnv *env, jobject thiz)
{
    LOGD("thread looping");
    callbackStringJNI("readata start");
}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

void Java_janhodermarsky_proxenet_Interactive_displayToast(JNIEnv* env, jobject thiz, jobject charseq) {

  jclass toast = (*env)->FindClass(env, "android/widget/Toast");

  jmethodID methodMakeText = (*env)->GetStaticMethodID(env, toast, "makeText", "(Landroid/content/Context;Ljava/lang/CharSequence;I)Landroid/widget/Toast;");
  if (methodMakeText == NULL) {
    LOGD("toast.makeText not Found");
    return;
  }

  jobject toastobj = (*env)->CallStaticObjectMethod(env, toast, methodMakeText, thiz, charseq, 0);

  //toastobj.show();
  jmethodID methodShow = (*env)->GetMethodID(env, toast, "show", "()V");
  if (methodShow == NULL) {
    LOGD("toast.show not Found");
    return;
  }
  (*env)->CallVoidMethod(env, toastobj, methodShow);

  return;
}

void Java_janhodermarsky_proxenet_Interactive_breadTest(JNIEnv* env, jobject thiz, jobject charseq) {

  jclass toast = (*env)->FindClass(env, "janhodermarsky/proxenet/Bread");

  jmethodID methodMakeText = (*env)->GetStaticMethodID(env, toast, "makeText", "(Landroid/content/Context;Ljava/lang/CharSequence;)Ljava/lang/String;");
  if (methodMakeText == NULL) {
    LOGD("toast.makeText not Found");
    return;
  }

  jobject toastobj = (*env)->CallStaticObjectMethod(env, toast, methodMakeText, thiz, charseq);

  return;
}

