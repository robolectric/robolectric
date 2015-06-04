package org.robolectric.shadows;

import android.os.Build;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.TestRunners;
import org.robolectric.annotation.Config;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(TestRunners.MultiApiWithDefaults.class)
public class ShadowRelativeLayoutTest {

  @Test
  @Config(sdk = Build.VERSION_CODES.JELLY_BEAN_MR2)
  public void getRules_shouldShowAddRuleData_forApiLevel18() throws Exception {
    ImageView imageView = new ImageView(RuntimeEnvironment.application);
    RelativeLayout layout = new RelativeLayout(RuntimeEnvironment.application);
    layout.addView(imageView, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) imageView.getLayoutParams();
    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
    layoutParams.addRule(RelativeLayout.ALIGN_TOP, 1234);
    int[] rules = layoutParams.getRules();
    assertThat(rules).isEqualTo(new int[]{0, 0, 0, 0, 0, 0, 1234, 0, 0, 0, 0, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0});
  }

  @Test
  @Config(sdk = Build.VERSION_CODES.JELLY_BEAN)
  public void getRules_shouldShowAddRuleData_forApiLevel16() throws Exception {
    ImageView imageView = new ImageView(RuntimeEnvironment.application);
    RelativeLayout layout = new RelativeLayout(RuntimeEnvironment.application);
    layout.addView(imageView, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) imageView.getLayoutParams();
    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
    layoutParams.addRule(RelativeLayout.ALIGN_TOP, 1234);
    int[] rules = layoutParams.getRules();
    assertThat(rules).isEqualTo(new int[]{0, 0, 0, 0, 0, 0, 1234, 0, 0, 0, 0, -1, 0, 0, 0, 0});
  }
}
