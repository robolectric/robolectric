package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static com.google.common.truth.Truth.assertThat;

import android.app.Activity;
import android.widget.ScrollView;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.junit.rules.SetSystemPropertyRule;

@RunWith(RobolectricTestRunner.class)
@Config(minSdk = O)
public final class ScrollViewTest {
  @Rule public SetSystemPropertyRule setSystemPropertyRule = new SetSystemPropertyRule();

  /**
   * Checks that even if {@code robolectric.useRealScrolling} is set to false, real scrolling code
   * is used when RNG is enabled.
   */
  @Test
  public void smoothScrollTo_usesRealCode_whenRNGEnabled() {
    setSystemPropertyRule.set("robolectric.useRealScrolling", "false");

    Activity activity = Robolectric.setupActivity(Activity.class);
    ScrollView scrollView = new ScrollView(activity);
    scrollView.smoothScrollTo(100, 100);
    // Because the scroll view has no children, it should not get scrolled.
    assertThat(scrollView.getScrollX()).isEqualTo(0);
    assertThat(scrollView.getScrollY()).isEqualTo(0);
  }
}
