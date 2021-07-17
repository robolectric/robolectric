package org.robolectric.shadows;

import static android.content.pm.PackageManager.MATCH_DIRECT_BOOT_AWARE;
import static android.content.pm.PackageManager.MATCH_DIRECT_BOOT_UNAWARE;
import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.R;
import static com.google.common.base.Preconditions.checkNotNull;

import android.Manifest.permission;
import android.annotation.RequiresPermission;
import android.annotation.SystemApi;
import android.app.Activity;
import android.app.AppOpsManager;
import android.app.AppOpsManager.Mode;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.CrossProfileApps;
import android.content.pm.ICrossProfileApps;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Process;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nullable;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** Robolectric implementation of {@link CrossProfileApps}. */
@Implements(value = CrossProfileApps.class, minSdk = P)
public class ShadowCrossProfileApps {

  private final Set<UserHandle> targetUserProfiles = new LinkedHashSet<>();
  private final List<StartedMainActivity> startedMainActivities = new ArrayList<>();
  private final List<StartedActivity> startedActivities =
      Collections.synchronizedList(new ArrayList<>());

  private Context context;
  private PackageManager packageManager;
  // Whether the current application has the interact across profile AppOps.
  private volatile int canInteractAcrossProfileAppOps = AppOpsManager.MODE_ERRORED;

  // Whether the current application has requested the interact across profile permission.
  private volatile boolean hasRequestedInteractAcrossProfiles = false;

  @Implementation
  protected void __constructor__(Context context, ICrossProfileApps service) {
    this.context = context;
    this.packageManager = context.getPackageManager();
  }

  /**
   * Returns a list of {@link UserHandle}s currently accessible. This list is populated from calls
   * to {@link #addTargetUserProfile(UserHandle)}.
   */
  @Implementation
  protected List<UserHandle> getTargetUserProfiles() {
    return ImmutableList.copyOf(targetUserProfiles);
  }

  /**
   * Returns a {@link Drawable} that can be shown for profile switching, which is guaranteed to
   * always be the same for a particular user and to be distinct between users.
   */
  @Implementation
  protected Drawable getProfileSwitchingIconDrawable(UserHandle userHandle) {
    verifyCanAccessUser(userHandle);
    return new ColorDrawable(userHandle.getIdentifier());
  }

  /**
   * Returns a {@link CharSequence} that can be shown as a label for profile switching, which is
   * guaranteed to always be the same for a particular user and to be distinct between users.
   */
  @Implementation
  protected CharSequence getProfileSwitchingLabel(UserHandle userHandle) {
    verifyCanAccessUser(userHandle);
    return "Switch to " + userHandle;
  }

  /**
   * Simulates starting the main activity specified in the specified profile, performing the same
   * security checks done by the real {@link CrossProfileApps}.
   *
   * <p>The most recent main activity started can be queried by {@link #peekNextStartedActivity()}.
   */
  @Implementation
  protected void startMainActivity(ComponentName componentName, UserHandle targetUser) {
    verifyCanAccessUser(targetUser);
    verifyActivityInManifest(componentName, /* requireMainActivity= */ true);
    startedMainActivities.add(new StartedMainActivity(componentName, targetUser));
    startedActivities.add(new StartedActivity(componentName, targetUser));
  }

  /**
   * Simulates starting the activity specified in the specified profile, performing the same
   * security checks done by the real {@link CrossProfileApps}.
   *
   * <p>The most recent main activity started can be queried by {@link #peekNextStartedActivity()}.
   */
  @Implementation(minSdk = Q)
  @SystemApi
  @RequiresPermission(permission.INTERACT_ACROSS_PROFILES)
  protected void startActivity(ComponentName componentName, UserHandle targetUser) {
    verifyCanAccessUser(targetUser);
    verifyActivityInManifest(componentName, /* requireMainActivity= */ false);
    verifyHasInteractAcrossProfilesPermission();
    startedActivities.add(new StartedActivity(componentName, targetUser));
  }

  /**
   * Simulates starting the activity specified in the specified profile, performing the same
   * security checks done by the real {@link CrossProfileApps}.
   *
   * <p>The most recent main activity started can be queried by {@link #peekNextStartedActivity()}.
   */
  @Implementation(minSdk = R)
  @SystemApi
  @RequiresPermission(permission.INTERACT_ACROSS_PROFILES)
  protected void startActivity(Intent intent, UserHandle targetUser, @Nullable Activity activity) {
    startActivity(intent, targetUser, activity, /* options= */ null);
  }

  /**
   * Simulates starting the activity specified in the specified profile, performing the same
   * security checks done by the real {@link CrossProfileApps}.
   *
   * <p>The most recent main activity started can be queried by {@link #peekNextStartedActivity()}.
   */
  @Implementation(minSdk = R)
  @SystemApi
  @RequiresPermission(permission.INTERACT_ACROSS_PROFILES)
  protected void startActivity(
      Intent intent, UserHandle targetUser, @Nullable Activity activity, @Nullable Bundle options) {
    ComponentName componentName = intent.getComponent();
    if (componentName == null) {
      throw new IllegalArgumentException("Must set ComponentName on Intent");
    }
    verifyCanAccessUser(targetUser);
    verifyHasInteractAcrossProfilesPermission();
    startedActivities.add(
        new StartedActivity(componentName, targetUser, intent, activity, options));
  }

  /** Adds {@code userHandle} to the list of accessible handles. */
  public void addTargetUserProfile(UserHandle userHandle) {
    if (userHandle.equals(Process.myUserHandle())) {
      throw new IllegalArgumentException("Cannot target current user");
    }
    targetUserProfiles.add(userHandle);
  }

  /** Removes {@code userHandle} from the list of accessible handles, if present. */
  public void removeTargetUserProfile(UserHandle userHandle) {
    if (userHandle.equals(Process.myUserHandle())) {
      throw new IllegalArgumentException("Cannot target current user");
    }
    targetUserProfiles.remove(userHandle);
  }

  /** Clears the list of accessible handles. */
  public void clearTargetUserProfiles() {
    targetUserProfiles.clear();
  }

  /**
   * Returns the most recent {@link ComponentName}, {@link UserHandle} pair started by {@link
   * CrossProfileApps#startMainActivity(ComponentName, UserHandle)}, wrapped in {@link
   * StartedMainActivity}.
   *
   * @deprecated Use {@link #peekNextStartedActivity()} instead.
   */
  @Nullable
  @Deprecated
  public StartedMainActivity peekNextStartedMainActivity() {
    if (startedMainActivities.isEmpty()) {
      return null;
    } else {
      return Iterables.getLast(startedMainActivities);
    }
  }

  /**
   * Returns the most recent {@link ComponentName}, {@link UserHandle} pair started by {@link
   * CrossProfileApps#startMainActivity(ComponentName, UserHandle)} or {@link
   * CrossProfileApps#startActivity(ComponentName, UserHandle)}, {@link #startActivity(Intent,
   * UserHandle, Activity)}, {@link #startActivity(Intent, UserHandle, Activity, Bundle)}, wrapped
   * in {@link StartedActivity}.
   */
  @Nullable
  public StartedActivity peekNextStartedActivity() {
    if (startedActivities.isEmpty()) {
      return null;
    } else {
      return Iterables.getLast(startedActivities);
    }
  }

  /**
   * Consumes the most recent {@link ComponentName}, {@link UserHandle} pair started by {@link
   * CrossProfileApps#startMainActivity(ComponentName, UserHandle)} or {@link
   * CrossProfileApps#startActivity(ComponentName, UserHandle)}, {@link #startActivity(Intent,
   * UserHandle, Activity)}, {@link #startActivity(Intent, UserHandle, Activity, Bundle)}, and
   * returns it wrapped in {@link StartedActivity}.
   */
  @Nullable
  public StartedActivity getNextStartedActivity() {
    if (startedActivities.isEmpty()) {
      return null;
    } else {
      return startedActivities.remove(startedActivities.size() - 1);
    }
  }

  /**
   * Clears all records of {@link StartedActivity}s from calls to {@link
   * CrossProfileApps#startActivity(ComponentName, UserHandle)} or {@link
   * CrossProfileApps#startMainActivity(ComponentName, UserHandle)}, {@link #startActivity(Intent,
   * UserHandle, Activity)}, {@link #startActivity(Intent, UserHandle, Activity, Bundle)}.
   */
  public void clearNextStartedActivities() {
    startedActivities.clear();
  }

  private void verifyCanAccessUser(UserHandle userHandle) {
    if (!targetUserProfiles.contains(userHandle)) {
      throw new SecurityException(
          "Not allowed to access "
              + userHandle
              + " (did you forget to call addTargetUserProfile?)");
    }
  }

  /**
   * Ensure the current package has the permission to interact across profiles.
   */
  protected void verifyHasInteractAcrossProfilesPermission() {
    if (RuntimeEnvironment.getApiLevel() >= R) {
      if (!canInteractAcrossProfiles()) {
        throw new SecurityException("Attempt to launch activity without required the permissions.");
      }
      return;
    }
    if (context.checkSelfPermission(permission.INTERACT_ACROSS_PROFILES)
        != PackageManager.PERMISSION_GRANTED) {
      throw new SecurityException(
          "Attempt to launch activity without required "
              + permission.INTERACT_ACROSS_PROFILES
              + " permission");
    }
  }

  /**
   * Ensures that {@code component} is present in the manifest as an exported and enabled activity.
   * This check and the error thrown are the same as the check done by the real {@link
   * CrossProfileApps}.
   *
   * <p>If {@code requireMainActivity} is true, then this also asserts that the activity is a
   * launcher activity.
   */
  private void verifyActivityInManifest(ComponentName component, boolean requireMainActivity) {
    Intent launchIntent = new Intent();
    if (requireMainActivity) {
      launchIntent
          .setAction(Intent.ACTION_MAIN)
          .addCategory(Intent.CATEGORY_LAUNCHER)
          .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
          .setPackage(component.getPackageName());
    } else {
      launchIntent.setComponent(component);
    }

    boolean existsMatchingActivity =
        Iterables.any(
            packageManager.queryIntentActivities(
                launchIntent, MATCH_DIRECT_BOOT_AWARE | MATCH_DIRECT_BOOT_UNAWARE),
            resolveInfo -> {
              ActivityInfo activityInfo = resolveInfo.activityInfo;
              return TextUtils.equals(activityInfo.packageName, component.getPackageName())
                  && TextUtils.equals(activityInfo.name, component.getClassName())
                  && activityInfo.exported;
            });
    if (!existsMatchingActivity) {
      throw new SecurityException(
          "Attempt to launch activity without "
              + " category Intent.CATEGORY_LAUNCHER or activity is not exported"
              + component);
    }
  }

  /**
   * Checks if the current application can interact across profile.
   *
   * <p>This checks for the existence of a target user profile, and if the app has
   * INTERACT_ACROSS_USERS, INTERACT_ACROSS_USERS_FULL or INTERACT_ACROSS_PROFILES permission.
   * Importantly, the {@code interact_across_profiles} AppOps is only checked through the value set
   * by {@link #setInteractAcrossProfilesAppOp(int)} or by {@link
   * #setInteractAcrossProfilesAppOp(String, int)}, if the application has the needed permissions.
   */
  @Implementation(minSdk = R)
  protected boolean canInteractAcrossProfiles() {
    if (getTargetUserProfiles().isEmpty()) {
      return false;
    }
    return hasPermission(permission.INTERACT_ACROSS_USERS_FULL)
        || hasPermission(permission.INTERACT_ACROSS_PROFILES)
        || hasPermission(permission.INTERACT_ACROSS_USERS)
        || canInteractAcrossProfileAppOps == AppOpsManager.MODE_ALLOWED;
  }

  /**
   * Returns whether the calling package can request to navigate the user to the relevant settings
   * page to request user consent to interact across profiles.
   *
   * <p>This checks for the existence of a target user profile, and if the app has requested the
   * INTERACT_ACROSS_PROFILES permission in its manifest. As Robolectric doesn't interpret the
   * permissions in the manifest, whether or not the app has requested this is defined by {@link
   * #setHasRequestedInteractAcrossProfiles(boolean)}.
   *
   * <p>If the test uses {@link #setInteractAcrossProfilesAppOp(int)}, it implies the app has
   * requested the AppOps.
   *
   * <p>In short, compared to {@link #canInteractAcrossProfiles()}, it doesn't check if the user has
   * the AppOps or not.
   */
  @Implementation(minSdk = R)
  protected boolean canRequestInteractAcrossProfiles() {
    if (getTargetUserProfiles().isEmpty()) {
      return false;
    }
    return hasRequestedInteractAcrossProfiles;
  }

  /**
   * Sets whether or not the current application has requested the interact across profile
   * permission in its manifest.
   */
  public void setHasRequestedInteractAcrossProfiles(boolean value) {
    hasRequestedInteractAcrossProfiles = value;
  }

  /**
   * Returns an intent with the same action as the one returned by system when requesting the same.
   *
   * <p>Note: Currently, the system will also set the package name as a URI, but as this is not
   * specified in the main doc, we shouldn't rely on it. The purpose is only to make an intent can
   * that be recognised in a test.
   *
   * @throws SecurityException if this is called while {@link
   *     CrossProfileApps#canRequestInteractAcrossProfiles()} returns false.
   */
  @Implementation(minSdk = R)
  protected Intent createRequestInteractAcrossProfilesIntent() {
    if (!canRequestInteractAcrossProfiles()) {
      throw new SecurityException(
          "The calling package can not request to interact across profiles.");
    }
    return new Intent(Settings.ACTION_MANAGE_CROSS_PROFILE_ACCESS);
  }

  /**
   * Checks whether the given intent will redirect toward the screen allowing the user to change the
   * interact across profiles AppOps.
   */
  public boolean isRequestInteractAcrossProfilesIntent(Intent intent) {
    return Settings.ACTION_MANAGE_CROSS_PROFILE_ACCESS.equals(intent.getAction());
  }

  private boolean hasPermission(String permission) {
    return context.getPackageManager().checkPermission(permission, context.getPackageName())
        == PackageManager.PERMISSION_GRANTED;
  }

  /**
   * Forces the {code interact_across_profile} AppOps for the current package.
   *
   * <p>If the value changes, this also sends the {@link
   * CrossProfileApps#ACTION_CAN_INTERACT_ACROSS_PROFILES_CHANGED} broadcast.
   */
  public void setInteractAcrossProfilesAppOp(@Mode int newMode) {
    hasRequestedInteractAcrossProfiles = true;
    if (canInteractAcrossProfileAppOps != newMode) {
      canInteractAcrossProfileAppOps = newMode;
      context.sendBroadcast(
          new Intent(CrossProfileApps.ACTION_CAN_INTERACT_ACROSS_PROFILES_CHANGED));
    }
  }

  /**
   * Checks permission and changes the AppOps value stored in {@link ShadowCrossProfileApps}.
   *
   * <p>In the real implementation, if there is no target profile, the AppOps is not changed, as it
   * will be set during the profile's initialization. The real implementation also really changes
   * the AppOps for all profiles the package is installed in.
   */
  @Implementation(minSdk = R)
  protected void setInteractAcrossProfilesAppOp(String packageName, @Mode int newMode) {
    if (!hasPermission(permission.INTERACT_ACROSS_USERS)
        || !hasPermission(permission.CONFIGURE_INTERACT_ACROSS_PROFILES)) {
      throw new SecurityException(
          "Requires INTERACT_ACROSS_USERS and CONFIGURE_INTERACT_ACROSS_PROFILES permission");
    }
    setInteractAcrossProfilesAppOp(newMode);
  }

  /**
   * Unlike the real system, we will assume a package can always configure its own cross profile
   * interaction.
   */
  @Implementation(minSdk = R)
  protected boolean canConfigureInteractAcrossProfiles(String packageName) {
    return context.getPackageName().equals(packageName);
  }

  /**
   * Container object to hold parameters passed to {@link #startMainActivity(ComponentName,
   * UserHandle)}.
   *
   * @deprecated Use {@link #peekNextStartedActivity()} and {@link StartedActivity} instead.
   */
  @Deprecated
  public static class StartedMainActivity {

    private final ComponentName componentName;
    private final UserHandle userHandle;

    public StartedMainActivity(ComponentName componentName, UserHandle userHandle) {
      this.componentName = checkNotNull(componentName);
      this.userHandle = checkNotNull(userHandle);
    }

    public ComponentName getComponentName() {
      return componentName;
    }

    public UserHandle getUserHandle() {
      return userHandle;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      StartedMainActivity that = (StartedMainActivity) o;
      return Objects.equals(componentName, that.componentName)
          && Objects.equals(userHandle, that.userHandle);
    }

    @Override
    public int hashCode() {
      return Objects.hash(componentName, userHandle);
    }
  }

  /**
   * Container object to hold parameters passed to {@link #startMainActivity(ComponentName,
   * UserHandle)} or {@link #startActivity(ComponentName, UserHandle)}, {@link
   * #startActivity(Intent, UserHandle, Activity)}, {@link #startActivity(Intent, UserHandle,
   * Activity, Bundle)}.
   *
   * <p>Note: {@link #equals} and {@link #hashCode} are only defined for the {@link ComponentName}
   * and {@link UserHandle}.
   */
  public static final class StartedActivity {

    private final ComponentName componentName;
    private final UserHandle userHandle;
    @Nullable private final Intent intent;
    @Nullable private final Activity activity;
    @Nullable private final Bundle options;

    public StartedActivity(ComponentName componentName, UserHandle userHandle) {
      this(
          componentName, userHandle, /* intent= */ null, /* activity= */ null, /* options= */ null);
    }

    public StartedActivity(
        ComponentName componentName,
        UserHandle userHandle,
        @Nullable Intent intent,
        @Nullable Activity activity,
        @Nullable Bundle options) {
      this.componentName = checkNotNull(componentName);
      this.userHandle = checkNotNull(userHandle);
      this.intent = intent;
      this.activity = activity;
      this.options = options;
    }

    public ComponentName getComponentName() {
      return componentName;
    }

    public UserHandle getUserHandle() {
      return userHandle;
    }

    @Nullable
    public Intent getIntent() {
      return intent;
    }

    @Nullable
    public Bundle getOptions() {
      return options;
    }

    @Nullable
    public Activity getActivity() {
      return activity;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      StartedActivity that = (StartedActivity) o;
      return Objects.equals(componentName, that.componentName)
          && Objects.equals(userHandle, that.userHandle);
    }

    @Override
    public int hashCode() {
      return Objects.hash(componentName, userHandle);
    }
  }
}
