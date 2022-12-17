/*
 * Copyright (C) 2022 The Android Open Source Project
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

package org.robolectric.nativeruntime;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.HardwareBuffer;
import android.os.Parcel;

/**
 * Native methods for Surface JNI registration.
 *
 * <p>Native method signatures are derived from
 * https://cs.android.com/android/platform/superproject/+/android-12.0.0_r1:frameworks/base/graphics/java/android/view/Surface.java
 */
public final class SurfaceNatives {

  public static native long nativeCreateFromSurfaceTexture(SurfaceTexture surfaceTexture);

  public static native long nativeCreateFromSurfaceControl(long surfaceControlNativeObject);

  public static native long nativeGetFromSurfaceControl(
      long surfaceObject, long surfaceControlNativeObject);

  public static native long nativeGetFromBlastBufferQueue(
      long surfaceObject, long blastBufferQueueNativeObject);

  public static native long nativeLockCanvas(long nativeObject, Canvas canvas, Rect dirty);

  public static native void nativeUnlockCanvasAndPost(long nativeObject, Canvas canvas);

  public static native void nativeRelease(long nativeObject);

  public static native boolean nativeIsValid(long nativeObject);

  public static native boolean nativeIsConsumerRunningBehind(long nativeObject);

  public static native long nativeReadFromParcel(long nativeObject, Parcel source);

  public static native void nativeWriteToParcel(long nativeObject, Parcel dest);

  public static native void nativeAllocateBuffers(long nativeObject);

  public static native int nativeGetWidth(long nativeObject);

  public static native int nativeGetHeight(long nativeObject);

  public static native long nativeGetNextFrameNumber(long nativeObject);

  public static native int nativeSetScalingMode(long nativeObject, int scalingMode);

  public static native int nativeForceScopedDisconnect(long nativeObject);

  public static native int nativeAttachAndQueueBufferWithColorSpace(
      long nativeObject, HardwareBuffer buffer, int colorSpaceId);

  public static native int nativeSetSharedBufferModeEnabled(long nativeObject, boolean enabled);

  public static native int nativeSetAutoRefreshEnabled(long nativeObject, boolean enabled);

  public static native int nativeSetFrameRate(
      long nativeObject, float frameRate, int compatibility, int changeFrameRateStrategy);

  private SurfaceNatives() {}
}
