package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.app.admin.PolicyValue;
import android.app.admin.StringPolicyValue;
import org.robolectric.util.reflector.Constructor;
import org.robolectric.util.reflector.ForType;

/** A Factory class representing {@link StringPolicyValue} */
public class PolicyValueFactory {
  private PolicyValueFactory() {}

  /** Return a real instance of StringPolicyValue */
  public static PolicyValue<String> create() {
    return reflector(PolicyValueReflector.class).newStringPolicyValue();
  }

  @ForType(StringPolicyValue.class)
  private interface PolicyValueReflector {
    @Constructor
    PolicyValue<String> newStringPolicyValue();
  }
}
