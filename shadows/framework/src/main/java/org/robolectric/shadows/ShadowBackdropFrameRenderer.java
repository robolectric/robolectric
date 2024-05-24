package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.S;
import static android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.os.Looper;
import android.view.Choreographer;
import android.view.ThreadedRenderer;
import com.google.common.util.concurrent.Uninterruptibles;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.LooperMode;
import org.robolectric.annotation.LooperMode.Mode;
import org.robolectric.annotation.RealObject;
import org.robolectric.config.ConfigurationRegistry;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;

/** Shadow for {@link BackdropFrameRenderer} */
@Implements(
    className = "com.android.internal.policy.BackdropFrameRenderer",
    minSdk = S,
    maxSdk = UPSIDE_DOWN_CAKE,
    isInAndroidSdk = false)
public class ShadowBackdropFrameRenderer {

  // Updated to the real value in the generated Shadow constructor
  @RealObject private final Object realBackdropFrameRenderer = null;
  private Looper looper;

  private static final List<Object> activeRenderers = new CopyOnWriteArrayList<>();

  @Implementation
  protected void run() {
    try {
      Looper.prepare();
      activeRenderers.add(realBackdropFrameRenderer);
      looper = Looper.myLooper();
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

  // called from ShadowChoreographer to ensure choreographer is unpaused before this is executed
  static void reset() {
    for (Object renderer : activeRenderers) {
      reflector(BackdropFrameRendererReflector.class, renderer).releaseRenderer();
      // Explicitly quit the looper if in legacy looper mode - otherwise it will hang forever
      if (ConfigurationRegistry.get(LooperMode.Mode.class) == Mode.LEGACY) {
        ShadowBackdropFrameRenderer shadowBackdropFrameRenderer = Shadow.extract(renderer);
        shadowBackdropFrameRenderer.looper.quit();
      }
      Uninterruptibles.joinUninterruptibly((Thread) renderer);
      activeRenderers.remove(renderer);
    }
  }

  @ForType(className = "com.android.internal.policy.BackdropFrameRenderer")
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
