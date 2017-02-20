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
#include <pthread.h>

//Define boolean type
#ifndef MYBOOLEAN_H
#define MYBOOLEAN_H
#define false 0
#define true 1
typedef int bool; // or #define bool int
#endif

//Android logging macros
#define APPNAME ("TESTLIB")
#define LOGD(...) __android_log_print(ANDROID_LOG_WARN, APPNAME, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, APPNAME, __VA_ARGS__)
#define LOGV(...) __android_log_print(ANDROID_LOG_VERBOSE, APPNAME, __VA_ARGS__);

static JavaVM* cachedJVM;
static jobject cachedContext;
static jobject g_javaObj;
static jclass cachedclassID;

static pthread_mutex_t mutex = PTHREAD_MUTEX_INITIALIZER;


jint JNI_OnLoad(JavaVM *jvm, void *reserved)
{
    LOGV("JNI_OnLoad called");

    //init mutex
    if (pthread_mutex_init(&mutex, NULL) != 0)
    {
        LOGV("\nMutex init failed\n");
    }

    JNIEnv* env;
    if ((*jvm)->GetEnv(jvm, (void **)&env, JNI_VERSION_1_6) != JNI_OK) {
        LOGE("OnLoad: GetEnv failed.");
        return -1;
    }

    cachedJVM = jvm;

    return JNI_VERSION_1_6;
}

void Java_janhodermarsky_proxenet_ProxenetService_initJNICallback(JNIEnv* env, jobject thiz, jobject jobj) {
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
        LOGD("Failed to get JNIEnv. JavaVM is NULL");
        return NULL;
    }

    JNIEnv *env = NULL;
    // get jni environment
    jint ret = (*cachedJVM)->GetEnv(cachedJVM, (void**)&env, JNI_VERSION_1_6);

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
            LOGD("JNI interface version 1.6 not supported");
        default :
            LOGD("Failed to get the environment using GetEnv()");
            return NULL;
        }
}


//////////////////////////////////////////////////////////////

void detachFromThread(JavaVM *jvm)
{
    if(jvm != NULL)
    {
        (*jvm)->DetachCurrentThread(jvm);
        LOGV("Thread detached successfully.");
    }
    else
    {
        LOGE("JNIUtils detachFromThread invalid VM passed in");
    }
}

JNIEnv* attachToThread(bool attached, JavaVM *vm)
{
    JNIEnv *env = NULL;

    attached = false;

    if(vm != NULL)
    {
        jint result = (*vm)->GetEnv(vm, (void **)&env, JNI_VERSION_1_6);
        if (result != JNI_OK)
        {
            (*vm)->AttachCurrentThread(vm, &env, NULL);

            if((*env)->ExceptionCheck(env))
            {
                (*env)->ExceptionDescribe(env);
                (*env)->ExceptionClear(env);
                env = NULL;
                __android_log_write(ANDROID_LOG_ERROR, "JNI", "JNIUtils attachToThread failed to attach");
            }
            else
            {
                attached = true;

            }
        }
    }
    else
    {
        __android_log_write(ANDROID_LOG_ERROR, "JNI", "JNIUtils attachToThread invalid VM passed in");
    }
    return env;
}

bool checkForJNIException(JNIEnv * theJNIEnv)
{
    bool ret = false;
    if((*theJNIEnv)->ExceptionCheck(theJNIEnv))
    {
        ret = true;
        __android_log_write(ANDROID_LOG_ERROR, "JNI", "JNI Error occurred");
        (*theJNIEnv)->ExceptionDescribe(theJNIEnv);
        (*theJNIEnv)->ExceptionClear(theJNIEnv);
    }
    return ret;
}

static bool getMethodInfo(JniMethodInfo *methodinfo, const char *methodName, const char *paramCode)
{
    jmethodID methodID = 0;
    JNIEnv *pEnv = 0;
    bool bRet = false;

    do
    {
        ///////////////////////
        //pEnv = getJNIEnv();
        ///////////////////////

        bool attached = false;
        pEnv = attachToThread(attached, cachedJVM);
        if(attached)
        {
            detachFromThread(cachedJVM);
        }

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

        //LOGD("methodID = %s", methodID);

        bRet = true;
    } while (0);

    return bRet;
}

int len(char *t)
{
    int count=0;
    while(*t!='\0')
    {
        count++;
        t++;
        if(count > 100){
            *(t+1) = '\0';
        }
    }
    return(count);
}

void *callbackStringJNI(const char *newstr)
{
    if(newstr == NULL){
        return;
    }

    //static pthread_mutex_t cli_sprintf_lock = PTHREAD_MUTEX_INITIALIZER;


    pthread_mutex_lock(&mutex);

    JniMethodInfo methodInfo;
    if (!getMethodInfo(&methodInfo, "makeText", "(Landroid/content/Context;Ljava/lang/CharSequence;)Ljava/lang/String;"))    //"(Landroid/content/Context;Ljava/lang/CharSequence;)Ljava/lang/String;"  vs. "(Landroid/content/Context;[B)Ljava/lang/String;"
    {
        LOGD("Cannot find method!");
        return;
    }
    else{
        LOGD("Method messageMe was found! \\o/");
    }

    //LOGD("STRLEN BEFORE \"STRLEN\": %d", strlen(newstr));
    //LOGD("CONTENT BEFORE \"STRLEN\": \n%s", newstr);

    //********* CREATE BYTE ARRAY **********//
    //size_t length = strlen(newstr) + 1;
    //jbyteArray array = (*methodInfo.env)->NewByteArray(methodInfo.env, length);
    //(*methodInfo.env)->SetByteArrayRegion(methodInfo.env, array, 0, length, (const jbyte*)newstr);
    //**************************************//

    //********* CREATE STRING (CHAR SEQUENCE) *********//
    jstring jstr = (*methodInfo.env)->NewStringUTF(methodInfo.env, newstr);
    //*************************************************//

    jobject toastobj = (*methodInfo.env)->CallStaticObjectMethod(methodInfo.env, cachedclassID, methodInfo.methodID, g_javaObj, jstr);
    //jobject toastobj = (*methodInfo.env)->CallStaticObjectMethod(methodInfo.env, cachedclassID, methodInfo.methodID, g_javaObj, array); //cachedContext <- g_javaObj

    //(*methodInfo.env)->ReleaseByteArrayElements(methodInfo.env, array, newstr, JNI_ABORT);
    //(*methodInfo.env)->DeleteLocalRef(methodInfo.env, array);

    detachFromThread(cachedJVM);

    pthread_mutex_unlock(&mutex);
}

void ToastNativeFormated(const char *fmt, ...)
{
    LOGD("ToastNativeFormated");

    char buffer[512]; //buffer overflow possibility

    va_list args;
    va_start (args, fmt);
    vsnprintf (buffer, sizeof(buffer), fmt, args);
    va_end (args);

    JniMethodInfo methodInfo;
    if (!getMethodInfo(&methodInfo, "makeText", "(Landroid/content/Context;Ljava/lang/CharSequence;)Ljava/lang/String;"))
    {
        LOGD("Cannot find method!");
        return;
    }
    else{
        LOGD("Method messageMe was found! \\o/");
    }

    jstring jstr = (*methodInfo.env)->NewStringUTF(methodInfo.env, buffer);
    //jobject toastobj = (*methodInfo.env)->CallStaticObjectMethod(methodInfo.env, g_javaObj, methodInfo.methodID, jstr);
    jobject toastobj = (*methodInfo.env)->CallStaticObjectMethod(methodInfo.env, cachedclassID, methodInfo.methodID, cachedContext, jstr);

    detachFromThread(cachedJVM);
}

void Java_janhodermarsky_proxenet_ProxenetService_stringFromJNI(JNIEnv *env, jobject thiz, jstring message)
{
    callbackStringJNI("just testing...");
}

char *mergeString(const char *fmt, ...){

    LOGD("mergeString called.");

    char buffer[1024]; //buffer overflow possibility

    va_list args;
    va_start (args, fmt);
    vsnprintf (buffer, sizeof(buffer), fmt, args);
    va_end (args);

    return buffer;
}

void showBread(const char* message, size_t length)
{
    LOGD("--== BREAD MESSAGE ==--\nSTRLEN: %d\nCONTENT:\n%s", strlen(message), message);

    pthread_t thread1;
    int  iret1;

   /* typedef struct s_pthreadparams {
        char *msg;
        size_t len;
    } pthreadparams;

    pthreadparams params;
    params.len = length;
    params.msg = malloc(length + 1);
    strcpy(params.msg, message);
    */

    iret1 = pthread_create( &thread1, NULL, &callbackStringJNI, message);  //params
    if(iret1)
    {
    	LOGD("Error - pthread_create() return code: %d\n",iret1);
    }
}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

void Java_janhodermarsky_proxenet_ProxenetService_displayToast(JNIEnv* env, jobject thiz, jobject charseq) {

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

void Java_janhodermarsky_proxenet_ProxenetService_breadTest(JNIEnv* env, jobject thiz, jobject charseq) {

  jclass toast = (*env)->FindClass(env, "janhodermarsky/proxenet/Bread");

  jmethodID methodMakeText = (*env)->GetStaticMethodID(env, toast, "makeText", "(Landroid/content/Context;Ljava/lang/CharSequence;)Ljava/lang/String;");
  if (methodMakeText == NULL) {
    LOGD("toast.makeText not Found");
    return;
  }

  jobject toastobj = (*env)->CallStaticObjectMethod(env, toast, methodMakeText, thiz, charseq);

  return;
}

