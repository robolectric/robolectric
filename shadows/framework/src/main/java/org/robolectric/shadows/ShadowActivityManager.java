package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.O;
import static org.robolectric.shadow.api.Shadow.directlyOn;

import android.app.ActivityManager;
import android.app.IActivityManager;
import android.content.pm.ConfigurationInfo;
import android.os.Build.VERSION_CODES;
import android.os.Process;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.ReflectionHelpers;

@Implements(ActivityManager.class)
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
    return new ConfigurationInfo();
  }

  /**
   * @param tasks List of running tasks.
   */
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

  /**
   * @param services List of running services.
   */
  public void setServices(List<ActivityManager.RunningServiceInfo> services) {
    this.services.clear();
    this.services.addAll(services);
  }

  /**
   * @param processes List of running processes.
   */
  public void setProcesses(List<ActivityManager.RunningAppProcessInfo> processes) {
    this.processes.clear();
    this.processes.addAll(processes);
  }

  /**
   * @return Get the package name of the last background processes killed.
   */
  public String getBackgroundPackage() {
    return backgroundPackage;
  }

  /**
   * @param memoryClass Set the application's memory class.
   */
  public void setMemoryClass(int memoryClass) {
    this.memoryClass = memoryClass;
  }

  /**
   * @param memoryInfo Set the application's memory info.
   */
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

  /**
   * Override the return value of isLowRamDevice().
   */
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
}
