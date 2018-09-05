package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.M;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.app.ActivityManager;
import android.app.ActivityManager.AppTask;
import android.content.ComponentName;
import android.content.Context;
import android.os.Process;
import com.google.android.collect.Lists;
import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
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
  @Config(minSdk = LOLLIPOP)
  public void getAppTasks_shouldReturnAppTaskList() {
    final ActivityManager activityManager = getActivityManager();
    final AppTask task1 = ShadowAppTask.newInstance();
    final AppTask task2 = ShadowAppTask.newInstance();

    assertThat(activityManager.getAppTasks()).isEmpty();
    shadowOf(activityManager).setAppTasks(Lists.newArrayList(task1, task2));
    assertThat(activityManager.getAppTasks()).containsExactly(task1, task2);
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

  @Test @Config(minSdk = KITKAT)
  public void setIsLowRamDevice() {
    final ActivityManager activityManager = getActivityManager();
    shadowOf(activityManager).setIsLowRamDevice(true);
    assertThat(activityManager.isLowRamDevice()).isTrue();
  }

  @Test @Config(minSdk = M)
  public void getLockTaskModeState() throws Exception {
    assertThat(getActivityManager().getLockTaskModeState())
        .isEqualTo(ActivityManager.LOCK_TASK_MODE_NONE);

    shadowOf(getActivityManager()).setLockTaskModeState(ActivityManager.LOCK_TASK_MODE_LOCKED);
    assertThat(getActivityManager().getLockTaskModeState())
        .isEqualTo(ActivityManager.LOCK_TASK_MODE_LOCKED);
    assertThat(getActivityManager().isInLockTaskMode()).isTrue();
  }

  @Test
  public void getMyMemoryState() throws Exception {
    ActivityManager.RunningAppProcessInfo inState = new ActivityManager.RunningAppProcessInfo();
    ActivityManager.getMyMemoryState(inState);
    assertThat(inState.uid).isEqualTo(Process.myUid());
    assertThat(inState.pid).isEqualTo(Process.myPid());
    assertThat(inState.importanceReasonCode).isEqualTo(0);
    ActivityManager.RunningAppProcessInfo setState = new ActivityManager.RunningAppProcessInfo();
    setState.uid = Process.myUid();
    setState.pid = Process.myPid();
    setState.importanceReasonCode = ActivityManager.RunningAppProcessInfo.REASON_PROVIDER_IN_USE;
    shadowOf(getActivityManager()).setProcesses(ImmutableList.of(setState));
    inState = new ActivityManager.RunningAppProcessInfo();
    ActivityManager.getMyMemoryState(inState);
    assertThat(inState.importanceReasonCode)
        .isEqualTo(ActivityManager.RunningAppProcessInfo.REASON_PROVIDER_IN_USE);
  }

  ///////////////////////

  private ActivityManager getActivityManager() {
    return (ActivityManager) RuntimeEnvironment.application.getSystemService(
        Context.ACTIVITY_SERVICE);
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
