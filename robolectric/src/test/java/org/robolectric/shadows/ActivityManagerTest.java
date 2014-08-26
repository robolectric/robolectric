package org.robolectric.shadows;

import android.app.ActivityManager;
import android.content.Context;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(TestRunners.WithDefaults.class)
public class ActivityManagerTest {
  @Test
  public void getMemoryInfo_canGetMemoryInfoForOurProcess() {
    ActivityManager activityManager = (ActivityManager) Robolectric.application.getSystemService(Context.ACTIVITY_SERVICE);
    ShadowActivityManager shadowActivityManager = shadowOf(activityManager);
    ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
    memoryInfo.lowMemory = true;
    shadowActivityManager.setMemoryInfo(memoryInfo);
    ActivityManager.MemoryInfo fetchedMemoryInfo = new ActivityManager.MemoryInfo();
    activityManager.getMemoryInfo(fetchedMemoryInfo);
    assertThat(fetchedMemoryInfo.lowMemory).isTrue();
  }

  @Test
  public void getMemoryInfo_canGetMemoryInfoEvenWhenWeDidNotSetIt() {
    ActivityManager activityManager = (ActivityManager) Robolectric.application.getSystemService(Context.ACTIVITY_SERVICE);
    ActivityManager.MemoryInfo fetchedMemoryInfo = new ActivityManager.MemoryInfo();
    activityManager.getMemoryInfo(fetchedMemoryInfo);
    assertThat(fetchedMemoryInfo.lowMemory).isFalse();
  }

  @Test
  public void getLauncherLargeIconDensity_shouldWork() {
    ActivityManager activityManager = (ActivityManager) Robolectric.application.getSystemService(Context.ACTIVITY_SERVICE);
    assertThat(activityManager.getLauncherLargeIconDensity()).isGreaterThan(0);
  }
}
