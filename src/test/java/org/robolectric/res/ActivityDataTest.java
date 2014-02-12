package org.robolectric.res;

import android.content.pm.ActivityInfo;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ActivityDataTest {

  @Test
  public void test_non_android_namespace() {
    HashMap<String, String> attrs = new HashMap<String, String>();
    attrs.put("testns:name", ".test.TestActivity");
    ActivityData activityData = new ActivityData("testns", attrs, new ArrayList<IntentFilterData>());

    assertEquals(".test.TestActivity", activityData.getName());
    assertNull(activityData.getAllAttributes().get("android:name"));
  }

  @Test
  public void test_config_changes() {
    HashMap<String, String> attrs = new HashMap<String, String>();
    attrs.put("android:configChanges", "mcc|screenLayout|orientation");
    ActivityData activityData = new ActivityData(attrs, new ArrayList<IntentFilterData>());

    int flags = activityData.getConfigChanges();

    assertEquals(ActivityInfo.CONFIG_MCC, flags & ActivityInfo.CONFIG_MCC);
    assertEquals(ActivityInfo.CONFIG_SCREEN_LAYOUT, flags & ActivityInfo.CONFIG_SCREEN_LAYOUT);
    assertEquals(ActivityInfo.CONFIG_ORIENTATION, flags & ActivityInfo.CONFIG_ORIENTATION);

    //Spot check a few other possible values that shouldn't be in the flags.
    assertEquals(0, flags & ActivityInfo.CONFIG_MNC);
    assertEquals(0, flags & ActivityInfo.CONFIG_FONT_SCALE);
    assertEquals(0, flags & ActivityInfo.CONFIG_SCREEN_SIZE);
  }
}
