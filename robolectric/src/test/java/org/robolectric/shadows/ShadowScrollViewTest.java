package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.content.Context;
import android.view.View;
import android.widget.ScrollView;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ShadowScrollViewTest {
  private ScrollView scrollView;
  private View childView;

  @Before
  public void setUp() {
    Context context = ApplicationProvider.getApplicationContext();
    scrollView = new ScrollView(context);
    childView = new View(context);
  }

  @After
  public void tearDown() {
    scrollView.removeAllViews();
  }

  @Test
  public void shouldSmoothScrollTo() {
    scrollView.smoothScrollTo(7, 6);
    assertThat(scrollView.getScrollX()).isEqualTo(0);
    assertThat(scrollView.getScrollY()).isEqualTo(0);

    scrollView.addView(childView);
    scrollView.smoothScrollTo(7, 6);
    assertThat(scrollView.getScrollX()).isEqualTo(7);
    assertThat(scrollView.getScrollY()).isEqualTo(6);
  }

  @Test
  public void shouldSmoothScrollBy() {
    scrollView.smoothScrollTo(7, 6);
    scrollView.smoothScrollBy(10, 20);
    assertThat(scrollView.getScrollX()).isEqualTo(0);
    assertThat(scrollView.getScrollY()).isEqualTo(0);

    scrollView.addView(childView);
    scrollView.smoothScrollTo(7, 6);
    scrollView.smoothScrollBy(10, 20);
    assertThat(scrollView.getScrollX()).isEqualTo(17);
    assertThat(scrollView.getScrollY()).isEqualTo(26);
  }

  @Test
  public void shouldScrollTo() {
    scrollView.scrollTo(7, 6);
    assertThat(scrollView.getScrollX()).isEqualTo(0);
    assertThat(scrollView.getScrollY()).isEqualTo(0);

    scrollView.addView(childView);
    scrollView.scrollTo(7, 6);
    assertThat(scrollView.getScrollX()).isEqualTo(7);
    assertThat(scrollView.getScrollY()).isEqualTo(6);
  }
}
