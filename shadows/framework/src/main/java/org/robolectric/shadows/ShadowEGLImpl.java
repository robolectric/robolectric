package org.robolectric.shadows;

import com.google.android.gles_jni.EGLImpl;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(EGLImpl.class)
public class ShadowEGLImpl {

  @Implementation
  protected static void __staticInitializer__() {
    // no-op native initializer
  }
}
