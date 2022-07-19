package org.robolectric.shadows;

import static org.junit.Assert.assertEquals;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

@RunWith(AndroidJUnit4.class)
public class ShadowScrollViewTest {
  @Test
  public void shouldSmoothScrollTo() {
    ScrollView scrollView = new ScrollView(ApplicationProvider.getApplicationContext());
    scrollView.smoothScrollTo(7, 6);

    assertEquals(7, scrollView.getScrollX());
    assertEquals(6, scrollView.getScrollY());
  }

  @Test
  public void shouldSmoothScrollBy() {
    ScrollView scrollView = new ScrollView(ApplicationProvider.getApplicationContext());
    scrollView.smoothScrollTo(7, 6);
    scrollView.smoothScrollBy(10, 20);

    assertEquals(17, scrollView.getScrollX());
    assertEquals(26, scrollView.getScrollY());
  }

  @Test
  public void realCode_shouldSmoothScrollTo() {
    try {
      System.setProperty("robolectric.nativeruntime.enableGraphics", "true");
      Activity activity = Robolectric.setupActivity(Activity.class);
      ScrollView scrollView = new ScrollView(activity);
      View view = new View(activity);
      view.setLayoutParams(new ViewGroup.LayoutParams(1000, 1000));
      view.layout(
          0,
          0,
          activity.findViewById(android.R.id.content).getWidth(),
          activity.findViewById(android.R.id.content).getHeight());
      scrollView.addView(view);
      scrollView.smoothScrollTo(7, 6);
      assertEquals(7, scrollView.getScrollX());
      assertEquals(6, scrollView.getScrollY());
    } finally {
      System.clearProperty("robolectric.nativeruntime.enableGraphics");
    }
  }

  @Test
  public void realCode_shouldSmoothScrollBy() {
    try {
      System.setProperty("robolectric.nativeruntime.enableGraphics", "true");
      Activity activity = Robolectric.setupActivity(Activity.class);
      ScrollView scrollView = new ScrollView(activity);
      View view = new View(activity);
      view.setLayoutParams(new ViewGroup.LayoutParams(1000, 1000));
      view.layout(
          0,
          0,
          activity.findViewById(android.R.id.content).getWidth(),
          activity.findViewById(android.R.id.content).getHeight());
      scrollView.addView(view);
      scrollView.smoothScrollTo(7, 6);
      scrollView.smoothScrollBy(10, 20);
      assertEquals(17, scrollView.getScrollX());
      assertEquals(26, scrollView.getScrollY());
    } finally {
      System.clearProperty("robolectric.nativeruntime.enableGraphics");
    }
  }
}
