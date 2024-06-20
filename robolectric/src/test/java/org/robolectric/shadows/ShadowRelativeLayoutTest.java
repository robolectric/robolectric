package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ShadowRelativeLayoutTest {

  @Test
  public void getRules_shouldShowAddRuleData_sinceApiLevel17() {
    ImageView imageView = new ImageView(ApplicationProvider.getApplicationContext());
    RelativeLayout layout = new RelativeLayout(ApplicationProvider.getApplicationContext());
    layout.addView(
        imageView,
        new RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    RelativeLayout.LayoutParams layoutParams =
        (RelativeLayout.LayoutParams) imageView.getLayoutParams();
    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
    layoutParams.addRule(RelativeLayout.ALIGN_TOP, 1234);
    int[] rules = layoutParams.getRules();
    assertThat(rules)
        .isEqualTo(
            new int[] {0, 0, 0, 0, 0, 0, 1234, 0, 0, 0, 0, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0});
  }
}
