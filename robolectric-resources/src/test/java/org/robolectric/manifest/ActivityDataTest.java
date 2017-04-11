package org.robolectric.manifest;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnit4.class)
public class ActivityDataTest {

  @Test
  public void test_non_android_namespace() {
    HashMap<String, String> attrs = new HashMap<>();
    attrs.put("testns:name", ".test.TestActivity");
    ActivityData activityData = new ActivityData("testns", attrs, new ArrayList<IntentFilterData>());

    assertThat(activityData.getName()).isEqualTo(".test.TestActivity");
    assertThat(activityData.getAllAttributes().get("android:name")).isNull();
  }

  @Test
  public void test_config_changes() {
    HashMap<String, String> attrs = new HashMap<>();
    attrs.put("android:configChanges", "mcc|screenLayout|orientation");
    ActivityData activityData = new ActivityData(attrs, new ArrayList<IntentFilterData>());

    assertThat(activityData.getConfigChanges()).isEqualTo("mcc|screenLayout|orientation");
  }
}
