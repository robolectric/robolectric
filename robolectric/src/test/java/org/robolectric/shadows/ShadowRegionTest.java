package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.graphics.Rect;
import android.graphics.Region;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ShadowRegionTest {
  @Test
  public void testEquals() {
    Region region = new Region(new Rect(0, 0, 100, 100));
    assertThat(region.equals(region)).isTrue();
  }
}
