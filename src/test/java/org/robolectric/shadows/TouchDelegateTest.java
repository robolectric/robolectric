package org.robolectric.shadows;

import android.graphics.Rect;
import android.view.TouchDelegate;
import android.view.View;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class TouchDelegateTest {

  private ShadowTouchDelegate td;
  private Rect rect;
  private View view;

  @Before
  public void setUp() throws Exception {
    rect = new Rect( 1, 2, 3, 4 );
    view = new View( Robolectric.application );
    TouchDelegate realTD = new TouchDelegate( rect, view );
    td = Robolectric.shadowOf( realTD );
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
