package org.robolectric.shadows;

import static org.assertj.core.api.Assertions.assertThat;

import android.graphics.Rect;
import android.view.TouchDelegate;
import android.view.View;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class ShadowTouchDelegateTest {

  private ShadowTouchDelegate td;
  private Rect rect;
  private View view;

  @Before
  public void setUp() throws Exception {
    rect = new Rect(1, 2, 3, 4);
    view = new View(RuntimeEnvironment.application);
    TouchDelegate realTD = new TouchDelegate(rect, view);
    td = Shadows.shadowOf(realTD);
  }

  @Test
  public void testBounds() {
    Rect bounds = td.getBounds();
    assertThat(bounds).isEqualTo(rect);
  }

  @Test
  public void tetsDelegateView() {
    View view = td.getDelegateView();
    assertThat(view).isEqualTo(this.view);
  }
}
