package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.app.admin.DevicePolicyState;
import android.app.admin.PolicyKey;
import android.app.admin.PolicyState;
import android.os.UserHandle;
import java.util.Map;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.Constructor;
import org.robolectric.util.reflector.ForType;

/** Factory for {@link DevicePolicyState} */
public class DevicePolicyStateBuilder {
  private DevicePolicyStateBuilder() {}

  /** Return a real instance of {@link DevicePolicyState} */
  public static DevicePolicyState create(Map<UserHandle, Map<PolicyKey, PolicyState<?>>> policies) {
    DevicePolicyState devicePolicyState =
        reflector(DevicePolicyStateReflector.class).newDevicePolicyState();
    reflector(DevicePolicyStateReflector.class, devicePolicyState).setPolicy(policies);
    return devicePolicyState;
  }

  @ForType(DevicePolicyState.class)
  private interface DevicePolicyStateReflector {
    @Constructor
    DevicePolicyState newDevicePolicyState();

    @Accessor("mPolicies")
    void setPolicy(Map<UserHandle, Map<PolicyKey, PolicyState<?>>> policies);
  }
}
