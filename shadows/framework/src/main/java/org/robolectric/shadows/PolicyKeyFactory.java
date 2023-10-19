package org.robolectric.shadows;

import android.app.admin.NoArgsPolicyKey;
import android.app.admin.PolicyKey;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

/** Factory for {@link PolicyKey} */
public class PolicyKeyFactory {
  private PolicyKeyFactory() {}

  public static PolicyKey create(String identifier) {
    return ReflectionHelpers.callConstructor(
        NoArgsPolicyKey.class, ClassParameter.from(String.class, identifier));
  }
}
