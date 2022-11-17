package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.N_MR1;
import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.R;
import static android.os.Build.VERSION_CODES.TIRAMISU;
import static android.os.UserManager.USER_TYPE_FULL_GUEST;
import static android.os.UserManager.USER_TYPE_FULL_RESTRICTED;
import static android.os.UserManager.USER_TYPE_FULL_SECONDARY;
import static org.robolectric.shadow.api.Shadow.invokeConstructor;
import static org.robolectric.util.ReflectionHelpers.ClassParameter.from;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.Manifest.permission;
import android.annotation.UserIdInt;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.UserInfo;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.IUserManager;
import android.os.PersistableBundle;
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
import java.util.Set;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

/** Robolectric implementation of {@link android.os.UserManager}. */
@Implements(value = UserManager.class, minSdk = JELLY_BEAN_MR1)
public class ShadowUserManager {

  /**
   * The default user ID user for secondary user testing, when the ID is not otherwise specified.
   */
  public static final int DEFAULT_SECONDARY_USER_ID = 10;

  private static final int DEFAULT_MAX_SUPPORTED_USERS = 1;

  public static final int FLAG_PRIMARY = UserInfo.FLAG_PRIMARY;
  public static final int FLAG_ADMIN = UserInfo.FLAG_ADMIN;
  public static final int FLAG_GUEST = UserInfo.FLAG_GUEST;
  public static final int FLAG_RESTRICTED = UserInfo.FLAG_RESTRICTED;
  public static final int FLAG_DEMO = UserInfo.FLAG_DEMO;
  public static final int FLAG_MANAGED_PROFILE = UserInfo.FLAG_MANAGED_PROFILE;
  public static final int FLAG_PROFILE = UserInfo.FLAG_PROFILE;
  public static final int FLAG_FULL = UserInfo.FLAG_FULL;
  public static final int FLAG_SYSTEM = UserInfo.FLAG_SYSTEM;

  private static int maxSupportedUsers = DEFAULT_MAX_SUPPORTED_USERS;
  private static boolean isMultiUserSupported = false;

  @RealObject private UserManager realObject;
  private UserManagerState userManagerState;
  private Boolean managedProfile;
  private boolean userUnlocked = true;
  private boolean isSystemUser = true;

  /**
   * Holds whether or not a managed profile can be unlocked. If a profile is not in this map, it is
   * assume it can be unlocked.
   */
  private String seedAccountName;

  private String seedAccountType;
  private PersistableBundle seedAccountOptions;

  private Context context;
  private boolean enforcePermissions;
  private int userSwitchability = UserManager.SWITCHABILITY_STATUS_OK;

  /**
   * Global UserManager state. Shared across {@link UserManager}s created in different {@link
   * Context}s.
   */
  static class UserManagerState {
    private final Map<Integer, Integer> userPidMap = new HashMap<>();
    /** Holds the serial numbers for all users and profiles, indexed by UserHandle.id */
    private final BiMap<Integer, Long> userSerialNumbers = HashBiMap.create();
    /** Holds all UserStates, indexed by UserHandle.id */
    private final Map<Integer, UserState> userState = new HashMap<>();
    /** Holds the UserInfo for all registered users and profiles, indexed by UserHandle.id */
    private final Map<Integer, UserInfo> userInfoMap = new HashMap<>();
    /**
     * Each user holds a list of UserHandles of assocated profiles and user itself. User is indexed
     * by UserHandle.id. See UserManager.getProfiles(userId).
     */
    private final Map<Integer, List<UserHandle>> userProfilesListMap = new HashMap<>();

    private final Map<Integer, Bundle> userRestrictions = new HashMap<>();
    private final Map<String, Bundle> applicationRestrictions = new HashMap<>();
    private final Map<Integer, Boolean> profileIsLocked = new HashMap<>();
    private final Map<Integer, Bitmap> userIcon = new HashMap<>();

    private int nextUserId = DEFAULT_SECONDARY_USER_ID;

    public UserManagerState() {
      int id = UserHandle.USER_SYSTEM;
      String name = "system_user";
      int flags = UserInfo.FLAG_PRIMARY | UserInfo.FLAG_ADMIN;

      userSerialNumbers.put(id, (long) id);
      // Start the user as shut down.
      userState.put(id, UserState.STATE_SHUTDOWN);

      // Update UserInfo regardless if was added or not
      userInfoMap.put(id, new UserInfo(id, name, flags));
      userProfilesListMap.put(id, new ArrayList<>());
      // getUserProfiles() includes user's handle
      userProfilesListMap.get(id).add(new UserHandle(id));
      userPidMap.put(id, Process.myUid());
    }
  }

  @Implementation
  protected void __constructor__(Context context, IUserManager service) {
    this.context = context;
    invokeConstructor(
        UserManager.class,
        realObject,
        from(Context.class, context),
        from(IUserManager.class, service));

    userManagerState = ShadowApplication.getInstance().getUserManagerState();
  }

  /**
   * Compared to real Android, there is no check that the package name matches the application
   * package name and the method returns instantly.
   *
   * @see #setApplicationRestrictions(String, Bundle)
   */
  @Implementation(minSdk = JELLY_BEAN_MR2)
  protected Bundle getApplicationRestrictions(String packageName) {
    Bundle bundle = userManagerState.applicationRestrictions.get(packageName);
    return bundle != null ? bundle : new Bundle();
  }

  /** Sets the value returned by {@link UserManager#getApplicationRestrictions(String)}. */
  public void setApplicationRestrictions(String packageName, Bundle restrictions) {
    userManagerState.applicationRestrictions.put(packageName, restrictions);
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
    return userManagerState.userSerialNumbers.get(userHandle.getIdentifier());
  }

  @Implementation(minSdk = LOLLIPOP)
  protected List<UserHandle> getUserProfiles() {
    ImmutableList.Builder<UserHandle> builder = new ImmutableList.Builder<>();
    List<UserHandle> profiles = userManagerState.userProfilesListMap.get(UserHandle.myUserId());
    if (profiles != null) {
      return builder.addAll(profiles).build();
    }
    for (List<UserHandle> profileList : userManagerState.userProfilesListMap.values()) {
      if (profileList.contains(Process.myUserHandle())) {
        return builder.addAll(profileList).build();
      }
    }
    return ImmutableList.of(Process.myUserHandle());
  }

  /**
   * If any profiles have been added using {@link #addProfile}, return those profiles.
   *
   * <p>Otherwise follow real android behaviour.
   */
  @Implementation(minSdk = LOLLIPOP)
  protected List<UserInfo> getProfiles(int userHandle) {
    if (userManagerState.userProfilesListMap.containsKey(userHandle)) {
      ArrayList<UserInfo> infos = new ArrayList<>();
      for (UserHandle profileHandle : userManagerState.userProfilesListMap.get(userHandle)) {
        infos.add(userManagerState.userInfoMap.get(profileHandle.getIdentifier()));
      }
      return infos;
    }
    return reflector(UserManagerReflector.class, realObject).getProfiles(userHandle);
  }

  @Implementation(minSdk = R)
  protected List<UserHandle> getEnabledProfiles() {
    ArrayList<UserHandle> userHandles = new ArrayList<>();
    for (UserHandle profileHandle : getAllProfiles()) {
      if (userManagerState.userInfoMap.get(profileHandle.getIdentifier()).isEnabled()) {
        userHandles.add(profileHandle);
      }
    }

    return userHandles;
  }

  @Implementation(minSdk = R)
  protected List<UserHandle> getAllProfiles() {
    ArrayList<UserHandle> userHandles = new ArrayList<>();
    if (userManagerState.userProfilesListMap.containsKey(context.getUserId())) {
      userHandles.addAll(userManagerState.userProfilesListMap.get(context.getUserId()));
      return userHandles;
    }

    userHandles.add(UserHandle.of(context.getUserId()));
    return userHandles;
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

  @Implementation(minSdk = R)
  protected UserHandle createProfile(String name, String userType, Set<String> disallowedPackages) {
    int flags = getDefaultUserTypeFlags(userType);
    flags |= FLAG_PROFILE; // assume createProfile used with a profile userType
    if (enforcePermissions && !hasManageUsersPermission() && !hasCreateUsersPermission()) {
      throw new SecurityException(
          "You either need MANAGE_USERS or CREATE_USERS "
              + "permission to create an user with flags: "
              + flags);
    }

    if (userManagerState.userInfoMap.size() >= getMaxSupportedUsers()) {
      return null;
    }

    int profileId = userManagerState.nextUserId++;
    addProfile(context.getUserId(), profileId, name, flags);
    userManagerState.userInfoMap.get(profileId).userType = userType;
    return UserHandle.of(profileId);
  }

  private static int getDefaultUserTypeFlags(String userType) {
    switch (userType) {
      case UserManager.USER_TYPE_PROFILE_MANAGED:
        return FLAG_PROFILE | FLAG_MANAGED_PROFILE;
      case UserManager.USER_TYPE_FULL_SECONDARY:
        return FLAG_FULL;
      case UserManager.USER_TYPE_FULL_GUEST:
        return FLAG_FULL | FLAG_GUEST;
      case UserManager.USER_TYPE_FULL_DEMO:
        return FLAG_FULL | FLAG_DEMO;
      case UserManager.USER_TYPE_FULL_RESTRICTED:
        return FLAG_FULL | FLAG_RESTRICTED;
      case UserManager.USER_TYPE_FULL_SYSTEM:
        return FLAG_FULL | FLAG_SYSTEM;
      case UserManager.USER_TYPE_SYSTEM_HEADLESS:
        return FLAG_SYSTEM;
      default:
        return 0;
    }
  }

  /** Add a profile to be returned by {@link #getProfiles(int)}.* */
  public void addProfile(
      int userHandle, int profileUserHandle, String profileName, int profileFlags) {
    // Don't override serial number set by setSerialNumberForUser()
    if (!userManagerState.userSerialNumbers.containsKey(profileUserHandle)) {
      // use UserHandle id as serial number unless setSerialNumberForUser() is used
      userManagerState.userSerialNumbers.put(profileUserHandle, (long) profileUserHandle);
    }
    UserInfo profileUserInfo = new UserInfo(profileUserHandle, profileName, profileFlags);
    if (RuntimeEnvironment.getApiLevel() >= LOLLIPOP) {
      profileUserInfo.profileGroupId = userHandle;
      UserInfo parentUserInfo = getUserInfo(userHandle);
      if (parentUserInfo != null) {
        parentUserInfo.profileGroupId = userHandle;
      }
    }
    userManagerState.userInfoMap.put(profileUserHandle, profileUserInfo);
    // Insert profile to the belonging user's userProfilesList
    userManagerState.userProfilesListMap.putIfAbsent(userHandle, new ArrayList<>());
    List<UserHandle> list = userManagerState.userProfilesListMap.get(userHandle);
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
    UserState state = userManagerState.userState.get(handle.getIdentifier());

    return state == UserState.STATE_RUNNING_UNLOCKED;
  }

  /**
   * If permissions are enforced (see {@link #enforcePermissionChecks(boolean)}) and the application
   * doesn't have the {@link android.Manifest.permission#MANAGE_USERS} permission, throws a {@link
   * SecurityManager} exception.
   *
   * @return false by default, or the value specified via {@link #setManagedProfile(boolean)}
   * @see #enforcePermissionChecks(boolean)
   * @see #setManagedProfile(boolean)
   */
  @Implementation(minSdk = LOLLIPOP)
  protected boolean isManagedProfile() {
    if (enforcePermissions && !hasManageUsersPermission()) {
      throw new SecurityException(
          "You need MANAGE_USERS permission to: check if specified user a "
              + "managed profile outside your profile group");
    }

    if (managedProfile != null) {
      return managedProfile;
    }

    if (RuntimeEnvironment.getApiLevel() >= R) {
      return isManagedProfile(context.getUserId());
    }

    return false;
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

  /** Setter for {@link UserManager#isManagedProfile()}. */
  public void setManagedProfile(boolean managedProfile) {
    this.managedProfile = managedProfile;
  }

  @Implementation(minSdk = R)
  protected boolean isProfile() {
    if (enforcePermissions && !hasManageUsersPermission()) {
      throw new SecurityException(
          "You need INTERACT_ACROSS_USERS or MANAGE_USERS permission to: check isProfile");
    }

    return getUserInfo(context.getUserId()).isProfile();
  }

  @Implementation(minSdk = R)
  protected boolean isUserOfType(String userType) {
    if (enforcePermissions && !hasManageUsersPermission()) {
      throw new SecurityException("You need MANAGE_USERS permission to: check user type");
    }

    UserInfo info = getUserInfo(context.getUserId());
    return info != null && info.userType != null && info.userType.equals(userType);
  }

  @Implementation(minSdk = R)
  protected boolean isSameProfileGroup(UserHandle user, UserHandle otherUser) {
    if (enforcePermissions && !hasManageUsersPermission()) {
      throw new SecurityException(
          "You need MANAGE_USERS permission to: check if in the same profile group");
    }

    UserInfo userInfo = userManagerState.userInfoMap.get(user.getIdentifier());
    UserInfo otherUserInfo = userManagerState.userInfoMap.get(otherUser.getIdentifier());

    if (userInfo == null
        || otherUserInfo == null
        || userInfo.profileGroupId == UserInfo.NO_PROFILE_GROUP_ID
        || otherUserInfo.profileGroupId == UserInfo.NO_PROFILE_GROUP_ID) {
      return false;
    }

    return userInfo.profileGroupId == otherUserInfo.profileGroupId;
  }

  @Implementation(minSdk = LOLLIPOP)
  protected boolean hasUserRestriction(String restrictionKey, UserHandle userHandle) {
    Bundle bundle = userManagerState.userRestrictions.get(userHandle.getIdentifier());
    return bundle != null && bundle.getBoolean(restrictionKey);
  }

  /**
   * Shadows UserManager.setUserRestriction() API. This allows UserManager.hasUserRestriction() to
   * return meaningful results in test environment; thus, allowing test to verify the invoking of
   * UserManager.setUserRestriction().
   */
  @Implementation(minSdk = JELLY_BEAN_MR2)
  protected void setUserRestriction(String key, boolean value, UserHandle userHandle) {
    Bundle bundle = getUserRestrictionsForUser(userHandle);
    bundle.putBoolean(key, value);
  }

  @Implementation(minSdk = JELLY_BEAN_MR2)
  protected void setUserRestriction(String key, boolean value) {
    setUserRestriction(key, value, Process.myUserHandle());
  }

  /**
   * @deprecated When possible, please use the real Android framework API {@link
   *     UserManager#setUserRestriction()}.
   */
  @Deprecated
  public void setUserRestriction(UserHandle userHandle, String restrictionKey, boolean value) {
    setUserRestriction(restrictionKey, value, userHandle);
  }

  /** Removes all user restrictions set of a user identified by {@code userHandle}. */
  public void clearUserRestrictions(UserHandle userHandle) {
    userManagerState.userRestrictions.remove(userHandle.getIdentifier());
  }

  @Implementation(minSdk = JELLY_BEAN_MR2)
  protected Bundle getUserRestrictions(UserHandle userHandle) {
    return new Bundle(getUserRestrictionsForUser(userHandle));
  }

  private Bundle getUserRestrictionsForUser(UserHandle userHandle) {
    Bundle bundle = userManagerState.userRestrictions.get(userHandle.getIdentifier());
    if (bundle == null) {
      bundle = new Bundle();
      userManagerState.userRestrictions.put(userHandle.getIdentifier(), bundle);
    }
    return bundle;
  }

  /**
   * @see #addProfile(int, int, String, int)
   * @see #addUser(int, String, int)
   */
  @Implementation
  protected long getSerialNumberForUser(UserHandle userHandle) {
    Long result = userManagerState.userSerialNumbers.get(userHandle.getIdentifier());
    return result == null ? -1L : result;
  }

  /**
   * {@link #addUser} uses UserHandle for serialNumber. setSerialNumberForUser() allows assigning an
   * arbitary serialNumber. Some test use serialNumber!=0 as secondary user check, so it's necessary
   * to "fake" the serialNumber to a non-zero value.
   */
  public void setSerialNumberForUser(UserHandle userHandle, long serialNumber) {
    userManagerState.userSerialNumbers.put(userHandle.getIdentifier(), serialNumber);
  }

  /**
   * @see #addProfile(int, int, String, int)
   * @see #addUser(int, String, int)
   */
  @Implementation
  protected UserHandle getUserForSerialNumber(long serialNumber) {
    Integer userHandle = userManagerState.userSerialNumbers.inverse().get(serialNumber);
    return userHandle == null ? null : new UserHandle(userHandle);
  }

  /**
   * @see #addProfile(int, int, String, int)
   * @see #addUser(int, String, int)
   */
  @Implementation
  protected int getUserSerialNumber(@UserIdInt int userHandle) {
    Long result = userManagerState.userSerialNumbers.get(userHandle);
    return result != null ? result.intValue() : -1;
  }

  private String getUserName(@UserIdInt int userHandle) {
    UserInfo user = getUserInfo(userHandle);
    return user == null ? "" : user.name;
  }

  /**
   * Returns the name of the user.
   *
   * <p>On real Android, if a UserHandle.USER_SYSTEM user is found but does not have a name, it will
   * return a name like "Owner". In Robolectric, the USER_SYSTEM user always has a name.
   */
  @Implementation(minSdk = Q)
  protected String getUserName() {
    if (RuntimeEnvironment.getApiLevel() >= R) {
      return getUserName(context.getUserId());
    }

    return getUserName(UserHandle.myUserId());
  }

  @Implementation(minSdk = R)
  protected void setUserName(String name) {
    if (enforcePermissions && !hasManageUsersPermission()) {
      throw new SecurityException("You need MANAGE_USERS permission to: rename users");
    }
    UserInfo user = getUserInfo(context.getUserId());
    user.name = name;
  }

  @Implementation(minSdk = Q)
  protected Bitmap getUserIcon() {
    if (enforcePermissions
        && !hasManageUsersPermission()
        && !hasGetAccountsPrivilegedPermission()) {
      throw new SecurityException(
          "You need MANAGE_USERS or GET_ACCOUNTS_PRIVILEGED permissions to: get user icon");
    }

    int userId = UserHandle.myUserId();
    if (RuntimeEnvironment.getApiLevel() >= R) {
      userId = context.getUserId();
    }

    return userManagerState.userIcon.get(userId);
  }

  @Implementation(minSdk = Q)
  protected void setUserIcon(Bitmap icon) {
    if (enforcePermissions && !hasManageUsersPermission()) {
      throw new SecurityException("You need MANAGE_USERS permission to: update users");
    }

    int userId = UserHandle.myUserId();
    if (RuntimeEnvironment.getApiLevel() >= R) {
      userId = context.getUserId();
    }

    userManagerState.userIcon.put(userId, icon);
  }

  /** @return user id for given user serial number. */
  @HiddenApi
  @Implementation(minSdk = JELLY_BEAN_MR1)
  @UserIdInt
  protected int getUserHandle(int serialNumber) {
    Integer userHandle = userManagerState.userSerialNumbers.inverse().get((long) serialNumber);
    return userHandle == null ? -1 : userHandle;
  }

  @HiddenApi
  @Implementation(minSdk = JELLY_BEAN_MR1)
  protected static int getMaxSupportedUsers() {
    return maxSupportedUsers;
  }

  public void setMaxSupportedUsers(int maxSupportedUsers) {
    ShadowUserManager.maxSupportedUsers = maxSupportedUsers;
  }

  private boolean hasManageUsersPermission() {
    return context
            .getPackageManager()
            .checkPermission(permission.MANAGE_USERS, context.getPackageName())
        == PackageManager.PERMISSION_GRANTED;
  }

  private boolean hasCreateUsersPermission() {
    return context
            .getPackageManager()
            .checkPermission(permission.CREATE_USERS, context.getPackageName())
        == PackageManager.PERMISSION_GRANTED;
  }

  private boolean hasModifyQuietModePermission() {
    return context
            .getPackageManager()
            .checkPermission(permission.MODIFY_QUIET_MODE, context.getPackageName())
        == PackageManager.PERMISSION_GRANTED;
  }

  private boolean hasGetAccountsPrivilegedPermission() {
    return context
            .getPackageManager()
            .checkPermission(permission.GET_ACCOUNTS_PRIVILEGED, context.getPackageName())
        == PackageManager.PERMISSION_GRANTED;
  }

  private void checkPermissions() {
    // TODO Ensure permisions
    //              throw new SecurityException("You need INTERACT_ACROSS_USERS or MANAGE_USERS
    // permission "
    //                + "to: check " + name);throw new SecurityException();
  }

  /** @return false by default, or the value specified via {@link #setIsDemoUser(boolean)} */
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

  /** @return 'true' by default, or the value specified via {@link #setIsSystemUser(boolean)} */
  @Implementation(minSdk = M)
  protected boolean isSystemUser() {
    if (isSystemUser == false) {
      return false;
    } else {
      return reflector(UserManagerReflector.class, realObject).isSystemUser();
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

  /** @return 'false' by default, or the value specified via {@link #setIsLinkedUser(boolean)} */
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
   *
   * @deprecated use {@link ShadowUserManager#addUser()} instead
   */
  @Deprecated
  public void setIsRestrictedProfile(boolean isRestrictedProfile) {
    if (RuntimeEnvironment.getApiLevel() >= R) {
      setUserType(isRestrictedProfile ? USER_TYPE_FULL_RESTRICTED : USER_TYPE_FULL_SECONDARY);
      return;
    }
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
    if (RuntimeEnvironment.getApiLevel() >= R) {
      setUserType(isGuestUser ? USER_TYPE_FULL_GUEST : USER_TYPE_FULL_SECONDARY);
      return;
    }
    UserInfo userInfo = getUserInfo(UserHandle.myUserId());
    if (isGuestUser) {
      userInfo.flags |= UserInfo.FLAG_GUEST;
    } else {
      userInfo.flags &= ~UserInfo.FLAG_GUEST;
    }
  }

  public void setIsUserEnabled(int userId, boolean enabled) {
    UserInfo userInfo = getUserInfo(userId);
    if (enabled) {
      userInfo.flags &= ~UserInfo.FLAG_DISABLED;
    } else {
      userInfo.flags |= UserInfo.FLAG_DISABLED;
    }
  }

  /** @see #setUserState(UserHandle, UserState) */
  @Implementation
  protected boolean isUserRunning(UserHandle handle) {
    checkPermissions();
    UserState state = userManagerState.userState.get(handle.getIdentifier());

    if (state == UserState.STATE_RUNNING_LOCKED
        || state == UserState.STATE_RUNNING_UNLOCKED
        || state == UserState.STATE_RUNNING_UNLOCKING) {
      return true;
    } else {
      return false;
    }
  }

  /** @see #setUserState(UserHandle, UserState) */
  @Implementation
  protected boolean isUserRunningOrStopping(UserHandle handle) {
    checkPermissions();
    UserState state = userManagerState.userState.get(handle.getIdentifier());

    if (state == UserState.STATE_RUNNING_LOCKED
        || state == UserState.STATE_RUNNING_UNLOCKED
        || state == UserState.STATE_RUNNING_UNLOCKING
        || state == UserState.STATE_STOPPING) {
      return true;
    } else {
      return false;
    }
  }

  /** @see #setUserState(UserHandle, UserState) */
  @Implementation(minSdk = R)
  protected boolean isUserUnlockingOrUnlocked(UserHandle handle) {
    checkPermissions();
    UserState state = userManagerState.userState.get(handle.getIdentifier());

    return state == UserState.STATE_RUNNING_UNLOCKING || state == UserState.STATE_RUNNING_UNLOCKED;
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
   * Sets the current state for a given user, see {@link UserManager#isUserRunning(UserHandle)} and
   * {@link UserManager#isUserRunningOrStopping(UserHandle)}
   */
  public void setUserState(UserHandle handle, UserState state) {
    userManagerState.userState.put(handle.getIdentifier(), state);
  }

  /**
   * Query whether the quiet mode is enabled for a managed profile.
   *
   * <p>This method checks whether the user handle corresponds to a managed profile, and then query
   * its state. When quiet, the user is not running.
   */
  @Implementation(minSdk = N)
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
   * <p>This will succeed unless {@link #setProfileIsLocked(UserHandle, boolean)} is called with
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
      userManagerState.userState.put(userProfileHandle, UserState.STATE_SHUTDOWN);
      info.flags |= UserInfo.FLAG_QUIET_MODE;
    } else {
      if (userManagerState.profileIsLocked.getOrDefault(userProfileHandle, false)) {
        return false;
      }
      userManagerState.userState.put(userProfileHandle, UserState.STATE_RUNNING_UNLOCKED);
      info.flags &= ~UserInfo.FLAG_QUIET_MODE;
    }

    if (enableQuietMode) {
      sendQuietModeBroadcast(Intent.ACTION_MANAGED_PROFILE_UNAVAILABLE, userHandle);
    } else {
      sendQuietModeBroadcast(Intent.ACTION_MANAGED_PROFILE_AVAILABLE, userHandle);
      sendQuietModeBroadcast(Intent.ACTION_MANAGED_PROFILE_UNLOCKED, userHandle);
    }

    return true;
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
    userManagerState.profileIsLocked.put(profileHandle.getIdentifier(), isLocked);
  }

  @Implementation(minSdk = Build.VERSION_CODES.N)
  protected long[] getSerialNumbersOfUsers(boolean excludeDying) {
    return getUsers().stream()
        .map(userInfo -> getUserSerialNumber(userInfo.getUserHandle().getIdentifier()))
        .mapToLong(l -> l)
        .toArray();
  }

  @Implementation
  protected List<UserInfo> getUsers() {
    return new ArrayList<>(userManagerState.userInfoMap.values());
  }

  @Implementation
  protected UserInfo getUserInfo(int userHandle) {
    return userManagerState.userInfoMap.get(userHandle);
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

  @Implementation(minSdk = Build.VERSION_CODES.N)
  protected String getSeedAccountName() {
    return seedAccountName;
  }

  /** Setter for {@link UserManager#getSeedAccountName()} */
  public void setSeedAccountName(String seedAccountName) {
    this.seedAccountName = seedAccountName;
  }

  @Implementation(minSdk = Build.VERSION_CODES.N)
  protected String getSeedAccountType() {
    return seedAccountType;
  }

  /** Setter for {@link UserManager#getSeedAccountType()} */
  public void setSeedAccountType(String seedAccountType) {
    this.seedAccountType = seedAccountType;
  }

  @Implementation(minSdk = Build.VERSION_CODES.N)
  protected PersistableBundle getSeedAccountOptions() {
    return seedAccountOptions;
  }

  /** Setter for {@link UserManager#getSeedAccountOptions()} */
  public void setSeedAccountOptions(PersistableBundle seedAccountOptions) {
    this.seedAccountOptions = seedAccountOptions;
  }

  @Implementation(minSdk = Build.VERSION_CODES.N)
  protected void clearSeedAccountData() {
    seedAccountName = null;
    seedAccountType = null;
    seedAccountOptions = null;
  }

  @Implementation(minSdk = JELLY_BEAN_MR1)
  protected boolean removeUser(int userHandle) {
    userManagerState.userInfoMap.remove(userHandle);
    userManagerState.userPidMap.remove(userHandle);
    userManagerState.userSerialNumbers.remove(userHandle);
    userManagerState.userState.remove(userHandle);
    userManagerState.userRestrictions.remove(userHandle);
    userManagerState.profileIsLocked.remove(userHandle);
    userManagerState.userIcon.remove(userHandle);
    userManagerState.userProfilesListMap.remove(userHandle);
    // if it's a profile, remove from the belong list in userManagerState.userProfilesListMap
    UserHandle profileHandle = new UserHandle(userHandle);
    for (List<UserHandle> list : userManagerState.userProfilesListMap.values()) {
      if (list.remove(profileHandle)) {
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
    if (!userManagerState.userInfoMap.containsKey(userId)) {
      throw new UnsupportedOperationException("Must add user before switching to it");
    }

    ShadowProcess.setUid(userManagerState.userPidMap.get(userId));

    Application application = (Application) context.getApplicationContext();
    ShadowContextImpl shadowContext = Shadow.extract(application.getBaseContext());
    shadowContext.setUserId(userId);

    if (RuntimeEnvironment.getApiLevel() >= R) {
      reflector(UserManagerReflector.class, realObject).setUserId(userId);
    }
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
    if (!userManagerState.userSerialNumbers.containsKey(id)) {
      // use UserHandle id as serial number unless setSerialNumberForUser() is used
      userManagerState.userSerialNumbers.put(id, (long) id);
    }
    // Start the user as shut down.
    userManagerState.userState.put(id, UserState.STATE_SHUTDOWN);

    // Update UserInfo regardless if was added or not
    userManagerState.userInfoMap.put(id, new UserInfo(id, name, flags));
    if (!userManagerState.userProfilesListMap.containsKey(id)) {
      userManagerState.userProfilesListMap.put(id, new ArrayList<>());
      // getUserProfiles() includes user's handle
      userManagerState.userProfilesListMap.get(id).add(new UserHandle(id));
      userManagerState.userPidMap.put(
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

  @Implementation(minSdk = R)
  protected boolean hasUserRestrictionForUser(String restrictionKey, UserHandle userHandle) {
    return hasUserRestriction(restrictionKey, userHandle);
  }

  private void setUserType(String userType) {
    UserInfo userInfo = getUserInfo(UserHandle.myUserId());
    userInfo.userType = userType;
  }

  /**
   * Request the quiet mode.
   *
   * <p>If {@link #setProfileIsLocked(UserHandle, boolean)} is called with {@code true} for the
   * managed profile a request to disable the quiet mode will fail and return {@code false} (i.e. as
   * if the user refused to authenticate). Otherwise, the call will always succeed and return {@code
   * true}.
   *
   * <p>This method simply re-directs to {@link ShadowUserManager#requestQuietModeEnabled(boolean,
   * UserHandle)} as it already has the desired behavior irrespective of the flag's value.
   */
  @Implementation(minSdk = R)
  protected boolean requestQuietModeEnabled(
      boolean enableQuietMode, UserHandle userHandle, int flags) {
    return requestQuietModeEnabled(enableQuietMode, userHandle);
  }

  @Implementation(minSdk = TIRAMISU)
  protected Bundle getUserRestrictions() {
    return getUserRestrictions(UserHandle.getUserHandleForUid(Process.myUid()));
  }

  @Implementation(minSdk = TIRAMISU)
  protected boolean hasUserRestrictionForUser(String restrictionKey, int userId) {
    Bundle bundle = getUserRestrictions(UserHandle.getUserHandleForUid(userId));
    return bundle != null && bundle.getBoolean(restrictionKey);
  }

  @Resetter
  public static void reset() {
    maxSupportedUsers = DEFAULT_MAX_SUPPORTED_USERS;
    isMultiUserSupported = false;
  }

  @ForType(UserManager.class)
  interface UserManagerReflector {

    @Direct
    List getProfiles(int userHandle);

    @Direct
    boolean isSystemUser();

    @Accessor("mUserId")
    void setUserId(int userId);
  }
}
