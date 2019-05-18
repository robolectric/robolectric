package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.shadows.ShadowLooper.shadowMainLooper;

import android.view.animation.LinearInterpolator;
import android.widget.OverScroller;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.LooperMode;

@RunWith(AndroidJUnit4.class)
public class ShadowOverScrollerTest {
  private OverScroller overScroller;

  @Before
  public void setUp() {
    overScroller =
        new OverScroller(ApplicationProvider.getApplicationContext(), new LinearInterpolator());
  }

  @Test
  public void shouldScrollOverTime() {
    overScroller.startScroll(0, 0, 100, 200, 1000);

    assertThat(overScroller.getStartX()).isEqualTo(0);
    assertThat(overScroller.getStartY()).isEqualTo(0);
    assertThat(overScroller.getFinalX()).isEqualTo(100);
    assertThat(overScroller.getFinalY()).isEqualTo(200);
    assertThat(overScroller.isScrollingInDirection(1, 1)).isTrue();
    assertThat(overScroller.isScrollingInDirection(-1, -1)).isFalse();

    assertThat(overScroller.getCurrX()).isEqualTo(0);
    assertThat(overScroller.getCurrY()).isEqualTo(0);
    assertThat(overScroller.timePassed()).isEqualTo(0);
    assertThat(overScroller.isFinished()).isFalse();

    ShadowSystemClock.advanceBy(Duration.ofMillis(100));
    overScroller.computeScrollOffset();
    assertThat(overScroller.getCurrX()).isEqualTo(10);
    assertThat(overScroller.getCurrY()).isEqualTo(20);
    assertThat(overScroller.timePassed()).isEqualTo(100);
    assertThat(overScroller.isFinished()).isFalse();

    ShadowSystemClock.advanceBy(Duration.ofMillis(401));
    overScroller.computeScrollOffset();
    assertThat(overScroller.getCurrX()).isEqualTo(50);
    assertThat(overScroller.getCurrY()).isEqualTo(100);
    assertThat(overScroller.timePassed()).isEqualTo(501);
    assertThat(overScroller.isFinished()).isFalse();

    ShadowSystemClock.advanceBy(Duration.ofMillis(1000));
    overScroller.computeScrollOffset();
    assertThat(overScroller.getCurrX()).isEqualTo(100);
    assertThat(overScroller.getCurrY()).isEqualTo(200);
    assertThat(overScroller.timePassed()).isEqualTo(1501);
    assertThat(overScroller.isFinished()).isEqualTo(true);
    assertThat(overScroller.isScrollingInDirection(1, 1)).isFalse();
    assertThat(overScroller.isScrollingInDirection(-1, -1)).isFalse();
  }

  @Test
  public void computeScrollOffsetShouldCalculateWhetherScrollIsFinished() {
    assertThat(overScroller.computeScrollOffset()).isFalse();

    overScroller.startScroll(0, 0, 100, 200, 1000);
    assertThat(overScroller.computeScrollOffset()).isTrue();

    ShadowSystemClock.advanceBy(Duration.ofMillis(500));
    assertThat(overScroller.computeScrollOffset()).isTrue();

    ShadowSystemClock.advanceBy(Duration.ofMillis(500));
    assertThat(overScroller.computeScrollOffset()).isTrue();
    assertThat(overScroller.computeScrollOffset()).isFalse();
  }

  @Test
  public void abortAnimationShouldMoveToFinalPositionImmediately() {
    overScroller.startScroll(0, 0, 100, 200, 1000);
    ShadowSystemClock.advanceBy(Duration.ofMillis(500));
    overScroller.abortAnimation();

    assertThat(overScroller.getCurrX()).isEqualTo(100);
    assertThat(overScroller.getCurrY()).isEqualTo(200);
    assertThat(overScroller.timePassed()).isEqualTo(500);
    assertThat(overScroller.isFinished()).isTrue();
  }

  @Test
  public void forceFinishedShouldFinishWithoutMovingFurther() {
    overScroller.startScroll(0, 0, 100, 200, 1000);
    ShadowSystemClock.advanceBy(Duration.ofMillis(500));
    overScroller.computeScrollOffset();
    overScroller.forceFinished(true);

    ShadowSystemClock.advanceBy(Duration.ofMillis(500));
    overScroller.computeScrollOffset();
    assertThat(overScroller.getCurrX()).isEqualTo(50);
    assertThat(overScroller.getCurrY()).isEqualTo(100);
    assertThat(overScroller.timePassed()).isEqualTo(1000);
    assertThat(overScroller.isFinished()).isTrue();
  }
}
