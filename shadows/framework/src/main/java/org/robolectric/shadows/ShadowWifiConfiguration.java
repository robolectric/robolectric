package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.R;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.net.wifi.SecurityParams;
import android.net.wifi.WifiConfiguration;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

/** Shadow Implementation of {@link android.net.wifi.WifiConfiguration} */
@Implements(value = WifiConfiguration.class)
public class ShadowWifiConfiguration {
  @RealObject private WifiConfiguration realObject;

  private int securityType = -1; // for Android R

  /* Returns a copy of the {@link WifiConfiguration} it shadows. */
  public WifiConfiguration copy() {
    return new WifiConfiguration(realObject);
  }

  @Implementation(minSdk = R, maxSdk = R)
  protected void setSecurityParams(int securityType) {
    reflector(WifiConfigurationReflector.class, realObject).setSecurityParams(securityType);
    this.securityType = securityType;
  }

  /** Returns the security type set by {@code setSecurityParams}. */
  public Set<Integer> getSecurityTypes() {
    if (RuntimeEnvironment.getApiLevel() == R) {
      return ImmutableSet.of(securityType);
    } else {
      List<Object> params =
          reflector(WifiConfigurationReflector.class, realObject).getSecurityParams();
      return params.stream()
          .map(s -> ((SecurityParams) s).getSecurityType())
          .collect(Collectors.toSet());
    }
  }

  @ForType(WifiConfiguration.class)
  interface WifiConfigurationReflector {
    @Accessor("mSecurityParamsList")
    List<Object> getSecurityParams();

    @Direct
    void setSecurityParams(int securityType);
  }
}
