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

import android.graphics.Bitmap;
import android.graphics.HardwareRenderer.ASurfaceTransactionCallback;
import android.graphics.HardwareRenderer.FrameCompleteCallback;
import android.graphics.HardwareRenderer.FrameDrawingCallback;
import android.graphics.HardwareRenderer.PictureCapturedCallback;
import android.graphics.HardwareRenderer.PrepareSurfaceControlForWebviewCallback;
import android.view.Surface;
import java.io.FileDescriptor;

/**
 * Native methods for {@link HardwareRenderer} JNI registration.
 *
 * <p>Native method signatures are derived from
 * https://cs.android.com/android/platform/superproject/+/android-12.0.0_r1:frameworks/base/graphics/java/android/graphics/HardwareRenderer.java
 */
public final class HardwareRendererNatives {
  public static native void disableVsync();

  public static native void preload();

  public static native boolean isWebViewOverlaysEnabled();

  public static native void setupShadersDiskCache(String cacheFile, String skiaCacheFile);

  public static native void nRotateProcessStatsBuffer();

  public static native void nSetProcessStatsBuffer(int fd);

  public static native int nGetRenderThreadTid(long nativeProxy);

  public static native long nCreateRootRenderNode();

  public static native long nCreateProxy(boolean translucent, long rootRenderNode);

  public static native void nDeleteProxy(long nativeProxy);

  public static native boolean nLoadSystemProperties(long nativeProxy);

  public static native void nSetName(long nativeProxy, String name);

  public static native void nSetSurface(long nativeProxy, Surface window, boolean discardBuffer);

  public static native void nSetSurfaceControl(long nativeProxy, long nativeSurfaceControl);

  public static native boolean nPause(long nativeProxy);

  public static native void nSetStopped(long nativeProxy, boolean stopped);

  public static native void nSetLightGeometry(
      long nativeProxy, float lightX, float lightY, float lightZ, float lightRadius);

  public static native void nSetLightAlpha(
      long nativeProxy, float ambientShadowAlpha, float spotShadowAlpha);

  public static native void nSetOpaque(long nativeProxy, boolean opaque);

  public static native void nSetColorMode(long nativeProxy, int colorMode);

  public static native void nSetSdrWhitePoint(long nativeProxy, float whitePoint);

  public static native void nSetIsHighEndGfx(boolean isHighEndGfx);

  public static native int nSyncAndDrawFrame(long nativeProxy, long[] frameInfo, int size);

  public static native void nDestroy(long nativeProxy, long rootRenderNode);

  public static native void nRegisterAnimatingRenderNode(long rootRenderNode, long animatingNode);

  public static native void nRegisterVectorDrawableAnimator(long rootRenderNode, long animator);

  public static native long nCreateTextureLayer(long nativeProxy);

  public static native void nBuildLayer(long nativeProxy, long node);

  public static native boolean nCopyLayerInto(long nativeProxy, long layer, long bitmapHandle);

  public static native void nPushLayerUpdate(long nativeProxy, long layer);

  public static native void nCancelLayerUpdate(long nativeProxy, long layer);

  public static native void nDetachSurfaceTexture(long nativeProxy, long layer);

  public static native void nDestroyHardwareResources(long nativeProxy);

  public static native void nTrimMemory(int level);

  public static native void nOverrideProperty(String name, String value);

  public static native void nFence(long nativeProxy);

  public static native void nStopDrawing(long nativeProxy);

  public static native void nNotifyFramePending(long nativeProxy);

  public static native void nDumpProfileInfo(long nativeProxy, FileDescriptor fd, int dumpFlags);

  public static native void nAddRenderNode(
      long nativeProxy, long rootRenderNode, boolean placeFront);

  public static native void nRemoveRenderNode(long nativeProxy, long rootRenderNode);

  public static native void nDrawRenderNode(long nativeProxy, long rootRenderNode);

  public static native void nSetContentDrawBounds(
      long nativeProxy, int left, int top, int right, int bottom);

  public static native void nSetPictureCaptureCallback(
      long nativeProxy, PictureCapturedCallback callback);

  public static native void nSetASurfaceTransactionCallback(
      long nativeProxy, ASurfaceTransactionCallback callback);

  public static native void nSetPrepareSurfaceControlForWebviewCallback(
      long nativeProxy, PrepareSurfaceControlForWebviewCallback callback);

  public static native void nSetFrameCallback(long nativeProxy, FrameDrawingCallback callback);

  public static native void nSetFrameCompleteCallback(
      long nativeProxy, FrameCompleteCallback callback);

  public static native void nAddObserver(long nativeProxy, long nativeObserver);

  public static native void nRemoveObserver(long nativeProxy, long nativeObserver);

  public static native int nCopySurfaceInto(
      Surface surface, int srcLeft, int srcTop, int srcRight, int srcBottom, long bitmapHandle);

  public static native Bitmap nCreateHardwareBitmap(long renderNode, int width, int height);

  public static native void nSetHighContrastText(boolean enabled);

  public static native void nHackySetRTAnimationsEnabled(boolean enabled);

  public static native void nSetDebuggingEnabled(boolean enabled);

  public static native void nSetIsolatedProcess(boolean enabled);

  public static native void nSetContextPriority(int priority);

  public static native void nAllocateBuffers(long nativeProxy);

  public static native void nSetForceDark(long nativeProxy, boolean enabled);

  public static native void nSetDisplayDensityDpi(int densityDpi);

  public static native void nInitDisplayInfo(
      int width,
      int height,
      float refreshRate,
      int wideColorDataspace,
      long appVsyncOffsetNanos,
      long presentationDeadlineNanos);

  private HardwareRendererNatives() {}
}
