#ifdef HAVE_CONFIG_H
#include "config.h"
#endif

#ifdef _C_PLUGIN

/*******************************************************************************
 *
 * C plugin
 *
 */

#include <dlfcn.h>
#include <string.h>

#ifdef __LINUX__
#include <alloca.h>
#endif

#include "base64.h"

#include "core.h"
#include "utils.h"
#include "main.h"
#include "plugin.h"

#include "ndkfoo.h"

#include <android/log.h>

#define APPNAME "PROXENET"

#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, APPNAME, __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_WARN, APPNAME, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, APPNAME, __VA_ARGS__)
#define LOGV(...) __android_log_print(ANDROID_LOG_VERBOSE, APPNAME, __VA_ARGS__);


/**
 *
 */
int proxenet_c_initialize_vm(plugin_t* plugin)
{
	void *interpreter;

	interpreter = dlopen(plugin->fullpath, RTLD_NOW);
	if (!interpreter) {
		xlog(LOG_ERROR, "Failed to dlopen('%s'): %s\n", plugin->fullpath, dlerror());
		return -1;
	}

	plugin->interpreter->vm = interpreter;
        plugin->interpreter->ready = true;

	return 0;
}


/**
 *
 */
int proxenet_c_destroy_plugin(plugin_t* plugin)
{
        plugin->state = INACTIVE;
        plugin->pre_function  = NULL;
        plugin->post_function = NULL;

        if (dlclose((void*)plugin->interpreter->vm) < 0) {
                xlog(LOG_ERROR, "Failed to dlclose() for '%s': %s\n", plugin->name, dlerror());
                return -1;
        }

        return 0;
}


/**
 *
 */
int proxenet_c_destroy_vm(interpreter_t* interpreter)
{
        interpreter->ready = false;
        interpreter = NULL;
        return 0;
}

char* proxenet_request_hookx(unsigned long request_id, char *request, char* uri, size_t* buflen)
{
    const char* header = "X-Powered-By: Proxlife\r\n\r\n";
    char* newReq = malloc(*buflen + strlen(header) + 2);
    memcpy(newReq, request, *buflen-2);
    memcpy(newReq + (*buflen-2), header, strlen(header));

    *buflen = *buflen + strlen(header) + 2; //2

    //char buffer[4096] = "";
    //char *buffer = (char *)malloc(sizeof(char) * 1024);
    //snprintf(buffer, sizeof(buffer), "%s", newReq);

    char* buffer = malloc(strlen(newReq)*sizeof(char)+1);
    //char buffer[8128] = "";
    memset(buffer, '\0', strlen(newReq)*sizeof(char)+1);

    LOGD("STRLEN BEFORE BASE64: %d", strlen(buffer));
    LOGI("CONTENT BEFORE BASE64: \n%s", buffer);
    Base64encode(buffer, newReq, (unsigned int)(strlen(newReq)));
    //LOGI("STRLEN AFTER BASE64: %d", strlen(buffer));
    //LOGI("CONTENT AFTER BASE64: \n%s", buffer);

    //showBread(buffer, strlen(buffer));
    threadShowBread_start(buffer);

    //LOGI("THIS NIGGA - \n%s", newReq);

    free(request);
    free(buffer);

    return newReq;
}

/**
 *
 */
int proxenet_c_initialize_function(plugin_t* plugin, req_t type)
{
	void *interpreter;

        if (plugin->interpreter==NULL || plugin->interpreter->ready==false){
                xlog(LOG_ERROR, "%s\n", "[c] not ready (dlopen() failed?)");
                return -1;
        }

        interpreter = (void *) plugin->interpreter->vm;

	/* if already initialized, return ok */
	if (plugin->pre_function && type==REQUEST)
		return 0;

	if (plugin->post_function && type==RESPONSE)
		return 0;


	if (type == REQUEST) {
		plugin->pre_function = (*proxenet_request_hookx);//dlsym(interpreter, CFG_REQUEST_PLUGIN_FUNCTION);
		if (plugin->pre_function) {
#ifdef DEBUG
			xlog(LOG_DEBUG, "[C] '%s' request_hook function is at %p\n",
                             plugin->name,
                             plugin->pre_function);
#endif
			return 0;
		}

	} else {
		plugin->post_function = dlsym(interpreter, CFG_RESPONSE_PLUGIN_FUNCTION);
		if (plugin->post_function) {
#ifdef DEBUG
			xlog(LOG_DEBUG, "[C] '%s' response_hook function is at %p\n",
                             plugin->name,
                             plugin->post_function);
#endif
			return 0;
		}

	}

        xlog(LOG_ERROR, "[C] dlsym(%s) failed for '%s': %s\n",
             (type==REQUEST)?"REQUEST":"RESPONSE",
             plugin->name,
             dlerror());

	return -1;
}


/**
 * Execute a proxenet plugin written in C.
 *
 * @note Because there is no other consistent way in C of keeping track of the
 * right size of the request (strlen() will break at the first NULL byte), the
 * signature of functions in a C plugin must include a pointer to the size which
 * **must** be changed by the called plugin.
 * Therefore the definition is
 * char* proxenet_request_hook(unsigned int rid, char* buf, char* uri, size_t* buflen);
 *
 * See examples/ for examples.
 */
char* proxenet_c_plugin(plugin_t *plugin, request_t *request)
{
	char* (*plugin_function)(unsigned long, char*, char*, size_t*);
	char *bufres, *uri;
    size_t buflen;

	bufres = uri = NULL;

    uri = request->http_infos.uri;

    //ToastNativeFormated("%s", uri);
    //pthread_t thread1;
    //pthread_create( &thread1, NULL, &callbackStringJNI, uri);
    //showBread("Request intercepted\n");

	if (!uri)
		return NULL;

	if (request->type == REQUEST)
		plugin_function = plugin->pre_function;
	else
		plugin_function = plugin->post_function;

    buflen = request->size;
	bufres = (*plugin_function)(request->id, request->data, uri, &buflen);
	if(!bufres)
	{
        request->size = -1;
        goto end_exec_c_plugin;
    }

    request->data = proxenet_xstrdup(bufres, buflen);
    request->size = buflen;

end_exec_c_plugin:
	return request->data;
}

#endif
