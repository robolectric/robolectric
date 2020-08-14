package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;

import javax.microedition.khronos.egl.EGL;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.opengles.GL;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;

/** Shadow for {@link EGLContext} */
@Implements(value = EGLContext.class, minSdk = JELLY_BEAN_MR1, isInAndroidSdk = true)
public class ShadowEGLContext {

  private static EGL egl = null;
  private static GL gl = null;

  @Resetter
  public static synchronized void reset() {
    egl = null;
    gl = null;
  }

  public static void setEgl(EGL egl) {
    ShadowEGLContext.egl = egl;
  }

  public static void setGl(GL egl) {
    ShadowEGLContext.gl = egl;
  }

  @Implementation
  public static EGL getEGL() {
    return egl;
  }

  @Implementation
  public GL getGL() {
    return gl;
  }
}
