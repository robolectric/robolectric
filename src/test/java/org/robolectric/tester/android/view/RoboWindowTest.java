package org.robolectric.tester.android.view;

import android.view.View;
import android.view.ViewGroup;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class RoboWindowTest {

  @Test
  public void windowManager__shouldNotBeNull() throws Exception {
    RoboWindow window = new RoboWindow(null);
    Assert.assertNotNull(window.getWindowManager());
  }

  @Test
  public void decorViewFindViewById__shouldReturnContentWrapper() throws Exception {
    RoboWindow window = new RoboWindow(Robolectric.application);
    View contentView = new View(Robolectric.application);
    contentView.setTag("content view");
    window.setContentView(contentView);

    // This is the real meat of the test. ActionBarSherlock relies on this code:
    //   window.getDecorView().findViewById(R.id.content)
    ViewGroup contentWrapper = (ViewGroup) window.getDecorView().findViewById(android.R.id.content);
    assertThat(contentWrapper.getChildCount()).isEqualTo(1).as("child count");
    assertThat(contentWrapper.getChildAt(0).getTag()).isEqualTo(contentView.getTag());
  }

  @Test public void setContentViewByResource() throws Exception {
    RoboWindow window = new RoboWindow(Robolectric.application);
    window.setContentView(R.layout.text_views);

    ViewGroup contentWrapper = (ViewGroup) window.findViewById(android.R.id.content);
    assertThat(contentWrapper.getChildCount()).isEqualTo(1).as("child count");
  }

  @Test
  public void contentViewShouldBeMeasuredWithSpecExactly() {
    RoboWindow window = new RoboWindow(Robolectric.application);
    final int[] measureModes = {0, 0};
    View contentView = new View(Robolectric.application) {
      @Override
      protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        measureModes[0] = MeasureSpec.getMode(widthMeasureSpec);
        measureModes[1] = MeasureSpec.getMode(heightMeasureSpec);
      }
    };
    window.setContentView(contentView);
    contentView.requestLayout();
    assertThat(measureModes).isEqualTo(new int[] {View.MeasureSpec.EXACTLY, View.MeasureSpec.EXACTLY});
  }

}
