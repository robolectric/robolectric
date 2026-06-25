package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.BAKLAVA;
import static org.robolectric.RuntimeEnvironment.getApiLevel;
import static org.robolectric.util.reflector.Reflector.reflector;
import static org.robolectric.versioning.VersionCalculator.CINNAMON_BUN;

import android.content.Context;
import android.window.DesktopExperienceFlags;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Static;

/**
 * Shadow for DesktopExperienceFlags that clears static reference to Application.
 *
 * <p>This is needed to prevent memory leaks and potential test isolation issues.
 */
@Implements(value = DesktopExperienceFlags.class, minSdk = CINNAMON_BUN)
public class ShadowDesktopExperienceFlags {

  @Resetter
  public static void reset() {
    if (getApiLevel() > BAKLAVA) {
      reflector(DesktopExperienceFlagsReflector.class).setApplicationContext(null);
    }
  }

  @ForType(DesktopExperienceFlags.class)
  private interface DesktopExperienceFlagsReflector {
    @Accessor("sApplicationContext")
    @Static
    void setApplicationContext(Context context);
  }
}
