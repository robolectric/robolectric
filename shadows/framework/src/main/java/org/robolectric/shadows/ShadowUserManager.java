package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.N;

import android.content.pm.UserInfo;
import android.os.Bundle;
import android.os.Process;
import android.os.UserHandle;
import android.os.UserManager;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/**
 * Robolectric implementation of {@link android.os.UserManager}.
 */
@Implements(value = UserManager.class, minSdk = JELLY_BEAN_MR1)
public class ShadowUserManager {

  private boolean userUnlocked = true;
  private boolean managedProfile = false;
  private Map<UserHandle, Bundle> userRestrictions = new HashMap<>();
  private BiMap<UserHandle, Long> userProfiles = HashBiMap.create();
  private Map<String, Bundle> applicationRestrictions = new HashMap<>();
  private int nextUserSerial = 0;

  public ShadowUserManager() {
    addUserProfile(Process.myUserHandle());
  }

  /**
   * Compared to real Android, there is no check that the package name matches the application
   * package name and the method returns instantly.
   */
  @Implementation(minSdk = JELLY_BEAN_MR2)
  public Bundle getApplicationRestrictions(String packageName) {
    Bundle bundle = applicationRestrictions.get(packageName);
    return bundle != null ? bundle : new Bundle();
  }

  /**
   * Setter for {@link #getApplicationRestrictions(String)}
   */
  public void setApplicationRestrictions(String packageName, Bundle restrictions) {
    applicationRestrictions.put(packageName, restrictions);
  }

  /**
   * Adds a profile associated for the user that the calling process is running on.
   */
  public void addUserProfile(UserHandle userHandle) {
    setSerialNumberForUser(userHandle, nextUserSerial++);
  }

  @Implementation(minSdk = LOLLIPOP)
  public List<UserHandle> getUserProfiles(){
    return ImmutableList.copyOf(userProfiles.keySet());
  }

  @Implementation(minSdk = N)
  public boolean isUserUnlocked() {
    return userUnlocked;
  }

  /**
   * Setter for {@link UserManager#isUserUnlocked()}
   */
  public void setUserUnlocked(boolean userUnlocked) {
    this.userUnlocked = userUnlocked;
  }

  @Implementation(minSdk = LOLLIPOP)
  public boolean isManagedProfile() {
    return managedProfile;
  }

  /**
   * Setter for {@link UserManager#isManagedProfile()}
   */
  public void setManagedProfile(boolean managedProfile) {
    this.managedProfile = managedProfile;
  }

  @Implementation(minSdk = LOLLIPOP)
  public boolean hasUserRestriction(String restrictionKey, UserHandle userHandle) {
    Bundle bundle = userRestrictions.get(userHandle);
    return bundle != null && bundle.getBoolean(restrictionKey);
  }

  public void setUserRestriction(UserHandle userHandle, String restrictionKey, boolean value) {
    Bundle bundle = getUserRestrictionsForUser(userHandle);
    bundle.putBoolean(restrictionKey, value);
  }

  /**
   * Removes all user restrictions set of a user identified by {@code userHandle}.
   */
  public void clearUserRestrictions(UserHandle userHandle) {
    if (userRestrictions.containsKey(userHandle)) {
      userRestrictions.remove(userHandle);
    }
  }

  @Implementation(minSdk = JELLY_BEAN_MR2)
  public Bundle getUserRestrictions(UserHandle userHandle) {
    return getUserRestrictionsForUser(userHandle);
  }

  private Bundle getUserRestrictionsForUser(UserHandle userHandle) {
    Bundle bundle = userRestrictions.get(userHandle);
    if (bundle == null) {
      bundle = new Bundle();
      userRestrictions.put(userHandle, bundle);
    }
    return bundle;
  }

  @Implementation
  public long getSerialNumberForUser(UserHandle userHandle) {
    Long result = userProfiles.get(userHandle);
    return result == null ? -1L : result;
  }

  /**
   * @deprecated prefer {@link #addUserProfile()} to ensure consistency of profiles known to
   * UserManager. Furthermore, calling this method for the current user, i.e:
   * {@link Process.myUserHandle()} is no longer necessary as this user is always known to
   * UserManager and has a preassigned serial number.
   */
  @Deprecated
  public void setSerialNumberForUser(UserHandle userHandle, long serialNumber) {
    userProfiles.put(userHandle, serialNumber);
  }

  @Implementation
  public UserHandle getUserForSerialNumber(long serialNumber) {
    return userProfiles.inverse().get(serialNumber);
  }

  @Implementation
  public List<UserInfo> getUsers() {
    // Implement this - return empty list to avoid NPE from call to getUserCount()
    return ImmutableList.of();
  }
}
