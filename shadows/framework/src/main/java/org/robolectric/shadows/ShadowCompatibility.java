package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.R;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.compat.Compatibility;
import android.os.Build.VERSION_CODES;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Static;

/** Shadow for {@link Compatability}. */
@Implements(value = Compatibility.class, isInAndroidSdk = false, minSdk = R)
public class ShadowCompatibility {

  private static final long CALL_ACTIVITY_RESULT_BEFORE_RESUME = 78294732L;

  private static final long ENFORCE_EDGE_TO_EDGE = 309578419L;

  private static final long ENABLE_CHECKING_TELEPHONY_FEATURES_FOR_VCN = 330902016;

  private static final ImmutableMap<Long, Integer> ENABLED_SINCE_TARGET_SDK =
      ImmutableMap.of(
          ENFORCE_EDGE_TO_EDGE, 35,
          ENABLE_CHECKING_TELEPHONY_FEATURES_FOR_VCN, 35);

  @RealObject protected static Compatibility realCompatibility;

  @Implementation(minSdk = VERSION_CODES.S_V2)
  protected static boolean isChangeEnabled(long changeId) {
    if (changeId == CALL_ACTIVITY_RESULT_BEFORE_RESUME) {
      return false;
    } else if (ENABLED_SINCE_TARGET_SDK.containsKey(changeId)) {
      int targetSdkVersion = getTargetSdkVersion();
      return isEnabled(changeId, targetSdkVersion);
    }
    return reflector(CompatibilityReflector.class).isChangeEnabled(changeId);
  }

  @VisibleForTesting
  static boolean isEnabled(long flag, int targetSdkVersion) {
    int enabledSince = ENABLED_SINCE_TARGET_SDK.get(flag);
    return targetSdkVersion >= enabledSince;
  }

  private static int getTargetSdkVersion() {
    return RuntimeEnvironment.getApplication().getApplicationInfo().targetSdkVersion;
  }

  /** Reflector interface for {@link Compatibility}'s isChangeEnabled function. */
  @ForType(Compatibility.class)
  private interface CompatibilityReflector {

    @Direct
    @Static
    boolean isChangeEnabled(long changeId);
  }
}
