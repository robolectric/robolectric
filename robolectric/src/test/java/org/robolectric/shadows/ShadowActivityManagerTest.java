package org.robolectric.shadows;

import static android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND;
import static android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND_SERVICE;
import static android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_GONE;
import static android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.R;
import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.robolectric.Shadows.shadowOf;

import android.app.ActivityManager;
import android.app.ActivityManager.AppTask;
import android.app.Application;
import android.app.ApplicationExitInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.os.Process;
import android.os.UserHandle;
import android.os.UserManager;
import android.system.OsConstants;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.android.collect.Lists;
import com.google.common.collect.ImmutableList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;

@RunWith(AndroidJUnit4.class)
public class ShadowActivityManagerTest {

  private static final String PROCESS_NAME = "com.google.android.apps.app";

  private ActivityManager activityManager;
  private Application application;
  private Context context;
  private ShadowActivityManager shadowActivityManager;
  private UserManager userManager;

  @Before
  public void setUp() {
    application = ApplicationProvider.getApplicationContext();
    context = application;
    activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    shadowActivityManager = Shadow.extract(activityManager);
    userManager = (UserManager) context.getSystemService(Context.USER_SERVICE);
  }

  @Test
  public void getMemoryInfo_canGetMemoryInfoForOurProcess() {
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
    ActivityManager.MemoryInfo fetchedMemoryInfo = new ActivityManager.MemoryInfo();
    activityManager.getMemoryInfo(fetchedMemoryInfo);
    assertThat(fetchedMemoryInfo.lowMemory).isFalse();
  }

  @Test
  public void getRunningTasks_shouldReturnTaskList() {
    final ActivityManager.RunningTaskInfo task1 =
        buildTaskInfo(new ComponentName("org.robolectric", "Task 1"));
    final ActivityManager.RunningTaskInfo task2 =
        buildTaskInfo(new ComponentName("org.robolectric", "Task 2"));

    assertThat(activityManager.getRunningTasks(Integer.MAX_VALUE)).isEmpty();
    shadowActivityManager.setTasks(Lists.newArrayList(task1, task2));
    assertThat(activityManager.getRunningTasks(Integer.MAX_VALUE)).containsExactly(task1, task2);
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void getAppTasks_shouldReturnAppTaskList() {
    final AppTask task1 = ShadowAppTask.newInstance();
    final AppTask task2 = ShadowAppTask.newInstance();

    assertThat(activityManager.getAppTasks()).isEmpty();
    shadowActivityManager.setAppTasks(Lists.newArrayList(task1, task2));
    assertThat(activityManager.getAppTasks()).containsExactly(task1, task2);
  }

  @Test
  public void getRunningAppProcesses_shouldReturnProcessList() {
    final ActivityManager.RunningAppProcessInfo process1 =
        buildProcessInfo(new ComponentName("org.robolectric", "Process 1"));
    final ActivityManager.RunningAppProcessInfo process2 =
        buildProcessInfo(new ComponentName("org.robolectric", "Process 2"));

    assertThat(activityManager.getRunningAppProcesses().size()).isEqualTo(1);
    ActivityManager.RunningAppProcessInfo myInfo = activityManager.getRunningAppProcesses().get(0);
    assertThat(myInfo.pid).isEqualTo(android.os.Process.myPid());
    assertThat(myInfo.uid).isEqualTo(android.os.Process.myUid());
    assertThat(myInfo.processName).isEqualTo(application.getBaseContext().getPackageName());
    shadowActivityManager.setProcesses(Lists.newArrayList(process1, process2));
    assertThat(activityManager.getRunningAppProcesses()).containsExactly(process1, process2);
  }

  @Test
  public void getRunningServices_shouldReturnServiceList() {
    final ActivityManager.RunningServiceInfo service1 =
        buildServiceInfo(new ComponentName("org.robolectric", "Service 1"));
    final ActivityManager.RunningServiceInfo service2 =
        buildServiceInfo(new ComponentName("org.robolectric", "Service 2"));

    assertThat(activityManager.getRunningServices(Integer.MAX_VALUE)).isEmpty();
    shadowActivityManager.setServices(Lists.newArrayList(service1, service2));
    assertThat(activityManager.getRunningServices(Integer.MAX_VALUE))
        .containsExactly(service1, service2);
  }

  @Test
  public void getMemoryClass_shouldWork() {
    assertThat(activityManager.getMemoryClass()).isEqualTo(16);

    shadowActivityManager.setMemoryClass(42);
    assertThat(activityManager.getMemoryClass()).isEqualTo(42);
  }

  @Test
  public void killBackgroundProcesses_shouldWork() {
    assertThat(shadowActivityManager.getBackgroundPackage()).isNull();

    activityManager.killBackgroundProcesses("org.robolectric");
    assertThat(shadowActivityManager.getBackgroundPackage()).isEqualTo("org.robolectric");
  }

  @Test
  public void getLauncherLargeIconDensity_shouldWork() {
    assertThat(activityManager.getLauncherLargeIconDensity()).isGreaterThan(0);
  }

  @Test
  public void isUserAMonkey_shouldReturnFalse() {
    assertThat(ActivityManager.isUserAMonkey()).isFalse();
  }

  @Test
  @Config(minSdk = KITKAT)
  public void setIsLowRamDevice() {
    shadowActivityManager.setIsLowRamDevice(true);
    assertThat(activityManager.isLowRamDevice()).isTrue();
  }

  @Test
  @Config(minSdk = M)
  public void getLockTaskModeState() {
    assertThat(activityManager.getLockTaskModeState())
        .isEqualTo(ActivityManager.LOCK_TASK_MODE_NONE);

    shadowActivityManager.setLockTaskModeState(ActivityManager.LOCK_TASK_MODE_LOCKED);
    assertThat(activityManager.getLockTaskModeState())
        .isEqualTo(ActivityManager.LOCK_TASK_MODE_LOCKED);
    assertThat(activityManager.isInLockTaskMode()).isTrue();
  }

  @Test
  public void getMyMemoryState() {
    ActivityManager.RunningAppProcessInfo inState = new ActivityManager.RunningAppProcessInfo();
    ActivityManager.getMyMemoryState(inState);
    assertThat(inState.uid).isEqualTo(Process.myUid());
    assertThat(inState.pid).isEqualTo(Process.myPid());
    assertThat(inState.importanceReasonCode).isEqualTo(0);
    ActivityManager.RunningAppProcessInfo setState = new ActivityManager.RunningAppProcessInfo();
    setState.uid = Process.myUid();
    setState.pid = Process.myPid();
    setState.importanceReasonCode = ActivityManager.RunningAppProcessInfo.REASON_PROVIDER_IN_USE;
    shadowActivityManager.setProcesses(ImmutableList.of(setState));
    inState = new ActivityManager.RunningAppProcessInfo();
    ActivityManager.getMyMemoryState(inState);
    assertThat(inState.importanceReasonCode)
        .isEqualTo(ActivityManager.RunningAppProcessInfo.REASON_PROVIDER_IN_USE);
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR1)
  public void switchUser() {
    shadowOf(application).setSystemService(Context.USER_SERVICE, userManager);
    shadowOf(userManager).addUser(10, "secondary_user", 0);
    activityManager.switchUser(10);
    assertThat(UserHandle.myUserId()).isEqualTo(10);
  }

  @Test
  @Config(minSdk = android.os.Build.VERSION_CODES.Q)
  public void switchUser_withUserHandle_shouldAbleToSwitchUser() {
    UserHandle userHandle = shadowOf(userManager).addUser(10, "secondary_user", 0);
    activityManager.switchUser(userHandle);
    assertThat(UserHandle.myUserId()).isEqualTo(10);
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR1)
  public void getCurrentUser_default_returnZero() {
    assertThat(ActivityManager.getCurrentUser()).isEqualTo(0);
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR1)
  public void getCurrentUser_nonDefault_returnValueSet() {
    shadowOf(application).setSystemService(Context.USER_SERVICE, userManager);
    shadowOf(userManager).addUser(10, "secondary_user", 0);
    activityManager.switchUser(10);

    assertThat(ActivityManager.getCurrentUser()).isEqualTo(10);
  }

  @Test
  @Config(minSdk = O)
  public void onUidImportanceListener() {
    ActivityManager.OnUidImportanceListener listener =
        mock(ActivityManager.OnUidImportanceListener.class);
    InOrder inOrder = inOrder(listener);

    activityManager.addOnUidImportanceListener(listener, IMPORTANCE_FOREGROUND_SERVICE);

    shadowOf(activityManager).setUidImportance(Process.myUid(), IMPORTANCE_FOREGROUND);
    inOrder.verify(listener).onUidImportance(Process.myUid(), IMPORTANCE_FOREGROUND);

    shadowOf(activityManager).setUidImportance(Process.myUid(), IMPORTANCE_VISIBLE);
    inOrder.verify(listener).onUidImportance(Process.myUid(), IMPORTANCE_VISIBLE);

    shadowOf(activityManager).setUidImportance(Process.myUid(), IMPORTANCE_FOREGROUND_SERVICE);
    inOrder.verify(listener).onUidImportance(Process.myUid(), IMPORTANCE_FOREGROUND_SERVICE);

    activityManager.removeOnUidImportanceListener(listener);

    shadowOf(activityManager).setUidImportance(Process.myUid(), IMPORTANCE_VISIBLE);
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  @Config(minSdk = O)
  public void getUidImportance() {
    assertThat(activityManager.getUidImportance(Process.myUid())).isEqualTo(IMPORTANCE_GONE);

    shadowOf(activityManager).setUidImportance(Process.myUid(), IMPORTANCE_FOREGROUND);
    assertThat(activityManager.getUidImportance(Process.myUid())).isEqualTo(IMPORTANCE_FOREGROUND);

    shadowOf(activityManager).setUidImportance(Process.myUid(), IMPORTANCE_VISIBLE);
    assertThat(activityManager.getUidImportance(Process.myUid())).isEqualTo(IMPORTANCE_VISIBLE);

    shadowOf(activityManager).setUidImportance(Process.myUid(), IMPORTANCE_FOREGROUND_SERVICE);
    assertThat(activityManager.getUidImportance(Process.myUid()))
        .isEqualTo(IMPORTANCE_FOREGROUND_SERVICE);
  }

  @Test
  @Config(minSdk = M)
  public void getPackageImportance() {
    assertThat(activityManager.getPackageImportance(context.getPackageName()))
        .isEqualTo(IMPORTANCE_GONE);

    shadowOf(activityManager).setUidImportance(Process.myUid(), IMPORTANCE_FOREGROUND);
    assertThat(activityManager.getPackageImportance(context.getPackageName()))
        .isEqualTo(IMPORTANCE_FOREGROUND);

    shadowOf(activityManager).setUidImportance(Process.myUid(), IMPORTANCE_VISIBLE);
    assertThat(activityManager.getPackageImportance(context.getPackageName()))
        .isEqualTo(IMPORTANCE_VISIBLE);

    shadowOf(activityManager).setUidImportance(Process.myUid(), IMPORTANCE_FOREGROUND_SERVICE);
    assertThat(activityManager.getPackageImportance(context.getPackageName()))
        .isEqualTo(IMPORTANCE_FOREGROUND_SERVICE);
  }

  @Test
  @Config(minSdk = P)
  public void isBackgroundRestricted_returnsValueSet() {
    shadowActivityManager.setBackgroundRestricted(true);

    assertThat(activityManager.isBackgroundRestricted()).isTrue();
  }

  @Config(minSdk = R)
  @Test
  public void getHistoricalProcessExitReasons_noRecord_emptyListReturned() {
    List<ApplicationExitInfo> applicationExitInfoList =
        activityManager.getHistoricalProcessExitReasons(context.getPackageName(), 0, 0);

    assertThat(applicationExitInfoList).isEmpty();
  }

  @Config(minSdk = R)
  @Test
  public void getHistoricalProcessExitReasons_recordsRetunredInCorrectOrder() {
    addApplicationExitInfo(/* pid= */ 1);
    addApplicationExitInfo(/* pid= */ 2);
    addApplicationExitInfo(/* pid= */ 3);

    List<ApplicationExitInfo> applicationExitInfoList =
        activityManager.getHistoricalProcessExitReasons(
            context.getPackageName(), /* pid= */ 0, /* maxNum= */ 0);

    assertThat(applicationExitInfoList).hasSize(3);
    assertThat(applicationExitInfoList.get(0).getPid()).isEqualTo(3);
    assertThat(applicationExitInfoList.get(1).getPid()).isEqualTo(2);
    assertThat(applicationExitInfoList.get(2).getPid()).isEqualTo(1);
  }

  @Config(minSdk = R)
  @Test
  public void getHistoricalProcessExitReasons_pidSpecified_correctRecordReturned() {
    addApplicationExitInfo(/* pid= */ 1);
    addApplicationExitInfo(/* pid= */ 2);
    addApplicationExitInfo(/* pid= */ 3);

    List<ApplicationExitInfo> applicationExitInfoList =
        activityManager.getHistoricalProcessExitReasons(
            context.getPackageName(), /* pid= */ 2, /* maxNum= */ 0);

    assertThat(applicationExitInfoList).hasSize(1);
    assertThat(applicationExitInfoList.get(0).getPid()).isEqualTo(2);
  }

  @Config(minSdk = R)
  @Test
  public void getHistoricalProcessExitReasons_maxNumSpecified_correctNumberOfRecordsReturned() {
    addApplicationExitInfo(/* pid= */ 1);
    addApplicationExitInfo(/* pid= */ 2);
    addApplicationExitInfo(/* pid= */ 3);

    List<ApplicationExitInfo> applicationExitInfoList =
        activityManager.getHistoricalProcessExitReasons(
            context.getPackageName(), /* pid= */ 0, /* maxNum= */ 2);

    assertThat(applicationExitInfoList).hasSize(2);
    assertThat(applicationExitInfoList.get(0).getPid()).isEqualTo(3);
    assertThat(applicationExitInfoList.get(1).getPid()).isEqualTo(2);
  }

  @Config(minSdk = R)
  @Test
  public void addApplicationExitInfo_reasonSet() {
    addApplicationExitInfo(/* pid= */ 1, ApplicationExitInfo.REASON_ANR, /* status= */ 0);
    addApplicationExitInfo(/* pid= */ 2, ApplicationExitInfo.REASON_CRASH, /* status= */ 0);

    List<ApplicationExitInfo> applicationExitInfoList =
        activityManager.getHistoricalProcessExitReasons(
            context.getPackageName(), /* pid= */ 0, /* maxNum= */ 0);

    assertThat(applicationExitInfoList).hasSize(2);
    assertThat(applicationExitInfoList.get(0).getReason())
        .isEqualTo(ApplicationExitInfo.REASON_CRASH);
    assertThat(applicationExitInfoList.get(1).getReason())
        .isEqualTo(ApplicationExitInfo.REASON_ANR);
  }

  @Config(minSdk = R)
  @Test
  public void addApplicationExitInfo_statusSet() {
    addApplicationExitInfo(/* pid= */ 1, ApplicationExitInfo.REASON_SIGNALED, OsConstants.SIGABRT);
    addApplicationExitInfo(/* pid= */ 2, ApplicationExitInfo.REASON_CRASH, /* status= */ 0);

    List<ApplicationExitInfo> applicationExitInfoList =
        activityManager.getHistoricalProcessExitReasons(
            context.getPackageName(), /* pid= */ 0, /* maxNum= */ 0);

    assertThat(applicationExitInfoList).hasSize(2);
    assertThat(applicationExitInfoList.get(0).getStatus()).isEqualTo(0);
    assertThat(applicationExitInfoList.get(1).getStatus()).isEqualTo(OsConstants.SIGABRT);
  }

  @Config(minSdk = R)
  @Test
  public void addApplicationExitInfo_processNameSet() {
    addApplicationExitInfo(/* pid= */ 1);

    List<ApplicationExitInfo> applicationExitInfoList =
        activityManager.getHistoricalProcessExitReasons(
            context.getPackageName(), /* pid= */ 0, /* maxNum= */ 0);

    assertThat(applicationExitInfoList).hasSize(1);
    assertThat(applicationExitInfoList.get(0).getProcessName()).isEqualTo(PROCESS_NAME);
  }

  @Config(minSdk = R)
  @Test
  public void addApplicationExitInfo_timestampSet() {
    shadowActivityManager.addApplicationExitInfo(
        ShadowActivityManager.ApplicationExitInfoBuilder.newBuilder().setTimestamp(123).build());
    shadowActivityManager.addApplicationExitInfo(
        ShadowActivityManager.ApplicationExitInfoBuilder.newBuilder().setTimestamp(456).build());

    List<ApplicationExitInfo> applicationExitInfoList =
        activityManager.getHistoricalProcessExitReasons(
            context.getPackageName(), /* pid= */ 0, /* maxNum= */ 0);

    assertThat(applicationExitInfoList).hasSize(2);
    assertThat(applicationExitInfoList.get(0).getTimestamp()).isEqualTo(456);
    assertThat(applicationExitInfoList.get(1).getTimestamp()).isEqualTo(123);
  }

  @Test
  public void getDeviceConfigurationInfo_returnsValueSet() {
    ConfigurationInfo configurationInfo = new ConfigurationInfo();
    shadowActivityManager.setDeviceConfigurationInfo(configurationInfo);

    assertThat(activityManager.getDeviceConfigurationInfo()).isEqualTo(configurationInfo);
  }

  @Config(minSdk = KITKAT)
  @Test
  public void isApplicationUserDataCleared_returnsDefaultFalse() {
    assertThat(shadowActivityManager.isApplicationUserDataCleared()).isFalse();
  }

  @Config(minSdk = KITKAT)
  @Test
  public void isApplicationUserDataCleared_returnsTrue() {
    activityManager.clearApplicationUserData();
    assertThat(shadowActivityManager.isApplicationUserDataCleared()).isTrue();
  }

  private void addApplicationExitInfo(int pid) {
    addApplicationExitInfo(
        /* pid= */ pid, ApplicationExitInfo.REASON_SIGNALED, /* status= */ OsConstants.SIGKILL);
  }

  private void addApplicationExitInfo(int pid, int reason, int status) {
    shadowActivityManager.addApplicationExitInfo(
        ShadowActivityManager.ApplicationExitInfoBuilder.newBuilder()
            .setProcessName(PROCESS_NAME)
            .setPid(pid)
            .setReason(reason)
            .setStatus(status)
            .build());
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
