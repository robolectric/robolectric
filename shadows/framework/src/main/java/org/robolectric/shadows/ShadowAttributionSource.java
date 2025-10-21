package org.robolectric.shadows;

import android.content.AttributionSource;
import android.content.Context;
import android.os.Build.VERSION_CODES;
import com.google.common.collect.Sets;
import java.util.Set;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

/** Shadow for {@link AttributionSource}. */
@Implements(value = AttributionSource.class, minSdk = VERSION_CODES.S, isInAndroidSdk = false)
public final class ShadowAttributionSource {
  @RealObject AttributionSource realAttributionSource;
  private static final Set<AttributionSource> trustedAttributionSources =
      Sets.newConcurrentHashSet();

  public static void addTrustedAttributionSource(AttributionSource attributionSource) {
    trustedAttributionSources.add(attributionSource);
  }

  public static void clearTrustedAttributionSources() {
    trustedAttributionSources.clear();
  }

  @Implementation
  protected boolean isTrusted(Context context) {
    return trustedAttributionSources.contains(realAttributionSource);
  }
}
