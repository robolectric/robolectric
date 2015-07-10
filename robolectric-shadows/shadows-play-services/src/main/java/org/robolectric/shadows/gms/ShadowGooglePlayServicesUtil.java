package org.robolectric.shadows.gms;

import android.content.Context;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Implementation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

/**
 * Shadow for {@link com.google.android.gms.common.GooglePlayServicesUtil}.
 */
@Implements(GooglePlayServicesUtil.class)
public class ShadowGooglePlayServicesUtil {
  private static int availabilityCode = ConnectionResult.SERVICE_MISSING;

  @Implementation
  public static int isGooglePlayServicesAvailable(Context context) {
    return availabilityCode;
  }

  public static void setIsGooglePlayServicesAvailable(int availabilityCode) {
    ShadowGooglePlayServicesUtil.availabilityCode = availabilityCode;
  }
}