package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.app.admin.EnforcingAdmin;
import android.app.admin.PolicyState;
import android.app.admin.PolicyValue;
import java.util.LinkedHashMap;
import java.util.Map;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.Constructor;
import org.robolectric.util.reflector.ForType;

/** Builder for {@link PolicyState} */
public class PolicyStateBuilder {
  private Map<EnforcingAdmin, PolicyValue<?>> policiesSetByAdmins =
      new LinkedHashMap<EnforcingAdmin, PolicyValue<?>>();
  private PolicyValue<?> currentResolvedPolicy;

  private PolicyStateBuilder() {}

  private PolicyState<?> policyState = reflector(PolicyStateReflector.class).newPolicyState();

  public static PolicyStateBuilder newBuilder() {
    return new PolicyStateBuilder();
  }

  /** Set the policy state for the {@link EnforcingAdmin}. */
  public PolicyStateBuilder setPolicy(EnforcingAdmin enforcingAdmin, PolicyValue<?> value) {
    this.policiesSetByAdmins.put(enforcingAdmin, value);
    return this;
  }

  /** Set the current resolved policy value. */
  public PolicyStateBuilder setCurrentResolvedPolicy(PolicyValue<?> currentResolvedPolicy) {
    this.currentResolvedPolicy = currentResolvedPolicy;
    return this;
  }

  public PolicyState<?> build() {
    reflector(PolicyStateReflector.class, policyState).setPoliciesSetByAdmins(policiesSetByAdmins);
    reflector(PolicyStateReflector.class, policyState)
        .setCurrentResolvedPolicy(currentResolvedPolicy);
    return policyState;
  }

  @ForType(PolicyState.class)
  private interface PolicyStateReflector {
    @Constructor
    PolicyState<?> newPolicyState();

    @Accessor("mPoliciesSetByAdmins")
    void setPoliciesSetByAdmins(Map<EnforcingAdmin, PolicyValue<?>> policiesSetByAdmins);

    @Accessor("mCurrentResolvedPolicy")
    void setCurrentResolvedPolicy(PolicyValue<?> currentResolvedPolicy);
  }
}
