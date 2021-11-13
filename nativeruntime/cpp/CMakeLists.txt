cmake_minimum_required(VERSION 3.10)

# This is needed to ensure that static libraries can be linked into shared libraries.
set(CMAKE_POSITION_INDEPENDENT_CODE ON)

# Some libutils headers require C++17
set (CMAKE_CXX_STANDARD 17)

project(nativeruntime)

if (WIN32)
  if(NOT DEFINED ENV{JAVA_HOME})
    message(FATAL_ERROR "JAVA_HOME is required in Windows")
  endif()
  # find_package JNI is broken on Windows, manually include header files
  set(JNI_INCLUDE_DIRS "$ENV{JAVA_HOME}/include" "$ENV{JAVA_HOME}/include/win32")
else()
  find_package(JNI REQUIRED)
endif()

set(ANDROID_SQLITE_DIR "${CMAKE_CURRENT_SOURCE_DIR}/../external/sqlite")

if(NOT EXISTS "${ANDROID_SQLITE_DIR}/dist/sqlite3.c")
  message(FATAL_ERROR "SQLite submodule missing. Please run `git submodule update --init`.")
endif()

if(DEFINED ENV{ICU_ROOT_DIR})
  if (WIN32)
    if(NOT EXISTS "$ENV{ICU_ROOT_DIR}/lib/libsicuin.a")
      message(FATAL_ERROR "ICU_ROOT_DIR does not contain 'lib/libsicuin.a'.")
    endif()
  else()
    if(NOT EXISTS "$ENV{ICU_ROOT_DIR}/lib/libicui18n.a")
      message(FATAL_ERROR "ICU_ROOT_DIR does not contain 'lib/libicui18n.a'.")
    endif()
  endif()

  message(NOTICE "Using $ENV{ICU_ROOT_DIR} as the ICU root dir")
  list(APPEND CMAKE_PREFIX_PATH "$ENV{ICU_ROOT_DIR}")
  if (WIN32)
    find_library(STATIC_ICUI18N_LIBRARY libsicuin.a)
    find_library(STATIC_ICUUC_LIBRARY libsicuuc.a)
    find_library(STATIC_ICUDATA_LIBRARY libsicudt.a)
  else()
    find_library(STATIC_ICUI18N_LIBRARY libicui18n.a)
    find_library(STATIC_ICUUC_LIBRARY libicuuc.a)
    find_library(STATIC_ICUDATA_LIBRARY libicudata.a)
  endif()
  include_directories($ENV{ICU_ROOT_DIR}/include)
else()
  set(ICU_SUBMODULE_DIR "${CMAKE_CURRENT_SOURCE_DIR}/../external/icu")

  if(NOT EXISTS "${ICU_SUBMODULE_DIR}/icu4c/source/i18n/ucol.cpp")
    message(FATAL_ERROR "ICU submodule missing. Please run `git submodule update --init`.")
  endif()

  message(NOTICE "Using ${ICU_SUBMODULE_DIR} as the ICU root dir")

  if (WIN32)
    if(NOT EXISTS "${ICU_SUBMODULE_DIR}/icu4c/source/lib/libsicuin.a")
      message(FATAL_ERROR "ICU not built. Please run `./gradlew :nativeruntime:buildICU`.")
    endif()
  else()
    if(NOT EXISTS "${ICU_SUBMODULE_DIR}/icu4c/source/lib/libicui18n.a")
      message(FATAL_ERROR "ICU not built. Please run `./gradlew :nativeruntime:buildICU`.")
    endif()
  endif()

  list(APPEND CMAKE_PREFIX_PATH "${ICU_SUBMODULE_DIR}/icu4c/source/")
  if (WIN32)
    find_library(STATIC_ICUI18N_LIBRARY libsicuin.a)
    find_library(STATIC_ICUUC_LIBRARY libsicuuc.a)
    find_library(STATIC_ICUDATA_LIBRARY libsicudt.a)
  else()
    find_library(STATIC_ICUI18N_LIBRARY libicui18n.a)
    find_library(STATIC_ICUUC_LIBRARY libicuuc.a)
    find_library(STATIC_ICUDATA_LIBRARY libicudata.a)
  endif()
  include_directories(${ICU_SUBMODULE_DIR}/icu4c/source/i18n)
  include_directories(${ICU_SUBMODULE_DIR}/icu4c/source/common)
endif()

# Build flags derived from
# https://cs.android.com/android/platform/superproject/+/android-11.0.0_r1:external/sqlite/dist/Android.bp

set(SQLITE_COMPILE_OPTIONS
  -DHAVE_USLEEP=1
  -DNDEBUG=1
  -DSQLITE_DEFAULT_AUTOVACUUM=1
  -DSQLITE_DEFAULT_FILE_FORMAT=4
  -DSQLITE_DEFAULT_FILE_PERMISSIONS=0600
  -DSQLITE_DEFAULT_JOURNAL_SIZE_LIMIT=1048576
  -DSQLITE_DEFAULT_LEGACY_ALTER_TABLE
  -DSQLITE_ENABLE_BATCH_ATOMIC_WRITE
  -DSQLITE_ENABLE_FTS3
  -DSQLITE_ENABLE_FTS3=1
  -DSQLITE_ENABLE_FTS3_BACKWARDS
  -DSQLITE_ENABLE_FTS4
  -DSQLITE_ENABLE_ICU=1
  -DSQLITE_ENABLE_MEMORY_MANAGEMENT=1
  -DSQLITE_HAVE_ISNAN
  -DSQLITE_OMIT_BUILTIN_TEST
  -DSQLITE_OMIT_COMPILEOPTION_DIAGS
  -DSQLITE_OMIT_LOAD_EXTENSION
  -DSQLITE_POWERSAFE_OVERWRITE=1
  -DSQLITE_SECURE_DELETE
  -DSQLITE_TEMP_STORE=3
  -DSQLITE_THREADSAFE=2
)

include_directories(${ANDROID_SQLITE_DIR}/dist)
include_directories(${ANDROID_SQLITE_DIR}/android)

add_library(androidsqlite STATIC
  ${ANDROID_SQLITE_DIR}/android/OldPhoneNumberUtils.cpp
  ${ANDROID_SQLITE_DIR}/android/PhoneNumberUtils.cpp
  ${ANDROID_SQLITE_DIR}/android/PhoneNumberUtils.h
  ${ANDROID_SQLITE_DIR}/android/sqlite3_android.cpp
  ${ANDROID_SQLITE_DIR}/dist/sqlite3.c
  ${ANDROID_SQLITE_DIR}/dist/sqlite3ext.h
)

target_compile_options(androidsqlite PRIVATE ${SQLITE_COMPILE_OPTIONS})

if (WIN32)
  target_link_libraries(androidsqlite
    --static
    ${STATIC_ICUI18N_LIBRARY}
    ${STATIC_ICUUC_LIBRARY}
    ${STATIC_ICUDATA_LIBRARY}
    gcc
    stdc++
  )
else()
  target_link_libraries(androidsqlite
    ${STATIC_ICUI18N_LIBRARY}
    ${STATIC_ICUUC_LIBRARY}
    ${STATIC_ICUDATA_LIBRARY}
    -ldl
    -lpthread
  )
endif()

include_directories(${JNI_INCLUDE_DIRS})

add_subdirectory (liblog)
include_directories(liblog/include)

include_directories(libnativehelper/include)

add_subdirectory (libutils)
include_directories(libutils/include)

add_subdirectory (androidfw)
include_directories(androidfw/include)

add_subdirectory (libcutils)
include_directories(libcutils/include)

include_directories(base/include)

add_library(nativeruntime SHARED
  jni/AndroidRuntime.cpp
  jni/AndroidRuntime.h
  jni/JNIMain.cpp
  jni/robo_android_database_CursorWindow.cpp
  jni/robo_android_database_SQLiteCommon.cpp
  jni/robo_android_database_SQLiteCommon.h
  jni/robo_android_database_SQLiteConnection.cpp
)

target_link_libraries(nativeruntime
  log
  utils
  androidsqlite
  cutils
  androidfw
)

if (CMAKE_HOST_SYSTEM_NAME MATCHES "Linux")
  target_link_libraries(nativeruntime
    -static-libgcc
    -static-libstdc++
    -Wl,--no-undefined # print an error if there are any undefined symbols
  )
endif()
