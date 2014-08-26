package org.robolectric.shadows;

import android.view.ViewGroup;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * {@link org.robolectric.shadows.ShadowViewGroup.ShadowMarginLayoutParams} test suite.
 */
@RunWith(TestRunners.WithDefaults.class)
public class MarginLayoutParamsTest  {

  @Test
  public void testSetMargins() {
    ViewGroup.MarginLayoutParams marginLayoutParams = new ViewGroup.MarginLayoutParams(0, 0);
    marginLayoutParams.setMargins(1, 2, 3, 4);
    assertThat(marginLayoutParams.leftMargin).isEqualTo(1);
    assertThat(marginLayoutParams.topMargin).isEqualTo(2);
    assertThat(marginLayoutParams.rightMargin).isEqualTo(3);
    assertThat(marginLayoutParams.bottomMargin).isEqualTo(4);
  }
}
