cmake_minimum_required(VERSION 3.10)

project(nativeruntime)

# Some libutils headers require C++17
set (CMAKE_CXX_STANDARD 17)

find_package(JNI REQUIRED)

# On Mac OS, search Homebrew for the icu4c distribution. The system version
# does not include headers and static libraries.
if (CMAKE_HOST_SYSTEM_NAME MATCHES "Darwin")
  execute_process(
    COMMAND brew --prefix icu4c
    RESULT_VARIABLE BREW_ICU4C
    OUTPUT_VARIABLE BREW_ICU4C_PREFIX
    OUTPUT_STRIP_TRAILING_WHITESPACE
  )

  if (NOT BREW_ICU4C EQUAL 0)
    message( FATAL_ERROR "'brew --prefix icu4c' failed. Ensure homebrew is installed and run 'brew install icu4c'.")
  endif()

  if (BREW_ICU4C EQUAL 0 AND EXISTS "${BREW_ICU4C_PREFIX}")
    message(STATUS "Found icu4c installed by Homebrew at ${BREW_ICU4C_PREFIX}")
    list(APPEND CMAKE_PREFIX_PATH ${BREW_ICU4C_PREFIX})
    find_library(BREW_ICUUC_LIBRARY libicuuc.a)
    find_library(BREW_ICUI18N_LIBRARY libicui18n.a)
    find_library(BREW_ICUDATA_LIBRARY libicudata.a)
    include_directories(${BREW_ICU4C_PREFIX}/include)
  endif()

  if (NOT BREW_ICUUC_LIBRARY)
    message(FATAL_ERROR "libicuuc.a not found. Please run 'brew install icu4c'")
  endif()
endif()

set(CMAKE_POSITION_INDEPENDENT_CODE ON)

# Build flags derived from
# https://cs.android.com/android/platform/superproject/+/android-11.0.0_r1:external/sqlite/dist/Android.bp
set(ANDROID_SQLITE_DIR "${CMAKE_CURRENT_SOURCE_DIR}/../external/sqlite")

if(NOT EXISTS "${ANDROID_SQLITE_DIR}/dist/sqlite3.c")
  message(FATAL_ERROR "SQLite submodule missing. Please run `git submodule update --init`.")
endif()

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

if (CMAKE_HOST_SYSTEM_NAME MATCHES "Darwin")
  target_link_libraries(androidsqlite
    ${BREW_ICUUC_LIBRARY}
    ${BREW_ICUI18N_LIBRARY}
    ${BREW_ICUDATA_LIBRARY}
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
  )
endif()
