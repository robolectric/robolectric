package org.robolectric.shadows;

import static org.robolectric.shadow.api.Shadow.invokeConstructor;
import static org.robolectric.util.ReflectionHelpers.ClassParameter.from;

import android.annotation.NonNull;
import android.annotation.Nullable;
import android.app.role.IRoleManager;
import android.app.role.RoleManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import com.android.internal.util.Preconditions;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;

/** A shadow implementation of {@link android.app.role.RoleManager}. */
@Implements(value = RoleManager.class, minSdk = Build.VERSION_CODES.Q)
public class ShadowRoleManager {

  @RealObject protected RoleManager roleManager;

  // See RoleService implementation
  private static final String[] DEFAULT_APPLICATION_ROLES = {
    RoleManager.ROLE_ASSISTANT,
    RoleManager.ROLE_BROWSER,
    RoleManager.ROLE_CALL_REDIRECTION,
    RoleManager.ROLE_CALL_SCREENING,
    RoleManager.ROLE_DIALER,
    RoleManager.ROLE_HOME,
    RoleManager.ROLE_SMS,
  };

  private Context context;

  // Roles that exist but are currently unavailable have their value set to {@code null}.
  private static final Map<String, String> roleToHolder = new HashMap<>();

  @Implementation(maxSdk = Build.VERSION_CODES.R)
  protected void __constructor__(Context context) {
    this.context = context;
    invokeConstructor(RoleManager.class, roleManager, from(Context.class, context));
  }

  @Implementation(minSdk = Build.VERSION_CODES.S)
  protected void __constructor__(Context context, IRoleManager service) {
    this.context = context;
    invokeConstructor(
        RoleManager.class,
        roleManager,
        from(Context.class, context),
        from(IRoleManager.class, service));
  }

  /**
   * Check whether the calling application is holding a particular role.
   *
   * <p>Callers can add held roles via {@link #addHeldRole(String)}
   *
   * @param roleName the name of the role to check for
   * @return whether the calling application is holding the role
   */
  @Implementation
  protected boolean isRoleHeld(@NonNull String roleName) {
    Preconditions.checkStringNotEmpty(roleName, "roleName cannot be null or empty");
    return context.getPackageName().equals(roleToHolder.get(roleName));
  }

  /**
   * Add a role that would be held by the calling app when invoking {@link
   * RoleManager#isRoleHeld(String)}.
   *
   * <p>This method makes the role available as well.
   */
  public void addHeldRole(@NonNull String roleName) {
    addAvailableRole(roleName);
    roleToHolder.put(roleName, context.getPackageName());
  }

  /* Remove a role previously added via {@link #addHeldRole(String)}. */
  public void removeHeldRole(@NonNull String roleName) {
    Preconditions.checkArgument(isRoleHeld(roleName), "the supplied roleName was never added.");
    roleToHolder.put(roleName, null);
  }

  /**
   * Check whether a particular role is available on the device.
   *
   * <p>Ideally available roles would be autodetected based on the state of other services or
   * features present, but for now callers can add available roles via {@link
   * #addAvailableRole(String)}.
   *
   * @param roleName the name of the role to check for
   * @return whether the role is available
   */
  @Implementation
  protected boolean isRoleAvailable(@NonNull String roleName) {
    Preconditions.checkStringNotEmpty(roleName, "roleName cannot be null or empty");
    return roleToHolder.containsKey(roleName);
  }

  /**
   * Add a role that will be recognized as available when invoking {@link
   * RoleManager#isRoleAvailable(String)}.
   */
  public void addAvailableRole(@NonNull String roleName) {
    Preconditions.checkStringNotEmpty(roleName, "roleName cannot be null or empty");
    if (!isRoleAvailable(roleName)) {
      roleToHolder.put(roleName, null);
    }
  }

  /* Remove a role previously added via {@link #addAvailableRole(String)}. */
  public void removeAvailableRole(@NonNull String roleName) {
    Preconditions.checkArgument(
        roleToHolder.containsKey(roleName), "the supplied roleName was never added.");
    roleToHolder.remove(roleName);
  }

  @Nullable
  @Implementation(minSdk = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
  protected String getDefaultApplication(@NonNull String roleName) {
    return roleToHolder.get(roleName);
  }

  @Implementation(minSdk = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
  protected void setDefaultApplication(
      @NonNull String roleName,
      @Nullable String packageName,
      int flags,
      @NonNull Executor executor,
      @NonNull Consumer<Boolean> callback) {
    Preconditions.checkArgument(
        Arrays.asList(DEFAULT_APPLICATION_ROLES).contains(roleName),
        "the supplied roleName in not a default app.");
    try {
      context.getPackageManager().getPackageInfo(packageName, 0);
      if (isRoleAvailable(roleName)) {
        roleToHolder.put(roleName, packageName);
        executor.execute(() -> callback.accept(true));
        return;
      }
    } catch (PackageManager.NameNotFoundException e) {
      // fall through to failure
    }
    executor.execute(() -> callback.accept(false));
  }

  @Resetter
  public static void reset() {
    roleToHolder.clear();
  }
}
