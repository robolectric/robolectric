package org.robolectric.shadows;

import static android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_GONE;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.R;
import static java.util.stream.Collectors.toCollection;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.annotation.RequiresPermission;
import android.app.ActivityManager;
import android.app.ApplicationExitInfo;
import android.app.IActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.content.pm.IPackageDataObserver;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build.VERSION_CODES;
import android.os.Handler;
import android.os.Process;
import android.os.UserHandle;
import android.util.ArrayMap;
import android.util.SparseIntArray;
import androidx.annotation.RequiresApi;
import com.google.common.base.Preconditions;
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
import org.robolectric.util.ReflectionHelpers.ClassParameter;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

/** Shadow for {@link android.app.ActivityManager} */
@Implements(value = ActivityManager.class, looseSignatures = true)
public class ShadowActivityManager {
  private int memoryClass = 16;
  private String backgroundPackage;
  private ActivityManager.MemoryInfo memoryInfo;
  private final List<ActivityManager.AppTask> appTasks = new CopyOnWriteArrayList<>();
  private final List<ActivityManager.RunningTaskInfo> tasks = new CopyOnWriteArrayList<>();
  private final List<ActivityManager.RunningServiceInfo> services = new CopyOnWriteArrayList<>();
  private static final List<ActivityManager.RunningAppProcessInfo> processes =
      new CopyOnWriteArrayList<>();
  private final List<ImportanceListener> importanceListeners = new CopyOnWriteArrayList<>();
  private final SparseIntArray uidImportances = new SparseIntArray();
  @RealObject private ActivityManager realObject;
  private Boolean isLowRamDeviceOverride = null;
  private int lockTaskModeState = ActivityManager.LOCK_TASK_MODE_NONE;
  private boolean isBackgroundRestricted;
  private final Deque<Object> appExitInfoList = new ArrayDeque<>();
  private ConfigurationInfo configurationInfo;
  private Context context;

  @Implementation
  protected void __constructor__(Context context, Handler handler) {
    Shadow.invokeConstructor(
        ActivityManager.class,
        realObject,
        ClassParameter.from(Context.class, context),
        ClassParameter.from(Handler.class, handler));
    this.context = context;
    ActivityManager.RunningAppProcessInfo processInfo = new ActivityManager.RunningAppProcessInfo();
    fillInProcessInfo(processInfo);
    processInfo.processName = context.getPackageName();
    processInfo.pkgList = new String[] {context.getPackageName()};
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
        Shadow.extract(context.getSystemService(Context.USER_SERVICE));
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
    return reflector(ActivityManagerReflector.class, realObject).isLowRamDevice();
  }

  /** Override the return value of isLowRamDevice(). */
  public void setIsLowRamDevice(boolean isLowRamDevice) {
    isLowRamDeviceOverride = isLowRamDevice;
  }

  @Implementation(minSdk = O)
  protected void addOnUidImportanceListener(Object listener, Object importanceCutpoint) {
    importanceListeners.add(new ImportanceListener(listener, (Integer) importanceCutpoint));
  }

  @Implementation(minSdk = O)
  protected void removeOnUidImportanceListener(Object listener) {
    importanceListeners.remove(new ImportanceListener(listener));
  }

  @Implementation(minSdk = M)
  protected int getPackageImportance(String packageName) {
    try {
      return uidImportances.get(
          context.getPackageManager().getPackageUid(packageName, 0), IMPORTANCE_GONE);
    } catch (NameNotFoundException e) {
      return IMPORTANCE_GONE;
    }
  }

  @Implementation(minSdk = O)
  protected int getUidImportance(int uid) {
    return uidImportances.get(uid, IMPORTANCE_GONE);
  }

  public void setUidImportance(int uid, int importance) {
    uidImportances.put(uid, importance);
    for (ImportanceListener listener : importanceListeners) {
      listener.onUidImportanceChanged(uid, importance);
    }
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

  /**
   * Adds an {@link ApplicationExitInfo} with the given information
   *
   * @deprecated Prefer using overload with {@link ApplicationExitInfoBuilder}
   */
  @Deprecated
  @RequiresApi(api = R)
  public void addApplicationExitInfo(String processName, int pid, int reason, int status) {
    ApplicationExitInfo info =
        ApplicationExitInfoBuilder.newBuilder()
            .setProcessName(processName)
            .setPid(pid)
            .setReason(reason)
            .setStatus(status)
            .build();
    addApplicationExitInfo(info);
  }

  /** Adds given {@link ApplicationExitInfo}, see {@link ApplicationExitInfoBuilder} */
  @RequiresApi(api = R)
  public void addApplicationExitInfo(Object info) {
    Preconditions.checkArgument(info instanceof ApplicationExitInfo);
    appExitInfoList.addFirst(info);
  }

  @Implementation
  protected boolean clearApplicationUserData(String packageName, IPackageDataObserver observer) {
    // The real ActivityManager calls clearApplicationUserData on the ActivityManagerService that
    // calls PackageManager#clearApplicationUserData.
    context.getPackageManager().clearApplicationUserData(packageName, observer);
    return true;
  }

  /**
   * Returns true after clearing application user data was requested by calling {@link
   * ActivityManager#clearApplicationUserData()}.
   */
  public boolean isApplicationUserDataCleared() {
    PackageManager packageManager = RuntimeEnvironment.getApplication().getPackageManager();
    return Shadow.<ShadowApplicationPackageManager>extract(packageManager)
        .getClearedApplicationUserDataPackages()
        .contains(RuntimeEnvironment.getApplication().getPackageName());
  }

  /** Builder class for {@link ApplicationExitInfo} */
  @RequiresApi(api = R)
  public static class ApplicationExitInfoBuilder {

    private final ApplicationExitInfo instance;

    public static ApplicationExitInfoBuilder newBuilder() {
      return new ApplicationExitInfoBuilder();
    }

    public ApplicationExitInfoBuilder setDefiningUid(int uid) {
      instance.setDefiningUid(uid);
      return this;
    }

    public ApplicationExitInfoBuilder setDescription(String description) {
      instance.setDescription(description);
      return this;
    }

    public ApplicationExitInfoBuilder setImportance(int importance) {
      instance.setImportance(importance);
      return this;
    }

    public ApplicationExitInfoBuilder setPackageUid(int packageUid) {
      instance.setPackageUid(packageUid);
      return this;
    }

    public ApplicationExitInfoBuilder setPid(int pid) {
      instance.setPid(pid);
      return this;
    }

    public ApplicationExitInfoBuilder setProcessName(String processName) {
      instance.setProcessName(processName);
      return this;
    }

    public ApplicationExitInfoBuilder setProcessStateSummary(byte[] processStateSummary) {
      instance.setProcessStateSummary(processStateSummary);
      return this;
    }

    public ApplicationExitInfoBuilder setPss(long pss) {
      instance.setPss(pss);
      return this;
    }

    public ApplicationExitInfoBuilder setRealUid(int realUid) {
      instance.setRealUid(realUid);
      return this;
    }

    public ApplicationExitInfoBuilder setReason(int reason) {
      instance.setReason(reason);
      return this;
    }

    public ApplicationExitInfoBuilder setRss(long rss) {
      instance.setRss(rss);
      return this;
    }

    public ApplicationExitInfoBuilder setStatus(int status) {
      instance.setStatus(status);
      return this;
    }

    public ApplicationExitInfoBuilder setTimestamp(long timestamp) {
      instance.setTimestamp(timestamp);
      return this;
    }

    public ApplicationExitInfo build() {
      return instance;
    }

    private ApplicationExitInfoBuilder() {
      this.instance = new ApplicationExitInfo();
    }
  }

  @ForType(ActivityManager.class)
  interface ActivityManagerReflector {

    @Direct
    boolean isLowRamDevice();
  }

  /**
   * Helper class mimicing the package-private UidObserver class inside {@link ActivityManager}.
   *
   * <p>This class is responsible for maintaining the cutpoint of the corresponding {@link
   * ActivityManager.OnUidImportanceListener} and invoking the listener only when the importance of
   * a given UID crosses the cutpoint.
   */
  private static class ImportanceListener {

    private final ActivityManager.OnUidImportanceListener listener;
    private final int importanceCutpoint;

    private final ArrayMap<Integer, Boolean> lastAboveCuts = new ArrayMap<>();

    ImportanceListener(Object listener) {
      this(listener, 0);
    }

    ImportanceListener(Object listener, int importanceCutpoint) {
      this.listener = (ActivityManager.OnUidImportanceListener) listener;
      this.importanceCutpoint = importanceCutpoint;
    }

    void onUidImportanceChanged(int uid, int importance) {
      Boolean isAboveCut = importance > importanceCutpoint;
      if (!isAboveCut.equals(lastAboveCuts.get(uid))) {
        lastAboveCuts.put(uid, isAboveCut);
        listener.onUidImportance(uid, importance);
      }
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof ImportanceListener)) {
        return false;
      }

      ImportanceListener that = (ImportanceListener) o;
      return listener.equals(that.listener);
    }

    @Override
    public int hashCode() {
      return listener.hashCode();
    }
  }
}
