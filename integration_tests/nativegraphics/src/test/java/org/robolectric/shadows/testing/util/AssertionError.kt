/*
 * Copyright 2020 The Android Open Source Project
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

package org.robolectric.shadows.testing.util

/**
 * Helper to use ThrowNew from JNI to throw an AssertionError.
 *
 * ThrowNew calls <init>(String), but that constructor for AssertionError is private. In this case,
 * there is no Throwable cause, so simplify the native code.
 */
class AssertionError(msg: String) : java.lang.AssertionError(msg, null)
