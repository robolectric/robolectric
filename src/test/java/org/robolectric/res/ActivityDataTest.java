package org.robolectric.res;

import org.junit.Test;
import java.util.HashMap;
import java.util.ArrayList;
import android.content.pm.ActivityInfo;
import static org.fest.assertions.api.Assertions.*;

public class ActivityDataTest {

  @Test
  public void test_non_android_namespace() {
    HashMap<String, String> attrs = new HashMap<String, String>();
    attrs.put("testns:name", ".test.TestActivity");
    ActivityData activityData = new ActivityData("testns", attrs, new ArrayList<IntentFilterData>());

    assertThat(activityData.getName()).isEqualTo(".test.TestActivity");
    assertThat(activityData.getAllAttributes().get("android:name")).isNull();
  }

  @Test
  public void test_config_changes() {
    HashMap<String, String> attrs = new HashMap<String, String>();
    attrs.put("android:configChanges", "mcc|screenLayout|orientation");
    ActivityData activityData = new ActivityData(attrs, new ArrayList<IntentFilterData>());

    final int flags = activityData.getConfigChanges();
    assertThat(flags & ActivityInfo.CONFIG_MCC).isEqualTo(ActivityInfo.CONFIG_MCC);
    assertThat(flags & ActivityInfo.CONFIG_SCREEN_LAYOUT).isEqualTo(ActivityInfo.CONFIG_SCREEN_LAYOUT);
    assertThat(flags & ActivityInfo.CONFIG_ORIENTATION).isEqualTo(ActivityInfo.CONFIG_ORIENTATION);

    // Spot check a few other possible values that shouldn't be in the flags.
    assertThat(flags & ActivityInfo.CONFIG_MNC).isZero();
    assertThat(flags & ActivityInfo.CONFIG_FONT_SCALE).isZero();
    assertThat(flags & ActivityInfo.CONFIG_SCREEN_SIZE).isZero();
  }
}
