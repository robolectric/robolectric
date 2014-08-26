package org.robolectric.shadows;

import android.view.animation.BounceInterpolator;
import android.widget.Scroller;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class ScrollerTest {
  private Scroller scroller;

  @Before
  public void setup() throws Exception {
    scroller = new Scroller(Robolectric.application, new BounceInterpolator());
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

    Robolectric.idleMainLooper(334);
    assertThat(scroller.getCurrX()).isEqualTo(4);
    assertThat(scroller.getCurrY()).isEqualTo(12);
    assertThat(scroller.isFinished()).isFalse();
    assertThat(scroller.timePassed()).isEqualTo(334);

    Robolectric.idleMainLooper(166);
    assertThat(scroller.getCurrX()).isEqualTo(6);
    assertThat(scroller.getCurrY()).isEqualTo(18);
    assertThat(scroller.isFinished()).isFalse();
    assertThat(scroller.timePassed()).isEqualTo(500);

    Robolectric.idleMainLooper(500);
    assertThat(scroller.getCurrX()).isEqualTo(12);
    assertThat(scroller.getCurrY()).isEqualTo(36);
    assertThat(scroller.isFinished()).isFalse();
    assertThat(scroller.timePassed()).isEqualTo(1000);

    Robolectric.idleMainLooper(1);
    assertThat(scroller.isFinished()).isTrue();
    assertThat(scroller.timePassed()).isEqualTo(1001);
  }

  @Test
  public void computeScrollOffsetShouldCalculateWhetherScrollIsFinished() throws Exception {
    assertThat(scroller.computeScrollOffset()).isFalse();

    scroller.startScroll(0, 0, 12, 36, 1000);
    assertThat(scroller.computeScrollOffset()).isTrue();

    Robolectric.idleMainLooper(500);
    assertThat(scroller.computeScrollOffset()).isTrue();

    Robolectric.idleMainLooper(500);
    assertThat(scroller.computeScrollOffset()).isTrue();
    assertThat(scroller.computeScrollOffset()).isFalse();
  }
}
