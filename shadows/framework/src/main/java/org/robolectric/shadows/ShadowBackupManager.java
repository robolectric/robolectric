package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.M;

import android.app.backup.BackupManager;
import android.app.backup.IBackupManagerMonitor;
import android.app.backup.IRestoreObserver;
import android.app.backup.IRestoreSession;
import android.app.backup.RestoreSession;
import android.app.backup.RestoreSet;
import android.content.Context;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

/**
 * A stub implementation of {@link BackupManager} that instead of connecting to a real backup
 * transport and performing restores, stores which packages are restored from which backup set, and
 * can be verified using methods on the shadow like {@link #getPackageRestoreToken(String)}.
 */
@Implements(BackupManager.class)
public class ShadowBackupManager {

  private static BackupManagerServiceState serviceState = new BackupManagerServiceState();

  @RealObject private BackupManager realBackupManager;
  private Context context;

  @Resetter
  public static void reset() {
    serviceState = new BackupManagerServiceState();
  }

  @Implementation
  protected void __constructor__(Context context) {
    this.context = context;
    Shadow.invokeConstructor(
        BackupManager.class, realBackupManager, ClassParameter.from(Context.class, context));
  }

  @Implementation
  protected void dataChanged() {
    serviceState.dataChangedCount.merge(context.getPackageName(), 1, Integer::sum);
  }

  /** Returns whether {@link #dataChanged()} was called. */
  public boolean isDataChanged() {
    return serviceState.dataChangedCount.containsKey(context.getPackageName());
  }

  /** Returns number of times {@link #dataChanged()} was called. */
  public int getDataChangedCount() {
    return serviceState.dataChangedCount.getOrDefault(context.getPackageName(), 0);
  }

  @Implementation(minSdk = LOLLIPOP)
  @HiddenApi // SystemApi
  protected void setBackupEnabled(boolean isEnabled) {
    enforceBackupPermission("setBackupEnabled");
    serviceState.backupEnabled = isEnabled;
  }

  @Implementation(minSdk = LOLLIPOP)
  @HiddenApi // SystemApi
  protected boolean isBackupEnabled() {
    enforceBackupPermission("isBackupEnabled");
    return serviceState.backupEnabled;
  }

  @Implementation
  @HiddenApi // SystemApi
  protected RestoreSession beginRestoreSession() {
    enforceBackupPermission("beginRestoreSession");
    return ReflectionHelpers.callConstructor(
        RestoreSession.class,
        ClassParameter.from(Context.class, context),
        ClassParameter.from(IRestoreSession.class, new FakeRestoreSession()));
  }

  @Implementation(minSdk = M)
  @HiddenApi // SystemApi
  protected long getAvailableRestoreToken(String packageName) {
    enforceBackupPermission("getAvailableRestoreToken");
    return getPackageRestoreToken(packageName);
  }

  /**
   * Returns the restore token for the given package, or {@code 0} if the package was not restored.
   */
  public long getPackageRestoreToken(String packageName) {
    Long token = serviceState.restoredPackages.get(packageName);
    return token != null ? token : 0L;
  }

  /** Adds a restore set available to be restored. */
  public void addAvailableRestoreSets(long restoreToken, List<String> packages) {
    serviceState.restoreData.put(restoreToken, packages);
  }

  private void enforceBackupPermission(String message) {
    RuntimeEnvironment.getApplication()
        .enforceCallingOrSelfPermission(android.Manifest.permission.BACKUP, message);
  }

  private static class FakeRestoreSession implements IRestoreSession {

    // Override method for SDK < 26
    public int getAvailableRestoreSets(IRestoreObserver observer) throws RemoteException {
      return getAvailableRestoreSets(observer, null);
    }

    @Override
    public int getAvailableRestoreSets(IRestoreObserver observer, IBackupManagerMonitor monitor)
        throws RemoteException {
      post(
          () -> {
            Set<Long> restoreTokens = serviceState.restoreData.keySet();
            Set<RestoreSet> restoreSets = new HashSet<>();
            for (long token : restoreTokens) {
              restoreSets.add(new RestoreSet("RestoreSet-" + token, "device", token));
            }
            observer.restoreSetsAvailable(restoreSets.toArray(new RestoreSet[restoreSets.size()]));
          });
      return BackupManager.SUCCESS;
    }

    // Override method for SDK < 26
    public int restoreAll(long token, IRestoreObserver observer) throws RemoteException {
      return restoreAll(token, observer, null);
    }

    @Override
    public int restoreAll(long token, IRestoreObserver observer, IBackupManagerMonitor monitor)
        throws RemoteException {
      return restorePackages(token, observer, null, monitor);
    }

    // Override method for SDK <= 25
    public int restoreSome(long token, IRestoreObserver observer, String[] packages)
        throws RemoteException {
      return restorePackages(token, observer, packages, null);
    }

    // Override method for SDK <= 28
    public int restoreSome(
        long token, IRestoreObserver observer, IBackupManagerMonitor monitor, String[] packages)
        throws RemoteException {
      return restorePackages(token, observer, packages, monitor);
    }

    public int restorePackages(
        long token, IRestoreObserver observer, String[] packages, IBackupManagerMonitor monitor)
        throws RemoteException {
      List<String> restorePackages = new ArrayList<>(serviceState.restoreData.get(token));
      if (packages != null) {
        restorePackages.retainAll(Arrays.asList(packages));
      }
      post(() -> observer.restoreStarting(restorePackages.size()));
      for (int i = 0; i < restorePackages.size(); i++) {
        final int index = i; // final copy of i
        post(() -> observer.onUpdate(index, restorePackages.get(index)));
        serviceState.restoredPackages.put(restorePackages.get(index), token);
      }
      post(() -> observer.restoreFinished(BackupManager.SUCCESS));
      serviceState.lastRestoreToken = token;
      return BackupManager.SUCCESS;
    }

    // Override method for SDK < 26
    public int restorePackage(String packageName, IRestoreObserver observer)
        throws RemoteException {
      return restorePackage(packageName, observer, null);
    }

    @Override
    public int restorePackage(
        String packageName, IRestoreObserver observer, IBackupManagerMonitor monitor)
        throws RemoteException {
      if (serviceState.lastRestoreToken == 0L) {
        return -1;
      }
      List<String> restorePackages = serviceState.restoreData.get(serviceState.lastRestoreToken);
      if (!restorePackages.contains(packageName)) {
        return BackupManager.ERROR_PACKAGE_NOT_FOUND;
      }
      post(() -> observer.restoreStarting(1));
      post(() -> observer.onUpdate(0, packageName));
      serviceState.restoredPackages.put(packageName, serviceState.lastRestoreToken);
      post(() -> observer.restoreFinished(BackupManager.SUCCESS));
      return BackupManager.SUCCESS;
    }

    @Override
    public void endRestoreSession() throws RemoteException {
      // do nothing
    }

    @Override
    public IBinder asBinder() {
      return null;
    }

    private void post(RemoteRunnable runnable) {
      new Handler(Looper.getMainLooper())
          .post(
              () -> {
                try {
                  runnable.run();
                } catch (RemoteException e) {
                  throw new RuntimeException(e);
                }
              });
    }
  }

  private static class BackupManagerServiceState {
    boolean backupEnabled = true;
    long lastRestoreToken = 0L;
    final Map<String, Integer> dataChangedCount = new HashMap<>();
    final Map<Long, List<String>> restoreData = new HashMap<>();
    final Map<String, Long> restoredPackages = new HashMap<>();
  }

  private interface RemoteRunnable {
    void run() throws RemoteException;
  }
}
