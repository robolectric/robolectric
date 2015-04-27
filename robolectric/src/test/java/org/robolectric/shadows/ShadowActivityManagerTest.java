package org.robolectric.shadows;

import android.app.ActivityManager;
import android.content.Context;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.TestRunners;
import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.Shadows.shadowOf;

@RunWith(TestRunners.WithDefaults.class)
public class ShadowActivityManagerTest {
  @Test
  public void getMemoryInfo_canGetMemoryInfoForOurProcess() {
    ActivityManager activityManager = (ActivityManager) RuntimeEnvironment.application.getSystemService(Context.ACTIVITY_SERVICE);
    ShadowActivityManager shadowActivityManager = shadowOf(activityManager);
    ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
    memoryInfo.availMem = 12345;
    memoryInfo.lowMemory = true;
    memoryInfo.threshold = 10000;
    memoryInfo.totalMem = 55555;
    shadowActivityManager.setMemoryInfo(memoryInfo);
    ActivityManager.MemoryInfo fetchedMemoryInfo = new ActivityManager.MemoryInfo();
    activityManager.getMemoryInfo(fetchedMemoryInfo);
    assertThat(fetchedMemoryInfo.availMem).isEqualTo(12345);
    assertThat(fetchedMemoryInfo.lowMemory).isTrue();
    assertThat(fetchedMemoryInfo.threshold).isEqualTo(10000);
    assertThat(fetchedMemoryInfo.totalMem).isEqualTo(55555);
  }

  @Test
  public void getMemoryInfo_canGetMemoryInfoEvenWhenWeDidNotSetIt() {
    ActivityManager activityManager = (ActivityManager) RuntimeEnvironment.application.getSystemService(Context.ACTIVITY_SERVICE);
    ActivityManager.MemoryInfo fetchedMemoryInfo = new ActivityManager.MemoryInfo();
    activityManager.getMemoryInfo(fetchedMemoryInfo);
    assertThat(fetchedMemoryInfo.lowMemory).isFalse();
  }

  @Test
  public void getLauncherLargeIconDensity_shouldWork() {
    ActivityManager activityManager = (ActivityManager) RuntimeEnvironment.application.getSystemService(Context.ACTIVITY_SERVICE);
    assertThat(activityManager.getLauncherLargeIconDensity()).isGreaterThan(0);
  }

  @Test
  public void isUserAMonkey_should_be_always_false() {
      assertThat(ActivityManager.isUserAMonkey()).isFalse();
  }
}
