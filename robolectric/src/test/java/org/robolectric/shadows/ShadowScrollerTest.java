package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.shadows.ShadowBaseLooper.shadowMainLooper;

import android.view.animation.BounceInterpolator;
import android.widget.Scroller;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ShadowScrollerTest {
  private Scroller scroller;

  @Before
  public void setup() throws Exception {
    scroller = new Scroller(ApplicationProvider.getApplicationContext(), new BounceInterpolator());
  }

  @Test
  public void shouldScrollOverTime() throws Exception {
    scroller.startScroll(0, 0, 12, 36, 1000);

    assertThat(scroller.getStartX()).isEqualTo(0);
    assertThat(scroller.getStartY()).isEqualTo(0);
    assertThat(scroller.getFinalX()).isEqualTo(12);
    assertThat(scroller.getFinalY()).isEqualTo(36);
    assertThat(scroller.getDuration()).isEqualTo(1000);

    assertThat(scroller.getCurrX()).isEqualTo(0);
    assertThat(scroller.getCurrY()).isEqualTo(0);
    assertThat(scroller.isFinished()).isFalse();
    assertThat(scroller.timePassed()).isEqualTo(0);

    shadowMainLooper().idleFor(334, TimeUnit.MILLISECONDS);
    assertThat(scroller.getCurrX()).isEqualTo(4);
    assertThat(scroller.getCurrY()).isEqualTo(12);
    assertThat(scroller.isFinished()).isFalse();
    assertThat(scroller.timePassed()).isEqualTo(334);

    shadowMainLooper().idleFor(166, TimeUnit.MILLISECONDS);
    assertThat(scroller.getCurrX()).isEqualTo(6);
    assertThat(scroller.getCurrY()).isEqualTo(18);
    assertThat(scroller.isFinished()).isFalse();
    assertThat(scroller.timePassed()).isEqualTo(500);

    shadowMainLooper().idleFor(500, TimeUnit.MILLISECONDS);
    assertThat(scroller.getCurrX()).isEqualTo(12);
    assertThat(scroller.getCurrY()).isEqualTo(36);
    assertThat(scroller.isFinished()).isFalse();
    assertThat(scroller.timePassed()).isEqualTo(1000);

    shadowMainLooper().idleFor(1, TimeUnit.MILLISECONDS);
    assertThat(scroller.isFinished()).isTrue();
    assertThat(scroller.timePassed()).isEqualTo(1001);
  }

  @Test
  public void computeScrollOffsetShouldCalculateWhetherScrollIsFinished() throws Exception {
    assertThat(scroller.computeScrollOffset()).isFalse();

    scroller.startScroll(0, 0, 12, 36, 1000);
    assertThat(scroller.computeScrollOffset()).isTrue();

    shadowMainLooper().idleFor(500, TimeUnit.MILLISECONDS);
    assertThat(scroller.computeScrollOffset()).isTrue();

    shadowMainLooper().idleFor(500, TimeUnit.MILLISECONDS);
    assertThat(scroller.computeScrollOffset()).isTrue();
    assertThat(scroller.computeScrollOffset()).isFalse();
  }
}
