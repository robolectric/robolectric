package org.robolectric.shadows;

import android.graphics.drawable.Animatable2;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;

import static android.os.Build.VERSION_CODES.KITKAT_WATCH;
import static android.os.Build.VERSION_CODES.N;
import static com.google.common.truth.Truth.assertThat;

@RunWith(AndroidJUnit4.class)
public class ShadowAnimatedVectorDrawableTest {

  private AnimatedVectorDrawable animatedVectorDrawable;

  @Before
  public void setUp() {
    Robolectric.getForegroundThreadScheduler().pause();
    animatedVectorDrawable =
        (AnimatedVectorDrawable) ApplicationProvider.getApplicationContext()
            .getResources()
            .getDrawable(R.drawable.animated_vector, null);
  }

  @Test
  @Config(minSdk = N)
  public void start_ShouldRunAnimationAndNotifyListeners() {
    boolean[] didCallOnAnimationStart = {false},
        didCallOnAnimationEnd = {false};
    animatedVectorDrawable.registerAnimationCallback(new Animatable2.AnimationCallback() {
      @Override
      public void onAnimationStart(Drawable drawable) {
        didCallOnAnimationStart[0] = true;
      }

      @Override
      public void onAnimationEnd(Drawable drawable) {
        didCallOnAnimationEnd[0] = true;
      }
    });

    animatedVectorDrawable.start();
    Robolectric.flushForegroundThreadScheduler();

    assertThat(didCallOnAnimationStart[0]).isTrue();
    assertThat(didCallOnAnimationEnd[0]).isTrue();
  }
}
