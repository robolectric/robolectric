package org.robolectric.shadows;

import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.os.Build.VERSION_CODES;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

/** Shadow for EGL14. Currently doesn't handle real graphics work, but avoids crashing when run. */
@Implements(value = EGL14.class, minSdk = VERSION_CODES.LOLLIPOP)
public class ShadowEGL14 {
  private static final long UNUSED_HANDLE_ID = 43L;

  @Implementation
  protected static EGLDisplay eglGetDisplay(int displayId) {
    return createEglDisplay();
  }

  @Implementation
  protected static boolean eglInitialize(
      EGLDisplay dpy, int[] major, int majorOffset, int[] minor, int minorOffset) {
    return true;
  }

  @Implementation
  protected static boolean eglChooseConfig(
      EGLDisplay dpy,
      int[] attribList,
      int attribListOffset,
      EGLConfig[] configs,
      int configsOffset,
      int configSize,
      int[] numConfig,
      int numConfigOffset) {
    configs[configsOffset] = createEglConfig();
    numConfig[numConfigOffset] = 1;
    return true;
  }

  @Implementation
  protected static EGLContext eglCreateContext(
      EGLDisplay dpy, EGLConfig config, EGLContext shareContext, int[] attribList, int offset) {
    int majorVersion = getAttribValue(attribList, EGL14.EGL_CONTEXT_CLIENT_VERSION);
    switch (majorVersion) {
      case 2:
      case 3:
        return createEglContext(majorVersion);
      default:
        break;
    }
    return EGL14.EGL_NO_CONTEXT;
  }

  @Implementation
  protected static boolean eglQueryContext(
      EGLDisplay dpy, EGLContext ctx, int attribute, int[] value, int offset) {
    value[offset] = 0;
    switch (attribute) {
      case EGL14.EGL_CONTEXT_CLIENT_VERSION:
        // We stored the version in the handle field when we created the context.
        value[offset] = (int) ctx.getNativeHandle();
        break;
      default:
        // Use default output set above switch.
    }
    return true;
  }

  @Implementation
  protected static EGLSurface eglCreatePbufferSurface(
      EGLDisplay dpy, EGLConfig config, int[] attribList, int offset) {
    return createEglSurface();
  }

  @Implementation
  protected static EGLSurface eglCreateWindowSurface(
      EGLDisplay dpy, EGLConfig config, Object win, int[] attribList, int offset) {
    return createEglSurface();
  }

  @Implementation
  protected static boolean eglMakeCurrent(
      EGLDisplay dpy, EGLSurface draw, EGLSurface read, EGLContext ctx) {
    return true;
  }

  @Implementation
  protected static boolean eglSwapBuffers(EGLDisplay dpy, EGLSurface surface) {
    return true;
  }

  @Implementation
  protected static int eglGetError() {
    return EGL14.EGL_SUCCESS;
  }

  private static EGLDisplay createEglDisplay() {
    return ReflectionHelpers.callConstructor(
        EGLDisplay.class, ClassParameter.from(long.class, UNUSED_HANDLE_ID));
  }

  private static EGLConfig createEglConfig() {
    return ReflectionHelpers.callConstructor(
        EGLConfig.class, ClassParameter.from(long.class, UNUSED_HANDLE_ID));
  }

  private static EGLContext createEglContext(int version) {
    // As a hack store the version number in the unused handle ID so we can retrieve it later
    // if the caller queries a context.
    return ReflectionHelpers.callConstructor(
        EGLContext.class, ClassParameter.from(long.class, version));
  }

  private static EGLSurface createEglSurface() {
    return ReflectionHelpers.callConstructor(
        EGLSurface.class, ClassParameter.from(long.class, UNUSED_HANDLE_ID));
  }

  private static int getAttribValue(int[] attribList, int attribute) {
    int attribValue = 0;
    for (int i = 0; i < attribList.length; i += 2) {
      if (attribList[i] == attribute) {
        attribValue = attribList[i + 1];
      }
    }
    return attribValue;
  }
}
