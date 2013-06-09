LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under,src)

LOCAL_SDK_VERSION := current

LOCAL_PACKAGE_NAME := TestContentProvider

#$(shell $(LOCAL_PATH)/some_script.sh $(LOCAL_PATH))
include $(BUILD_PACKAGE)

#ifeq ($(WITH_DEXPREOPT), true)
#WITH_DEXPREOPT := false
#include $(BUILD_PACKAGE)
#WITH_DEXPREOPT := true
#else
#include $(BUILD_PACKAGE)
#endif

# Use the following include to make our test apk.
include $(call all-makefiles-under,$(LOCAL_PATH))
