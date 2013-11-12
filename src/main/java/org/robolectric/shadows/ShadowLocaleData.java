package org.robolectric.shadows;

import org.robolectric.Robolectric;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import java.util.Locale;

import static org.robolectric.Robolectric.newInstanceOf;

@Implements(value = Robolectric.Anything.class, className = ShadowLocaleData.REAL_CLASS_NAME)
public class ShadowLocaleData {
  public static final String REAL_CLASS_NAME = "libcore.icu.LocaleData";

  @Implementation
  public static Object get(Locale locale) {
    return newInstanceOf(REAL_CLASS_NAME);
  }
}
