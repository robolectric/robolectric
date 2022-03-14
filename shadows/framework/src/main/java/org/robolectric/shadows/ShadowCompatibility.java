package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.compat.Compatibility;
import android.compat.annotation.ChangeId;
import android.os.Build.VERSION_CODES;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Static;

/**
 * Robolectric shadow to disable CALL_ACTIVITY_RESULT_BEFORE_RESUME using Compatibility's
 * isChangeEnabled.
 */
@Implements(value = Compatibility.class, isInAndroidSdk = false)
public class ShadowCompatibility {

  private static final long CALL_ACTIVITY_RESULT_BEFORE_RESUME = 78294732L;

  @RealObject protected static Compatibility realCompatibility;

  @Implementation(minSdk = VERSION_CODES.S_V2)
  protected static boolean isChangeEnabled(@ChangeId long changeId) {
    if (changeId == CALL_ACTIVITY_RESULT_BEFORE_RESUME) {
      return false;
    }
    return reflector(CompatibilityReflector.class).isChangeEnabled(changeId);
  }

  /** Reflector interface for {@link Compatibility}'s isChangeEnabled function. */
  @ForType(Compatibility.class)
  private interface CompatibilityReflector {

    @Direct
    @Static
    boolean isChangeEnabled(@ChangeId long changeId);
  }
}
