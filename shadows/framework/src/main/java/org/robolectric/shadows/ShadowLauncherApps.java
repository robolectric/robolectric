package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.L;
import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.N_MR1;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.Q;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.content.ComponentName;
import android.content.IntentSender;
import android.content.pm.ApplicationInfo;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.LauncherApps;
import android.content.pm.LauncherApps.ShortcutQuery;
import android.content.pm.PackageInstaller.SessionCallback;
import android.content.pm.PackageInstaller.SessionInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ShortcutInfo;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.os.UserHandle;
import android.util.Pair;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;

/** Shadow of {@link android.content.pm.LauncherApps}. */
@Implements(LauncherApps.class)
public class ShadowLauncherApps {
  private static List<ShortcutInfo> shortcuts = new ArrayList<>();
  private static final Multimap<UserHandle, String> enabledPackages = HashMultimap.create();
  private static final Multimap<UserHandle, ComponentName> enabledActivities =
      HashMultimap.create();
  private static final Multimap<UserHandle, LauncherActivityInfo> shortcutActivityList =
      Multimaps.synchronizedMultimap(HashMultimap.create());
  private static final Multimap<UserHandle, LauncherActivityInfo> activityList =
      Multimaps.synchronizedMultimap(HashMultimap.create());
  private static final Map<UserHandle, Map<String, ApplicationInfo>> applicationInfoList =
      new HashMap<>();
  private static final Map<UserHandle, Map<String, Bundle>> suspendedPackageLauncherExtras =
      new HashMap<>();

  private static final List<Pair<LauncherApps.Callback, Handler>> callbacks = new ArrayList<>();
  private static boolean hasShortcutHostPermission = false;

  @Resetter
  public static void reset() {
    shortcuts.clear();
    enabledPackages.clear();
    enabledActivities.clear();
    shortcutActivityList.clear();
    activityList.clear();
    applicationInfoList.clear();
    suspendedPackageLauncherExtras.clear();
    callbacks.clear();
    hasShortcutHostPermission = false;
  }

  /**
   * Adds a dynamic shortcut to be returned by {@link #getShortcuts(ShortcutQuery, UserHandle)}.
   *
   * @param shortcutInfo the shortcut to add.
   */
  public void addDynamicShortcut(ShortcutInfo shortcutInfo) {
    shortcuts.add(shortcutInfo);
    shortcutsChanged(shortcutInfo.getPackage(), Lists.newArrayList(shortcutInfo));
  }

  private void shortcutsChanged(String packageName, List<ShortcutInfo> shortcuts) {
    for (Pair<LauncherApps.Callback, Handler> callbackPair : callbacks) {
      callbackPair.second.post(
          () ->
              callbackPair.first.onShortcutsChanged(
                  packageName, shortcuts, Process.myUserHandle()));
    }
  }

  /**
   * Fires {@link LauncherApps.Callback#onPackageAdded(String, UserHandle)} on all of the registered
   * callbacks, with the provided packageName.
   *
   * @param packageName the package the was added.
   */
  public void notifyPackageAdded(String packageName) {
    for (Pair<LauncherApps.Callback, Handler> callbackPair : callbacks) {
      callbackPair.second.post(
          () -> callbackPair.first.onPackageAdded(packageName, Process.myUserHandle()));
    }
  }

  /**
   * Adds an enabled package to be checked by {@link #isPackageEnabled(String, UserHandle)}.
   *
   * @param userHandle the user handle to be added.
   * @param packageName the package name to be added.
   */
  public void addEnabledPackage(UserHandle userHandle, String packageName) {
    enabledPackages.put(userHandle, packageName);
  }

  /**
   * Sets an activity referenced by ComponentName as enabled, to be checked by {@link
   * #isActivityEnabled(ComponentName, UserHandle)}.
   *
   * @param userHandle the user handle to be set.
   * @param componentName the component name of the activity to be enabled.
   */
  public void setActivityEnabled(UserHandle userHandle, ComponentName componentName) {
    enabledActivities.put(userHandle, componentName);
  }

  /**
   * Adds a {@link LauncherActivityInfo} to be retrieved by {@link
   * #getShortcutConfigActivityList(String, UserHandle)}.
   *
   * @param userHandle the user handle to be added.
   * @param activityInfo the {@link LauncherActivityInfo} to be added.
   */
  public void addShortcutConfigActivity(UserHandle userHandle, LauncherActivityInfo activityInfo) {
    shortcutActivityList.put(userHandle, activityInfo);
  }

  /**
   * Adds a {@link LauncherActivityInfo} to be retrieved by {@link #getActivityList(String,
   * UserHandle)}.
   *
   * @param userHandle the user handle to be added.
   * @param activityInfo the {@link LauncherActivityInfo} to be added.
   */
  public void addActivity(UserHandle userHandle, LauncherActivityInfo activityInfo) {
    activityList.put(userHandle, activityInfo);
  }

  /**
   * Fires {@link LauncherApps.Callback#onPackageRemoved(String, UserHandle)} on all of the
   * registered callbacks, with the provided packageName.
   *
   * @param packageName the package the was removed.
   */
  public void notifyPackageRemoved(String packageName) {
    for (Pair<LauncherApps.Callback, Handler> callbackPair : callbacks) {
      callbackPair.second.post(
          () -> callbackPair.first.onPackageRemoved(packageName, Process.myUserHandle()));
    }
  }

  /**
   * Adds a {@link ApplicationInfo} to be retrieved by {@link #getApplicationInfo(String, int,
   * UserHandle)}.
   *
   * @param userHandle the user handle to be added.
   * @param packageName the package name to be added.
   * @param applicationInfo the application info to be added.
   */
  public void addApplicationInfo(
      UserHandle userHandle, String packageName, ApplicationInfo applicationInfo) {
    if (!applicationInfoList.containsKey(userHandle)) {
      applicationInfoList.put(userHandle, new HashMap<>());
    }
    applicationInfoList.get(userHandle).put(packageName, applicationInfo);
  }

  @Implementation(minSdk = Q)
  protected void startPackageInstallerSessionDetailsActivity(
      @Nonnull SessionInfo sessionInfo, @Nullable Rect sourceBounds, @Nullable Bundle opts) {
    throw new UnsupportedOperationException(
        "This method is not currently supported in Robolectric.");
  }

  @Implementation
  protected void startAppDetailsActivity(
      ComponentName component, UserHandle user, Rect sourceBounds, Bundle opts) {
    throw new UnsupportedOperationException(
        "This method is not currently supported in Robolectric.");
  }

  @Implementation(minSdk = O)
  protected List<LauncherActivityInfo> getShortcutConfigActivityList(
      @Nullable String packageName, @Nonnull UserHandle user) {
    // Take a snapshot to avoid concurrent modification exceptions.
    ImmutableSet<LauncherActivityInfo> activityInfosSnapshot =
        ImmutableSet.copyOf(shortcutActivityList.get(user));
    return activityInfosSnapshot.stream()
        .filter(matchesPackage(packageName))
        .collect(Collectors.toList());
  }

  @Implementation(minSdk = O)
  @Nullable
  protected IntentSender getShortcutConfigActivityIntent(@Nonnull LauncherActivityInfo info) {
    throw new UnsupportedOperationException(
        "This method is not currently supported in Robolectric.");
  }

  @Implementation
  protected boolean isPackageEnabled(String packageName, UserHandle user) {
    return enabledPackages.get(user).contains(packageName);
  }

  @Implementation(minSdk = L)
  protected List<LauncherActivityInfo> getActivityList(String packageName, UserHandle user) {
    // Take a snapshot to avoid concurrent modification exceptions.
    ImmutableSet<LauncherActivityInfo> activityInfosSnapshot =
        ImmutableSet.copyOf(activityList.get(user));
    return activityInfosSnapshot.stream()
        .filter(matchesPackage(packageName))
        .collect(Collectors.toList());
  }

  @Implementation(minSdk = O)
  protected ApplicationInfo getApplicationInfo(
      @Nonnull String packageName, int flags, @Nonnull UserHandle user)
      throws NameNotFoundException {
    if (applicationInfoList.containsKey(user)) {
      Map<String, ApplicationInfo> map = applicationInfoList.get(user);
      if (map.containsKey(packageName)) {
        return map.get(packageName);
      }
    }
    throw new NameNotFoundException(
        "Package " + packageName + " not found for user " + user.getIdentifier());
  }

  /**
   * Adds a {@link Bundle} to be retrieved by {@link #getSuspendedPackageLauncherExtras(String,
   * UserHandle)}.
   *
   * @param userHandle the user handle to be added.
   * @param packageName the package name to be added.
   * @param bundle the bundle for the extras.
   */
  public void addSuspendedPackageLauncherExtras(
      UserHandle userHandle, String packageName, Bundle bundle) {
    if (!suspendedPackageLauncherExtras.containsKey(userHandle)) {
      suspendedPackageLauncherExtras.put(userHandle, new HashMap<>());
    }
    suspendedPackageLauncherExtras.get(userHandle).put(packageName, bundle);
  }

  @Implementation(minSdk = P)
  @Nullable
  protected Bundle getSuspendedPackageLauncherExtras(String packageName, UserHandle user)
      throws NameNotFoundException {
    Map<String, Bundle> map = suspendedPackageLauncherExtras.get(user);
    if (map != null && map.containsKey(packageName)) {
      return map.get(packageName);
    }

    throw new NameNotFoundException(
        "Suspended package extras for  "
            + packageName
            + " not found for user "
            + user.getIdentifier());
  }

  @Implementation(minSdk = Q)
  protected boolean shouldHideFromSuggestions(
      @Nonnull String packageName, @Nonnull UserHandle user) {
    throw new UnsupportedOperationException(
        "This method is not currently supported in Robolectric.");
  }

  @Implementation(minSdk = L)
  protected boolean isActivityEnabled(ComponentName component, UserHandle user) {
    return enabledActivities.containsEntry(user, component);
  }

  /**
   * Sets the return value of {@link #hasShortcutHostPermission()}. If this isn't explicitly set,
   * {@link #hasShortcutHostPermission()} defaults to returning false.
   *
   * @param permission boolean to be returned
   */
  public void setHasShortcutHostPermission(boolean permission) {
    hasShortcutHostPermission = permission;
  }

  @Implementation(minSdk = N)
  protected boolean hasShortcutHostPermission() {
    return hasShortcutHostPermission;
  }

  /**
   * This method is an incomplete implementation of this API that only supports querying for pinned
   * dynamic shortcuts. It also doesn't not support {@link ShortcutQuery#setChangedSince(long)}.
   */
  @Implementation(minSdk = N_MR1)
  @Nullable
  protected List<ShortcutInfo> getShortcuts(
      @Nonnull ShortcutQuery query, @Nonnull UserHandle user) {
    if (reflector(ReflectorShortcutQuery.class, query).getChangedSince() != 0) {
      throw new UnsupportedOperationException(
          "Robolectric does not currently support ShortcutQueries that filter on time since"
              + " change.");
    }
    int flags = reflector(ReflectorShortcutQuery.class, query).getQueryFlags();
    if ((flags & ShortcutQuery.FLAG_MATCH_PINNED) == 0
        || (flags & ShortcutQuery.FLAG_MATCH_DYNAMIC) == 0) {
      throw new UnsupportedOperationException(
          "Robolectric does not currently support ShortcutQueries that match non-dynamic"
              + " Shortcuts.");
    }
    Iterable<ShortcutInfo> shortcutsItr = shortcuts;

    List<String> ids = reflector(ReflectorShortcutQuery.class, query).getShortcutIds();
    if (ids != null) {
      shortcutsItr = Iterables.filter(shortcutsItr, shortcut -> ids.contains(shortcut.getId()));
    }
    ComponentName activity = reflector(ReflectorShortcutQuery.class, query).getActivity();
    if (activity != null) {
      shortcutsItr =
          Iterables.filter(shortcutsItr, shortcut -> shortcut.getActivity().equals(activity));
    }
    String packageName = reflector(ReflectorShortcutQuery.class, query).getPackage();
    if (packageName != null && !packageName.isEmpty()) {
      shortcutsItr =
          Iterables.filter(shortcutsItr, shortcut -> shortcut.getPackage().equals(packageName));
    }
    return Lists.newArrayList(shortcutsItr);
  }

  @Implementation(minSdk = N_MR1)
  protected void pinShortcuts(
      @Nonnull String packageName, @Nonnull List<String> shortcutIds, @Nonnull UserHandle user) {
    Iterable<ShortcutInfo> changed =
        Iterables.filter(shortcuts, shortcut -> !shortcutIds.contains(shortcut.getId()));
    List<ShortcutInfo> ret = Lists.newArrayList(changed);
    shortcuts =
        Lists.newArrayList(
            Iterables.filter(shortcuts, shortcut -> shortcutIds.contains(shortcut.getId())));

    shortcutsChanged(packageName, ret);
  }

  @Implementation(minSdk = N_MR1)
  protected void startShortcut(
      @Nonnull String packageName,
      @Nonnull String shortcutId,
      @Nullable Rect sourceBounds,
      @Nullable Bundle startActivityOptions,
      @Nonnull UserHandle user) {
    throw new UnsupportedOperationException(
        "This method is not currently supported in Robolectric.");
  }

  @Implementation(minSdk = N_MR1)
  protected void startShortcut(
      @Nonnull ShortcutInfo shortcut,
      @Nullable Rect sourceBounds,
      @Nullable Bundle startActivityOptions) {
    throw new UnsupportedOperationException(
        "This method is not currently supported in Robolectric.");
  }

  @Implementation
  protected void registerCallback(LauncherApps.Callback callback) {
    registerCallback(callback, null);
  }

  @Implementation
  protected void registerCallback(LauncherApps.Callback callback, Handler handler) {
    callbacks.add(
        Pair.create(callback, handler != null ? handler : new Handler(Looper.myLooper())));
  }

  @Implementation
  protected void unregisterCallback(LauncherApps.Callback callback) {
    int index = Iterables.indexOf(callbacks, pair -> pair.first == callback);
    if (index != -1) {
      callbacks.remove(index);
    }
  }

  @Implementation(minSdk = Q)
  protected void registerPackageInstallerSessionCallback(
      @Nonnull Executor executor, @Nonnull SessionCallback callback) {
    throw new UnsupportedOperationException(
        "This method is not currently supported in Robolectric.");
  }

  @Implementation(minSdk = Q)
  protected void unregisterPackageInstallerSessionCallback(@Nonnull SessionCallback callback) {
    throw new UnsupportedOperationException(
        "This method is not currently supported in Robolectric.");
  }

  @Implementation(minSdk = Q)
  @Nonnull
  protected List<SessionInfo> getAllPackageInstallerSessions() {
    throw new UnsupportedOperationException(
        "This method is not currently supported in Robolectric.");
  }

  private Predicate<LauncherActivityInfo> matchesPackage(@Nullable String packageName) {
    return info ->
        packageName == null
            || (info.getComponentName() != null
                && packageName.equals(info.getComponentName().getPackageName()));
  }

  @ForType(ShortcutQuery.class)
  private interface ReflectorShortcutQuery {
    @Accessor("mChangedSince")
    long getChangedSince();

    @Accessor("mQueryFlags")
    int getQueryFlags();

    @Accessor("mShortcutIds")
    List<String> getShortcutIds();

    @Accessor("mActivity")
    ComponentName getActivity();

    @Accessor("mPackage")
    String getPackage();
  }
}
