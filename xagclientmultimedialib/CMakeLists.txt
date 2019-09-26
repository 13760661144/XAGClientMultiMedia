# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

add_definitions(-DSOCKLEN_T=socklen_t)
add_definitions(-DNO_SSTREAM=1)
add_definitions(-DNEWLOCALE_NOT_USED)
#add_definitions(-D_LARGEFILE_SOURCE=1)
#add_definitions(-D_FILE_OFFSET_BITS=64)
add_definitions(-DBSD=1)

# 头文件引入
include_directories(src/main/cpp/live/UsageEnvironment/include)
include_directories(src/main/cpp/live/BasicUsageEnvironment/include)
include_directories(src/main/cpp/live/groupsock/include)
include_directories(src/main/cpp/live/liveMedia/include)

#设置源码连接BasicUsageEnvironment
file(GLOB BasicUsageEnvironmentFiles
        src/main/cpp/live/BasicUsageEnvironment/*.cpp)
#添加临时库,Add a library to the project using the specified source files.
#If no type is given explicitly the type is STATIC or SHARED based on whether the current value of the variable BUILD_SHARED_LIBS is ON.
add_library(BasicUsageEnvironment
        ${BasicUsageEnvironmentFiles})

#设置源码连接groupsock
file(GLOB groupsockfiles
        src/main/cpp/live/groupsock/*.cpp src/main/cpp/live/groupsock/*.c)
add_library(groupsock
        ${groupsockfiles})

#设置源码连接liveMedia
file(GLOB liveMediaFiles
        src/main/cpp/live/liveMedia/*.hh src/main/cpp/live/liveMedia/*.cpp src/main/cpp/live/liveMedia/*.c)
add_library(liveMedia ${liveMediaFiles})

#设置源码连接UsageEnvironment
file(GLOB UsageEnvironmentFiles
        src/main/cpp/live/UsageEnvironment/*.cpp)
add_library(UsageEnvironment
        ${UsageEnvironmentFiles})

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

add_library( # Sets the name of the library.
        native-lib

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        src/main/cpp/native-lib.cpp
        src/main/cpp/ndkH264Decode.cpp
        src/main/cpp/testRTSPClient.cpp
        src/main/cpp/shaders.c
        src/main/cpp/opengles_display.c)

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.


#Users/wangyijin/Desktop/demo/FFmpegForAndroidDemo/app/
set(ARM_DIR ${CMAKE_SOURCE_DIR}/src/main/jniLibs)
message(STATUS "execute CMakeLists")

message(${ARM_DIR}/${ANDROID_ABI})
#导入头文件
include_directories(${CMAKE_SOURCE_DIR}/src/main/cpp/include)

#添加动态库
add_library(avcodec
        SHARED
        IMPORTED)
#设置动态库路径
set_target_properties(avcodec
        PROPERTIES IMPORTED_LOCATION
        ${ARM_DIR}/${ANDROID_ABI}/libavcodec.so
        )

add_library(avformat
        SHARED
        IMPORTED)
set_target_properties(avformat
        PROPERTIES IMPORTED_LOCATION
        ${ARM_DIR}/${ANDROID_ABI}/libavformat.so)
add_library(avutil
        SHARED
        IMPORTED)
set_target_properties(avutil
        PROPERTIES IMPORTED_LOCATION
        ${ARM_DIR}/${ANDROID_ABI}/libavutil.so)

add_library(swresample
        SHARED
        IMPORTED)
set_target_properties(swresample
        PROPERTIES IMPORTED_LOCATION
        ${ARM_DIR}/${ANDROID_ABI}/libswresample.so)
add_library(swscale
        SHARED
        IMPORTED)
set_target_properties(swscale
        PROPERTIES IMPORTED_LOCATION
        ${ARM_DIR}/${ANDROID_ABI}/libswscale.so)
add_library(avfilter
        SHARED
        IMPORTED)
set_target_properties(avfilter
        PROPERTIES IMPORTED_LOCATION
        ${ARM_DIR}/${ANDROID_ABI}/libavfilter.so)

target_link_libraries( # Specifies the target library.
        native-lib
        avcodec
        avformat
        avfilter
        avutil
        android
        swresample
        swscale
        GLESv2
        # Links the target library to the log library
        # included in the NDK.
        ${log-lib}
          liveMedia
                groupsock
                BasicUsageEnvironment
                UsageEnvironment)