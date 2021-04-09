package org.robolectric.shadows;

import android.app.role.RoleManager;
import android.os.Build;
import android.util.ArraySet;
import androidx.annotation.NonNull;
import com.android.internal.util.Preconditions;
import java.util.Set;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

/** A shadow implementation of {@link android.app.role.RoleManager}. */
@Implements(value = RoleManager.class, minSdk = Build.VERSION_CODES.Q)
public class ShadowRoleManager {

  @RealObject protected RoleManager roleManager;

  private final Set<String> heldRoles = new ArraySet<>();
  private final Set<String> availableRoles = new ArraySet<>();

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
    return heldRoles.contains(roleName);
  }

  /**
   * Add a role that would be held by the calling app when invoking {@link
   * RoleManager#isRoleHeld(String)}.
   */
  public void addHeldRole(@NonNull String roleName) {
    heldRoles.add(roleName);
  }

  /* Remove a role previously added via {@link #addHeldRole(String)}. */
  public void removeHeldRole(@NonNull String roleName) {
    Preconditions.checkArgument(
        heldRoles.contains(roleName), "the supplied roleName was never added.");
    heldRoles.remove(roleName);
  }

  /**
   * Check whether a particular role is available on the device.
   *
   * <p>Callers can add available roles via {@link #addAvailableRole(String)}
   *
   * @param roleName the name of the role to check for
   * @return whether the role is available
   */
  @Implementation
  protected boolean isRoleAvailable(@NonNull String roleName) {
    Preconditions.checkStringNotEmpty(roleName, "roleName cannot be null or empty");
    return availableRoles.contains(roleName);
  }

  /**
   * Add a role that will be recognized as available when invoking {@link
   * RoleManager#isRoleAvailable(String)}.
   */
  public void addAvailableRole(@NonNull String roleName) {
    Preconditions.checkStringNotEmpty(roleName, "roleName cannot be null or empty");
    availableRoles.add(roleName);
  }

  /* Remove a role previously added via {@link #addAvailableRole(String)}. */
  public void removeAvailableRole(@NonNull String roleName) {
    Preconditions.checkArgument(
        availableRoles.contains(roleName), "the supplied roleName was never added.");
    availableRoles.remove(roleName);
  }
}
