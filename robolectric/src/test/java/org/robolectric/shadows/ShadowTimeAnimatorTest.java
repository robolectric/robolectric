package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.KITKAT;
import static com.google.common.truth.Truth.assertThat;

import android.animation.TimeAnimator;
import android.animation.TimeAnimator.TimeListener;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.time.Duration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;

/** Unit tests for ShadowTimeAnimator. */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = KITKAT)
public class ShadowTimeAnimatorTest {

  @Test
  public void progressTimeBy_started_shouldUpdateListener() {
    TimeListenerImpl listener = new TimeListenerImpl();
    TimeAnimator timeAnimator = new TimeAnimator();
    timeAnimator.setTimeListener(listener);
    timeAnimator.start();

    Shadows.shadowOf(timeAnimator).progressTimeBy(Duration.ofMillis(100), Duration.ofMillis(30));

    assertThat(listener.updateCount).isEqualTo(4);
  }

  @Test
  public void progressTimeBy_notStarted_shouldUpdateListener() {
    TimeListenerImpl listener = new TimeListenerImpl();
    TimeAnimator timeAnimator = new TimeAnimator();
    timeAnimator.setTimeListener(listener);

    Shadows.shadowOf(timeAnimator).progressTimeBy(Duration.ofMillis(100), Duration.ofMillis(30));

    assertThat(listener.updateCount).isEqualTo(0);
  }

  private static final class TimeListenerImpl implements TimeListener {

    private int updateCount = 0;

    @Override
    public void onTimeUpdate(TimeAnimator animation, long totalTime, long deltaTime) {
      updateCount++;
    }
  }
}
