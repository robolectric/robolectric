package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.S;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Insets;
import android.graphics.Rect;
import android.view.ThreadedRenderer;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.android.internal.policy.BackdropFrameRenderer;
import com.android.internal.policy.DecorView;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.util.ReflectionHelpers;

/** Tests for {@link ShadowBackdropFrameRenderer} */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = S)
public class ShadowBackdropFrameRendererTest {

  @Test
  public void releaseRenderer_afterCreate_doesNotLeakThread() throws Exception {
    Activity activity = Robolectric.setupActivity(Activity.class);
    DecorView decorView = (DecorView) activity.getWindow().getDecorView();
    for (int i = 0; i < 100; i++) {
      BackdropFrameRenderer r =
          new BackdropFrameRenderer(
              decorView,
              ThreadedRenderer.create(RuntimeEnvironment.getApplication(), false, "renderer"),
              new Rect(0, 0, 0, 0),
              null,
              null,
              null,
              Color.BLUE,
              Color.BLUE,
              false,
              Insets.of(0, 0, 0, 0));
      ReflectionHelpers.callInstanceMethod(r, "releaseRenderer");
      // Without the ShadowBackdropFrameRenderer.run override, the call to join would hang
      // indefinitely.
      r.join();
    }
  }
}
