package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.N;

import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import java.util.Collections;
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
  private Map<UserHandle, Long> serialNumbers = new HashMap<>();

  @Implementation(minSdk = JELLY_BEAN_MR2)
  public Bundle getApplicationRestrictions(String packageName) {
    return null;
  }

  @Implementation(minSdk = LOLLIPOP)
  public List<UserHandle> getUserProfiles(){
    return Collections.emptyList();
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
    Long result = serialNumbers.get(userHandle);
    return result == null ? -1L : result;
  }

  public void setSerialNumberForUser(UserHandle userHandle, long serialNumber) {
    serialNumbers.put(userHandle, serialNumber);
  }
}
