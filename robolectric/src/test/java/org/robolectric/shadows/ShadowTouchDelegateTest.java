package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.graphics.Rect;
import android.view.TouchDelegate;
import android.view.View;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Shadows;

@RunWith(AndroidJUnit4.class)
public class ShadowTouchDelegateTest {

  private ShadowTouchDelegate td;
  private Rect rect;
  private View view;

  @Before
  public void setUp() throws Exception {
    rect = new Rect(1, 2, 3, 4);
    view = new View(ApplicationProvider.getApplicationContext());
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
