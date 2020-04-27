package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.N_MR1;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.Q;
import static org.robolectric.shadow.api.Shadow.directlyOn;
import static org.robolectric.shadow.api.Shadow.invokeConstructor;
import static org.robolectric.util.ReflectionHelpers.ClassParameter.from;

import android.Manifest.permission;
import android.annotation.UserIdInt;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.UserInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.IUserManager;
import android.os.Process;
import android.os.UserHandle;
import android.os.UserManager;
import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;

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
  public static final int FLAG_DEMO = UserInfo.FLAG_DEMO;
  public static final int FLAG_MANAGED_PROFILE = UserInfo.FLAG_MANAGED_PROFILE;

  private static boolean isMultiUserSupported = false;
  protected static Map<Integer, Integer> userPidMap = new HashMap<>();

  @RealObject private UserManager realObject;

  private boolean userUnlocked = true;
  private boolean managedProfile = false;
  private boolean isSystemUser = true;
  private Map<Integer, Bundle> userRestrictions = new HashMap<>();
  /** Holds the serial numbers for all users and profiles, indexed by UserHandle.id */
  protected BiMap<Integer, Long> userSerialNumbers = HashBiMap.create();

  private Map<String, Bundle> applicationRestrictions = new HashMap<>();
  /** Holds all UserStates, indexed by UserHandle.id */
  protected Map<Integer, UserState> userState = new HashMap<>();
  /** Holds the UserInfo for all registered users and profiles, indexed by UserHandle.id */
  protected Map<Integer, UserInfo> userInfoMap = new HashMap<>();
  /**
   * Holds whether or not a managed profile can be unlocked. If a profile is not in this map, it is
   * assume it can be unlocked.
   */
  private final Map<Integer, Boolean> profileIsLocked = new HashMap<>();
  /**
   * Each user holds a list of UserHandles of assocated profiles and user itself. User is indexed by
   * UserHandle.id. See UserManager.getProfiles(userId).
   */
  protected Map<Integer, List<UserHandle>> userProfilesListMap = new HashMap<>();

  private String seedAccountType;

  private Context context;
  private boolean enforcePermissions;
  private int userSwitchability = UserManager.SWITCHABILITY_STATUS_OK;

  @Implementation
  protected void __constructor__(Context context, IUserManager service) {
    this.context = context;
    invokeConstructor(
        UserManager.class,
        realObject,
        from(Context.class, context),
        from(IUserManager.class, service));
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
   * <p>The user is assigned an arbitrary unique serial number.
   *
   * @return the user's serial number
   * @deprecated use either addUser() or addProfile()
   */
  @Deprecated
  public long addUserProfile(UserHandle userHandle) {
    addProfile(UserHandle.myUserId(), userHandle.getIdentifier(), "", 0);
    return userSerialNumbers.get(userHandle.getIdentifier());
  }

  @Implementation(minSdk = LOLLIPOP)
  protected List<UserHandle> getUserProfiles() {
    return ImmutableList.copyOf(userProfilesListMap.get(UserHandle.myUserId()));
  }

  /**
   * If any profiles have been added using {@link #addProfile}, return those profiles.
   *
   * Otherwise follow real android behaviour.
   */
  @Implementation(minSdk = LOLLIPOP)
  protected List<UserInfo> getProfiles(int userHandle) {
    if (userProfilesListMap.containsKey(userHandle)) {
      ArrayList<UserInfo> infos = new ArrayList<>();
      for (UserHandle profileHandle : userProfilesListMap.get(userHandle)) {
        infos.add(userInfoMap.get(profileHandle.getIdentifier()));
      }
      return infos;
    }
    return directlyOn(realObject, UserManager.class, "getProfiles", from(int.class, userHandle));
  }

  @Implementation(minSdk = LOLLIPOP)
  protected UserInfo getProfileParent(int userId) {
    if (enforcePermissions && !hasManageUsersPermission()) {
      throw new SecurityException("Requires MANAGE_USERS permission");
    }
    UserInfo profile = getUserInfo(userId);
    if (profile == null) {
      return null;
    }
    int parentUserId = profile.profileGroupId;
    if (parentUserId == userId || parentUserId == UserInfo.NO_PROFILE_GROUP_ID) {
      return null;
    } else {
      return getUserInfo(parentUserId);
    }
  }

  /** Add a profile to be returned by {@link #getProfiles(int)}.**/
  public void addProfile(
      int userHandle, int profileUserHandle, String profileName, int profileFlags) {
    // Don't override serial number set by setSerialNumberForUser()
    if (!userSerialNumbers.containsKey(profileUserHandle)) {
      // use UserHandle id as serial number unless setSerialNumberForUser() is used
      userSerialNumbers.put(profileUserHandle, (long) profileUserHandle);
    }
    userInfoMap.put(profileUserHandle, new UserInfo(profileUserHandle, profileName, profileFlags));
    // Insert profile to the belonging user's userProfilesList
    userProfilesListMap.putIfAbsent(userHandle, new ArrayList<>());
    List<UserHandle> list = userProfilesListMap.get(userHandle);
    UserHandle handle = new UserHandle(profileUserHandle);
    if (!list.contains(handle)) {
      list.add(handle);
    }
  }

  /** Setter for {@link UserManager#isUserUnlocked()} */
  public void setUserUnlocked(boolean userUnlocked) {
    this.userUnlocked = userUnlocked;
  }

  @Implementation(minSdk = N)
  protected boolean isUserUnlocked() {
    return userUnlocked;
  }

  /** @see #setUserState(UserHandle, UserState) */
  @Implementation(minSdk = 24)
  protected boolean isUserUnlocked(UserHandle handle) {
    checkPermissions();
    UserState state = userState.get(handle.getIdentifier());

    return state == UserState.STATE_RUNNING_UNLOCKED;
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

  /**
   * If permissions are enforced (see {@link #enforcePermissionChecks(boolean)}) and the application
   * doesn't have the {@link android.Manifest.permission#MANAGE_USERS} permission, throws a {@link
   * SecurityManager} exception.
   *
   * @return true if the profile added has FLAG_MANAGED_PROFILE
   * @see #enforcePermissionChecks(boolean)
   * @see #addProfile(int, int, String, int)
   * @see #addUser(int, String, int)
   */
  @Implementation(minSdk = N)
  protected boolean isManagedProfile(int userHandle) {
    if (enforcePermissions && !hasManageUsersPermission()) {
      throw new SecurityException(
          "You need MANAGE_USERS permission to: check if specified user a "
              + "managed profile outside your profile group");
    }
    UserInfo info = getUserInfo(userHandle);
    return info != null && ((info.flags & FLAG_MANAGED_PROFILE) == FLAG_MANAGED_PROFILE);
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
   * @see #addProfile(int, int, String, int)
   * @see #addUser(int, String, int)
   */
  @Implementation
  protected long getSerialNumberForUser(UserHandle userHandle) {
    Long result = userSerialNumbers.get(userHandle.getIdentifier());
    return result == null ? -1L : result;
  }

  /**
   * {@link #addUser} uses UserHandle for serialNumber. setSerialNumberForUser() allows assigning an
   * arbitary serialNumber. Some test use serialNumber!=0 as secondary user check, so it's necessary
   * to "fake" the serialNumber to a non-zero value.
   */
  public void setSerialNumberForUser(UserHandle userHandle, long serialNumber) {
    userSerialNumbers.put(userHandle.getIdentifier(), serialNumber);
  }

  /**
   * @see #addProfile(int, int, String, int)
   * @see #addUser(int, String, int)
   */
  @Implementation
  protected UserHandle getUserForSerialNumber(long serialNumber) {
    Integer userHandle = userSerialNumbers.inverse().get(serialNumber);
    return userHandle == null ? null : new UserHandle(userHandle);
  }

  /**
   * @see #addProfile(int, int, String, int)
   * @see #addUser(int, String, int)
   */
  @Implementation
  protected int getUserSerialNumber(@UserIdInt int userHandle) {
    Long result = userSerialNumbers.get(userHandle);
    return result != null ? result.intValue() : -1;
  }

  /**
   * Returns the name of the user.
   *
   * On real Android, if a UserHandle.USER_SYSTEM user is found but does not have a name, it will
   * return a name like "Owner". In Robolectric, the USER_SYSTEM user always has a name.
   */
  @Implementation(minSdk = Q)
  protected String getUserName() {
    UserInfo user = getUserInfo(UserHandle.myUserId());
    return user == null ? "" : user.name;
  }

  /** @return user id for given user serial number. */
  @HiddenApi
  @Implementation(minSdk = JELLY_BEAN_MR1)
  @UserIdInt
  protected int getUserHandle(int serialNumber) {
    Integer userHandle = userSerialNumbers.inverse().get((long) serialNumber);
    return userHandle == null ? -1 : userHandle;
  }

  private boolean hasManageUsersPermission() {
    return context.getPackageManager().checkPermission(permission.MANAGE_USERS, context.getPackageName()) == PackageManager.PERMISSION_GRANTED;
  }

  private boolean hasModifyQuietModePermission() {
    return context
            .getPackageManager()
            .checkPermission(permission.MODIFY_QUIET_MODE, context.getPackageName())
        == PackageManager.PERMISSION_GRANTED;
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
  @Implementation(minSdk = P)
  protected boolean isRestrictedProfile() {
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
   * Describes the current state of the user. State can be set using {@link
   * #setUserState(UserHandle, UserState)}.
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

  /**
   * Query whether the quiet mode is enabled for a managed profile.
   *
   * <p>This method checks whether the user handle corresponds to a managed profile, and then query
   * its state. When quiet, the user is not running.
   */
  @Implementation(minSdk = O)
  protected boolean isQuietModeEnabled(UserHandle userHandle) {
    // Return false if this is not a managed profile (this is the OS's behavior).
    if (!isManagedProfileWithoutPermission(userHandle)) {
      return false;
    }

    UserInfo info = getUserInfo(userHandle.getIdentifier());
    return (info.flags & UserInfo.FLAG_QUIET_MODE) == UserInfo.FLAG_QUIET_MODE;
  }

  /**
   * Request the quiet mode.
   *
   * This will succeed unless {@link #setProfileIsLocked(UserHandle, boolean)} is called with
   * {@code true} for the managed profile, in which case it will always fail.
   */
  @Implementation(minSdk = Q)
  protected boolean requestQuietModeEnabled(boolean enableQuietMode, UserHandle userHandle) {
    if (enforcePermissions && !hasManageUsersPermission() && !hasModifyQuietModePermission()) {
      throw new SecurityException("Requires MANAGE_USERS or MODIFY_QUIET_MODE permission");
    }
    Preconditions.checkArgument(isManagedProfileWithoutPermission(userHandle));
    int userProfileHandle = userHandle.getIdentifier();
    UserInfo info = getUserInfo(userHandle.getIdentifier());
    if (enableQuietMode) {
      userState.put(userProfileHandle, UserState.STATE_SHUTDOWN);
      info.flags |= UserInfo.FLAG_QUIET_MODE;
    } else {
      if (profileIsLocked.getOrDefault(userProfileHandle, false)) {
        return true;
      }
      userState.put(userProfileHandle, UserState.STATE_RUNNING_UNLOCKED);
      info.flags &= ~UserInfo.FLAG_QUIET_MODE;
    }
    sendQuietModeBroadcast(
        enableQuietMode
            ? Intent.ACTION_MANAGED_PROFILE_UNAVAILABLE
            : Intent.ACTION_MANAGED_PROFILE_AVAILABLE,
        userHandle);
    return false;
  }

  /**
   * If the current application has the necessary rights, it will receive the background action too.
   */
  protected void sendQuietModeBroadcast(String action, UserHandle profileHandle) {
    Intent intent = new Intent(action);
    intent.putExtra(Intent.EXTRA_USER, profileHandle);
    intent.setFlags(Intent.FLAG_RECEIVER_FOREGROUND | Intent.FLAG_RECEIVER_REGISTERED_ONLY);
    // Send the broadcast to the context-registered receivers.
    context.sendBroadcast(intent);
  }

  /**
   * Check if a profile is managed, not checking permissions.
   *
   * <p>This is useful to implement other methods.
   */
  private boolean isManagedProfileWithoutPermission(UserHandle userHandle) {
    UserInfo info = getUserInfo(userHandle.getIdentifier());
    return (info != null && ((info.flags & FLAG_MANAGED_PROFILE) == FLAG_MANAGED_PROFILE));
  }

  public void setProfileIsLocked(UserHandle profileHandle, boolean isLocked) {
    profileIsLocked.put(profileHandle.getIdentifier(), isLocked);
  }

  @Implementation
  protected List<UserInfo> getUsers() {
    return new ArrayList<>(userInfoMap.values());
  }

  @Implementation
  protected UserInfo getUserInfo(int userHandle) {
    return userInfoMap.get(userHandle);
  }


  /**
   * Sets whether switching users is allowed or not; controls the return value of {@link
   * UserManager#canSwitchUser()}
   *
   * @deprecated use {@link #setUserSwitchability} instead
   */
  @Deprecated
  public void setCanSwitchUser(boolean canSwitchUser) {
    setUserSwitchability(
        canSwitchUser
            ? UserManager.SWITCHABILITY_STATUS_OK
            : UserManager.SWITCHABILITY_STATUS_USER_SWITCH_DISALLOWED);
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
    userProfilesListMap.remove(userHandle);
    // if it's a profile, remove from the belong list in userProfilesListMap
    UserHandle profielHandle = new UserHandle(userHandle);
    for (List<UserHandle> list : userProfilesListMap.values()) {
      if (list.remove(profielHandle)) {
        break;
      }
    }
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

    // Don't override serial number set by setSerialNumberForUser()
    if (!userSerialNumbers.containsKey(id)) {
      // use UserHandle id as serial number unless setSerialNumberForUser() is used
      userSerialNumbers.put(id, (long) id);
    }
    // Start the user as shut down.
    userState.put(id, UserState.STATE_SHUTDOWN);

    // Update UserInfo regardless if was added or not
    userInfoMap.put(id, new UserInfo(id, name, flags));
    if (!userProfilesListMap.containsKey(id)) {
      userProfilesListMap.put(id, new ArrayList<>());
      // getUserProfiles() includes user's handle
      userProfilesListMap.get(id).add(new UserHandle(id));
      userPidMap.put(
          id,
          id == UserHandle.USER_SYSTEM
              ? Process.myUid()
              : id * UserHandle.PER_USER_RANGE + ShadowProcess.getRandomApplicationUid());
    }
    return userHandle;
  }

 /**
   * Returns {@code true} by default, or the value specified via {@link #setCanSwitchUser(boolean)}.
   */
  @Implementation(minSdk = N, maxSdk = Q)
  protected boolean canSwitchUsers() {
    return getUserSwitchability() == UserManager.SWITCHABILITY_STATUS_OK;
  }

  @Implementation(minSdk = Q)
  protected int getUserSwitchability() {
    return userSwitchability;
  }

  /** Sets the user switchability for all users. */
  public void setUserSwitchability(int switchability) {
    this.userSwitchability = switchability;
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
