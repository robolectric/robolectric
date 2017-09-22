package org.robolectric.shadows;

import static org.assertj.core.api.Assertions.assertThat;

import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Gallery;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class ShadowLayoutParamsTest {
  @Test
  public void testConstructor() throws Exception {
    Gallery.LayoutParams layoutParams = new Gallery.LayoutParams(123, 456);
    assertThat(layoutParams.width).isEqualTo(123);
    assertThat(layoutParams.height).isEqualTo(456);
  }

  @Test
  public void constructor_canTakeSourceLayoutParams() throws Exception {
    ViewGroup.LayoutParams sourceLayoutParams = new ViewGroup.LayoutParams(123, 456);
    ViewGroup.LayoutParams layoutParams1 = new ViewGroup.LayoutParams(sourceLayoutParams);
    FrameLayout.LayoutParams layoutParams2 = new FrameLayout.LayoutParams(sourceLayoutParams);
    assertThat(layoutParams1.height).isEqualTo(456);
    assertThat(layoutParams1.width).isEqualTo(123);
    assertThat(layoutParams2.height).isEqualTo(456);
    assertThat(layoutParams2.width).isEqualTo(123);
  }
}
