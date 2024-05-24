package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.S;
import static android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE;
import static org.robolectric.util.ReflectionHelpers.ClassParameter.from;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Insets;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.ThreadedRenderer;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.android.internal.policy.DecorView;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.util.ReflectionHelpers;

/** Tests for {@link ShadowBackdropFrameRenderer} */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = S, maxSdk = UPSIDE_DOWN_CAKE)
public class ShadowBackdropFrameRendererTest {

  @Test
  public void releaseRenderer_afterCreate_doesNotLeakThread() throws Exception {
    Activity activity = Robolectric.setupActivity(Activity.class);
    DecorView decorView = (DecorView) activity.getWindow().getDecorView();
    Class<?> backdropFrameRendererClass =
        Class.forName("com.android.internal.policy.BackdropFrameRenderer");
    for (int i = 0; i < 100; i++) {
      Object r =
          ReflectionHelpers.callConstructor(
              backdropFrameRendererClass,
              from(DecorView.class, decorView),
              from(
                  ThreadedRenderer.class,
                  ThreadedRenderer.create(RuntimeEnvironment.getApplication(), false, "renderer")),
              from(Rect.class, new Rect(0, 0, 0, 0)),
              from(Drawable.class, null),
              from(Drawable.class, null),
              from(Drawable.class, null),
              from(Integer.TYPE, Color.BLUE),
              from(Integer.TYPE, Color.BLUE),
              from(Boolean.TYPE, false),
              from(Insets.class, Insets.of(0, 0, 0, 0)));
      ReflectionHelpers.callInstanceMethod(r, "releaseRenderer");
      // Without the ShadowBackdropFrameRenderer.run override, the call to join would hang
      // indefinitely.
      ((Thread) r).join();
    }
  }
}
