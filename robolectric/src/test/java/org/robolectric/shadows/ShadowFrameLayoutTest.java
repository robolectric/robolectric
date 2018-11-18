package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertNotNull;

import android.view.View;
import android.widget.FrameLayout;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ShadowFrameLayoutTest {

  private FrameLayout frameLayout;

  @Before
  public void setUp() throws Exception {
    frameLayout = new FrameLayout(ApplicationProvider.getApplicationContext());
  }

  @Test
  public void testNotNull() {
    assertNotNull(frameLayout);
  }

  @Test
  public void onMeasure_shouldNotLayout() throws Exception {
    assertThat(frameLayout.getHeight()).isEqualTo(0);
    assertThat(frameLayout.getWidth()).isEqualTo(0);

    frameLayout.measure(View.MeasureSpec.makeMeasureSpec(150, View.MeasureSpec.AT_MOST),
        View.MeasureSpec.makeMeasureSpec(300, View.MeasureSpec.AT_MOST));

    assertThat(frameLayout.getHeight()).isEqualTo(0);
    assertThat(frameLayout.getWidth()).isEqualTo(0);
  }
}
