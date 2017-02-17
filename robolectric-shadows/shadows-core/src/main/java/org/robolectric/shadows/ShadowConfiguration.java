package org.robolectric.shadows;

import android.content.res.Configuration;
import java.util.Locale;

import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

import static org.robolectric.shadow.api.Shadow.directlyOn;

/**
 * Shadow for {@link android.content.res.Configuration}.
 * @deprecated This will be removed in Robolectric 3.4 - {@link Configuration} is pure Java so there is no need for a
 * shadow to exist. The methods have been preserved but marked deprecated to prevent build breakages but in this
 * version implementation has been modified to simply call through to the Framework code which may in some cases cause
 * test failures due to the way the shadow diverged in behaviour from the Framework code. Some notes have been added
 * to help you migrate in these cases.
 *
 * Some notes to help you migrate:-
 *
 * <ol>
 * <li>{@link #setLocale} only exists in API 17+ so calling this on earlier APIs will fail with {@link NoSuchMethodException}
 * <li>{@link #setToDefaults()} overrides the frameworks natural defaults to set the flags for
 *     {@link Configuration#screenLayout} to include {@link Configuration#SCREENLAYOUT_LONG_NO} and
 *     {@link Configuration#SCREENLAYOUT_SIZE_NORMAL}
 * <li>{@link #overrideQualifiers} and {@link #getQualifiers()} have no effect and can be removed.
 * </ol>
 */
@Deprecated
@Implements(Configuration.class)
public class ShadowConfiguration {

  @RealObject
  private Configuration realConfiguration;

  @Deprecated
  @Implementation
  public void setTo(Configuration o) {
    directlyOn(realConfiguration, Configuration.class).setTo(o);
  }

  @Deprecated
  @Implementation
  public void setToDefaults() {
    directlyOn(realConfiguration, Configuration.class).setToDefaults();
  }

  @Deprecated
  @Implementation
  public void setLocale( Locale l ) {
    directlyOn(realConfiguration, Configuration.class).setLocale(l);
  }

  @Deprecated
  public void overrideQualifiers(String qualifiers) {
    // Never worked.
  }

  /**
   * @deprecated  Use {@link RuntimeEnvironment#getQualifiers()} although there should be no reason to obtain this
   * since it is typically set in tests through {@link Config#qualifiers()} so you should use a constant in these cases.
   */
  @Deprecated
  public String getQualifiers() {
    return RuntimeEnvironment.getQualifiers();
  }
}
