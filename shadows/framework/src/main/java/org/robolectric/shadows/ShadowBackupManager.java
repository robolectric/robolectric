package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.M;

import android.app.backup.BackupManager;
import android.app.backup.BackupTransport;
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
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
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
 * transport and performing restores, stores which packages are restored from which backup set, what
 * the final result should be and can be verified using methods on the shadow like {@link
 * #getPackageRestoreToken(String)} and {@link #getPackageRestoreCount(String)}.
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

  @Implementation
  @HiddenApi // SystemApi
  protected void setBackupEnabled(boolean isEnabled) {
    enforceBackupPermission("setBackupEnabled");
    serviceState.backupEnabled = isEnabled;
  }

  @Implementation
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
   * Returns the last recorded restore token for the given package, or {@code 0} if the package was
   * not restored.
   */
  public long getPackageRestoreToken(String packageName) {
    List<Long> result = serviceState.restoredPackages.get(packageName);
    return result.isEmpty() ? 0L : result.get(result.size() - 1);
  }

  /** Returns the number of recorded restores for the given package. */
  public int getPackageRestoreCount(String packageName) {
    return serviceState.restoredPackages.get(packageName).size();
  }

  /** Adds a restore set available to be restored successfully. */
  public void addAvailableRestoreSets(long restoreToken, List<String> packages) {
    addAvailableRestoreSets(restoreToken, packages, BackupTransport.TRANSPORT_OK);
  }

  /** Adds a restore set available to be restored and the final result of the restore session. */
  public void addAvailableRestoreSets(long restoreToken, List<String> packages, int result) {
    serviceState.restoreData.put(restoreToken, new RestoreData(packages, result));
  }

  /**
   * Causes the {@link IRestoreObserver#restoreSetsAvailable} callback to receive {@code null},
   * regardless of whether any restore sets were added to this shadow. Can be used to simulate a
   * failure by the transport to fetch available restore sets.
   */
  public void setNullAvailableRestoreSets(boolean value) {
    serviceState.nullRestoreData = value;
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
            if (serviceState.nullRestoreData) {
              observer.restoreSetsAvailable(null);
              return;
            }

            Set<Long> restoreTokens = serviceState.restoreData.keySet();
            Set<RestoreSet> restoreSets = new HashSet<>();
            for (long token : restoreTokens) {
              restoreSets.add(new RestoreSet("RestoreSet-" + token, "device", token));
            }
            observer.restoreSetsAvailable(restoreSets.toArray(new RestoreSet[0]));
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

    @Override
    public int restorePackages(
        long token, IRestoreObserver observer, String[] packages, IBackupManagerMonitor monitor)
        throws RemoteException {
      RestoreData restoreData = serviceState.takeRestoreData(token);
      List<String> restorePackages = new ArrayList<>(restoreData.packages);
      if (packages != null) {
        restorePackages.retainAll(Arrays.asList(packages));
      }
      post(() -> observer.restoreStarting(restorePackages.size()));
      for (int i = 0; i < restorePackages.size(); i++) {
        final int index = i; // final copy of i
        post(() -> observer.onUpdate(index, restorePackages.get(index)));
        serviceState.restoredPackages.put(restorePackages.get(index), token);
      }
      post(() -> observer.restoreFinished(restoreData.result));
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
      RestoreData restoreData = serviceState.takeRestoreData(serviceState.lastRestoreToken);
      List<String> restorePackages = new ArrayList<>(restoreData.packages);
      if (!restorePackages.contains(packageName)) {
        return BackupManager.ERROR_PACKAGE_NOT_FOUND;
      }
      post(() -> observer.restoreStarting(1));
      post(() -> observer.onUpdate(0, packageName));
      serviceState.restoredPackages.put(packageName, serviceState.lastRestoreToken);
      post(() -> observer.restoreFinished(restoreData.result));
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
    boolean nullRestoreData;
    long lastRestoreToken = 0L;
    final Map<String, Integer> dataChangedCount = new HashMap<>();
    final ListMultimap<Long, RestoreData> restoreData = ArrayListMultimap.create();
    final ListMultimap<String, Long> restoredPackages = ArrayListMultimap.create();

    /**
     * Returns the first {@link RestoreData} matching the given token and removes it from the
     * existing restore data if it's not the last one.
     */
    RestoreData takeRestoreData(long token) {
      List<RestoreData> results = restoreData.get(token);
      if (results.isEmpty()) {
        return new RestoreData(new ArrayList<>(), -1);
      }
      RestoreData data = results.get(0);
      if (results.size() > 1) {
        restoreData.remove(token, data);
      }
      return data;
    }
  }

  private static class RestoreData {
    final List<String> packages;
    final int result;

    public RestoreData(List<String> packages, int result) {
      this.packages = packages;
      this.result = result;
    }
  }

  private interface RemoteRunnable {
    void run() throws RemoteException;
  }
}
