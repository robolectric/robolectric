/*
 * Copyright (C) 2005 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// Derived from
// https://cs.android.com/android/platform/superproject/+/android-11.0.0_r1:frameworks/base/core/jni/AndroidRuntime.cpp

#include "AndroidRuntime.h"

#include <assert.h>

#include "jni.h"

using namespace android;

/*static*/ JavaVM* AndroidRuntime::mJavaVM = nullptr;

/*static*/ JavaVM* AndroidRuntime::getJavaVM() {
  return AndroidRuntime::mJavaVM;
}

/*
 * Get the JNIEnv pointer for this thread.
 *
 * Returns NULL if the slot wasn't allocated or populated.
 */
/*static*/ JNIEnv* AndroidRuntime::getJNIEnv() {
  JNIEnv* env;
  JavaVM* vm = AndroidRuntime::getJavaVM();
  assert(vm != nullptr);

  if (vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_4) != JNI_OK)
    return nullptr;
  return env;
}
