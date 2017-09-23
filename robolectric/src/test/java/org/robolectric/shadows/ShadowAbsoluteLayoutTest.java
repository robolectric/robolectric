package org.robolectric.shadows;

import static org.assertj.core.api.Assertions.assertThat;

import android.view.ViewGroup;
import android.widget.AbsoluteLayout;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class ShadowAbsoluteLayoutTest {
  @Test
  public void getLayoutParams_shouldReturnAbsoluteLayoutParams() throws Exception {
    ViewGroup.LayoutParams layoutParams = (new AbsoluteLayout(RuntimeEnvironment.application) {
      @Override protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return super.generateDefaultLayoutParams();
      }
    }).generateDefaultLayoutParams();

    assertThat(layoutParams).isInstanceOf(AbsoluteLayout.LayoutParams.class);
  }
}
