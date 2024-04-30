package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.compat.Compatibility;
import android.compat.annotation.ChangeId;
import android.os.Build.VERSION_CODES;
import com.google.common.annotations.VisibleForTesting;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Static;
import org.robolectric.versioning.AndroidVersions.U;

/** Shadow for {@link Compatability}. */
@Implements(value = Compatibility.class, isInAndroidSdk = false)
public class ShadowCompatibility {

  private static final long CALL_ACTIVITY_RESULT_BEFORE_RESUME = 78294732L;

  private static final long ENFORCE_EDGE_TO_EDGE = 309578419L;

  @RealObject protected static Compatibility realCompatibility;

  @Implementation(minSdk = VERSION_CODES.S_V2)
  protected static boolean isChangeEnabled(@ChangeId long changeId) {
    if (changeId == CALL_ACTIVITY_RESULT_BEFORE_RESUME) {
      return false;
    } else if (changeId == ENFORCE_EDGE_TO_EDGE) {
      int targetSdkVersion =
          RuntimeEnvironment.getApplication().getApplicationInfo().targetSdkVersion;
      return isEdgeToEdgeEnabled(targetSdkVersion);
    }
    return reflector(CompatibilityReflector.class).isChangeEnabled(changeId);
  }

  @VisibleForTesting
  static boolean isEdgeToEdgeEnabled(int targetSdkVersion) {
    // Edge-to-edge is enforced for apps that target Android V and above.
    return targetSdkVersion > U.SDK_INT;
  }

  /** Reflector interface for {@link Compatibility}'s isChangeEnabled function. */
  @ForType(Compatibility.class)
  private interface CompatibilityReflector {

    @Direct
    @Static
    boolean isChangeEnabled(@ChangeId long changeId);
  }
}
