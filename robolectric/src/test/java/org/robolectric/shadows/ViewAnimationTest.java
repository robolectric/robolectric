package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.app.UiAutomation;
import android.graphics.Bitmap;
import android.os.Looper;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Transformation;
import android.view.animation.TranslateAnimation;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;
import org.robolectric.annotation.GraphicsMode.Mode;

@RunWith(AndroidJUnit4.class)
@Config(minSdk = O)
@GraphicsMode(Mode.NATIVE)
public class ViewAnimationTest {
  @Test
  public void viewAnimations_interpolateWhenDrawIsCalled() {
    try {
      ShadowView.setUseRealViewAnimations(true);
      Activity activity = Robolectric.setupActivity(Activity.class);
      View view = new View(activity);
      ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(100, 100);
      view.setLayoutParams(params);
      ((ViewGroup) activity.findViewById(android.R.id.content)).addView(view);

      shadowOf(Looper.getMainLooper()).runToEndOfTasks();

      long startTime = SystemClock.uptimeMillis();

      final TestTranslateAnimation animation = new TestTranslateAnimation(0, 100, 0, 0);
      animation.setDuration(1000);
      view.startAnimation(animation);

      shadowOf(Looper.getMainLooper()).idleFor(500, TimeUnit.MILLISECONDS);
      UiAutomation uiAutomation = InstrumentationRegistry.getInstrumentation().getUiAutomation();
      Bitmap bitmap = uiAutomation.takeScreenshot();
      assertThat(bitmap).isNotNull();
      assertThat(animation.lastAnimationTime.toEpochMilli()).isEqualTo(startTime + 500);
    } finally {
      ShadowView.setUseRealViewAnimations(false);
    }
  }

  public static class TestTranslateAnimation extends TranslateAnimation {

    public Instant lastAnimationTime;

    public TestTranslateAnimation(
        float fromXDelta, float toXDelta, float fromYDelta, float toYDelta) {
      super(fromXDelta, toXDelta, fromYDelta, toYDelta);
    }

    @Override
    public boolean getTransformation(
        long currentTime, Transformation outTransformation, float scale) {
      boolean result = super.getTransformation(currentTime, outTransformation, scale);
      lastAnimationTime = Instant.ofEpochMilli(currentTime);
      return result;
    }
  }
}
