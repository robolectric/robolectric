package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Gallery;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
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
