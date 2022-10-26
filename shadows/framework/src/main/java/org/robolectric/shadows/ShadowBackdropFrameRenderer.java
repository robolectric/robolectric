package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.S;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.os.Looper;
import android.view.Choreographer;
import android.view.ThreadedRenderer;
import com.android.internal.policy.BackdropFrameRenderer;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;

/** Shadow for {@link BackdropFrameRenderer} */
@Implements(value = BackdropFrameRenderer.class, minSdk = S, isInAndroidSdk = false)
public class ShadowBackdropFrameRenderer {

  // Updated to the real value in the generated Shadow constructor
  @RealObject private final BackdropFrameRenderer realBackdropFrameRenderer = null;

  @Implementation
  protected void run() {
    try {
      Looper.prepare();
      synchronized (realBackdropFrameRenderer) {
        ThreadedRenderer renderer =
            reflector(BackdropFrameRendererReflector.class, realBackdropFrameRenderer)
                .getRenderer();
        if (renderer == null) {
          // This can happen if 'releaseRenderer' is called immediately after 'start'.
          return;
        }
        reflector(BackdropFrameRendererReflector.class, realBackdropFrameRenderer)
            .setChoreographer(Choreographer.getInstance());
      }
      Looper.loop();
    } finally {
      reflector(BackdropFrameRendererReflector.class, realBackdropFrameRenderer).releaseRenderer();
    }
    synchronized (realBackdropFrameRenderer) {
      reflector(BackdropFrameRendererReflector.class, realBackdropFrameRenderer)
          .setChoreographer(null);
      Choreographer.releaseInstance();
    }
  }

  @ForType(BackdropFrameRenderer.class)
  interface BackdropFrameRendererReflector {
    void releaseRenderer();

    @Accessor("mRenderer")
    ThreadedRenderer getRenderer();

    @Accessor("mChoreographer")
    void setChoreographer(Choreographer c);

    @Accessor("mChoreographer")
    Choreographer getChoreographer();
  }
}
