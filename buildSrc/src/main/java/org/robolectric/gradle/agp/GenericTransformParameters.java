/*
 * Copyright (C) 2019 The Android Open Source Project
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

/*
 * This class comes from AGP internals:
 * https://cs.android.com/android-studio/platform/tools/base/+/mirror-goog-studio-main:build-system/gradle-core/src/main/java/com/android/build/gradle/internal/dependency/GenericTransformParameters.kt;bpv=0
 */

package org.robolectric.gradle.agp;

import org.gradle.api.artifacts.transform.TransformParameters;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Internal;

/** Generic {@link TransformParameters} for all of our Artifact Transforms. */
// TODO Keep the original Kotlin implementation when `buildSrc` is migrated to Kotlin.
public interface GenericTransformParameters extends TransformParameters {
  @Internal
  Property<String> getProjectName();
}
