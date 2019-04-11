package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.N_MR1;
import static android.os.Build.VERSION_CODES.Q;
import static org.robolectric.shadow.api.Shadow.directlyOn;

import android.Manifest.permission;
import android.annotation.UserIdInt;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.UserInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.IUserManager;
import android.os.Process;
import android.os.UserHandle;
import android.os.UserManager;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

/**
 * Robolectric implementation of {@link android.os.UserManager}.
 */
@Implements(value = UserManager.class, minSdk = JELLY_BEAN_MR1)
public class ShadowUserManager {

  /**
   * The default user ID user for secondary user testing, when the ID is not otherwise specified.
   */
  public static final int DEFAULT_SECONDARY_USER_ID = 10;

  public static final int FLAG_PRIMARY = UserInfo.FLAG_PRIMARY;
  public static final int FLAG_ADMIN = UserInfo.FLAG_ADMIN;
  public static final int FLAG_GUEST = UserInfo.FLAG_GUEST;
  public static final int FLAG_RESTRICTED = UserInfo.FLAG_RESTRICTED;

  private static boolean isMultiUserSupported = false;
  private static Map<Integer, Integer> userPidMap = new HashMap<>();

  @RealObject private UserManager realObject;

  private boolean userUnlocked = true;
  private boolean managedProfile = false;
  private boolean isSystemUser = true;
  private Map<Integer, Bundle> userRestrictions = new HashMap<>();
  private BiMap<UserHandle, Long> userProfiles = HashBiMap.create();
  private Map<String, Bundle> applicationRestrictions = new HashMap<>();
  private long nextUserSerial = 0;
  private Map<Integer, UserState> userState = new HashMap<>();
  private Map<Integer, UserInfo> userInfoMap = new HashMap<>();
  private Map<Integer, List<UserInfo>> profiles = new HashMap<>();
  private String seedAccountType;

  private Context context;
  private boolean enforcePermissions;
  private boolean canSwitchUser = false;

  @Implementation
  protected void __constructor__(Context context, IUserManager service) {
    this.context = context;
    addUser(UserHandle.USER_SYSTEM, "system_user", UserInfo.FLAG_PRIMARY | UserInfo.FLAG_ADMIN);
  }

  /**
   * Compared to real Android, there is no check that the package name matches the application
   * package name and the method returns instantly.
   *
   * @see #setApplicationRestrictions(String, Bundle)
   */
  @Implementation(minSdk = JELLY_BEAN_MR2)
  protected Bundle getApplicationRestrictions(String packageName) {
    Bundle bundle = applicationRestrictions.get(packageName);
    return bundle != null ? bundle : new Bundle();
  }

  /**
   * Sets the value returned by {@link UserManager#getApplicationRestrictions(String)}.
   */
  public void setApplicationRestrictions(String packageName, Bundle restrictions) {
    applicationRestrictions.put(packageName, restrictions);
  }

  /**
   * Adds a profile associated for the user that the calling process is running on.
   *
   * The user is assigned an arbitrary unique serial number.
   *
   * @return the user's serial number
   */
  public long addUserProfile(UserHandle userHandle) {
    long serialNumber = nextUserSerial++;
    userProfiles.put(userHandle, serialNumber);
    return serialNumber;
  }

  @Implementation(minSdk = LOLLIPOP)
  protected List<UserHandle> getUserProfiles() {
    return ImmutableList.copyOf(userProfiles.keySet());
  }

  /**
   * If any profiles have been added using {@link #addProfile}, return those profiles.
   *
   * Otherwise follow real android behaviour.
   */
  @Implementation(minSdk = LOLLIPOP)
  protected List<UserInfo> getProfiles(int userHandle) {
    if (profiles.containsKey(userHandle)) {
      return ImmutableList.copyOf(profiles.get(userHandle));
    }
    return directlyOn(
        realObject, UserManager.class, "getProfiles", ClassParameter.from(int.class, userHandle));
  }

  /** Add a profile to be returned by {@link #getProfiles(int)}.**/
  public void addProfile(
      int userHandle, int profileUserHandle, String profileName, int profileFlags) {
    profiles.putIfAbsent(userHandle, new ArrayList<>());
    profiles.get(userHandle).add(new UserInfo(profileUserHandle, profileName, profileFlags));
  }

  @Implementation(minSdk = N)
  protected boolean isUserUnlocked() {
    return userUnlocked;
  }

  /**
   * Setter for {@link UserManager#isUserUnlocked()}
   */
  public void setUserUnlocked(boolean userUnlocked) {
    this.userUnlocked = userUnlocked;
  }

  /**
   * If permissions are enforced (see {@link #enforcePermissionChecks(boolean)}) and the application
   * doesn't have the {@link android.Manifest.permission#MANAGE_USERS} permission, throws a
   * {@link SecurityManager} exception.
   *
   * @return `false` by default, or the value specified via {@link #setManagedProfile(boolean)}
   * @see #enforcePermissionChecks(boolean)
   * @see #setManagedProfile(boolean)
   */
  @Implementation(minSdk = LOLLIPOP)
  protected boolean isManagedProfile() {
    if (enforcePermissions && !hasManageUsersPermission()) {
      throw new SecurityException(
          "You need MANAGE_USERS permission to: check if specified user a " +
              "managed profile outside your profile group");
    }
    return managedProfile;
  }

  public void enforcePermissionChecks(boolean enforcePermissions) {
    this.enforcePermissions = enforcePermissions;
  }

  /**
   * Setter for {@link UserManager#isManagedProfile()}.
   */
  public void setManagedProfile(boolean managedProfile) {
    this.managedProfile = managedProfile;
  }

  @Implementation(minSdk = LOLLIPOP)
  protected boolean hasUserRestriction(String restrictionKey, UserHandle userHandle) {
    Bundle bundle = userRestrictions.get(userHandle.getIdentifier());
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
    userRestrictions.remove(userHandle.getIdentifier());
  }

  @Implementation(minSdk = JELLY_BEAN_MR2)
  protected Bundle getUserRestrictions(UserHandle userHandle) {
    return new Bundle(getUserRestrictionsForUser(userHandle));
  }

  private Bundle getUserRestrictionsForUser(UserHandle userHandle) {
    Bundle bundle = userRestrictions.get(userHandle.getIdentifier());
    if (bundle == null) {
      bundle = new Bundle();
      userRestrictions.put(userHandle.getIdentifier(), bundle);
    }
    return bundle;
  }

  /**
   * @see #addUserProfile(UserHandle)
   */
  @Implementation
  protected long getSerialNumberForUser(UserHandle userHandle) {
    Long result = userProfiles.get(userHandle);
    return result == null ? -1L : result;
  }

  /**
   * @deprecated prefer {@link #addUserProfile(UserHandle)} to ensure consistency of profiles known
   * to the {@link UserManager}. Furthermore, calling this method for the current user, i.e: {@link
   * Process#myUserHandle()} is no longer necessary as this user is always known to UserManager and
   * has a preassigned serial number.
   */
  @Deprecated
  public void setSerialNumberForUser(UserHandle userHandle, long serialNumber) {
    userProfiles.put(userHandle, serialNumber);
  }

  /**
   * @see #addUserProfile(UserHandle)
   */
  @Implementation
  protected UserHandle getUserForSerialNumber(long serialNumber) {
    return userProfiles.inverse().get(serialNumber);
  }

  /** @see #addUserProfile(UserHandle) */
  @Implementation
  protected int getUserSerialNumber(@UserIdInt int userHandle) {
    for (Entry<UserHandle, Long> entry : userProfiles.entrySet()) {
      if (entry.getKey().getIdentifier() == userHandle) {
        return entry.getValue().intValue();
      }
    }
    return -1;
  }

  private boolean hasManageUsersPermission() {
    return context.getPackageManager().checkPermission(permission.MANAGE_USERS, context.getPackageName()) == PackageManager.PERMISSION_GRANTED;
  }

  private void checkPermissions() {
    // TODO Ensure permisions
    //              throw new SecurityException("You need INTERACT_ACROSS_USERS or MANAGE_USERS
    // permission "
    //                + "to: check " + name);throw new SecurityException();
  }

  /**
   * @return `false` by default, or the value specified via {@link #setIsDemoUser(boolean)}
   */
  @Implementation(minSdk = N_MR1)
  protected boolean isDemoUser() {
    return getUserInfo(UserHandle.myUserId()).isDemo();
  }

  /**
   * Sets that the current user is a demo user; controls the return value of {@link
   * UserManager#isDemoUser()}.
   *
   * @deprecated Use {@link ShadowUserManager#addUser(int, String, int)} to create a demo user
   *     instead of changing default user flags.
   */
  @Deprecated
  public void setIsDemoUser(boolean isDemoUser) {
    UserInfo userInfo = getUserInfo(UserHandle.myUserId());
    if (isDemoUser) {
      userInfo.flags |= UserInfo.FLAG_DEMO;
    } else {
      userInfo.flags &= ~UserInfo.FLAG_DEMO;
    }
  }

  /**
   * @return 'true' by default, or the value specified via {@link #setIsSystemUser(boolean)}
   */
  @Implementation(minSdk = M)
  protected boolean isSystemUser() {
    if (isSystemUser == false) {
      return false;
    } else {
      return directlyOn(realObject, UserManager.class, "isSystemUser");
    }
  }

  /**
   * Sets that the current user is the system user; controls the return value of {@link
   * UserManager#isSystemUser()}.
   *
   * @deprecated Use {@link ShadowUserManager#addUser(int, String, int)} to create a system user
   *     instead of changing default user flags.
   */
  @Deprecated
  public void setIsSystemUser(boolean isSystemUser) {
    this.isSystemUser = isSystemUser;
  }

  /**
   * Sets that the current user is the primary user; controls the return value of {@link
   * UserManager#isPrimaryUser()}.
   *
   * @deprecated Use {@link ShadowUserManager#addUser(int, String, int)} to create a primary user
   *     instead of changing default user flags.
   */
  @Deprecated
  public void setIsPrimaryUser(boolean isPrimaryUser) {
    UserInfo userInfo = getUserInfo(UserHandle.myUserId());
    if (isPrimaryUser) {
      userInfo.flags |= UserInfo.FLAG_PRIMARY;
    } else {
      userInfo.flags &= ~UserInfo.FLAG_PRIMARY;
    }
  }

  /**
   * @return 'false' by default, or the value specified via {@link #setIsLinkedUser(boolean)}
   */
  @Implementation(minSdk = JELLY_BEAN_MR2)
  protected boolean isLinkedUser() {
    return isRestrictedProfile();
  }

  /**
   * Sets that the current user is the linked user; controls the return value of {@link
   * UserManager#isLinkedUser()}.
   *
   * @deprecated Use {@link ShadowUserManager#addUser(int, String, int)} to create a linked user
   *     instead of changing default user flags.
   */
  @Deprecated
  public void setIsLinkedUser(boolean isLinkedUser) {
    setIsRestrictedProfile(isLinkedUser);
  }

  /**
   * Returns 'false' by default, or the value specified via {@link
   * #setIsRestrictedProfile(boolean)}.
   */
  public boolean isRestrictedProfile() {
    return getUserInfo(UserHandle.myUserId()).isRestricted();
  }

  /**
   * Sets this process running under a restricted profile; controls the return value of {@link
   * UserManager#isRestrictedProfile()}.
   */
  public void setIsRestrictedProfile(boolean isRestrictedProfile) {
    UserInfo userInfo = getUserInfo(UserHandle.myUserId());
    if (isRestrictedProfile) {
      userInfo.flags |= UserInfo.FLAG_RESTRICTED;
    } else {
      userInfo.flags &= ~UserInfo.FLAG_RESTRICTED;
    }
  }

  /**
   * Sets that the current user is the guest user; controls the return value of {@link
   * UserManager#isGuestUser()}.
   *
   * @deprecated Use {@link ShadowUserManager#addUser(int, String, int)} to create a guest user
   *     instead of changing default user flags.
   */
  @Deprecated
  public void setIsGuestUser(boolean isGuestUser) {
    UserInfo userInfo = getUserInfo(UserHandle.myUserId());
    if (isGuestUser) {
      userInfo.flags |= UserInfo.FLAG_GUEST;
    } else {
      userInfo.flags &= ~UserInfo.FLAG_GUEST;
    }
  }

  /**
   * @see #setUserState(UserHandle, UserState)
   */
  @Implementation
  protected boolean isUserRunning(UserHandle handle) {
    checkPermissions();
    UserState state = userState.get(handle.getIdentifier());

    if (state == UserState.STATE_RUNNING_LOCKED
        || state == UserState.STATE_RUNNING_UNLOCKED
        || state == UserState.STATE_RUNNING_UNLOCKING) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * @see #setUserState(UserHandle, UserState)
   */
  @Implementation
  protected boolean isUserRunningOrStopping(UserHandle handle) {
    checkPermissions();
    UserState state = userState.get(handle.getIdentifier());

    if (state == UserState.STATE_RUNNING_LOCKED
        || state == UserState.STATE_RUNNING_UNLOCKED
        || state == UserState.STATE_RUNNING_UNLOCKING
        || state == UserState.STATE_STOPPING) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * Describes the current state of the user. State can be set using
   * {@link #setUserState(UserHandle, UserState)}.
   */
  public enum UserState {
    // User is first coming up.
    STATE_BOOTING,
    // User is in the locked state.
    STATE_RUNNING_LOCKED,
    // User is in the unlocking state.
    STATE_RUNNING_UNLOCKING,
    // User is in the running state.
    STATE_RUNNING_UNLOCKED,
    // User is in the initial process of being stopped.
    STATE_STOPPING,
    // User is in the final phase of stopping, sending Intent.ACTION_SHUTDOWN.
    STATE_SHUTDOWN
  }

  /**
   * Sets the current state for a given user, see {@link UserManager#isUserRunning(UserHandle)}
   * and {@link UserManager#isUserRunningOrStopping(UserHandle)}
   */
  public void setUserState(UserHandle handle, UserState state) {
    userState.put(handle.getIdentifier(), state);
  }

  @Implementation
  protected List<UserInfo> getUsers() {
    return new ArrayList<UserInfo>(userInfoMap.values());
  }

  @Implementation
  protected UserInfo getUserInfo(int userHandle) {
    return userInfoMap.get(userHandle);
  }

  /**
   * Returns {@code false} by default, or the value specified via {@link
   * #setCanSwitchUser(boolean)}.
   */
  @Implementation(minSdk = N)
  protected boolean canSwitchUsers() {
    return canSwitchUser;
  }

  /**
   * Sets whether switching users is allowed or not; controls the return value of {@link
   * UserManager#canSwitchUsers()}
   */
  public void setCanSwitchUser(boolean canSwitchUser) {
    this.canSwitchUser = canSwitchUser;
  }

  @Implementation(minSdk = Build.VERSION_CODES.Q)
  protected String getSeedAccountType() {
    return seedAccountType;
  }

  /** Setter for {@link UserManager#getSeedAccountType()} */
  public void setSeedAccountType(String seedAccountType) {
    this.seedAccountType = seedAccountType;
  }

  @Implementation(minSdk = JELLY_BEAN_MR1)
  protected boolean removeUser(int userHandle) {
    userInfoMap.remove(userHandle);
    return true;
  }

  @Implementation(minSdk = Q)
  protected boolean removeUser(UserHandle user) {
    return removeUser(user.getIdentifier());
  }

  @Implementation(minSdk = N)
  protected static boolean supportsMultipleUsers() {
    return isMultiUserSupported;
  }

  /**
   * Sets whether multiple users are supported; controls the return value of {@link
   * UserManager#supportsMultipleUser}.
   */
  public void setSupportsMultipleUsers(boolean isMultiUserSupported) {
    ShadowUserManager.isMultiUserSupported = isMultiUserSupported;
  }

  /**
   * Switches the current user to {@code userHandle}.
   *
   * @param userId the integer handle of the user, where 0 is the primary user.
   */
  public void switchUser(int userId) {
    if (!userInfoMap.containsKey(userId)) {
      throw new UnsupportedOperationException("Must add user before switching to it");
    }

    ShadowProcess.setUid(userPidMap.get(userId));
  }

  /**
   * Creates a user with the specified name, userId and flags.
   *
   * @param id the unique id of user
   * @param name name of the user
   * @param flags 16 bits for user type. See {@link UserInfo#flags}
   * @return a handle to the new user
   */
  public UserHandle addUser(int id, String name, int flags) {
    UserHandle userHandle =
        id == UserHandle.USER_SYSTEM ? Process.myUserHandle() : new UserHandle(id);
    addUserProfile(userHandle);
    setSerialNumberForUser(userHandle, (long) id);
    userInfoMap.put(id, new UserInfo(id, name, flags));
    userPidMap.put(
        id,
        id == UserHandle.USER_SYSTEM
            ? Process.myUid()
            : id * UserHandle.PER_USER_RANGE + ShadowProcess.getRandomApplicationUid());
    return userHandle;
  }

  @Resetter
  public static void reset() {
    if (userPidMap != null && userPidMap.isEmpty() == false) {
      ShadowProcess.setUid(userPidMap.get(UserHandle.USER_SYSTEM));

      userPidMap.clear();
      userPidMap.put(UserHandle.USER_SYSTEM, Process.myUid());
    }
  }
}
