LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under,src)

# bundled
#LOCAL_STATIC_JAVA_LIBRARIES += \
#		android-common \
#		android-common-chips \
#		calendar-common

# unbundled
#LOCAL_STATIC_JAVA_LIBRARIES := \
#		android-common \
#		android-common-chips \
#		calendar-common
		
LOCAL_SDK_VERSION := current

LOCAL_PACKAGE_NAME := ListSynchroner


include $(BUILD_PACKAGE)

# Use the following include to make our test apk.
include $(call all-makefiles-under,$(LOCAL_PATH))
