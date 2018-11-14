package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.view.ViewGroup;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ShadowMarginLayoutParamsTest {

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
