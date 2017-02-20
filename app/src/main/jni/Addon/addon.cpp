#include <android/log.h>

#include <sys/types.h>
#include <sys/stat.h>

#include <fcntl.h>
#include <jni.h>
#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <stdlib.h>

#define BUFFER_SIZE (512 * 1024)

#define APPNAME "ADDON"
#define LOGI(...) __android_log_print(ANDROID_LOG_WARN, APPNAME, __VA_ARGS__)

typedef struct info {
                const char* AUTHOR;
                const char* PLUGIN_NAME;
} c_plugin_info_t;


c_plugin_info_t MyPlugin = {
    .AUTHOR = "Jan Hodermarsky",
    .PLUGIN_NAME = "AddHeader"
};

jint JNI_OnLoad(JavaVM* vm, void* reserved)
{
        JNIEnv *env;
        //gJavaVM = vm;
        LOGI("JNI_OnLoad called");
        if (vm->GetEnv((void**) &env, JNI_VERSION_1_4) != JNI_OK) {
            LOGI("Failed to get the environment using GetEnv()");
            return -1;
        }

        //proxenet(1, (void*)0, (void*)0);

        return JNI_VERSION_1_4;
}

extern "C" {

    JNIEXPORT jstring JNICALL
    Java_janhodermarsky_proxenet_NativeWrapper_stringFromJNI(JNIEnv *env, jobject thiz)
    {
        return env->NewStringUTF("Hello from JNI!");


    }

    JNIEXPORT jstring JNICALL
    Java_janhodermarsky_proxenet_NativeWrapper_readMemoryInfo(JNIEnv *env, jobject thiz, jstring file)
    {
        const char *file_path = env->GetStringUTFChars(file, 0);
        LOGI("file_path -> %s", file_path);

        int fd;
        int err = 0;
        char *string;

        fd = open(file_path, O_RDONLY);
        if (fd < 0) {
            LOGI("Error opening file");
            close(fd);
            goto err;
        }

        string = (char*) malloc(BUFFER_SIZE);
        if (string == NULL) {
            LOGI("Error: out of memory");
            goto err;
        }

        if (read(fd, string, BUFFER_SIZE) < 0) {
            LOGI("Error reading the file");
            free(string);
            goto err;
        } else {
            string[BUFFER_SIZE] = '\0';
        }

        jstring result;
        if (string) {
            LOGI("string is: %s", string);
            result = env->NewStringUTF(string);
            //result = env->NewStringUTF("Successfully read file!");
        } else {
            result = env->NewStringUTF("Could not read file!");
        }
        free(string);
        close(fd);

        return result;

err:
        return env->NewStringUTF("Could not read file!");
    }

    char* proxenet_request_hook(unsigned long request_id, char *request, char* uri, size_t* buflen)
    {
        //LOGI("Stand up!\n%s", request);

        const char* header = "X-Powered-By: c-proxenet\r\n\r\n";
        char* newReq = malloc(*buflen + strlen(header) + 2);
        memcpy(newReq, request, *buflen-2);
        memcpy(newReq + (*buflen-2), header, strlen(header));
        free(request);
        *buflen = *buflen + strlen(header) + 2;
        return newReq;
    }


    char* proxenet_response_hook(unsigned long response_id, char *response, char* uri, size_t* buflen)
    {
        return response;
    }

}
