package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.text.format.DateUtils;
import org.robolectric.annotation.Implements;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Static;

/** Shadow for {@link DateUtils}. */
@Implements(value = DateUtils.class, isInAndroidSdk = false)
public class ShadowDateUtils {

  /**
   * internal only
   *
   * <p>Does not need to be a resetter method because Configuration at test startup.
   */
  public static void resetLastConfig() {
    reflector(DateUtilsReflector.class).setLastConfig(null);
  }

  @ForType(DateUtils.class)
  interface DateUtilsReflector {
    @Static
    @Accessor("sLastConfig")
    void setLastConfig(String lastConfig);
  }
}
