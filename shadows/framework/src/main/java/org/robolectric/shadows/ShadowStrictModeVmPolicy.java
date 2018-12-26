package org.robolectric.shadows;

import android.os.Build;
import android.os.StrictMode;
import android.os.StrictMode.VmPolicy;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.util.ReflectionHelpers;

@Implements(value=StrictMode.VmPolicy.class, minSdk = Build.VERSION_CODES.P)
public class ShadowStrictModeVmPolicy {

  @Implementation
  protected static void __staticInitializer__() {
    ReflectionHelpers.callStaticMethod(StrictMode.VmPolicy.class, "__staticInitializer__");
    if (RuntimeEnvironment.getApiLevel() >= Build.VERSION_CODES.P) {
      // if VmPolicy was referenced first, sVmPolicy won't be set properly. So force a
      // re-initialization
      ReflectionHelpers.setStaticField(StrictMode.class, "sVmPolicy", VmPolicy.LAX);
    }
  }
}
