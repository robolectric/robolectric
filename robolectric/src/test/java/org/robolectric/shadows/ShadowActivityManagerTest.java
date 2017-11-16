package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.M;
import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import com.google.android.collect.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
public class ShadowActivityManagerTest {

  @Test
  public void getMemoryInfo_canGetMemoryInfoForOurProcess() {
    final ActivityManager activityManager = getActivityManager();
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
    final ActivityManager activityManager = getActivityManager();
    ActivityManager.MemoryInfo fetchedMemoryInfo = new ActivityManager.MemoryInfo();
    activityManager.getMemoryInfo(fetchedMemoryInfo);
    assertThat(fetchedMemoryInfo.lowMemory).isFalse();
  }

  @Test
  public void getRunningTasks_shouldReturnTaskList() {
    final ActivityManager activityManager = getActivityManager();
    final ActivityManager.RunningTaskInfo task1 = buildTaskInfo(new ComponentName("org.robolectric", "Task 1"));
    final ActivityManager.RunningTaskInfo task2 = buildTaskInfo(new ComponentName("org.robolectric", "Task 2"));

    assertThat(activityManager.getRunningTasks(Integer.MAX_VALUE)).isEmpty();
    shadowOf(activityManager).setTasks(Lists.newArrayList(task1, task2));
    assertThat(activityManager.getRunningTasks(Integer.MAX_VALUE)).containsExactly(task1, task2);
  }

  @Test
  public void getRunningAppProcesses_shouldReturnProcessList() {
    final ActivityManager activityManager = getActivityManager();
    final ActivityManager.RunningAppProcessInfo process1 = buildProcessInfo(new ComponentName("org.robolectric", "Process 1"));
    final ActivityManager.RunningAppProcessInfo process2 = buildProcessInfo(new ComponentName("org.robolectric", "Process 2"));

    assertThat(activityManager.getRunningAppProcesses().size()).isEqualTo(1);
    ActivityManager.RunningAppProcessInfo myInfo = activityManager.getRunningAppProcesses().get(0);
    assertThat(myInfo.pid).isEqualTo(android.os.Process.myPid());
    assertThat(myInfo.uid).isEqualTo(android.os.Process.myUid());
    assertThat(myInfo.processName).isEqualTo(RuntimeEnvironment.application.getBaseContext().getPackageName());
    shadowOf(activityManager).setProcesses(Lists.newArrayList(process1, process2));
    assertThat(activityManager.getRunningAppProcesses()).containsExactly(process1, process2);
  }

  @Test
  public void getRunningServices_shouldReturnServiceList() {
    final ActivityManager activityManager = getActivityManager();
    final ActivityManager.RunningServiceInfo service1 = buildServiceInfo(new ComponentName("org.robolectric", "Service 1"));
    final ActivityManager.RunningServiceInfo service2 = buildServiceInfo(new ComponentName("org.robolectric", "Service 2"));

    assertThat(activityManager.getRunningServices(Integer.MAX_VALUE)).isEmpty();
    shadowOf(activityManager).setServices(Lists.newArrayList(service1, service2));
    assertThat(activityManager.getRunningServices(Integer.MAX_VALUE)).containsExactly(service1, service2);
  }

  @Test
  public void getMemoryClass_shouldWork() {
    final ActivityManager activityManager = getActivityManager();
    assertThat(activityManager.getMemoryClass()).isEqualTo(16);

    shadowOf(activityManager).setMemoryClass(42);
    assertThat(activityManager.getMemoryClass()).isEqualTo(42);
  }

  @Test
  public void killBackgroundProcesses_shouldWork() {
    final ActivityManager activityManager = getActivityManager();
    assertThat(shadowOf(activityManager).getBackgroundPackage()).isNull();

    activityManager.killBackgroundProcesses("org.robolectric");
    assertThat(shadowOf(activityManager).getBackgroundPackage()).isEqualTo("org.robolectric");
  }

  @Test
  public void getLauncherLargeIconDensity_shouldWork() {
    final ActivityManager activityManager = getActivityManager();
    assertThat(activityManager.getLauncherLargeIconDensity()).isGreaterThan(0);
  }

  @Test
  public void isUserAMonkey_shouldReturnFalse() {
    assertThat(ActivityManager.isUserAMonkey()).isFalse();
  }

  @Test @Config(minSdk = M)
  public void getLockTaskModeState() throws Exception {
    assertThat(getActivityManager().getLockTaskModeState()).isEqualTo(0); // just don't throw
  }

  ///////////////////////
  
  private ActivityManager getActivityManager() {
    return (ActivityManager) RuntimeEnvironment.application.getSystemService(Context.ACTIVITY_SERVICE);
  }

  private ActivityManager.RunningTaskInfo buildTaskInfo(ComponentName name) {
    final ActivityManager.RunningTaskInfo info = new ActivityManager.RunningTaskInfo();
    info.baseActivity = name;
    return info;
  }

  private ActivityManager.RunningServiceInfo buildServiceInfo(ComponentName name) {
    final ActivityManager.RunningServiceInfo info = new ActivityManager.RunningServiceInfo();
    info.service = name;
    return info;
  }

  private ActivityManager.RunningAppProcessInfo buildProcessInfo(ComponentName name) {
    final ActivityManager.RunningAppProcessInfo info = new ActivityManager.RunningAppProcessInfo();
    info.importanceReasonComponent = name;
    return info;
  }
}
