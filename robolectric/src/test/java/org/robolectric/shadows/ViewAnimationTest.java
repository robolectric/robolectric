package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.TIRAMISU;
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
      // on >= TIRAMISU, real draws are performed

      UiAutomation uiAutomation = InstrumentationRegistry.getInstrumentation().getUiAutomation();
      Bitmap bitmap = uiAutomation.takeScreenshot();
      assertThat(bitmap).isNotNull();
      assertThat(animation.lastAnimationTimeUptimeMs).isEqualTo(startTime + 500);
    } finally {
      ShadowView.setUseRealViewAnimations(false);
    }
  }

  @Test
  @Config(minSdk = TIRAMISU)
  public void viewAnimations_interpolateWhenDrawIsCalled_realDrawing() {
    try {
      ShadowView.setUseRealViewAnimations(true);
      ShadowView.setUseRealDrawTraversals(true);
      // If Choreographer is not paused and real drawing is supported, the animation will advance
      // to the end
      ShadowPausedChoreographer.setPaused(true);
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

      // notably: skip the taking screenshot step as real drawing is enabled
      assertThat(animation.lastAnimationTimeUptimeMs).isEqualTo(startTime + 500);
    } finally {
      ShadowView.setUseRealViewAnimations(false);
      ShadowPausedChoreographer.setPaused(false);
      ShadowView.setUseRealDrawTraversals(false);
    }
  }

  public static class TestTranslateAnimation extends TranslateAnimation {

    public long lastAnimationTimeUptimeMs;

    public TestTranslateAnimation(
        float fromXDelta, float toXDelta, float fromYDelta, float toYDelta) {
      super(fromXDelta, toXDelta, fromYDelta, toYDelta);
    }

    @Override
    public boolean getTransformation(
        long currentTime, Transformation outTransformation, float scale) {
      boolean result = super.getTransformation(currentTime, outTransformation, scale);
      lastAnimationTimeUptimeMs = currentTime;
      return result;
    }
  }
}
