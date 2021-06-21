package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.os.Build.VERSION_CODES;
import android.os.LocaleList;
import java.util.Locale;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Static;

/** Shadow for {@link android.os.LocaleList} */
@Implements(value = LocaleList.class, minSdk = VERSION_CODES.N)
public class ShadowLocaleList {

  @Resetter
  public static void reset() {
    LocaleListReflector localeListReflector = reflector(LocaleListReflector.class);
    localeListReflector.setLastDefaultLocale(null);
    localeListReflector.setDefaultLocaleList(null);
    localeListReflector.setDefaultAdjustedLocaleList(null);
    localeListReflector.setLastExplicitlySetLocaleList(null);
  }

  @ForType(LocaleList.class)
  interface LocaleListReflector {
    @Static
    @Accessor("sLastDefaultLocale")
    void setLastDefaultLocale(Locale lastDefaultLocal);

    @Static
    @Accessor("sDefaultLocaleList")
    void setDefaultLocaleList(LocaleList localeList);

    @Static
    @Accessor("sDefaultAdjustedLocaleList")
    void setDefaultAdjustedLocaleList(LocaleList localeList);

    @Static
    @Accessor("sLastExplicitlySetLocaleList")
    void setLastExplicitlySetLocaleList(LocaleList localeList);
  }
}
