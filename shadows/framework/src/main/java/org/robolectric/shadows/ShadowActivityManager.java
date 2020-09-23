package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.R;
import static java.util.stream.Collectors.toCollection;
import static org.robolectric.shadow.api.Shadow.directlyOn;

import android.annotation.RequiresPermission;
import android.app.ActivityManager;
import android.app.ApplicationExitInfo;
import android.app.IActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.os.Build.VERSION_CODES;
import android.os.Process;
import android.os.UserHandle;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;

/** Shadow for {@link android.app.ActivityManager} */
@Implements(value = ActivityManager.class, looseSignatures = true)
public class ShadowActivityManager {
  private int memoryClass = 16;
  private String backgroundPackage;
  private ActivityManager.MemoryInfo memoryInfo;
  private final List<ActivityManager.AppTask> appTasks = new CopyOnWriteArrayList<>();
  private final List<ActivityManager.RunningTaskInfo> tasks = new CopyOnWriteArrayList<>();
  private final List<ActivityManager.RunningServiceInfo> services = new CopyOnWriteArrayList<>();
  private static List<ActivityManager.RunningAppProcessInfo> processes =
      new CopyOnWriteArrayList<>();
  @RealObject private ActivityManager realObject;
  private Boolean isLowRamDeviceOverride = null;
  private int lockTaskModeState = ActivityManager.LOCK_TASK_MODE_NONE;
  private boolean isBackgroundRestricted;
  private final Deque<Object> appExitInfoList = new ArrayDeque<>();
  private ConfigurationInfo configurationInfo;

  public ShadowActivityManager() {
    ActivityManager.RunningAppProcessInfo processInfo = new ActivityManager.RunningAppProcessInfo();
    fillInProcessInfo(processInfo);
    processInfo.processName = RuntimeEnvironment.application.getPackageName();
    processInfo.pkgList = new String[] {RuntimeEnvironment.application.getPackageName()};
    processes.add(processInfo);
  }

  @Implementation
  protected int getMemoryClass() {
    return memoryClass;
  }

  @Implementation
  protected static boolean isUserAMonkey() {
    return false;
  }

  @Implementation(minSdk = JELLY_BEAN_MR1)
  @HiddenApi
  @RequiresPermission(
      anyOf = {
        "android.permission.INTERACT_ACROSS_USERS",
        "android.permission.INTERACT_ACROSS_USERS_FULL"
      })
  protected static int getCurrentUser() {
    return UserHandle.myUserId();
  }

  @Implementation
  protected List<ActivityManager.RunningTaskInfo> getRunningTasks(int maxNum) {
    return tasks;
  }

  /**
   * For tests, returns the list of {@link android.app.ActivityManager.AppTask} set using {@link
   * #setAppTasks(List)}. Returns empty list if nothing is set.
   *
   * @see #setAppTasks(List)
   * @return List of current AppTask.
   */
  @Implementation(minSdk = LOLLIPOP)
  protected List<ActivityManager.AppTask> getAppTasks() {
    return appTasks;
  }

  @Implementation
  protected List<ActivityManager.RunningServiceInfo> getRunningServices(int maxNum) {
    return services;
  }

  @Implementation
  protected List<ActivityManager.RunningAppProcessInfo> getRunningAppProcesses() {
    // This method is explicitly documented not to return an empty list
    if (processes.isEmpty()) {
      return null;
    }
    return processes;
  }

  /** Returns information seeded by {@link #setProcesses}. */
  @Implementation
  protected static void getMyMemoryState(ActivityManager.RunningAppProcessInfo inState) {
    fillInProcessInfo(inState);
    for (ActivityManager.RunningAppProcessInfo info : processes) {
      if (info.pid == Process.myPid()) {
        inState.importance = info.importance;
        inState.lru = info.lru;
        inState.importanceReasonCode = info.importanceReasonCode;
        inState.importanceReasonPid = info.importanceReasonPid;
        inState.lastTrimLevel = info.lastTrimLevel;
        inState.pkgList = info.pkgList;
        inState.processName = info.processName;
      }
    }
  }

  private static void fillInProcessInfo(ActivityManager.RunningAppProcessInfo processInfo) {
    processInfo.pid = Process.myPid();
    processInfo.uid = Process.myUid();
  }

  @HiddenApi
  @Implementation(minSdk = JELLY_BEAN_MR1)
  protected boolean switchUser(int userid) {
    ShadowUserManager shadowUserManager =
        Shadow.extract(RuntimeEnvironment.application.getSystemService(Context.USER_SERVICE));
    shadowUserManager.switchUser(userid);
    return true;
  }

  @Implementation(minSdk = android.os.Build.VERSION_CODES.Q)
  protected boolean switchUser(UserHandle userHandle) {
    return switchUser(userHandle.getIdentifier());
  }

  @Implementation
  protected void killBackgroundProcesses(String packageName) {
    backgroundPackage = packageName;
  }

  @Implementation
  protected void getMemoryInfo(ActivityManager.MemoryInfo outInfo) {
    if (memoryInfo != null) {
      outInfo.availMem = memoryInfo.availMem;
      outInfo.lowMemory = memoryInfo.lowMemory;
      outInfo.threshold = memoryInfo.threshold;
      outInfo.totalMem = memoryInfo.totalMem;
    }
  }

  @Implementation
  protected android.content.pm.ConfigurationInfo getDeviceConfigurationInfo() {
    return configurationInfo == null ? new ConfigurationInfo() : configurationInfo;
  }

  /**
   * Sets the {@link android.content.pm.ConfigurationInfo} returned by {@link
   * ActivityManager#getDeviceConfigurationInfo()}, but has no effect otherwise.
   */
  public void setDeviceConfigurationInfo(ConfigurationInfo configurationInfo) {
    this.configurationInfo = configurationInfo;
  }

  /** @param tasks List of running tasks. */
  public void setTasks(List<ActivityManager.RunningTaskInfo> tasks) {
    this.tasks.clear();
    this.tasks.addAll(tasks);
  }

  /**
   * Sets the values to be returned by {@link #getAppTasks()}.
   *
   * @see #getAppTasks()
   * @param tasks List of app tasks.
   */
  public void setAppTasks(List<ActivityManager.AppTask> appTasks) {
    this.appTasks.clear();
    this.appTasks.addAll(appTasks);
  }

  /** @param services List of running services. */
  public void setServices(List<ActivityManager.RunningServiceInfo> services) {
    this.services.clear();
    this.services.addAll(services);
  }

  /** @param processes List of running processes. */
  public void setProcesses(List<ActivityManager.RunningAppProcessInfo> processes) {
    ShadowActivityManager.processes.clear();
    ShadowActivityManager.processes.addAll(processes);
  }

  /** @return Get the package name of the last background processes killed. */
  public String getBackgroundPackage() {
    return backgroundPackage;
  }

  /** @param memoryClass Set the application's memory class. */
  public void setMemoryClass(int memoryClass) {
    this.memoryClass = memoryClass;
  }

  /** @param memoryInfo Set the application's memory info. */
  public void setMemoryInfo(ActivityManager.MemoryInfo memoryInfo) {
    this.memoryInfo = memoryInfo;
  }

  @Implementation(minSdk = O)
  protected static IActivityManager getService() {
    return ReflectionHelpers.createNullProxy(IActivityManager.class);
  }

  @Implementation(minSdk = KITKAT)
  protected boolean isLowRamDevice() {
    if (isLowRamDeviceOverride != null) {
      return isLowRamDeviceOverride;
    }
    return directlyOn(realObject, ActivityManager.class, "isLowRamDevice");
  }

  /** Override the return value of isLowRamDevice(). */
  public void setIsLowRamDevice(boolean isLowRamDevice) {
    isLowRamDeviceOverride = isLowRamDevice;
  }

  @Implementation(minSdk = VERSION_CODES.M)
  protected int getLockTaskModeState() {
    return lockTaskModeState;
  }

  @Implementation(minSdk = VERSION_CODES.LOLLIPOP)
  protected boolean isInLockTaskMode() {
    return getLockTaskModeState() != ActivityManager.LOCK_TASK_MODE_NONE;
  }

  /**
   * Sets lock task mode state to be reported by {@link ActivityManager#getLockTaskModeState}, but
   * has no effect otherwise.
   */
  public void setLockTaskModeState(int lockTaskModeState) {
    this.lockTaskModeState = lockTaskModeState;
  }

  @Resetter
  public static void reset() {
    processes.clear();
  }

  /** Returns the background restriction state set by {@link #setBackgroundRestricted}. */
  @Implementation(minSdk = P)
  protected boolean isBackgroundRestricted() {
    return isBackgroundRestricted;
  }

  /**
   * Sets the background restriction state reported by {@link
   * ActivityManager#isBackgroundRestricted}, but has no effect otherwise.
   */
  public void setBackgroundRestricted(boolean isBackgroundRestricted) {
    this.isBackgroundRestricted = isBackgroundRestricted;
  }

  /**
   * Returns the matched {@link ApplicationExitInfo} added by {@link #addApplicationExitInfo}.
   * {@code packageName} is ignored.
   */
  @Implementation(minSdk = R)
  protected Object getHistoricalProcessExitReasons(Object packageName, Object pid, Object maxNum) {
    return appExitInfoList.stream()
        .filter(
            appExitInfo ->
                (int) pid == 0 || ((ApplicationExitInfo) appExitInfo).getPid() == (int) pid)
        .limit((int) maxNum == 0 ? appExitInfoList.size() : (int) maxNum)
        .collect(toCollection(ArrayList::new));
  }

  /** Adds an {@link ApplicationExitInfo} with the given information */
  public void addApplicationExitInfo(String processName, int pid, int reason, int status) {
    ApplicationExitInfo appExitInfo = new ApplicationExitInfo();
    appExitInfo.setProcessName(processName);
    appExitInfo.setPid(pid);
    appExitInfo.setReason(reason);
    appExitInfo.setStatus(status);
    appExitInfoList.addFirst(appExitInfo);
  }
}
