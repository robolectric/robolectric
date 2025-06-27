package org.robolectric.shadows;

import static java.util.Objects.requireNonNull;
import static org.robolectric.shadow.api.Shadow.invokeConstructor;

import android.annotation.CallbackExecutor;
import android.app.role.IRoleManager;
import android.app.role.OnRoleHoldersChangedListener;
import android.app.role.RoleManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.UserHandle;
import com.android.internal.util.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.ReflectionHelpers;

/** A shadow implementation of {@link android.app.role.RoleManager}. */
@Implements(value = RoleManager.class, minSdk = Build.VERSION_CODES.Q)
public class ShadowRoleManager {

  @RealObject protected RoleManager roleManager;

  // See RoleService implementation
  private static final ImmutableSet<String> DEFAULT_APPLICATION_ROLES =
      ImmutableSet.of(
          RoleManager.ROLE_ASSISTANT,
          RoleManager.ROLE_BROWSER,
          RoleManager.ROLE_CALL_REDIRECTION,
          RoleManager.ROLE_CALL_SCREENING,
          RoleManager.ROLE_DIALER,
          RoleManager.ROLE_HOME,
          RoleManager.ROLE_SMS);

  private Context context;

  @Implementation(maxSdk = Build.VERSION_CODES.R)
  protected void __constructor__(Context context) {
    this.context = context;
    invokeConstructor(
        RoleManager.class,
        roleManager,
        ReflectionHelpers.ClassParameter.from(Context.class, context));
  }

  @Implementation(minSdk = Build.VERSION_CODES.S)
  protected void __constructor__(Context context, IRoleManager service) {
    this.context = context;
    invokeConstructor(
        RoleManager.class,
        roleManager,
        ReflectionHelpers.ClassParameter.from(Context.class, context),
        ReflectionHelpers.ClassParameter.from(IRoleManager.class, service));
  }

  private static final Map<UserHandle, RoleUserState> userStates = new HashMap<>();

  private static class RoleUserState {
    final Map<String, Set<String>> roleHolders = new HashMap<>();
    final Map<OnRoleHoldersChangedListener, Executor> roleHoldersListener = new HashMap<>();
    final UserHandle user;

    RoleUserState(UserHandle u) {
      user = u;
    }

    void addRoleHolder(String roleName, String roleHolder) {
      Set<String> holders = roleHolders.computeIfAbsent(roleName, (String k) -> new HashSet<>());
      if (!roleHolder.isEmpty()) {
        holders.add(roleHolder);
      }

      broadcastRoleHoldersChanged(roleName);
    }

    void removeRoleHolder(String roleName, String roleHolder) {
      Preconditions.checkArgument(
          roleHolders.get(roleName) != null, "the supplied roleName was never added for this user");
      Preconditions.checkArgument(
          roleHolder.isEmpty() || roleHolders.get(roleName).contains(roleHolder),
          "the supplied roleHolder does not hold this role for this user.");

      if (!roleHolder.isEmpty()) {
        roleHolders.get(roleName).remove(roleHolder);
      }

      if (roleHolders.get(roleName).isEmpty()) {
        roleHolders.remove(roleName);
      }

      broadcastRoleHoldersChanged(roleName);
    }

    private void broadcastRoleHoldersChanged(String roleName) {
      roleHoldersListener.forEach(
          (listener, executor) ->
              executor.execute(() -> listener.onRoleHoldersChanged(roleName, user)));

      getUserState(UserHandle.ALL)
          .roleHoldersListener
          .forEach(
              (listener, executor) ->
                  executor.execute(() -> listener.onRoleHoldersChanged(roleName, user)));
    }

    static RoleUserState getUserState(UserHandle u) {
      return userStates.computeIfAbsent(u, RoleUserState::new);
    }
  }

  /**
   * Add a role that would be held by the given {@code roleHolder} app for the specified user.
   *
   * <p>This method makes the role available as well.
   */
  public static void addRoleHolder(
      @Nonnull String roleName, @Nonnull String roleHolder, @Nonnull UserHandle user) {
    Preconditions.checkStringNotEmpty(roleName, "roleName cannot be null or empty");
    requireNonNull(roleHolder);
    requireNonNull(user);
    Preconditions.checkArgument(user.getIdentifier() >= UserHandle.USER_SYSTEM, "Invalid user");

    RoleUserState.getUserState(user).addRoleHolder(roleName, roleHolder);
  }

  /**
   * Remove a role that would be held by the given {@code roleHolder} app for the specified user.
   *
   * <p>This method makes the role unavailable if no other role holders remain.
   */
  public static void removeRoleHolder(
      @Nonnull String roleName, @Nonnull String roleHolder, @Nonnull UserHandle user) {
    Preconditions.checkStringNotEmpty(roleName, "roleName cannot be null or empty");
    requireNonNull(roleHolder);
    requireNonNull(user);
    Preconditions.checkArgument(user.getIdentifier() >= UserHandle.USER_SYSTEM, "Invalid user");
    Preconditions.checkArgument(
        RoleUserState.getUserState(user).roleHolders.containsKey(roleName),
        "the supplied roleName was never added.");

    RoleUserState.getUserState(user).removeRoleHolder(roleName, roleHolder);
  }

  /**
   * Add a role that would be held by the calling app when invoking {@link
   * RoleManager#isRoleHeld(String)}.
   *
   * <p>This method makes the role available as well.
   *
   * @deprecated - Please use {@link ShadowRoleManager#addRoleHolder}
   */
  @Deprecated
  public void addHeldRole(@Nonnull String roleName) {
    addRoleHolder(roleName, context.getPackageName(), context.getUser());
  }

  /**
   * Remove a role previously added via {@link #addRoleHolder(String, String, UserHandle)}.
   *
   * @deprecated - Please use {@link ShadowRoleManager#removeRoleHolder}
   */
  @Deprecated
  public void removeHeldRole(@Nonnull String roleName) {
    removeRoleHolder(roleName, context.getPackageName(), context.getUser());
  }

  /**
   * Add a role that will be recognized as available when invoking {@link
   * RoleManager#isRoleAvailable(String)}.
   *
   * @deprecated - Please use {@link ShadowRoleManager#addRoleHolder}
   */
  @Deprecated
  public void addAvailableRole(@Nonnull String roleName) {
    addRoleHolder(roleName, "", context.getUser());
  }

  /**
   * Remove a role previously added via {@link #addRoleHolder(String, String, UserHandle)}.
   *
   * @deprecated - Please use {@link ShadowRoleManager#removeRoleHolder}
   */
  @Deprecated
  public void removeAvailableRole(@Nonnull String roleName) {
    removeRoleHolder(roleName, "", context.getUser());
  }

  @Implementation
  protected boolean isRoleHeld(@Nonnull String roleName) {
    Preconditions.checkStringNotEmpty(roleName, "roleName cannot be null or empty");
    return RoleUserState.getUserState(context.getUser())
        .roleHolders
        .getOrDefault(roleName, ImmutableSet.of())
        .contains(context.getPackageName());
  }

  @Implementation
  protected boolean isRoleAvailable(@Nonnull String roleName) {
    Preconditions.checkStringNotEmpty(roleName, "roleName cannot be null or empty");
    return RoleUserState.getUserState(context.getUser()).roleHolders.containsKey(roleName);
  }

  @Nullable
  @Implementation(minSdk = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
  protected String getDefaultApplication(@Nonnull String roleName) {
    return RoleUserState.getUserState(context.getUser())
        .roleHolders
        .getOrDefault(roleName, ImmutableSet.of())
        .stream()
        .findFirst()
        .orElse(null);
  }

  @Implementation(minSdk = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
  protected void setDefaultApplication(
      @Nonnull String roleName,
      @Nullable String packageName,
      int flags,
      @Nonnull Executor executor,
      @Nonnull Consumer<Boolean> callback) {
    Preconditions.checkArgument(
        DEFAULT_APPLICATION_ROLES.contains(roleName),
        "the supplied roleName in not a default app.");
    try {
      context.getPackageManager().getPackageInfo(packageName, 0);
      if (isRoleAvailable(roleName)) {
        RoleUserState.getUserState(context.getUser()).addRoleHolder(roleName, packageName);
        executor.execute(() -> callback.accept(true));
        return;
      }
    } catch (PackageManager.NameNotFoundException e) {
      // fall through to failure
    }
    executor.execute(() -> callback.accept(false));
  }

  @Implementation
  protected void addOnRoleHoldersChangedListenerAsUser(
      @CallbackExecutor @Nonnull Executor executor,
      @Nonnull OnRoleHoldersChangedListener listener,
      @Nonnull UserHandle user) {
    requireNonNull(executor);
    requireNonNull(listener);
    requireNonNull(user);

    RoleUserState.getUserState(user).roleHoldersListener.put(listener, executor);
  }

  @Implementation
  protected void removeOnRoleHoldersChangedListenerAsUser(
      @Nonnull OnRoleHoldersChangedListener listener, @Nonnull UserHandle user) {
    requireNonNull(listener);
    requireNonNull(user);

    RoleUserState.getUserState(user).roleHoldersListener.remove(listener);
  }

  @Implementation
  protected List<String> getRoleHolders(@Nonnull String roleName) {
    Preconditions.checkStringNotEmpty(roleName, "roleName cannot be null or empty");
    return getRoleHoldersAsUser(roleName, context.getUser());
  }

  @Implementation
  protected List<String> getRoleHoldersAsUser(@Nonnull String roleName, @Nonnull UserHandle user) {
    Preconditions.checkStringNotEmpty(roleName, "roleName cannot be null or empty");
    requireNonNull(user);

    return ImmutableList.copyOf(
        new ArrayList<>(
            RoleUserState.getUserState(user)
                .roleHolders
                .getOrDefault(roleName, ImmutableSet.of())));
  }

  @Resetter
  public static void reset() {
    userStates.clear();
  }
}
