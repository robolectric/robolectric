package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static com.google.common.truth.Truth.assertThat;

import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
public class ShadowRelativeLayoutTest {

  @Test
  @Config(minSdk = JELLY_BEAN_MR1)
  public void getRules_shouldShowAddRuleData_sinceApiLevel17() throws Exception {
    ImageView imageView = new ImageView(ApplicationProvider.getApplicationContext());
    RelativeLayout layout = new RelativeLayout(ApplicationProvider.getApplicationContext());
    layout.addView(imageView, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) imageView.getLayoutParams();
    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
    layoutParams.addRule(RelativeLayout.ALIGN_TOP, 1234);
    int[] rules = layoutParams.getRules();
    assertThat(rules).isEqualTo(new int[]{0, 0, 0, 0, 0, 0, 1234, 0, 0, 0, 0, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0});
  }

  @Test
  @Config(maxSdk = JELLY_BEAN)
  public void getRules_shouldShowAddRuleData_uptoApiLevel16() throws Exception {
    ImageView imageView = new ImageView(ApplicationProvider.getApplicationContext());
    RelativeLayout layout = new RelativeLayout(ApplicationProvider.getApplicationContext());
    layout.addView(imageView, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) imageView.getLayoutParams();
    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
    layoutParams.addRule(RelativeLayout.ALIGN_TOP, 1234);
    int[] rules = layoutParams.getRules();
    assertThat(rules).isEqualTo(new int[]{0, 0, 0, 0, 0, 0, 1234, 0, 0, 0, 0, -1, 0, 0, 0, 0});
  }
}
