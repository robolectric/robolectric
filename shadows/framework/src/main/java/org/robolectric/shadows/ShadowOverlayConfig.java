package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.R;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.os.Process;
import com.android.internal.content.om.OverlayConfig;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Static;

/** Shadow for {@link OverlayConfig}. */
@Implements(value = OverlayConfig.class, minSdk = R, isInAndroidSdk = false)
public class ShadowOverlayConfig {

  @RealObject private OverlayConfig realOverlayConfig;

  /** Override to skip the check on pid == ROOT_PID */
  @Implementation
  protected static OverlayConfig getZygoteInstance() {
    int origUid = Process.myUid();
    ShadowProcess.setUid(0);
    OverlayConfig result = reflector(OverlayConfigReflector.class).getZygoteInstance();
    ShadowProcess.setUid(origUid);
    return result;
  }

  @Implementation
  protected String[] createImmutableFrameworkIdmapsInZygote() {
    int origUid = Process.myUid();
    ShadowProcess.setUid(0);
    String[] result =
        reflector(OverlayConfigReflector.class, realOverlayConfig)
            .createImmutableFrameworkIdmapsInZygote();
    ShadowProcess.setUid(origUid);
    return result;
  }

  @ForType(OverlayConfig.class)
  interface OverlayConfigReflector {

    @Static
    @Direct
    OverlayConfig getZygoteInstance();

    @Direct
    String[] createImmutableFrameworkIdmapsInZygote();
  }
}
