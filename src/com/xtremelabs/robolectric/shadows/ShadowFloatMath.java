/*
 * Copyright (C) 2007 The Android Open Source Project
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

package com.xtremelabs.robolectric.shadows;

import android.util.FloatMath;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(FloatMath.class)
public class ShadowFloatMath {
    @Implementation
    public static float floor(float value) {
        return (float) Math.floor(value);
    }

    @Implementation
    public static float ceil(float value) {
        return (float) Math.ceil(value);
    }

    @Implementation
    public static float sin(float angle) {
        return (float) Math.sin(angle);
    }

    @Implementation
    public static float cos(float angle) {
        return (float) Math.cos(angle);
    }

    @Implementation
    public static float sqrt(float value) {
        return (float) Math.sqrt(value);
    }
}
