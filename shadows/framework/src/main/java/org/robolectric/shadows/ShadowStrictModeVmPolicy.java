package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.os.Build;
import android.os.StrictMode;
import android.os.StrictMode.VmPolicy;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Static;

@Implements(value = StrictMode.VmPolicy.class, minSdk = Build.VERSION_CODES.P)
public class ShadowStrictModeVmPolicy {

  @Implementation
  protected static void __staticInitializer__() {
    Shadow.directInitialize(StrictMode.VmPolicy.class);

    if (RuntimeEnvironment.getApiLevel() >= Build.VERSION_CODES.P) {
      // if VmPolicy was referenced first, sVmPolicy won't be set properly. So force a
      // re-initialization.
      reflector(_StrictMode_.class).setVmPolicy(VmPolicy.LAX);
    }
  }

  /** Accessor interface for {@link StrictMode}'s internals. */
  @ForType(StrictMode.class)
  private interface _StrictMode_ {
    @Static
    @Accessor("sVmPolicy")
    void setVmPolicy(VmPolicy vmPolicy);
  }
}
