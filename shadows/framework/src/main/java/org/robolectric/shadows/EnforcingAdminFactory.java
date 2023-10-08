package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.app.admin.Authority;
import android.app.admin.EnforcingAdmin;
import android.os.UserHandle;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.Constructor;
import org.robolectric.util.reflector.ForType;

/** Factory for {@link EnforcingAdmin} */
public class EnforcingAdminFactory {
  private EnforcingAdminFactory() {}

  /**
   * Return an {@link EnforcingAdmin} which can enforce a certain policy
   *
   * @param userHandle The {@link UserHandle} on which the admin is installed on.
   * @param packageName The package name of the admin.
   * @param authority The {@link Authority} on which the admin is acting on, e.g. DPC, DeviceAdmin,
   *     etc..
   */
  public static EnforcingAdmin create(
      UserHandle userHandle, String packageName, Authority authority) {
    EnforcingAdmin enforcingAdmin = reflector(EnforcingAdminReflector.class).newEnforcingAdmin();
    reflector(EnforcingAdminReflector.class, enforcingAdmin).setUserHandle(userHandle);
    reflector(EnforcingAdminReflector.class, enforcingAdmin).setPackageName(packageName);
    reflector(EnforcingAdminReflector.class, enforcingAdmin).setAuthority(authority);
    return enforcingAdmin;
  }

  @ForType(EnforcingAdmin.class)
  private interface EnforcingAdminReflector {
    @Constructor
    EnforcingAdmin newEnforcingAdmin();

    @Accessor("mUserHandle")
    void setUserHandle(UserHandle userHandle);

    @Accessor("mPackageName")
    void setPackageName(String packageName);

    @Accessor("mAuthority")
    void setAuthority(Authority authority);
  }
}
