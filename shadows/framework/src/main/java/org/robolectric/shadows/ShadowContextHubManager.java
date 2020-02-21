package org.robolectric.shadows;

import android.hardware.location.ContextHubClient;
import android.hardware.location.ContextHubClientCallback;
import android.hardware.location.ContextHubInfo;
import android.hardware.location.ContextHubManager;
import android.os.Build.VERSION_CODES;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.util.ReflectionHelpers;

/** Shadow for {@link ContextHubManager}. */
@Implements(
    value = ContextHubManager.class,
    minSdk = VERSION_CODES.P,
    isInAndroidSdk = false,
    looseSignatures = true)
public class ShadowContextHubManager {
  private static final List<ContextHubInfo> contextHubInfoList = new ArrayList<>();

  static {
    contextHubInfoList.add(new ContextHubInfo());
  }

  /**
   * Provides a list with fake {@link ContextHubInfo}s.
   *
   * <p>{@link ContextHubInfo} describes an optional physical chip on the device. This does not
   * exist in test; this implementation allows to avoid possible NPEs.
   */
  @Implementation(minSdk = VERSION_CODES.P)
  @HiddenApi
  protected List<ContextHubInfo> getContextHubs() {
    return contextHubInfoList;
  }

  @Implementation(minSdk = VERSION_CODES.P)
  @HiddenApi
  protected ContextHubClient createClient(
      ContextHubInfo contextHubInfo, ContextHubClientCallback contextHubClientCallback) {
    return ReflectionHelpers.newInstance(ContextHubClient.class);
  }

  @Implementation(minSdk = VERSION_CODES.P)
  @HiddenApi
  protected ContextHubClient createClient(
      ContextHubInfo contextHubInfo,
      ContextHubClientCallback contextHubClientCallback,
      Executor executor) {
    return ReflectionHelpers.newInstance(ContextHubClient.class);
  }
}
