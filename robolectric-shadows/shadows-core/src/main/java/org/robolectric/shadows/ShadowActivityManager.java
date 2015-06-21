package org.robolectric.shadows;

import android.app.ActivityManager;
import android.content.pm.ConfigurationInfo;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import java.util.ArrayList;
import java.util.List;

/**
 * Shadow for {@link android.app.ActivityManager}.
 */
@Implements(ActivityManager.class)
public class ShadowActivityManager {
  private int memoryClass = 16;
  private String backgroundPackage;
  private ActivityManager.MemoryInfo memoryInfo;
  private final List<ActivityManager.RunningTaskInfo> tasks = new ArrayList<>();
  private final List<ActivityManager.RunningServiceInfo> services = new ArrayList<>();
  private final List<ActivityManager.RunningAppProcessInfo> processes = new ArrayList<>();

  @Implementation
  public int getMemoryClass() {
    return memoryClass;
  }

  @Implementation
  public static boolean isUserAMonkey() {
    return false;
  }

  @Implementation
  public List<ActivityManager.RunningTaskInfo> getRunningTasks(int maxNum) {
    return tasks;
  }

  @Implementation
  public List<ActivityManager.RunningServiceInfo> getRunningServices(int maxNum) {
    return services;
  }

  @Implementation
  public List<ActivityManager.RunningAppProcessInfo> getRunningAppProcesses() {
    return processes;
  }

  @Implementation
  public void killBackgroundProcesses(String packageName) {
    backgroundPackage = packageName;
  }

  @Implementation
  public void getMemoryInfo(ActivityManager.MemoryInfo outInfo) {
    if (memoryInfo != null) {
      outInfo.availMem = memoryInfo.availMem;
      outInfo.lowMemory = memoryInfo.lowMemory;
      outInfo.threshold = memoryInfo.threshold;
      outInfo.totalMem = memoryInfo.totalMem;
    }
  }

  @Implementation
  public android.content.pm.ConfigurationInfo getDeviceConfigurationInfo() {
    return new ConfigurationInfo();
  }

  /**
   * Non-Android accessor.
   *
   * @param tasks List of running tasks.
   */
  public void setTasks(List<ActivityManager.RunningTaskInfo> tasks) {
    this.tasks.clear();
    this.tasks.addAll(tasks);
  }

  /**
   * Non-Android accessor.
   *
   * @param services List of running services.
   */
  public void setServices(List<ActivityManager.RunningServiceInfo> services) {
    this.services.clear();
    this.services.addAll(services);
  }

  /**
   * Non-Android accessor.
   *
   * @param processes List of running processes.
   */
  public void setProcesses(List<ActivityManager.RunningAppProcessInfo> processes) {
    this.processes.clear();
    this.processes.addAll(processes);
  }

  /**
   * Non-Android accessor.
   *
   * @return Get the package name of the last background processes killed.
   */
  public String getBackgroundPackage() {
    return backgroundPackage;
  }

  /**
   * Non-Android accessor.
   *
   * @param memoryClass Set the application's memory class.
   */
  public void setMemoryClass(int memoryClass) {
    this.memoryClass = memoryClass;
  }

  /**
   * Non-Android accessor.
   *
   * @param memoryInfo Set the application's memory info.
   */
  public void setMemoryInfo(ActivityManager.MemoryInfo memoryInfo) {
    this.memoryInfo = memoryInfo;
  }
}
