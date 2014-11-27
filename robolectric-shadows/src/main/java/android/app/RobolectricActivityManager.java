package android.app;

import android.content.ComponentName;
import android.content.IIntentReceiver;
import android.content.IIntentSender;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.UriPermission;
import android.content.pm.ApplicationInfo;
import android.content.pm.ConfigurationInfo;
import android.content.pm.IPackageDataObserver;
import android.content.pm.ParceledListSlice;
import android.content.pm.UserInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Debug;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.PersistableBundle;
import android.os.RemoteException;
import android.os.StrictMode;
import android.service.voice.IVoiceInteractionSession;
import com.android.internal.app.IVoiceInteractor;

import java.util.List;

public class RobolectricActivityManager implements IActivityManager {
  @Override
  public int startActivity(IApplicationThread caller, String callingPackage, Intent intent, String resolvedType, IBinder resultTo, String resultWho, int requestCode, int flags, ProfilerInfo profilerInfo, Bundle options) throws RemoteException {
    return 0;
  }

  @Override
  public int startActivityAsUser(IApplicationThread caller, String callingPackage, Intent intent, String resolvedType, IBinder resultTo, String resultWho, int requestCode, int flags, ProfilerInfo profilerInfo, Bundle options, int userId) throws RemoteException {
    return 0;
  }

  @Override
  public int startActivityAsCaller(IApplicationThread caller, String callingPackage, Intent intent, String resolvedType, IBinder resultTo, String resultWho, int requestCode, int flags, ProfilerInfo profilerInfo, Bundle options, int userId) throws RemoteException {
    return 0;
  }

  @Override
  public WaitResult startActivityAndWait(IApplicationThread caller, String callingPackage, Intent intent, String resolvedType, IBinder resultTo, String resultWho, int requestCode, int flags, ProfilerInfo profilerInfo, Bundle options, int userId) throws RemoteException {
    return null;
  }

  @Override
  public int startActivityWithConfig(IApplicationThread caller, String callingPackage, Intent intent, String resolvedType, IBinder resultTo, String resultWho, int requestCode, int startFlags, Configuration newConfig, Bundle options, int userId) throws RemoteException {
    return 0;
  }

  @Override
  public int startActivityIntentSender(IApplicationThread caller, IntentSender intent, Intent fillInIntent, String resolvedType, IBinder resultTo, String resultWho, int requestCode, int flagsMask, int flagsValues, Bundle options) throws RemoteException {
    return 0;
  }

  @Override
  public int startVoiceActivity(String callingPackage, int callingPid, int callingUid, Intent intent, String resolvedType, IVoiceInteractionSession session, IVoiceInteractor interactor, int flags, ProfilerInfo profilerInfo, Bundle options, int userId) throws RemoteException {
    return 0;
  }

  @Override
  public boolean startNextMatchingActivity(IBinder callingActivity, Intent intent, Bundle options) throws RemoteException {
    return false;
  }

  @Override
  public int startActivityFromRecents(int taskId, Bundle options) throws RemoteException {
    return 0;
  }

  @Override
  public boolean finishActivity(IBinder token, int code, Intent data, boolean finishTask) throws RemoteException {
    return false;
  }

  @Override
  public void finishSubActivity(IBinder token, String resultWho, int requestCode) throws RemoteException {

  }

  @Override
  public boolean finishActivityAffinity(IBinder token) throws RemoteException {
    return false;
  }

  @Override
  public void finishVoiceTask(IVoiceInteractionSession session) throws RemoteException {

  }

  @Override
  public boolean releaseActivityInstance(IBinder token) throws RemoteException {
    return false;
  }

  @Override
  public void releaseSomeActivities(IApplicationThread app) throws RemoteException {

  }

  @Override
  public boolean willActivityBeVisible(IBinder token) throws RemoteException {
    return false;
  }

  @Override
  public Intent registerReceiver(IApplicationThread caller, String callerPackage, IIntentReceiver receiver, IntentFilter filter, String requiredPermission, int userId) throws RemoteException {
    return null;
  }

  @Override
  public void unregisterReceiver(IIntentReceiver receiver) throws RemoteException {

  }

  @Override
  public int broadcastIntent(IApplicationThread caller, Intent intent, String resolvedType, IIntentReceiver resultTo, int resultCode, String resultData, Bundle map, String requiredPermission, int appOp, boolean serialized, boolean sticky, int userId) throws RemoteException {
    return 0;
  }

  @Override
  public void unbroadcastIntent(IApplicationThread caller, Intent intent, int userId) throws RemoteException {

  }

  @Override
  public void finishReceiver(IBinder who, int resultCode, String resultData, Bundle map, boolean abortBroadcast) throws RemoteException {

  }

  @Override
  public void attachApplication(IApplicationThread app) throws RemoteException {

  }

  @Override
  public void activityResumed(IBinder token) throws RemoteException {

  }

  @Override
  public void activityIdle(IBinder token, Configuration config, boolean stopProfiling) throws RemoteException {

  }

  @Override
  public void activityPaused(IBinder token) throws RemoteException {

  }

  @Override
  public void activityStopped(IBinder token, Bundle state, PersistableBundle persistentState, CharSequence description) throws RemoteException {

  }

  @Override
  public void activitySlept(IBinder token) throws RemoteException {

  }

  @Override
  public void activityDestroyed(IBinder token) throws RemoteException {

  }

  @Override
  public String getCallingPackage(IBinder token) throws RemoteException {
    return null;
  }

  @Override
  public ComponentName getCallingActivity(IBinder token) throws RemoteException {
    return null;
  }

  @Override
  public List<IAppTask> getAppTasks(String callingPackage) throws RemoteException {
    return null;
  }

  @Override
  public int addAppTask(IBinder activityToken, Intent intent, ActivityManager.TaskDescription description, Bitmap thumbnail) throws RemoteException {
    return 0;
  }

  @Override
  public Point getAppTaskThumbnailSize() throws RemoteException {
    return null;
  }

  @Override
  public List<ActivityManager.RunningTaskInfo> getTasks(int maxNum, int flags) throws RemoteException {
    return null;
  }

  @Override
  public List<ActivityManager.RecentTaskInfo> getRecentTasks(int maxNum, int flags, int userId) throws RemoteException {
    return null;
  }

  @Override
  public ActivityManager.TaskThumbnail getTaskThumbnail(int taskId) throws RemoteException {
    return null;
  }

  @Override
  public List<ActivityManager.RunningServiceInfo> getServices(int maxNum, int flags) throws RemoteException {
    return null;
  }

  @Override
  public List<ActivityManager.ProcessErrorStateInfo> getProcessesInErrorState() throws RemoteException {
    return null;
  }

  @Override
  public void moveTaskToFront(int task, int flags, Bundle options) throws RemoteException {

  }

  @Override
  public void moveTaskToBack(int task) throws RemoteException {

  }

  @Override
  public boolean moveActivityTaskToBack(IBinder token, boolean nonRoot) throws RemoteException {
    return false;
  }

  @Override
  public void moveTaskBackwards(int task) throws RemoteException {

  }

  @Override
  public void moveTaskToStack(int taskId, int stackId, boolean toTop) throws RemoteException {

  }

  @Override
  public void resizeStack(int stackId, Rect bounds) throws RemoteException {

  }

  @Override
  public List<ActivityManager.StackInfo> getAllStackInfos() throws RemoteException {
    return null;
  }

  @Override
  public ActivityManager.StackInfo getStackInfo(int stackId) throws RemoteException {
    return null;
  }

  @Override
  public boolean isInHomeStack(int taskId) throws RemoteException {
    return false;
  }

  @Override
  public void setFocusedStack(int stackId) throws RemoteException {

  }

  @Override
  public int getTaskForActivity(IBinder token, boolean onlyRoot) throws RemoteException {
    return 0;
  }

  @Override
  public ContentProviderHolder getContentProvider(IApplicationThread caller, String name, int userId, boolean stable) throws RemoteException {
    return null;
  }

  @Override
  public ContentProviderHolder getContentProviderExternal(String name, int userId, IBinder token) throws RemoteException {
    return null;
  }

  @Override
  public void removeContentProvider(IBinder connection, boolean stable) throws RemoteException {

  }

  @Override
  public void removeContentProviderExternal(String name, IBinder token) throws RemoteException {

  }

  @Override
  public void publishContentProviders(IApplicationThread caller, List<ContentProviderHolder> providers) throws RemoteException {

  }

  @Override
  public boolean refContentProvider(IBinder connection, int stableDelta, int unstableDelta) throws RemoteException {
    return false;
  }

  @Override
  public void unstableProviderDied(IBinder connection) throws RemoteException {

  }

  @Override
  public void appNotRespondingViaProvider(IBinder connection) throws RemoteException {

  }

  @Override
  public PendingIntent getRunningServiceControlPanel(ComponentName service) throws RemoteException {
    return null;
  }

  @Override
  public ComponentName startService(IApplicationThread caller, Intent service, String resolvedType, int userId) throws RemoteException {
    return null;
  }

  @Override
  public int stopService(IApplicationThread caller, Intent service, String resolvedType, int userId) throws RemoteException {
    return 0;
  }

  @Override
  public boolean stopServiceToken(ComponentName className, IBinder token, int startId) throws RemoteException {
    return false;
  }

  @Override
  public void setServiceForeground(ComponentName className, IBinder token, int id, Notification notification, boolean keepNotification) throws RemoteException {

  }

  @Override
  public int bindService(IApplicationThread caller, IBinder token, Intent service, String resolvedType, IServiceConnection connection, int flags, int userId) throws RemoteException {
    return 0;
  }

  @Override
  public boolean unbindService(IServiceConnection connection) throws RemoteException {
    return false;
  }

  @Override
  public void publishService(IBinder token, Intent intent, IBinder service) throws RemoteException {

  }

  @Override
  public void unbindFinished(IBinder token, Intent service, boolean doRebind) throws RemoteException {

  }

  @Override
  public void serviceDoneExecuting(IBinder token, int type, int startId, int res) throws RemoteException {

  }

  @Override
  public IBinder peekService(Intent service, String resolvedType) throws RemoteException {
    return null;
  }

  @Override
  public boolean bindBackupAgent(ApplicationInfo appInfo, int backupRestoreMode) throws RemoteException {
    return false;
  }

  @Override
  public void clearPendingBackup() throws RemoteException {

  }

  @Override
  public void backupAgentCreated(String packageName, IBinder agent) throws RemoteException {

  }

  @Override
  public void unbindBackupAgent(ApplicationInfo appInfo) throws RemoteException {

  }

  @Override
  public void killApplicationProcess(String processName, int uid) throws RemoteException {

  }

  @Override
  public boolean startInstrumentation(ComponentName className, String profileFile, int flags, Bundle arguments, IInstrumentationWatcher watcher, IUiAutomationConnection connection, int userId, String abiOverride) throws RemoteException {
    return false;
  }

  @Override
  public void finishInstrumentation(IApplicationThread target, int resultCode, Bundle results) throws RemoteException {

  }

  @Override
  public Configuration getConfiguration() throws RemoteException {
    return null;
  }

  @Override
  public void updateConfiguration(Configuration values) throws RemoteException {

  }

  @Override
  public void setRequestedOrientation(IBinder token, int requestedOrientation) throws RemoteException {

  }

  @Override
  public int getRequestedOrientation(IBinder token) throws RemoteException {
    return 0;
  }

  @Override
  public ComponentName getActivityClassForToken(IBinder token) throws RemoteException {
    return null;
  }

  @Override
  public String getPackageForToken(IBinder token) throws RemoteException {
    return null;
  }

  @Override
  public IIntentSender getIntentSender(int type, String packageName, IBinder token, String resultWho, int requestCode, Intent[] intents, String[] resolvedTypes, int flags, Bundle options, int userId) throws RemoteException {
    return null;
  }

  @Override
  public void cancelIntentSender(IIntentSender sender) throws RemoteException {

  }

  @Override
  public boolean clearApplicationUserData(String packageName, IPackageDataObserver observer, int userId) throws RemoteException {
    return false;
  }

  @Override
  public String getPackageForIntentSender(IIntentSender sender) throws RemoteException {
    return null;
  }

  @Override
  public int getUidForIntentSender(IIntentSender sender) throws RemoteException {
    return 0;
  }

  @Override
  public int handleIncomingUser(int callingPid, int callingUid, int userId, boolean allowAll, boolean requireFull, String name, String callerPackage) throws RemoteException {
    return 0;
  }

  @Override
  public void setProcessLimit(int max) throws RemoteException {

  }

  @Override
  public int getProcessLimit() throws RemoteException {
    return 0;
  }

  @Override
  public void setProcessForeground(IBinder token, int pid, boolean isForeground) throws RemoteException {

  }

  @Override
  public int checkPermission(String permission, int pid, int uid) throws RemoteException {
    return 0;
  }

  @Override
  public int checkUriPermission(Uri uri, int pid, int uid, int mode, int userId) throws RemoteException {
    return 0;
  }

  @Override
  public void grantUriPermission(IApplicationThread caller, String targetPkg, Uri uri, int mode, int userId) throws RemoteException {

  }

  @Override
  public void revokeUriPermission(IApplicationThread caller, Uri uri, int mode, int userId) throws RemoteException {

  }

  @Override
  public void takePersistableUriPermission(Uri uri, int modeFlags, int userId) throws RemoteException {

  }

  @Override
  public void releasePersistableUriPermission(Uri uri, int modeFlags, int userId) throws RemoteException {

  }

  @Override
  public ParceledListSlice<UriPermission> getPersistedUriPermissions(String packageName, boolean incoming) throws RemoteException {
    return null;
  }

  @Override
  public void showWaitingForDebugger(IApplicationThread who, boolean waiting) throws RemoteException {

  }

  @Override
  public void getMemoryInfo(ActivityManager.MemoryInfo outInfo) throws RemoteException {

  }

  @Override
  public void killBackgroundProcesses(String packageName, int userId) throws RemoteException {

  }

  @Override
  public void killAllBackgroundProcesses() throws RemoteException {

  }

  @Override
  public void forceStopPackage(String packageName, int userId) throws RemoteException {

  }

  @Override
  public void setLockScreenShown(boolean shown) throws RemoteException {

  }

  @Override
  public void unhandledBack() throws RemoteException {

  }

  @Override
  public ParcelFileDescriptor openContentUri(Uri uri) throws RemoteException {
    return null;
  }

  @Override
  public void setDebugApp(String packageName, boolean waitForDebugger, boolean persistent) throws RemoteException {

  }

  @Override
  public void setAlwaysFinish(boolean enabled) throws RemoteException {

  }

  @Override
  public void setActivityController(IActivityController watcher) throws RemoteException {

  }

  @Override
  public void enterSafeMode() throws RemoteException {

  }

  @Override
  public void noteWakeupAlarm(IIntentSender sender, int sourceUid, String sourcePkg) throws RemoteException {

  }

  @Override
  public boolean killPids(int[] pids, String reason, boolean secure) throws RemoteException {
    return false;
  }

  @Override
  public boolean killProcessesBelowForeground(String reason) throws RemoteException {
    return false;
  }

  @Override
  public void handleApplicationCrash(IBinder app, ApplicationErrorReport.CrashInfo crashInfo) throws RemoteException {

  }

  @Override
  public boolean handleApplicationWtf(IBinder app, String tag, boolean system, ApplicationErrorReport.CrashInfo crashInfo) throws RemoteException {
    return false;
  }

  @Override
  public void handleApplicationStrictModeViolation(IBinder app, int violationMask, StrictMode.ViolationInfo crashInfo) throws RemoteException {

  }

  @Override
  public void signalPersistentProcesses(int signal) throws RemoteException {

  }

  @Override
  public List<ActivityManager.RunningAppProcessInfo> getRunningAppProcesses() throws RemoteException {
    return null;
  }

  @Override
  public List<ApplicationInfo> getRunningExternalApplications() throws RemoteException {
    return null;
  }

  @Override
  public void getMyMemoryState(ActivityManager.RunningAppProcessInfo outInfo) throws RemoteException {

  }

  @Override
  public ConfigurationInfo getDeviceConfigurationInfo() throws RemoteException {
    return null;
  }

  @Override
  public boolean profileControl(String process, int userId, boolean start, ProfilerInfo profilerInfo, int profileType) throws RemoteException {
    return false;
  }

  @Override
  public boolean shutdown(int timeout) throws RemoteException {
    return false;
  }

  @Override
  public void stopAppSwitches() throws RemoteException {

  }

  @Override
  public void resumeAppSwitches() throws RemoteException {

  }

  @Override
  public void addPackageDependency(String packageName) throws RemoteException {

  }

  @Override
  public void killApplicationWithAppId(String pkg, int appid, String reason) throws RemoteException {

  }

  @Override
  public void closeSystemDialogs(String reason) throws RemoteException {

  }

  @Override
  public Debug.MemoryInfo[] getProcessMemoryInfo(int[] pids) throws RemoteException {
    return new Debug.MemoryInfo[0];
  }

  @Override
  public void overridePendingTransition(IBinder token, String packageName, int enterAnim, int exitAnim) throws RemoteException {

  }

  @Override
  public boolean isUserAMonkey() throws RemoteException {
    return false;
  }

  @Override
  public void setUserIsMonkey(boolean monkey) throws RemoteException {

  }

  @Override
  public void finishHeavyWeightApp() throws RemoteException {

  }

  @Override
  public boolean convertFromTranslucent(IBinder token) throws RemoteException {
    return false;
  }

  @Override
  public boolean convertToTranslucent(IBinder token, ActivityOptions options) throws RemoteException {
    return false;
  }

  @Override
  public void notifyActivityDrawn(IBinder token) throws RemoteException {

  }

  @Override
  public ActivityOptions getActivityOptions(IBinder token) throws RemoteException {
    return null;
  }

  @Override
  public void bootAnimationComplete() throws RemoteException {

  }

  @Override
  public void setImmersive(IBinder token, boolean immersive) throws RemoteException {

  }

  @Override
  public boolean isImmersive(IBinder token) throws RemoteException {
    return false;
  }

  @Override
  public boolean isTopActivityImmersive() throws RemoteException {
    return false;
  }

  @Override
  public boolean isTopOfTask(IBinder token) throws RemoteException {
    return false;
  }

  @Override
  public void crashApplication(int uid, int initialPid, String packageName, String message) throws RemoteException {

  }

  @Override
  public String getProviderMimeType(Uri uri, int userId) throws RemoteException {
    return null;
  }

  @Override
  public IBinder newUriPermissionOwner(String name) throws RemoteException {
    return null;
  }

  @Override
  public void grantUriPermissionFromOwner(IBinder owner, int fromUid, String targetPkg, Uri uri, int mode, int sourceUserId, int targetUserId) throws RemoteException {

  }

  @Override
  public void revokeUriPermissionFromOwner(IBinder owner, Uri uri, int mode, int userId) throws RemoteException {

  }

  @Override
  public int checkGrantUriPermission(int callingUid, String targetPkg, Uri uri, int modeFlags, int userId) throws RemoteException {
    return 0;
  }

  @Override
  public boolean dumpHeap(String process, int userId, boolean managed, String path, ParcelFileDescriptor fd) throws RemoteException {
    return false;
  }

  @Override
  public int startActivities(IApplicationThread caller, String callingPackage, Intent[] intents, String[] resolvedTypes, IBinder resultTo, Bundle options, int userId) throws RemoteException {
    return 0;
  }

  @Override
  public int getFrontActivityScreenCompatMode() throws RemoteException {
    return 0;
  }

  @Override
  public void setFrontActivityScreenCompatMode(int mode) throws RemoteException {

  }

  @Override
  public int getPackageScreenCompatMode(String packageName) throws RemoteException {
    return 0;
  }

  @Override
  public void setPackageScreenCompatMode(String packageName, int mode) throws RemoteException {

  }

  @Override
  public boolean getPackageAskScreenCompat(String packageName) throws RemoteException {
    return false;
  }

  @Override
  public void setPackageAskScreenCompat(String packageName, boolean ask) throws RemoteException {

  }

  @Override
  public boolean switchUser(int userid) throws RemoteException {
    return false;
  }

  @Override
  public boolean startUserInBackground(int userid) throws RemoteException {
    return false;
  }

  @Override
  public int stopUser(int userid, IStopUserCallback callback) throws RemoteException {
    return 0;
  }

  @Override
  public UserInfo getCurrentUser() throws RemoteException {
    return null;
  }

  @Override
  public boolean isUserRunning(int userid, boolean orStopping) throws RemoteException {
    return false;
  }

  @Override
  public int[] getRunningUserIds() throws RemoteException {
    return new int[0];
  }

  @Override
  public boolean removeTask(int taskId, int flags) throws RemoteException {
    return false;
  }

  @Override
  public void registerProcessObserver(IProcessObserver observer) throws RemoteException {

  }

  @Override
  public void unregisterProcessObserver(IProcessObserver observer) throws RemoteException {

  }

  @Override
  public boolean isIntentSenderTargetedToPackage(IIntentSender sender) throws RemoteException {
    return false;
  }

  @Override
  public boolean isIntentSenderAnActivity(IIntentSender sender) throws RemoteException {
    return false;
  }

  @Override
  public Intent getIntentForIntentSender(IIntentSender sender) throws RemoteException {
    return null;
  }

  @Override
  public String getTagForIntentSender(IIntentSender sender, String prefix) throws RemoteException {
    return null;
  }

  @Override
  public void updatePersistentConfiguration(Configuration values) throws RemoteException {

  }

  @Override
  public long[] getProcessPss(int[] pids) throws RemoteException {
    return new long[0];
  }

  @Override
  public void showBootMessage(CharSequence msg, boolean always) throws RemoteException {

  }

  @Override
  public void keyguardWaitingForActivityDrawn() throws RemoteException {

  }

  @Override
  public boolean shouldUpRecreateTask(IBinder token, String destAffinity) throws RemoteException {
    return false;
  }

  @Override
  public boolean navigateUpTo(IBinder token, Intent target, int resultCode, Intent resultData) throws RemoteException {
    return false;
  }

  @Override
  public int getLaunchedFromUid(IBinder activityToken) throws RemoteException {
    return 0;
  }

  @Override
  public String getLaunchedFromPackage(IBinder activityToken) throws RemoteException {
    return null;
  }

  @Override
  public void registerUserSwitchObserver(IUserSwitchObserver observer) throws RemoteException {

  }

  @Override
  public void unregisterUserSwitchObserver(IUserSwitchObserver observer) throws RemoteException {

  }

  @Override
  public void requestBugReport() throws RemoteException {

  }

  @Override
  public long inputDispatchingTimedOut(int pid, boolean aboveSystem, String reason) throws RemoteException {
    return 0;
  }

  @Override
  public Bundle getAssistContextExtras(int requestType) throws RemoteException {
    return null;
  }

  @Override
  public void reportAssistContextExtras(IBinder token, Bundle extras) throws RemoteException {

  }

  @Override
  public boolean launchAssistIntent(Intent intent, int requestType, String hint, int userHandle) throws RemoteException {
    return false;
  }

  @Override
  public void killUid(int uid, String reason) throws RemoteException {

  }

  @Override
  public void hang(IBinder who, boolean allowRestart) throws RemoteException {

  }

  @Override
  public void reportActivityFullyDrawn(IBinder token) throws RemoteException {

  }

  @Override
  public void restart() throws RemoteException {

  }

  @Override
  public void performIdleMaintenance() throws RemoteException {

  }

  @Override
  public IActivityContainer createActivityContainer(IBinder parentActivityToken, IActivityContainerCallback callback) throws RemoteException {
    return null;
  }

  @Override
  public void deleteActivityContainer(IActivityContainer container) throws RemoteException {

  }

  @Override
  public IActivityContainer getEnclosingActivityContainer(IBinder activityToken) throws RemoteException {
    return null;
  }

  @Override
  public IBinder getHomeActivityToken() throws RemoteException {
    return null;
  }

  @Override
  public void startLockTaskModeOnCurrent() throws RemoteException {

  }

  @Override
  public void startLockTaskMode(int taskId) throws RemoteException {

  }

  @Override
  public void startLockTaskMode(IBinder token) throws RemoteException {

  }

  @Override
  public void stopLockTaskMode() throws RemoteException {

  }

  @Override
  public void stopLockTaskModeOnCurrent() throws RemoteException {

  }

  @Override
  public boolean isInLockTaskMode() throws RemoteException {
    return false;
  }

  @Override
  public void setTaskDescription(IBinder token, ActivityManager.TaskDescription values) throws RemoteException {

  }

  @Override
  public Bitmap getTaskDescriptionIcon(String filename) throws RemoteException {
    return null;
  }

  @Override
  public boolean requestVisibleBehind(IBinder token, boolean visible) throws RemoteException {
    return false;
  }

  @Override
  public boolean isBackgroundVisibleBehind(IBinder token) throws RemoteException {
    return false;
  }

  @Override
  public void backgroundResourcesReleased(IBinder token) throws RemoteException {

  }

  @Override
  public void notifyLaunchTaskBehindComplete(IBinder token) throws RemoteException {

  }

  @Override
  public void notifyEnterAnimationComplete(IBinder token) throws RemoteException {

  }

  @Override
  public boolean testIsSystemReady() {
    return false;
  }

  @Override
  public IBinder asBinder() {
    return null;
  }
}
