package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Unit tests for {@link ShadowEGL14Test} */
@RunWith(AndroidJUnit4.class)
public final class ShadowEGL14Test {
  @Test
  public void eglGetCurrentContext() {
    assertThat(EGL14.eglGetCurrentContext()).isNotNull();
  }

  @Test
  public void eglGetDisplay() {
    assertThat(EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)).isNotNull();
  }

  @Test
  public void eglChooseConfig_retrieveConfigs() {
    EGLDisplay display = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
    // When the config array is not null, numConfig is filled with the number of configs returned
    // (up to the size of the config array).
    EGLConfig[] configs = new EGLConfig[1];
    int[] numConfig = new int[1];
    assertThat(EGL14.eglChooseConfig(display, new int[0], 0, configs, 0, 1, numConfig, 0)).isTrue();
    assertThat(numConfig[0]).isGreaterThan(0);
    assertThat(configs[0]).isNotNull();
  }

  @Test
  public void eglChooseConfig_countMatching() {
    EGLDisplay display = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
    // When the config array is null, numConfig is filled with the total number of configs matched.
    EGLConfig[] configs = null;
    int[] numConfig = new int[1];
    assertThat(EGL14.eglChooseConfig(display, new int[0], 0, configs, 0, 0, numConfig, 0)).isTrue();
    assertThat(numConfig[0]).isGreaterThan(0);
  }

  @Test
  public void eglCreateContext_v2() {
    EGLDisplay display = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
    int[] attribList = {EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, EGL14.EGL_NONE};
    EGLContext context = EGL14.eglCreateContext(display, createEglConfig(), null, attribList, 0);
    assertThat(context).isNotNull();
    int[] values = new int[1];
    EGL14.eglQueryContext(display, context, EGL14.EGL_CONTEXT_CLIENT_VERSION, values, 0);
    assertThat(values[0]).isEqualTo(2);
  }

  @Test
  public void eglCreatePbufferSurface() {
    EGLDisplay display = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
    assertThat(EGL14.eglCreatePbufferSurface(display, createEglConfig(), new int[0], 0))
        .isNotNull();
  }

  @Test
  public void eglCreateWindowSurface() {
    EGLDisplay display = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
    assertThat(EGL14.eglCreateWindowSurface(display, createEglConfig(), null, new int[0], 0))
        .isNotNull();
  }

  private EGLConfig createEglConfig() {
    EGLDisplay display = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
    EGLConfig[] configs = new EGLConfig[1];
    int[] numConfig = new int[1];
    EGL14.eglChooseConfig(display, new int[0], 0, configs, 0, 1, numConfig, 0);
    return configs[0];
  }
}
