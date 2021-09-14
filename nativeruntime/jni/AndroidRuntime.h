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
// https://cs.android.com/android/platform/superproject/+/android-11.0.0_r1:frameworks/base/core/jni/include/android_runtime/AndroidRuntime.h

#ifndef _RUNTIME_ANDROID_RUNTIME_H
#define _RUNTIME_ANDROID_RUNTIME_H

#include <jni.h>

namespace android {

class AndroidRuntime {
 public:
  /** return a pointer to the VM running in this process */
  static JavaVM* getJavaVM();

  /** return a pointer to the JNIEnv pointer for this thread */
  static JNIEnv* getJNIEnv();

 private:
  /* JNI JavaVM pointer */
  static JavaVM* mJavaVM;
};
}  // namespace android

#endif
