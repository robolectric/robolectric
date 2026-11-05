package org.robolectric.shadows;

import static org.junit.Assert.assertEquals;

import android.app.Activity;
import android.view.View;
import android.widget.ScrollView;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.GraphicsMode;
import org.robolectric.annotation.GraphicsMode.Mode;
import org.robolectric.junit.rules.SetSystemPropertyRule;

@RunWith(AndroidJUnit4.class)
public class ShadowScrollViewTest {
  @Rule public SetSystemPropertyRule setSystemPropertyRule = new SetSystemPropertyRule();

  @Test
  @GraphicsMode(Mode.LEGACY)
  public void shouldSmoothScrollTo() {
    // This test depends on broken scrolling behavior.
    setSystemPropertyRule.set("robolectric.useRealScrolling", "false");

    ScrollView scrollView = new ScrollView(ApplicationProvider.getApplicationContext());
    scrollView.smoothScrollTo(7, 6);

    assertEquals(7, scrollView.getScrollX());
    assertEquals(6, scrollView.getScrollY());
  }

  @Test
  @GraphicsMode(Mode.LEGACY)
  public void shouldSmoothScrollBy() {
    // This test depends on broken scrolling behavior.
    setSystemPropertyRule.set("robolectric.useRealScrolling", "false");

    ScrollView scrollView = new ScrollView(ApplicationProvider.getApplicationContext());
    scrollView.smoothScrollTo(7, 6);
    scrollView.smoothScrollBy(10, 20);
    assertEquals(17, scrollView.getScrollX());
    assertEquals(26, scrollView.getScrollY());
  }

  @Test
  public void realCode_shouldSmoothScrollTo() {
    setSystemPropertyRule.set("robolectric.useRealScrolling", "true");

    Activity activity = Robolectric.setupActivity(Activity.class);
    ScrollView scrollView = new ScrollView(activity);
    View view = new View(activity);
    view.setMinimumWidth(1000);
    view.setMinimumHeight(1000);
    scrollView.addView(view);
    activity.setContentView(scrollView);
    ShadowLooper.idleMainLooper();

    scrollView.smoothScrollTo(7, 6);
    ShadowLooper.idleMainLooper();

    assertEquals(0, scrollView.getScrollX());
    assertEquals(6, scrollView.getScrollY());
  }

  @Test
  public void realCode_shouldSmoothScrollBy() {
    setSystemPropertyRule.set("robolectric.useRealScrolling", "true");

    Activity activity = Robolectric.setupActivity(Activity.class);
    ScrollView scrollView = new ScrollView(activity);
    View view = new View(activity);
    view.setMinimumWidth(1000);
    view.setMinimumHeight(1000);
    scrollView.addView(view);
    activity.setContentView(scrollView);
    ShadowLooper.idleMainLooper();

    scrollView.smoothScrollTo(7, 6);
    ShadowLooper.idleMainLooper();
    scrollView.smoothScrollBy(10, 20);
    ShadowLooper.idleMainLooper();

    assertEquals(0, scrollView.getScrollX());
    assertEquals(26, scrollView.getScrollY());
  }
}
