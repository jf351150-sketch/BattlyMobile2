# Keep the Android/HotSpot integration outside the upstream SDL source tree.
ifeq ($(LOCAL_MODULE),SDL3)
LOCAL_SRC_FILES += ../sdl_android_device_bridge.c
LOCAL_LDFLAGS += -Wl,--wrap=SDL_IsDeXMode -Wl,--wrap=SDL_IsChromebook
LOCAL_LDFLAGS += -Wl,--wrap=Android_JNI_PollInputDevices
endif
include $(BATTLY_BUILD_SHARED_LIBRARY)
